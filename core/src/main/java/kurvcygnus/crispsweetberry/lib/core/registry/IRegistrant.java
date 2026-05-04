//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.core.registry;

import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.Objects;

/**
 * This the core of automatic registration.
 * @apiNote Make sure the registry class is an enum, and has only one enumeration named {@code INSTANCE}, it makes automatic registration 
 * works correctly, and help others understand automatic registration quickly.
 * @implSpec Don't forget to register your <u>{@link net.neoforged.fml.ModContainer ModContainer}</u> on <u>{@link CrispRegistrationManager}</u>:
 * <pre>{@code
 *  // At YourModEntryClass.<init>:
 *  CrispRegistrationManager.getInstance().register(modContainer, eventBus);
 * }</pre>
 * @implNote <h2>Some Q&A which will probably happen:</h2>
 * <ul>
 *     <li>
 *         Q: Is automatic registration with reflection dangerous, or unstable?<br>
 *         A: No. It depends. To put it simple, using {@code private} level constructor won't gonna work, since initialization phase is quite sensitive about 
 *         {@code private} access, which will throw exception in the end. Using annotation is also same as using {@code private} level constructor.
 *         And directly use {@code public} constructor may lead to abuse, thus, <b>using <u>{@link Enum}</u> is the best choice, because
 *         <a href="https://en.wikipedia.org/wiki/Singleton_pattern"><u>{@code Singleton}</u></a>
 *         mode is based on convention, and <u>{@link Enum}</u> is more mandatory than that</b>. Also, it is stable.</li>
 *     <li>
 *         Q: Will automatic registration lead to lifecycle issues?<br>
 *         A: Yes, <b>but if it happened, mostly is not automatic registration's fault</b>. In fact, non-automatic registration will also encounter such issues
 *         if you didn't notice the order of registries' initialization. Anyway, lifecycle issue can be solved by adjusting <u>{@link #getPriority()}</u>, 
 *         it is usually not a big deal, for our <u>{@link kurvcygnus.crispsweetberry.common.features features package}</u> and 
 *         <u>{@link kurvcygnus.crispsweetberry.common.qol QoL package}</u>, whose are DDD driven, we use <u>{@link net.neoforged.neoforge.common.util.Lazy Lazy}</u>
 *         when necessary.
 *     </li>
 *     <li>
 *         Q: Isn't manually adjusting initialization priorities troublesome?<br>
 *         A: Yeah, you got me. This is the only tricky issue we are deal with. Despite adjusting neither this our solution's property nor the traditional one are annoying,
 *         our solution's issue is implicit property view, I don't deny that, <b>this is a trade-off.</b>
 *     </li>
 * </ul>
 * @since Release 1.0
 * @author Kurv Cygnus
 * @see net.neoforged.neoforge.registries.DeferredRegister DeferredRegister
 * @see IEventBus Event Bus API
 * @see CrispRegistrationManager Auto Registration Implementation
 */
public interface IRegistrant
{
    /**
     * The method which will be called in auto registration phase.<br>
     * You should write your <u>{@link DeferredRegister}</u>'s register logic at here.
     * @see #listedRegister(List, IEventBus)
     * @see #varargedRegister(IEventBus, DeferredRegister[])
     */
    void register(@NotNull IEventBus bus);
    
    static void listedRegister(@NotNull List<@NotNull DeferredRegister<?>> registries, @NotNull IEventBus bus)
    {
        Objects.requireNonNull(registries, "Param \"registries\" must not be null!");
        Objects.requireNonNull(bus, "Param \"bus\" must not be null!");
        registries.forEach(deferredRegister -> deferredRegister.register(bus));
    }
    
    static void varargedRegister(@NotNull IEventBus bus, @NotNull DeferredRegister<?> @NotNull ... registries)
    {
        Objects.requireNonNull(bus, "Param \"bus\" must not be null!");
        Objects.requireNonNull(registries, "Param \"registries\" must not be null!");
        for(int index = 0, registriesLength = registries.length; index < registriesLength; index++)
        {
            final var deferredRegister = registries[index];
            Objects.requireNonNull(deferredRegister, "The deferredRegister in array's index %d is null!".formatted(index));
            deferredRegister.register(bus);
        }
    }
    
    /**
     * Decides the print output of this registry entry, when this is {@code true},
     * the info will be {@code "Registering Feature: #getJob()..."}.
     */
    default boolean isFeature() { return true; }
    
    /**
     * Gets the job of this entry.
     * This is used to make both log and class's responsibility more clear.
     */
    default @NotNull String getJob() { return modifyNameAsJob(); }
    
    private @NotNull String modifyNameAsJob()
    {
        final String name = this.getClass().getSimpleName();
        return name.
            replaceAll("Registr(y|ies)", "").
            replaceAll("(?<=[a-z])(?=[A-Z])", " ").
            trim();//* Convert Result Example: FooBarBoxRegistry -> Foo Bar Box
    }
    
    /**
     * Used for order sensitive registries.
     * @see PriorityRange
     * @apiNote Smaller number has higher property.
     */
    @NotNull PriorityPair getPriority();
    
    @ApiStatus.NonExtendable default int getFullPriority() { return getPriority().priorityRange().getPriority() + getPriority().priority; }
    
    record PriorityPair(@NotNull PriorityRange priorityRange, @Range(from = 1, to = 999) int priority)
    {
        @SuppressWarnings("ConstantValue")//! Defensive check.
        public PriorityPair
        {
            FunctionalUtils.throwIf(
                priority < 1 || priority > 999,
                "Param \"priority\" should be between 1 and 999!",
                IllegalArgumentException::new
            );
        }
    }
    
    /**
     * The <u>{@link Enum}</u> that represents the main range of a registry class.
     * @see #BASE
     * @see #FEATURE
     * @see #REFERENCE_HOLDER
     */
    enum PriorityRange
    {
        /**
         * Represents the first range which will be processed.<br>
         * @apiNote {@code BASE} refers to the registries whose are lite, with no extra dependencies,
         * like {@code BlockRegistry}, {@code ItemRegistry}, etc.<br>
         * Due to their attribute, they should be initialized at first.
         */
        BASE,
        
        /**
         * Represents the second range which will be processed.
         * @apiNote {@code FEATURE} refers to the registries whose are cohesive, holding order-sensitive entries,
         * which is also called, <u><a href="https://en.wikipedia.org/wiki/Domain-driven_design">DDD Design</a></u>.<br>
         * Due to their attribute, they should be processed after the lite are handled.
         */
        FEATURE,
        
        /**
         * Represents the last range will be processed.
         * @apiNote {@code REFERENCE_HOLDER} refers to registries that mainly holds references to other entries, like {@code CreativeModTabRegistry}.<br>
         * Obviously, this should be processed at last.
         */
        REFERENCE_HOLDER;
        
        private final int priority;
        
        PriorityRange() { this.priority = (this.ordinal() + 1) * 1000; }
        
        public int getPriority() { return this.priority; }
    }
}
