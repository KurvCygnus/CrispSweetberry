//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.core.registry;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.lib.base.lang.ISealableBox;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiConsumer;

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
    private static final ISealableBox<Boolean> ACCESS = ISealableBox.assignable(Boolean.TRUE);
    
    private static @Nullable CrispRegistrationManager INSTANCE = new CrispRegistrationManager();
    
    private final IMarkLogger logger = IMarkLogger.withMarkerSuffixes(LogUtils.getLogger(), "REGISTRY_MANAGER");
    private final Set<ModContainer> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    
    private CrispRegistrationManager() { if(!ACCESS.orThrow()) throw new AssertionError("No, you can't create a new instance of this!"); }
    
    @SubscribeEvent static void destroyInstance(@NotNull FMLLoadCompleteEvent event)
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
        
        logger.info("Searching registries for mod: {}...", modContainer.getModId());
        
        final ModFileScanData scanData = modContainer.getModInfo().getOwningFile().getFile().getScanResult();
        
        scanData.getClasses().stream().
            filter(data -> data.interfaces().contains(Type.getType(IRegistrant.class))).
            map(
                data ->
                {
                    try
                    {
                        final Class<?> registry = Class.forName(data.clazz().getClassName());
                        if(registry.isEnum() && registry.getEnumConstants().length == 1)
                        {
                            logger.debug("Captured the singleton of registry \"{}\".", registry.getSimpleName());
                            return (IRegistrant<?>) registry.getEnumConstants()[0];
                        }
                    }
                    catch(Exception e)
                    {
                        logger.error("Failed to instantiate registry: {}", data.clazz().getClassName(), e);
                        return null;
                    }
                    throw new IllegalStateException("Class \"%s\" has violated the contract: It is not a singleton enum.".formatted(data.clazz().getClassName()));
                }
            ).
            filter(Objects::nonNull).
            sorted(Comparator.comparingInt(r -> r.getPriority().fullPriority())).
            forEach(
                registrant ->
                {
                    registrant.register(eventBus);
                    logger.info(
                        "[{}] Registering {}{}...",
                        modContainer.getModId(),
                        registrant.isFeature() ? "Feature: " : "",
                        registrant.getJob()
                    );
                }
            );
        
        if(service == null || foundSequence == null)
            return;
        
        logger.info("[{}] Registry initialized. Start process Annotation \"{}\"'s delegation.", modContainer.getModId(), service.getSimpleName());
        
        scanData.getAnnotations().stream().
            filter(data -> data.annotationType().getClassName().equals(service.getName())).
            forEach(
                data ->
                {
                    try
                    {
                        final Class<?> host = Class.forName(data.clazz().getClassName());
                        final Field owner = host.getDeclaredField(data.memberName());
                        owner.setAccessible(true);
                        
                        final @Nullable A instance = owner.getAnnotation(service);
                        final @Nullable Object target = owner.get(null);
                        
                        if(target == null || instance == null)
                        {
                            logger.debug("Invalid entry: Member: {}, Annotation: {}, skipped.", target, instance);
                            return;
                        }
                        
                        foundSequence.accept(instance, target);
                    }
                    catch(Exception e) { logger.error("Failed to access class \"{}\", details: ", data.clazz().getClassName(), e); }
                }
            );
    }
    
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
        
        INSTANCE.register(modContainer, eventBus, service, foundSequence);
    }
    
    /**
     * This method starts the automatic registration of a mod.<br>
     * You should call this method in your mod's entry class's {@code <init>} method(<i>a.k.a Constructor</i>).
     * <hr>
     * <b>This class has a strict lifecycle, any improper, or invalid call will get exception.</b>
     * @see #registerWithAnnotationDelegate(ModContainer, IEventBus, Class, BiConsumer) Advanced Usage
     */
    public static void register(@NotNull ModContainer modContainer, @NotNull IEventBus eventBus) { delegate(modContainer, eventBus, null, null); }
    
    /**
     * Start the registration of all <u>{@link IRegistrant}</u>'s implementers, and after the registration is completed,
     * the <u>{@link Annotation}</u> your passed will be processed, once both annotated target and <u>{@link Annotation}</u> itself are presented,
     * the callback will be triggered.
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
}