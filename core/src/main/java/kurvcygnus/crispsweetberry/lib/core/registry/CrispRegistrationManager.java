//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.core.registry;

import com.google.errorprone.annotations.DoNotCall;
import kurvcygnus.crispsweetberry.lib.base.lang.ISealableBox;
import kurvcygnus.crispsweetberry.lib.base.lang.Pair;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.slf4j.helpers.MessageFormatter;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;

/**
 * This is the class that processes all <u>{@link IRegistrant}</u> implementers.
 * @implNote <b>This class won't take any physical memories after all mods' initialization are completed.
 * Once <u>{@link FMLLoadCompleteEvent}</u> is fired, this class will destroy its only one instance with its fields immediately.</b>
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see IRegistrant
 * @see #register(ModContainer, IEventBus) Usage
 */
@EventBusSubscriber(modid = "crispsweetberry") public final class CrispRegistrationManager
{
    private static final ISealableBox<Boolean> ACCESS = ISealableBox.assignableAtomic(Boolean.TRUE);
    
    private static @Nullable CrispRegistrationManager INSTANCE = new CrispRegistrationManager();
    
    private final IMarkLogger logger = IMarkLogger.marklessLogger();
    private final Set<ModContainer> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    
    private CrispRegistrationManager() { if(!ACCESS.orThrow()) throw new AssertionError("No, you can't create a new instance of this!"); }
    
    private static <A extends Annotation> void delegate(
        @NotNull ModContainer modContainer,
        @NotNull IEventBus eventBus,
        @Nullable Class<A> service,
        @Nullable BiConsumer<@NotNull A, @NotNull Object> foundSequence
    )
    {
        Objects.requireNonNull(modContainer, "Param \"modContainer\" must not be null!");
        Objects.requireNonNull(eventBus, "Param \"eventBus\" must not be null!");
        
        if(INSTANCE == null)
            throw new IllegalStateException("Registration phase is already over!");
        
        try(final var ignored = INSTANCE.logger.pushMarker(modContainer.getModId().toUpperCase()))
            { INSTANCE.register(modContainer, eventBus, service, foundSequence); }
    }
    
    /**
     * This method starts the automatic registration of a mod.<br>
     * You should call this method in your mod's entry class's {@code <init>} method(<i>a.k.a Constructor</i>).
     * <hr>
     * <b>This class has a strict lifecycle, any improper, or invalid call will get exception.</b>
     *
     * @see #registerWithAnnotationDelegate(ModContainer, IEventBus, Class, BiConsumer) Advanced Usage
     */
    public static void register(@NotNull ModContainer modContainer, @NotNull IEventBus eventBus) { delegate(modContainer, eventBus, null, null); }
    
    /**
     * Start the registration of all <u>{@link IRegistrant}</u>'s implementers, and after the registration is completed,
     * the <u>{@link Annotation}</u> your passed will be processed, once both annotated target and <u>{@link Annotation}</u> itself are presented,
     * the callback will be triggered.
     *
     * @apiNote Param {@code service}'s users are better in a <u>{@link Enum}</u> class, <span style="color: f84b4b">otherwise stability and safety are not granted.</span>
     */
    public static <A extends Annotation> void registerWithAnnotationDelegate(
        @NotNull ModContainer modContainer,
        @NotNull IEventBus eventBus,
        @NotNull Class<A> service,
        @NotNull BiConsumer<@NotNull A, @NotNull Object> foundSequence
    )
    {
        Objects.requireNonNull(service, "Param \"service\" must not be null!");
        Objects.requireNonNull(foundSequence, "Param \"foundSequence\" must not be null!");
        delegate(modContainer, eventBus, service, foundSequence);
    }
    
    @SubscribeEvent @DoNotCall static void destroyInstance(@NotNull FMLLoadCompleteEvent event)
    {
        assert INSTANCE != null;
        INSTANCE.logger.info("Registration phase finished, RegistrationManager destroyed.");
        INSTANCE = null;
        ACCESS.seal();
    }
    
    private <A extends Annotation> void register(
        @NotNull ModContainer modContainer,
        @NotNull IEventBus eventBus,
        @Nullable Class<A> service,
        @Nullable BiConsumer<@NotNull A, @NotNull Object> foundSequence
    ) throws IllegalArgumentException
    {
        if(!visited.add(modContainer))
            return;
        
        final var scanData = modContainer.getModInfo().getOwningFile().getFile().getScanResult();
        
        logger.info("Searching and registering config...");
        
        final var configs = new ArrayList<IRegistrant.OfConfigSupport<?, ?>>();
        final var registries = new ArrayList<IRegistrant<?>>();
        final var delegates = new ArrayList<Pair<A, @Nullable Object>>();
        
        final var classIterator = scanData.getClasses().iterator();
        final var annotationIterator = scanData.getAnnotations().iterator();
        
        final BooleanSupplier shouldDoDelegate = () -> service != null && foundSequence != null && annotationIterator.hasNext();
        
        while(classIterator.hasNext() || shouldDoDelegate.getAsBoolean())
        {
            final @Nullable var classData = classIterator.hasNext() ? classIterator.next() : null;
            final @Nullable var annotationData = shouldDoDelegate.getAsBoolean() ? annotationIterator.next() : null;
            
            tryAnalyseTarget(IRegistrant.OfConfigSupport.class, classData, configs, "ConfigHolder");
            //! A shame that ASM only recognizes the direct inheritors, the indirect are not supported QAQ
            tryAnalyseTarget(IRegistrant.OfSimpleConfigSupport.class, classData, configs, "ConfigHolder");
            tryAnalyseTarget(IRegistrant.class, classData, registries, "Registry");
            
            //noinspection DataFlowIssue
            if(annotationData != null && annotationData.annotationType().getClassName().equals(service.getName()))//! See `shouldDoDelegate`'s definition.
            {
                final var hostFQCN = annotationData.clazz().getClassName();
                
                try
                {
                    final var ownerName = annotationData.memberName();
                    
                    final var host = Class.forName(hostFQCN);
                    final var owner = host.getDeclaredField(ownerName);
                    owner.setAccessible(true);
                    
                    final @Nullable A instance = owner.getAnnotation(service);
                    final @Nullable Object value = owner.get(null);
                    
                    if(instance != null && value != null)
                    {
                        logger.debug(
                            "Found a delegate pair for annotation \"{}\", in \"{}\".",
                            host.getSimpleName(),
                            service.getSimpleName()
                        );
                        delegates.add(new Pair<>(instance, value));
                    }
                }
                catch(Exception e) { logger.error("Failed to access ConfigHolder class \"{}\", details: ", hostFQCN, e); }
            }
        }
        
        registries.sort(Comparator.comparingInt(r -> r.getPriority().fullPriority()));
        
        logger.info("All registrants founded and sorted. Starting instantiating.");
        
        if(!configs.isEmpty())
        {
            logger.info("Invoking configs...");
            for(final var configHolder: configs)
            {
                final var type = configHolder.getType();
                modContainer.registerConfig(type, configHolder.getSpec());
                logger.info("Registered {} config {}.", type.extension(), configHolder.getClass().getSimpleName());
            }
        }
        
        if(!registries.isEmpty())
        {
            logger.info("Invoking registries...");
            for(final var registry: registries)
            {
                registry.register(eventBus);
                logger.info(
                    "Registering {}{}...",
                    registry.isFeature() ? "Feature: " : "",
                    registry.getJob()
                );
            }
        }
        
        if(!delegates.isEmpty())
        {
            logger.info("Invoking delegations...");
            for(final var delegate: delegates)
            {
                assert foundSequence != null;
                foundSequence.accept(delegate.left(), delegate.right());
            }
        }
        
        logger.info("Mod loaded!");
    }
    
    @SuppressWarnings("unchecked")//! Internal Usage, relatively safe.
    private <E> void tryAnalyseTarget(
        @NotNull Class<?> clazz,//! Can't use `? extends E` to restrict, because Javac is stupid, and doesn't think that `Foo` and `Foo<?>` are the same thing.
        @Nullable ModFileScanData.ClassData classData,
        @NotNull List<E> list,
        @NotNull String type
    )
    {
        if(classData == null || !classData.interfaces().contains(Type.getType(clazz)))
            return;
        
        final var targetFQCN = classData.clazz().getClassName();
        
        try
        {
            final var target = Class.forName(targetFQCN);
            
            if(target.isInterface())//! Appears to be uninstantiable, and probably be original target's variant.
                return;
            
            if(!target.isEnum() || target.getEnumConstants().length != 1)
                throw new IllegalStateException(
                    MessageFormatter.format(
                        "Class \"{}\" has violated the contract of {}: It is not a singleton enum.",
                        targetFQCN,
                        type
                    ).getMessage()
                );
            
            logger.debug("Captured the singleton of {} \"{}\".", type, target.getSimpleName());
            list.add((E) target.getEnumConstants()[0]);
        }
        catch(Exception e) { logger.error("Failed to instantiate {} class \"{}\".", type, targetFQCN, e); }
    }
}