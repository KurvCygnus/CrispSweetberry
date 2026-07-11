//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.core.registry;

import com.google.errorprone.annotations.DoNotCall;
import kurvcygnus.crispsweetberry.lib.base.extensions.StackDebugger;
import kurvcygnus.crispsweetberry.lib.base.lang.ISealableBox;
import kurvcygnus.crispsweetberry.lib.base.lang.Pair;
import kurvcygnus.crispsweetberry.lib.base.stream.Invoker;
import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

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
    private final List<String> reports = new ArrayList<>(36);
    
    private CrispRegistrationManager() { if(!ACCESS.orThrow()) throw new AssertionError("No, you can't create a new instance of this!"); }
    
    /**
     * This method starts the automatic registration of a mod.<br>
     * You should call this method in your mod's entry class's {@code <init>} method(<i>a.k.a Constructor</i>).
     * <hr>
     * <span style="color: f84b4b">This class has a strict lifecycle, any improper, or invalid call will get exception.</span>
     * @see #registerWithAnnotationDelegate(ModContainer, IEventBus, Class, BiConsumer) Advanced Usage
     */
    public static void register(@NotNull ModContainer modContainer, @NotNull IEventBus eventBus, boolean doReport)
        { delegate(modContainer, eventBus, null, null, doReport); }
    
    /**
     * This method starts the automatic registration of a mod.<br>
     * You should call this method in your mod's entry class's {@code <init>} method(<i>a.k.a Constructor</i>).
     * <hr>
     * <span style="color: f84b4b">This class has a strict lifecycle, any improper, or invalid call will get exception.</span>
     * @see #registerWithAnnotationDelegate(ModContainer, IEventBus, Class, BiConsumer) Advanced Usage
     */
    public static void register(@NotNull ModContainer modContainer, @NotNull IEventBus eventBus)
        { delegate(modContainer, eventBus, null, null, false); }
    
    /**
     * Start the registration of all <u>{@link IRegistrant}</u>'s implementers, and after the registration is completed,
     * the <u>{@link Annotation}</u> your passed will be processed, once both annotated target and <u>{@link Annotation}</u> itself are presented,
     * the callback will be triggered.
     * @apiNote Param {@code service}'s users are better in a <u>{@link Enum}</u> class, <span style="color: f84b4b">otherwise stability and safety are not granted.</span>
     */
    public static <A extends Annotation> void registerWithAnnotationDelegate(
        @NotNull ModContainer modContainer,
        @NotNull IEventBus eventBus,
        @NotNull Class<? extends A> service,
        @NotNull BiConsumer<@NotNull A, @NotNull Object> foundSequence,
        boolean doReport
    )
    {
        Objects.requireNonNull(service, "Param \"service\" must not be null!");
        Objects.requireNonNull(foundSequence, "Param \"foundSequence\" must not be null!");
        delegate(modContainer, eventBus, service, foundSequence, doReport);
    }
    
    /**
     * Start the registration of all <u>{@link IRegistrant}</u>'s implementers, and after the registration is completed,
     * the <u>{@link Annotation}</u> your passed will be processed, once both annotated target and <u>{@link Annotation}</u> itself are presented,
     * the callback will be triggered.
     * @apiNote Param {@code service}'s users are better in a <u>{@link Enum}</u> class, <span style="color: f84b4b">otherwise stability and safety are not granted.</span>
     */
    public static <A extends Annotation> void registerWithAnnotationDelegate(
        @NotNull ModContainer modContainer,
        @NotNull IEventBus eventBus,
        @NotNull Class<? extends A> service,
        @NotNull BiConsumer<@NotNull A, @NotNull Object> foundSequence
    )
    {
        Objects.requireNonNull(service, "Param \"service\" must not be null!");
        Objects.requireNonNull(foundSequence, "Param \"foundSequence\" must not be null!");
        delegate(modContainer, eventBus, service, foundSequence, false);
    }
    
    private static <A extends Annotation> void delegate(
        @NotNull ModContainer modContainer,
        @NotNull IEventBus eventBus,
        @Nullable Class<? extends A> service,
        @Nullable BiConsumer<@NotNull A, @NotNull Object> foundSequence,
        boolean doReport
    )
    {
        Objects.requireNonNull(modContainer, "Param \"modContainer\" must not be null!");
        Objects.requireNonNull(eventBus, "Param \"eventBus\" must not be null!");
        
        final var modID = modContainer.getModId();
        
        //* 3 frames:
        //* 0. [[StackDebugger#getCallerFrame(int)]]
        //* 1. this method
        //* 2. [[CrispRegistrationManager#register]], or [[CrispRegistrationManager#registerWithAnnotationDelegate]]
        //* 3. Mod Entry Class's constructor method(`<init>`)
        final var callerInfo = StackDebugger.getCallerFrame(3);
        
        Objects.requireNonNull(
            callerInfo.getDeclaringClass().getAnnotation(Mod.class),
            TextUtils.format(
                "Error: Mod \"{}\"'s registration is not activated by its entry class! Called by: {}",
                modID,
                StackDebugger.toFullCallerInfo(callerInfo)
            )
        );
        
        if(!callerInfo.getMethodName().equals("<init>"))
            throw new IllegalArgumentException(
                TextUtils.format(
                    "Error: Mod \"{}\"'s registration is not activated by its entry class's constructor! Called by: {}",
                    modID,
                    StackDebugger.toFullCallerInfo(callerInfo)
                )
            );
        
        if(INSTANCE == null)
            throw new IllegalStateException("Registration phase is already over!");
        
        try(final var ignored = INSTANCE.logger.pushMarker(modID.toUpperCase()))
            { INSTANCE.register(modContainer, eventBus, service, foundSequence, doReport); }
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
        @Nullable Class<? extends A> service,
        @Nullable BiConsumer<@NotNull A, @NotNull Object> foundSequence,
        boolean doReport
    ) throws IllegalArgumentException
    {
        if(!visited.add(modContainer))
            return;
        
        final var scanData = modContainer.getModInfo().getOwningFile().getFile().getScanResult();
        
        logger.info("Searching and registering config...");
        
        final var configs = new ArrayList<IRegistrant.OfConfigSupport<?, ?>>();
        final var registries = new ArrayList<IRegistrant<?>>();
        final var delegates = new ArrayList<Pair<A, Object>>();
        
        final var classIterator = scanData.getClasses().iterator();
        final var annotationIterator = scanData.getAnnotations().iterator();
        final var delegateReports = new HashMap<Object, String>();
        
        final BooleanSupplier shouldDoDelegate = () -> service != null && foundSequence != null && annotationIterator.hasNext();
        
        while(classIterator.hasNext() || shouldDoDelegate.getAsBoolean())
        {
            final @Nullable var classData = classIterator.hasNext() ? classIterator.next() : null;
            final @Nullable var annotationData = shouldDoDelegate.getAsBoolean() ? annotationIterator.next() : null;
            
            tryAnalyseTarget(IRegistrant.OfConfigSupport.class, classData, configs, "ConfigHolder");
            //! A shame that ASM only recognizes the direct inheritors, the indirect are not supported QAQ
            tryAnalyseTarget(IRegistrant.OfSimpleConfigSupport.class, classData, configs, "ConfigHolder");
            tryAnalyseTarget(IRegistrant.class, classData, registries, "Registry");
            
            //? Nah, I won't extract these logic as an independent method, that just makes logic less centralized, and no too much help on understanding code.
            //noinspection DataFlowIssue
            if(annotationData != null && annotationData.annotationType().getClassName().equals(service.getName()))//! See `shouldDoDelegate`'s definition.
            {
                final var hostFQCN = annotationData.clazz().getClassName();
                
                try
                {
                    final var ownerName = annotationData.memberName();
                    
                    final var host = Class.forName(hostFQCN);
                    
                    if(!host.isEnum())
                        logger.warn("Class \"{}\" is not an enum: delegation may cause crash or unexpected behaviors.", host.getSimpleName());
                    
                    final var owner = host.getDeclaredField(ownerName);
                    
                    //! Take Module, or other edgy cases into consideration.
                    if(!owner.trySetAccessible())
                        throw new IllegalArgumentException(
                            TextUtils.format(
                                "Cannot access {}#{} to collect registration info. This usually shouldn't happen, please check whether access is limited.",
                                hostFQCN,
                                ownerName
                            )
                        );
                    
                    final @Nullable A instance = owner.getAnnotation(service);
                    final @Nullable Object value = owner.get(null);
                    
                    if(instance != null && value != null)
                    {
                        delegates.add(new Pair<>(instance, value));
                        delegateReports.put(
                            value,
                            TextUtils.format(
                                "{}#{} -> {}",
                                hostFQCN,
                                ownerName,
                                value.toString()
                            )
                        );
                    }
                }
                catch(Exception e) { logger.error("Failed to access ConfigHolder class \"{}\", details: ", hostFQCN, e); }
            }
        }
        
        registries.sort(Comparator.comparingInt(r -> r.getPriority().fullPriority()));
        
        logger.info("All registrants founded and sorted. Starting instantiating.");
        
        invokeInstantiation(
            configs,
            configHolder ->
            {
                final var configType = configHolder.getType();
                modContainer.registerConfig(configType, configHolder.getSpec());
                logger.info("Registered {} config {}.", configType.extension(), configHolder.getClass().getSimpleName());
                
                return TextUtils.format(
                    "[{}] {}",
                    configType.name(),
                    configHolder.getClass().getName()
                );
            },
            "Configs",
            doReport
        );
        
        invokeInstantiation(
            registries,
            registry ->
            {
                //* TIP: [[StringBuilder]]'s initial is [[String#length]] + 16(When [[String#length]] is less than [[Integer#MAX_VALUE]] - 16),
                //* so memory reallocation won't happen.
                final var reportMessage = new StringBuilder(
                    TextUtils.format(
                        "[{}] {}",
                        registry.getJob(),
                        registry.getClass().getName()
                    )
                );
                
                if(!registry.isActivated().test(modContainer))
                    return reportMessage.append("(Unactivated)").toString();
                
                registry.register(target -> target.register(eventBus));
                logger.info(
                    "Registering {}{}...",
                    registry.isFeature() ? "Feature: " : "",
                    registry.getJob()
                );
                
                return reportMessage.toString();
            },
            "Registries",
            doReport
        );
        
        invokeInstantiation(
            delegates,
            delegate ->
            {
                assert foundSequence != null;//! See assertion above.
                delegate.accept(foundSequence);
                return delegate.applyRight(delegateReports::get);
            },
            "Delegations",
            doReport
        );
        
        logger.info(
            "Mod loaded!{}",
            doReport ?//* Print this independently will print the metainfo header again, which is not good for reporting.
                TextUtils.format(
                    "\nHere's the full, ordered scan and instantiation target report list of mod \"{}\":{}",
                    modContainer.getModId(),
                    //! Don't use [[String#join]] at here, it will mess up the layout, since this adds '\n' at the start of report additionally.
                    Invoker.unit(reports).reduce("", (f, l) -> f + '\n' + l)
                ) :
                ""
        );
        
        reports.clear();
    }
    
    @SuppressWarnings("unchecked")//! Internal Usage, relatively safe.
    private static <E> void tryAnalyseTarget(
        @NotNull Class<?> clazz,//! Can't use `? extends E` to restrict, because Javac is stupid, and doesn't think that `Foo` and `Foo<?>` are the same thing.
        @Nullable ModFileScanData.ClassData classData,
        @NotNull List<E> list,
        @NotNull String type
    )
    {
        if(classData == null || !classData.interfaces().contains(Type.getType(clazz)))
            return;
        
        assert INSTANCE != null;
        final var logger = INSTANCE.logger;
        
        final var targetFQCN = classData.clazz().getClassName();
        
        try
        {
            final var target = Class.forName(targetFQCN);
            
            if(target.isInterface())//! Appears to be uninstantiable, and probably be original target's variant.
                return;
            
            if(!target.isEnum() || target.getEnumConstants().length != 1)
                throw new IllegalStateException(
                    TextUtils.format(
                        "Class \"{}\" has violated the contract of {}: It is not a singleton enum.",
                        targetFQCN,
                        type
                    )
                );
            
            logger.debug("Captured the singleton of {} \"{}\".", type, target.getSimpleName());
            list.add((E) target.getEnumConstants()[0]);
        }
        catch(Exception e) { logger.error("Failed to instantiate {} class \"{}\".", type, targetFQCN, e); }
    }
    
    private <E> void invokeInstantiation(
        @NotNull Collection<? extends E> targets,
        //! This [[Function]] holds side effects. Notes that.
        @NotNull Function<E, @Nullable String> instAction,
        @NotNull String type,
        boolean doReport
    )
    {
        if(targets.isEmpty())
            return;
        
        logger.info("Invoking {}...", type);
        
        if(doReport)
            reports.add(
                TextUtils.format(
                    """
                    |============================================================================================================================================|
                    |                                                            {}{}|
                    |============================================================================================================================================|
                    """,
                    type.toUpperCase(),
                    //? Yes. Hard-Coded, because I don't think it is worth to be extracted as a constant.
                    " ".repeat(80 - type.length())
                )
            );
        
        for(final var target: targets)
        {
            //! Side effect triggered.
            final @Nullable var reportMessage = instAction.apply(target);
            if(doReport && reportMessage != null)//! If report is not required, `reportMessage` will be discarded.
                reports.add(reportMessage);
        }
        
        if(doReport)
            reports.add("\n*============================================================================================================================================*\n");
    }
}