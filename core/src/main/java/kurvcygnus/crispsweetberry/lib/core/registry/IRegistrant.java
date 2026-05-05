//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.core.registry;

import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * This the core of automatic registration.
 * @apiNote Make sure the registry class is an enum, and has only one enumeration named {@code INSTANCE}, it makes automatic registration 
 * works correctly, and help others understand automatic registration quickly.
 * @implSpec
 * A simple example of usage:
 * <pre>{@code
 *  public enum FooRegistry implements IRegistrant
 *  {
 *      INST;
 *
 *      @Override public void register(@NotNull IEventBus bus) { BLOCK_DEFERRED_REGISTER.register(bus); }
 *
 *      @Override public @NotNull PriorityPair getPriority() { return ofPriority(PriorityRange.BASE, 1); }
 *
 *      private static final DeferredRegister<Block> BLOCK_DEFERRED_REGISTER = DeferredRegister.createBlocks("yourmodid");
 *
 *      public static final Holder<Block> FOO_BLOCK = BLOCK_DEFERRED_REGISTER.register(
 *          "foo", resourceLocation -> new ScaffoldingBlock(BlockBehaviour.Properties.of())
 *      );
 *  }
 * }</pre>
 * Don't forget to register your <u>{@link net.neoforged.fml.ModContainer ModContainer}</u> on <u>{@link CrispRegistrationManager}</u>:
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
 * @see ISortable Priority Definitions
 * @see IDefinitions Methods Definitions
 */
public non-sealed interface IRegistrant extends IDefinitions, ISortable
{
    //* Note: [[IRegistrant]] itself only includes the util methods.
    //* For getting started, or detailed documentation, see [[IDefinitions]] and [[ISortable]].
    
    /**
     * @apiNote It's recommend to use readonly view of <u>{@link java.util.LinkedHashSet LinkedHashSet}</u>
     * (more precisely, the implementers of <u>{@link SequencedSet}</u>),
     * the rest <u>{@link Set}</u> implementers are mostly <span style="color: f84b4b">UNORDERED</span>
     * (e.g. <u>{@link java.util.HashSet HashSet}</u>, <u>{@link Set#of()}</u>),
     * <span style="color: f84b4b">use them on your own risk.</span>
     * @see #composeRegistries(Consumer, Supplier)
     * @see #composeRegistries(Consumer)
     * @see #composeRegistries(DeferredRegister[])
     */
    default void registerBySet(@NotNull @Unmodifiable Set<@NotNull DeferredRegister<?>> registries, @NotNull IEventBus bus)
    {
        Objects.requireNonNull(registries, "Param \"registries\" must not be null!");
        Objects.requireNonNull(bus, "Param \"bus\" must not be null!");
        
        registries.forEach(deferredRegister -> deferredRegister.register(bus));
    }
    
    default void registerByVarargs(@NotNull IEventBus bus, @NotNull DeferredRegister<?> @NotNull ... registries)
    {
        Objects.requireNonNull(bus, "Param \"bus\" must not be null!");
        Objects.requireNonNull(registries, "Param \"registries\" must not be null!");
        
        final int length = registries.length;
        
        if(length == 0)
            throw new IllegalArgumentException("Param \"registries\" must have at least one element!");
        
        for(int index = 0; index < length; index++)
        {
            final var deferredRegister = registries[index];
            Objects.requireNonNull(deferredRegister, "The element in array's index %d is null!".formatted(index));
            deferredRegister.register(bus);
        }
    }
    
    static <C extends SequencedSet<DeferredRegister<?>>> @NotNull @Unmodifiable Set<DeferredRegister<?>> composeRegistries(
        @NotNull Consumer<SequencedSet<DeferredRegister<?>>> consumer,
        @NotNull Supplier<C> constructor
    )
    {
        Objects.requireNonNull(consumer, "Param \"consumer\" must not be null!");
        Objects.requireNonNull(constructor, "Param \"constructor\" must not be null!");
        final C set = constructor.get();
        consumer.accept(set);
        return Collections.unmodifiableSet(set);
    }
    
    static @NotNull @Unmodifiable Set<DeferredRegister<?>> composeRegistries(
        @NotNull Consumer<SequencedSet<DeferredRegister<?>>> consumer
    ) { return composeRegistries(consumer, LinkedHashSet::new); }
    
    static @NotNull @Unmodifiable Set<DeferredRegister<?>> composeRegistries(@NotNull DeferredRegister<?> @NotNull ... registries)
    {
        final int length = registries.length;
        
        if(length == 0)
            throw new IllegalArgumentException("Param \"registries\" must have at least one element!");
        return composeRegistries(set -> set.addAll(Arrays.asList(registries)));
    }
}

/**
 * This interface holds the methods that should be implemented by <u>{@link IRegistrant}</u>'s implementers.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see ISortable
 * @see IRegistrant
 */
sealed interface IDefinitions
{
    /**
     * The method which will be called in auto registration phase.<br>
     * You should write your <u>{@link DeferredRegister}</u>'s register logic at here.
     *
     * @see IRegistrant#registerBySet(Set, IEventBus)
     * @see IRegistrant#registerByVarargs(IEventBus, DeferredRegister[])
     */
    void register(@NotNull IEventBus bus);
    
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
        //* Convert Result Example: FooBarBoxRegistry -> Foo Bar Box
        return replaceAll(
            Constants.SPLIT_REGEX,
            replaceAll(Constants.REGISTRY_REGEX, name, ""),
            " "
        ).trim();
    }
    
    private @NotNull String replaceAll(@NotNull Pattern regex, @NotNull String text, @NotNull String replacement) { return regex.matcher(text).replaceAll(replacement); }
}

final class Constants
{
    private Constants() { throw new AssertionError(); }
    
    static final Pattern REGISTRY_REGEX = Pattern.compile("Registr(y|ies)");
    static final Pattern SPLIT_REGEX = Pattern.compile("(?<=[a-z])(?=[A-Z])");
}

/**
 * This interface stores the full definition of the order system that <u>{@link IRegistrant}</u> follows.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see IDefinitions
 * @see IRegistrant
 * @see PriorityPair
 * @see PriorityRange
 */
sealed interface ISortable
{
    /**
     * Produces a priority for auto registration's sorting.
     *
     * @see IRegistrant.PriorityPair
     * @see IRegistrant.PriorityRange
     */
    @ApiStatus.NonExtendable default @NotNull IRegistrant.PriorityPair ofPriority(@NotNull IRegistrant.PriorityRange mainRange, @Range(from = 1, to = 999) int subRange)
        { return new PriorityPair(mainRange, subRange); }
    
    /**
     * Used for order sensitive registries.
     *
     * @apiNote Smaller number has higher property.
     * @see PriorityRange
     */
    @NotNull PriorityPair getPriority();
    
    final class PriorityPair implements INestedPrintable
    {
        private final @NotNull PriorityRange mainRange;
        private final @Range(from = 1, to = 999) int subRange;
        
        @SuppressWarnings("ConstantValue")//! Defensive check.
        private PriorityPair(@NotNull PriorityRange mainRange, @Range(from = 1, to = 999) int subRange)
        {
            Objects.requireNonNull(mainRange, "Param \"mainRange\" must not be null!");
            FunctionalUtils.throwIf(
                subRange < 1 || subRange > 999,
                "Param \"subRange\" should be between 1 and 999!",
                IllegalArgumentException::new
            );
            this.mainRange = mainRange;
            this.subRange = subRange;
        }
        
        public @Range(from = 1001, to = 3999) int fullPriority() { return mainRange.priority + subRange; }
        
        @Override public boolean equals(Object obj)
        {
            return this == obj || obj instanceof PriorityPair that &&
                this.mainRange.equals(that.mainRange) &&
                this.subRange == that.subRange;
        }
        
        @Override public int hashCode() { return Objects.hash(mainRange, subRange); }
        
        @Override public @NotNull String toString() { return toNestedString(); }
        
        @Override public @NotNull @Unmodifiable Map<String, Supplier<@Nullable Object>> getFields()
            { return Map.of("mainRange", mainRange::name, "subRange", () -> subRange); }
    }
    
    /**
     * The <u>{@link Enum}</u> that represents the main range of a registry class.
     *
     * @see #BASE
     * @see #FEATURE
     * @see #REFERENCE_HOLDER
     */
    enum PriorityRange
    {
        /**
         * Represents the first range which will be processed.<br>
         *
         * @apiNote {@code BASE} refers to the registries whose are lite, with no extra dependencies,
         * like {@code BlockRegistry}, {@code ItemRegistry}, etc.<br>
         * Due to their attribute, they should be initialized at first.
         */
        BASE,
        
        /**
         * Represents the second range which will be processed.
         *
         * @apiNote {@code FEATURE} refers to the registries whose are cohesive, holding order-sensitive entries,
         * which is also called, <u><a href="https://en.wikipedia.org/wiki/Domain-driven_design">DDD Design</a></u>.<br>
         * Due to their attribute, they should be processed after the lite are handled.
         */
        FEATURE,
        
        /**
         * Represents the last range will be processed.
         *
         * @apiNote {@code REFERENCE_HOLDER} refers to registries that mainly holds references to other entries, like {@code CreativeModTabRegistry}.<br>
         * Obviously, this should be processed at last.
         */
        REFERENCE_HOLDER;
        
        private final int priority;
        
        PriorityRange() { this.priority = (this.ordinal() + 1) * 1000; }
    }
}
