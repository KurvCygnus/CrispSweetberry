//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.core.registry;

import com.google.errorprone.annotations.DoNotCall;
import kurvcygnus.crispsweetberry.lib.base.extensions.BaseNestedPrinter;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.*;
import org.slf4j.helpers.MessageFormatter;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.regex.Pattern;

/**
 * A facade interface, which makes the implementer capable of registering entries automatically.
 * @apiNote Make sure the <b>registry class is an enum, and has only one enumeration</b> named {@code INSTANCE}(<i>or {@code INST}, this is actually unlimited,
 * but we recommend to follow this, it makes your classes consistent</i>),
 * it makes automatic registration works correctly, and help others understand automatic registration quickly.
 * @implSpec
 * A simple example of usage:
 * <ul>
 *     <li>
 *         <h3>Registry:</h3>
 *         <pre>{@code
 *          public enum FooRegistry implements IRegistrant<FooRegistry>
 *          {
 *              INST;
 *
 *              @Override public void register(@NotNull IEventBus bus) { BLOCK_DEFERRED_REGISTER.register(bus); }
 *
 *              @Override public @NotNull PriorityPair getPriority() { return ofPriority(PriorityRange.BASE, 1); }
 *
 *              private static final DeferredRegister<Block> BLOCK_DEFERRED_REGISTER = DeferredRegister.createBlocks("yourmodid");
 *
 *              public static final Holder<Block> FOO_BLOCK = BLOCK_DEFERRED_REGISTER.register(
 *                  "foo", () -> new ScaffoldingBlock(BlockBehaviour.Properties.of())
 *              );
 *          }
 *         }</pre>
 *     </li>
 *     <li>
 *         <h3>Config:</h3>
 *         <pre>{@code
 *          public enum FooConfig implements IRegistrant.OfSimpleConfigSupport<FooConfig>
 *          {
 *              INST;
 *
 *              private static final Builder BUILDER = new Builder();
 *
 *              public static final BooleanValue FOO = BUILDER.
 *                  comment("FOO").
 *                  translation("yourmodid.config.foo").
 *                  define("Foo", false);
 *
 *              public static final ModConfigSpec SPEC = BUILDER.build();
 *
 *              @Override public @NotNull ModConfigSpec getSpec() { return SPEC; }
 *
 *              @Override public @NotNull ModConfig.Type getType() { return ModConfig.Type.CLIENT; }
 *          }
 *         }</pre>
 *     </li>
 * </ul>
 * Don't forget to register your <u>{@link net.neoforged.fml.ModContainer ModContainer}</u> on <u>{@link CrispRegistrationManager}</u>:
 * <pre>{@code
 *  // At YourModEntryClass.<init>:
 *  CrispRegistrationManager.register(modContainer, eventBus);
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
 *         it is usually not a big deal, for DDD driven designs, it's recommended to use <u>{@link net.neoforged.neoforge.common.util.Lazy Lazy}</u> when necessary.
 *     </li>
 *     <li>
 *         Q: Isn't manually adjusting initialization priorities troublesome?<br>
 *         A: Yeah, you got me. This is the only tricky issue we are deal with. Despite adjusting neither this our solution's property nor the traditional one are annoying,
 *         our solution's issue is implicit property view, I don't deny that, <b>this is a trade-off.</b>
 *     </li>
 * </ul>
 * @param <T> The type of the registry itself.
 * @since Release 1.0
 * @author Kurv Cygnus
 * @see net.neoforged.neoforge.registries.DeferredRegister DeferredRegister
 * @see IEventBus Event Bus API
 * @see CrispRegistrationManager Auto Registration Implementation
 * @see OfConfigSupport Config Support
 * @see ISortable Priority Definitions
 * @see IDefinitions Methods Definitions
 */
public non-sealed interface IRegistrant<T extends Enum<T> & IRegistrant<T>> extends IDefinitions<T>, ISortable
{
    //* |=========================================================================================|
    //* | Note: [[IRegistrant]] itself only includes registering util methods.                    |
    //* | For getting started, or detailed documentation, see [[IDefinitions]] and [[ISortable]]. |
    //* |=========================================================================================|
    
    /**
     * A simple util for registering listed <u>{@link DeferredRegister}</u>.
     * @implNote Yes, this could be <b>{@code static}</b>, but non-static grantees the better UX, instead of {@code IRegistrant.registerByList(...)}.
     * @apiNote This method will filter out the duplicated elements, with a warn printed.
     */
    @ApiStatus.NonExtendable default void registerByList(@NotNull List<@NotNull DeferredRegister<?>> registries, @NotNull IEventBus bus)
    {
        Objects.requireNonNull(registries, "Param \"registries\" must not be null!");
        Objects.requireNonNull(bus, "Param \"bus\" must not be null!");
        
        registerLoop(bus, registries::get, registries.size());
    }
    
    /**
     * A simple util for registering a group of <u>{@link DeferredRegister}</u>.
     * @implNote Yes, this could be <b>{@code static}</b>, but non-static grantees the better UX, instead of {@code IRegistrant.registerByVarargs(...)}.
     * @apiNote This method will filter out the duplicated elements, with a warn printed.
     */
    @ApiStatus.NonExtendable default void registerByVarargs(@NotNull IEventBus bus, @NotNull DeferredRegister<?> @NotNull ... registries)
    {
        Objects.requireNonNull(bus, "Param \"bus\" must not be null!");
        Objects.requireNonNull(registries, "Param \"registries\" must not be null!");
        
        registerLoop(bus, i -> registries[i], registries.length);
    }
    
    private void registerLoop(@NotNull IEventBus bus, @NotNull IntFunction<DeferredRegister<?>> getter, int size)
    {
        if(size == 0)
            throw new IllegalArgumentException("Param \"registries\" must have at least one element!");
        
        final var mem = new HashSet<DeferredRegister<?>>(size, 1F);
        
        for(int index = 0; index < size; index++)
        {
            final var deferredRegister = getter.apply(index);
            Objects.requireNonNull(
                deferredRegister,
                MessageFormatter.format(
                    "The element in array's index {} is null! Content: {}",
                    index,
                    deferredRegister
                ).getMessage()
            );
            
            if(!mem.add(deferredRegister))
            {
                CrispRegistrant.LOGGER.warn("The element in array's index {} is already registered! Content: {}", index, deferredRegister);
                continue;
            }
            
            deferredRegister.register(bus);
        }
    }
    
    /**
     * This interface supports the auto registration of <u>{@link net.neoforged.neoforge.common.ModConfigSpec mod configs}</u>.
     * @apiNote Since <u>{@link IConfigSpec}</u> supports customized implementation, and there do exists obvious ability difference between
     * <u>{@link IConfigSpec basic contract}</u> and some implementations
     * (e.g. <u>{@link ModConfigSpec}</u> supports <u>{@link ModConfigSpec#save()}</u>, which <u>{@link IConfigSpec}</u> can't),
     * this basic interface supports specifying the detailed <u>{@link IConfigSpec}</u> type.<br><br>
     * <i>If the type of config is <u>{@link ModConfigSpec}</u>, <u>{@link OfSimpleConfigSupport}</u> is recommended.</i>
     * @author Kurv Cygnus
     * @since 1.0 Release
     */
    interface OfConfigSupport<T extends Enum<T> & OfConfigSupport<T, C>, C extends IConfigSpec>
    {
        @NotNull C getSpec();
        
        @NotNull ModConfig.Type getType();
        
        /**
         * @implNote This is useless, literally. It is existed to suppress "unused" warnings,
         * since generic arg {@code T} is <b>only</b> used to restrict implementer's type, making sure that the implementer must be an <u>{@link Enum}</u>.
         */
        @SuppressWarnings("unused") @DoNotCall @Deprecated(forRemoval = true) private @Nullable T dummy() { return null; }
    }
    
    /**
     * This interface supports simple auto registration of <u>{@link net.neoforged.neoforge.common.ModConfigSpec mod configs}</u>.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    interface OfSimpleConfigSupport<T extends Enum<T> & OfSimpleConfigSupport<T>> extends OfConfigSupport<T, ModConfigSpec> {}
}

/**
 * This interface holds the methods that should be implemented by <u>{@link IRegistrant}</u>'s implementers.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see ISortable
 * @see IRegistrant
 */
sealed interface IDefinitions<T extends Enum<T> & IDefinitions<T>>
{
    /**
     * The method which will be called in auto registration phase.<br>
     * You should write your <u>{@link DeferredRegister}</u>'s register logic at here.
     * @see IRegistrant#registerByList(List, IEventBus)
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
        //* Convert Result Example: FooBarBoxRegistry -> Foo Bar Box
        return regexReplace(
            CrispRegistrant.SPLIT_REGEX,
            regexReplace(CrispRegistrant.REGISTRY_REGEX, this.getClass().getSimpleName(), ""),
            " "
        ).trim();
    }
    
    /**
     * @implNote <u>{@link String#replaceAll(String, String)}</u>'s implementation does literally complies regex every single time,
     * and this method is designed to solve this, it will be slightly faster than <u>{@link String#replaceAll(String, String)}</u>.
     */
    private static @NotNull String regexReplace(@NotNull Pattern regex, @NotNull String text, @NotNull String replacement)
        { return regex.matcher(text).replaceAll(replacement); }
    
    /**
     * @implNote This is useless, literally. It is existed to suppress "unused" warnings,
     * since generic arg {@code T} is <b>only</b> used to restrict implementer's type, making sure that the implementer must be an <u>{@link Enum}</u>.
     */
    @SuppressWarnings("unused") @DoNotCall @Deprecated(forRemoval = true) private @Nullable T dummy() { return null; }
}

/**
 * A package-private class which holds private constants. It is named {@code CrispRegistrant} to make <u>{@link #LOGGER}</u>'s metainfo prettier.
 * @since 1.0 Release
 */
final class CrispRegistrant
{
    private CrispRegistrant() { throw new IllegalAccessError("Class \"CrispRegistrant\" is not meant to be instantized!"); }
    
    static final IMarkLogger LOGGER = IMarkLogger.marklessLogger();
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
     * @see IRegistrant.PriorityPair
     * @see IRegistrant.PriorityRange
     * @implNote Yes, this could be <b>{@code static}</b>, but non-static grantees the better UX, instead of {@code IRegistrant.ofPriority(...)}.
     */
    @ApiStatus.NonExtendable default @NotNull PriorityPair ofPriority(@NotNull PriorityRange mainRange, @Range(from = 1, to = 999) int subRange)
        { return new PriorityPair(mainRange, subRange); }
    
    /**
     * Used for order sensitive registries.
     *
     * @apiNote Smaller number has higher property.
     * @see PriorityRange
     * @see #ofPriority(PriorityRange, int) 
     */
    @NotNull PriorityPair getPriority();
    
    final class PriorityPair extends BaseNestedPrinter<PriorityPair>
    {
        private static final @NotNull @Unmodifiable INestedFieldMap<PriorityPair> FIELD_MAP = INestedPrintable.buildFieldMap(
            map ->
            {
                map.put("mainRange", p -> p.mainRange.name());
                map.put("subRange", p -> p.subRange);
            },
            2
        );
        
        private final PriorityRange mainRange;
        private final @Range(from = 1, to = 999) int subRange;
        
        private PriorityPair(@NotNull PriorityRange mainRange, @Range(from = 1, to = 999) int subRange)
        {
            Objects.requireNonNull(mainRange, "Param \"mainRange\" must not be null!");
            
            if(subRange < 1 || subRange > 999)
                throw new IllegalArgumentException("Param \"subRange\" should be between 1 and 999!");
            
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
        
        @Override public @NotNull @Unmodifiable INestedFieldMap<PriorityPair> getFields() { return FIELD_MAP; }
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
         * Represents the first range which will be processed.
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
    }
}
