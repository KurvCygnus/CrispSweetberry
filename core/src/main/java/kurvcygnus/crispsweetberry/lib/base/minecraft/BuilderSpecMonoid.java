//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.minecraft;

import kurvcygnus.crispsweetberry.lib.base.functions.ITriConsumer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.*;

/**
 * A <b>monoid abstraction</b> for mutable builder patterns in Minecraft mod development,
 * solving the flexibility issue where <u>{@link net.minecraft.world.level.block.state.BlockBehaviour.Properties}</u>
 * and <u>{@link net.minecraft.world.item.Item.Properties}</u> must be resolved <b>before</b>
 * calling {@code super()} &mdash; which, prior to Java 22, is mandated to be the first statement
 * in a constructor. This makes it impossible for a parent abstract class to inspect or modify
 * the properties a child class submits.
 * <hr>
 * <h3><b>How It Solves This?</b></h3>
 * <p>This class models the builder lifecycle as a <b>monoid</b>:</p>
 * <ul>
 *     <li>
 *         <b>Identity</b> &mdash; A <u>{@link java.util.function.Supplier}</u> that creates a <b>fresh</b> builder instance.
 *         Each call to the supplier returns a <span style="color: 95cc6d">clean, unconfigured</span> builder.
 *     </li>
 *     <li>
 *         <b>Composition</b> &mdash; A chain of <u>{@link java.util.function.Consumer}</u> that configures the builder.
 *         Consumers are combined via <u>{@link java.util.function.Consumer#andThen(Consumer)}</u>,
 *         preserving <span style="color: f84b4b">order sensitivity</span> (later consumers may override
 *         settings from earlier ones).
 *     </li>
 * </ul>
 * The resulting <u>{@link Instance}</u> represents a <b>pre-configured builder factory</b>:
 * when <u>{@link Instance#get()}</u> is called, a fresh builder is created and
 * the entire configuration chain is applied atomically.
 * <hr>
 * <h3><b>Practical Usage in Parent-Child Hierarchies:</b></h3>
 * <p>
 * The critical method is <u>{@link Instance#map(Object)}</u>. A parent constructor
 * accepts a <u>{@link Nullable @Nullable}</u> Properties parameter from the child and passes it through
 * <u>{@link Instance#map(Object)}</u>:
 * </p>
 * <pre>{@code
 * // Step 1: Create shared Instance with base configuration
 * public static final BuilderSpecMonoid.Instance<BlockBehaviour.Properties> BASE =
 *     OF_BLOCK_PROPERTY.instantize(p -> p.noCollission().instabreak());
 *
 * // Step 2: Parent class uses #map() to retain configuration control
 * abstract class ParentBlock extends Block
 * {
 *     public ParentBlock(@Nullable Properties childProps)
 *     {
 *         super(BASE.map(childProps));
 *         // ^ If childProps is null, creates fresh properties and configures them.
 *         //   If childProps is non-null, applies the base config chain on top.
 *     }
 * }
 *
 * // Step 3: Child class appends its own configuration
 * class ConcreteBlock extends ParentBlock
 * {
 *     public ConcreteBlock() { super(BASE.getAndAppend(p -> p.lightLevel(...))); }
 * }
 * }</pre>
 * <p>
 * This achieves <b>inversion of control</b>: the parent decides the shared configuration,
 * while the child can extend it &mdash; without either side breaking the {@code super()} first-statement
 * constraint.
 * </p>
 * @apiNote Due to <b>immutable and functional attribute</b>, this util may bring obvious performance penalty. However,
 * this is acceptable at most cases, since they're only used during game initializing, which have no influence on gameplay.
 * @param <T> The builder type, exhaustive and non-extensible:
 *           <ul>
 *               <li><u>{@link BlockBehaviour.Properties}</u></li>
 *               <li><u>{@link Item.Properties}</u></li>
 *               <li><u>{@link AttributeSupplier.Builder}</u></li>
 *               <li><u>{@link DataComponentMap.Builder}</u></li>
 *               <li><u>{@link EntityType.Builder}</u></li>
 *               <li><u>{@link BlockEntityType.Builder}</u></li>
 *           </ul>
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class BuilderSpecMonoid<T>
{
    /**
     * Monoid for <u>{@link BlockBehaviour.Properties}</u>, the most common use case in block registration.
     * Identity: <u>{@link BlockBehaviour.Properties#of()}</u>
     */
    public static final BuilderSpecMonoid<BlockBehaviour.Properties> OF_BLOCK_PROPERTY          = new BuilderSpecMonoid<>(BlockBehaviour.Properties::of);
    
    /**
     * Monoid for <u>{@link Item.Properties}</u>.
     * Identity: <u>{@link Item.Properties#Properties() Item.Properties::new}</u>
     */
    public static final BuilderSpecMonoid<Item.Properties>           OF_ITEM_PROPERTY           = new BuilderSpecMonoid<>(Item.Properties::new);
    
    /**
     * Monoid for basic (non-living) entity attribute builders.
     * Identity: <u>{@link AttributeSupplier#builder()}</u>
     */
    public static final BuilderSpecMonoid<AttributeSupplier.Builder> OF_BASIC_ENTITY_ATTRIBUTE  = new BuilderSpecMonoid<>(AttributeSupplier::builder);
    
    /**
     * Monoid for living entity attribute builders, pre-populated with standard living entity attributes.
     * Identity: <u>{@link LivingEntity#createLivingAttributes()}</u>
     */
    public static final BuilderSpecMonoid<AttributeSupplier.Builder> OF_LIVING_ENTITY_ATTRIBUTE = new BuilderSpecMonoid<>(LivingEntity::createLivingAttributes);
    
    /**
     * Monoid for <u>{@link DataComponentMap.Builder}</u>, used for data component serialization templates.
     * Identity: <u>{@link DataComponentMap#builder()}</u>
     */
    public static final BuilderSpecMonoid<DataComponentMap.Builder>  OF_COMPONENT_MAP           = new BuilderSpecMonoid<>(DataComponentMap::builder);
    
    /**
     * @implNote Usage of <u>{@link Supplier}</u> is necessary, because most builders are <b>mutable</b>, which means, using raw {@code T} will
     * probably cause constant pollution, making this util class unusable.
     */
    private final Supplier<T> identity;
    
    private BuilderSpecMonoid(@NotNull Supplier<T> identity)
    {
        Objects.requireNonNull(identity, "Param \"identity\" must not be null!");
        this.identity = identity;
    }
    
    /**
     * Creates a monoid for <u>{@link EntityType.Builder}</u> with the given entity factory and category.<br>
     * <i><u>{@link EntityType.Builder}</u> must be created via a static factory and cannot be referenced by constructor,
     * which is why a dedicated factory method is necessary instead of a constant.</i>
     *
     * @param constructor The entity factory that creates entity instances.
     * @param category    The <u>{@link MobCategory}</u> for this entity type.
     * @param <T>         The concrete entity type.
     * @return A new monoid whose identity creates a fresh <u>{@link EntityType.Builder}</u> via
     *         <u>{@link EntityType.Builder#of(EntityType.EntityFactory, MobCategory)}</u>.
     */
    public static <T extends Entity> @NotNull BuilderSpecMonoid<EntityType.Builder<? extends T>> ofEntityType(
        EntityType.@NotNull EntityFactory<? extends T> constructor,
        @NotNull MobCategory category
    )
    {
        Objects.requireNonNull(constructor, "Param \"constructor\" must not be null!");
        Objects.requireNonNull(category, "Param \"category\" must not be null!");
        return new BuilderSpecMonoid<>(() -> EntityType.Builder.of(constructor, category));
    }
    
    /**
     * Creates a monoid for <u>{@link BlockEntityType.Builder}</u> with the given block entity factory and valid blocks.<br>
     * <i>Like <u>{@link EntityType.Builder}</u>, <u>{@link BlockEntityType.Builder}</u> also requires a static factory, making a dedicated
     * factory method necessary.</i>
     *
     * @param factory The block entity supplier.
     * @param blocks  The valid blocks this block entity type can be attached to.
     * @param <T>     The concrete block entity type.
     * @return A new monoid whose identity creates a fresh <u>{@link BlockEntityType.Builder}</u> via
     *         <u>{@link BlockEntityType.Builder#of(BlockEntityType.BlockEntitySupplier, Block...)}</u>.
     */
    public static <T extends BlockEntity> @NotNull BuilderSpecMonoid<BlockEntityType.Builder<T>> ofBlockEntityType(
        BlockEntityType.@NotNull BlockEntitySupplier<? extends T> factory,
        @NotNull Block @NotNull ... blocks
    )
    {
        Objects.requireNonNull(factory, "Param \"factory\" must not be null!");
        Objects.requireNonNull(blocks, "Param \"blocks\" must not be null!");
        return new BuilderSpecMonoid<>(() -> BlockEntityType.Builder.of(factory, blocks));
    }
    
    /**
     * Creates the first <u>{@link Instance}</u> from this monoid with the initial configuration step.
     * <p>
     * This is the entry point for defining a shared builder configuration. Starting from
     * this <u>{@link Instance}</u>, additional configuration can be chained via
     * <u>{@link Instance#compose(Consumer)}</u>.
     * </p>
     *
     * @param appender The first configuration operation to apply to the builder.
     * @return A new <u>{@link Instance}</u> that will create a fresh builder and apply
     *         the given {@code appender} whenever <u>{@link Instance#get()}</u> is called.
     */
    public @NotNull Instance<T> instantize(@NotNull Consumer<T> appender)
    {
        Objects.requireNonNull(appender, "Param \"appender\" must not be null!");
        return new Instance<>(identity, appender);
    }
    
    /**
     * A <b>pre-configured builder factory</b> produced by <u>{@link BuilderSpecMonoid#instantize(Consumer)}</u>.
     * <p>
     * Each <u>{@link Instance}</u> holds two components:
     * </p>
     * <ul>
     *     <li>
     *         An <b>identity</b> <u>{@link java.util.function.Supplier}</u> &mdash; the factory
     *         that creates a fresh, unconfigured builder.
     *     </li>
     *     <li>
     *         A <b>transformer</b> <u>{@link java.util.function.Consumer}</u> &mdash; the
     *         accumulated configuration chain to apply to the builder.
     *     </li>
     * </ul>
     * <p>
     * <span style="color: 95cc6d">Composition is <b>immutable</b>:</span> each call to
     * <u>{@link #compose(Consumer)}</u> returns a <b>new</b> <u>{@link Instance}</u> with the
     * additional configuration appended, leaving the original unchanged.
     * </p>
     * <p><b>Key methods:</b></p>
     * <ul>
     *     <li><u>{@link #get()}</u> &mdash; Creates a <b>fresh</b> builder and applies the full config chain.</li>
     *     <li><u>{@link #map(Object)}</u> &mdash; Applies the config chain to an <b>existing</b> builder
     *         (or creates a fresh one if the argument is {@code null}). This is the primary mechanism
     *         for parent constructors to retain property control.</li>
     *     <li><u>{@link #getAndAppend(Consumer)}</u> &mdash; Fresh builder + one additional
     *         configuration step, a convenience for concrete leaf classes.</li>
     * </ul>
     *
     * @param <T> The builder type.
     */
    public static final class Instance<T>
    {
        private final Supplier<T> identity;
        private final Consumer<T> transformer;

        private Instance(@NotNull Supplier<T> identity, @NotNull Consumer<T> transformer)
        {
            Objects.requireNonNull(identity, "Param \"identity\" must not be null!");
            Objects.requireNonNull(transformer, "Param \"transformer\" must not be null!");
            this.identity = identity;
            this.transformer = transformer;
        }
        
        /**
         * Creates a <b>fresh</b> builder from the identity supplier and applies the full configuration chain.
         *
         * @return A fully configured builder instance.
         */
        public @NotNull T get() { return apply(identity.get(), transformer); }

        /**
         * Applies the configuration chain to an existing builder, or creates a fresh one if {@code base} is {@code null}.
         * <p>
         * This is the <span style="color: 95cc6d">primary mechanism for parent constructors</span> to retain
         * property control. The parent declares its constructor parameter as {@code @Nullable T childProps}
         * and calls {@code sharedInstance.map(childProps)}. If the child passes its own properties, they are
         * used as the base and the shared config chain is applied on top; if the child passes {@code null},
         * a fresh builder is created from the monoid identity instead.
         * </p>
         * @apiNote The argument is mutated in-place via {@link java.util.function.Consumer#accept(Object)},
         * so it should not be reused after calling this method.
         * @param base An existing builder to configure, or {@code null} to create a fresh one.
         * @return The configured builder (either the same instance as {@code base} or a new one).
         */
        public @NotNull T map(@Nullable T base) { return apply(Objects.requireNonNullElseGet(base, this.identity), transformer); }

        /**
         * A convenience shortcut for <u>{@link #get()}</u> followed by one additional configuration step.
         * <p>
         * Equivalent to: {@code instance.compose(appender).get()}
         * </p>
         * <p>
         * This is <span style="color: 95cc6d">commonly used by concrete leaf classes</span> that build upon
         * a shared Instance and add only a small amount of specialization.
         * </p>
         *
         * @param appender An extra configuration operation applied after the existing chain.
         * @return A fully configured builder with both the existing and the extra configuration applied.
         */
        public @NotNull T getAndAppend(@NotNull Consumer<T> appender)
        {
            Objects.requireNonNull(appender, "Param \"appender\" must not be null!");
            return apply(get(), appender);
        }
        
        public <U> @NotNull Function<U, T> makeInputFactory(@NotNull BiConsumer<T, ? super U> appender)
        {
            Objects.requireNonNull(appender, "Param \"appender\" must not be null!");
            return input -> apply(get(), val -> appender.accept(val, input));
        }
        
        public <U, V> @NotNull BiFunction<U, V, T> makeInputFactory(@NotNull BiConsumer<T, ? super U> appender1, @NotNull BiConsumer<T, ? super V> appender2)
        {
            Objects.requireNonNull(appender1, "Param \"appender1\" must not be null!");
            Objects.requireNonNull(appender2, "Param \"appender2\" must not be null!");
            return (i1, i2) ->
            {
                final T target = get();
                appender1.accept(target, i1);
                appender2.accept(target, i2);
                return target;
            };
        }
        
        public <U, V> @NotNull BiFunction<U, V, T> makeInputFactory(@NotNull ITriConsumer<? super U, ? super V, T> appender)
        {
            Objects.requireNonNull(appender, "Param \"appender\" must not be null!");
            return (i1, i2) ->
            {
                final T target = get();
                appender.accept(i1, i2, target);
                return target;
            };
        }

        /**
         * Returns a <b>new</b> {@link Instance} with the given configuration step appended to the chain.
         * <p>
         * <span style="color: 95cc6d">The original <u>{@link Instance}</u> remains unchanged</span> &mdash;
         * composition is immutable.
         * </p>
         *
         * @param appender The configuration operation to append.
         * @return A new <u>{@link Instance}</u> representing the combined configuration.
         */
        public @NotNull Instance<T> compose(@NotNull Consumer<T> appender)
        {
            Objects.requireNonNull(appender, "Param \"appender\" must not be null!");
            return new Instance<>(identity, transformer.andThen(appender));
        }

        /**
         * Returns a <b>new</b> {@link Instance} with multiple configuration steps appended,
         * applied in the order they are provided.
         *
         * @param appenders The configuration operations to append, in order.
         * @return A new <u>{@link Instance}</u> representing the combined configuration.
         */
        @SafeVarargs public final @NotNull Instance<T> compose(@NotNull Consumer<T> @NotNull ... appenders)
        {
            var combined = transformer;
            for(final var appender: appenders)
                combined = combined.andThen(appender);
            return new Instance<>(identity, combined);
        }

        /**
         * Returns a <b>new</b> {@link Instance} by merging the transformer of another Instance
         * into this one's configuration chain.
         * <p>
         * This enables <b>composing pre-built Instances</b> together, allowing shared configuration
         * blocks to be defined independently and combined modularly.
         * </p>
         *
         * @param instance The other <u>{@link Instance}</u> whose transformer will be appended.
         * @return A new <u>{@link Instance}</u> representing the combined configuration.
         */
        public @NotNull Instance<T> compose(@NotNull Instance<T> instance)
        {
            Objects.requireNonNull(instance, "Param \"instance\" must not be null!");
            return compose(instance.transformer);
        }

        /**
         * Returns a <b>new</b> {@link Instance} by merging multiple Instances into this one's
         * configuration chain, applied in the order they are provided.
         * @param instances The other <u>{@link Instance}</u>s to merge, in order.
         * @return A new <u>{@link Instance}</u> representing the combined configuration.
         */
        @SafeVarargs public final @NotNull Instance<T> compose(@NotNull Instance<T> @NotNull ... instances)
        {
            Objects.requireNonNull(instances, "Param \"instances\" must not be null!");
            var combined = transformer;
            for(final var instance: instances)
            {
                final var transformer = Objects.requireNonNull(instance.transformer);
                combined = combined.andThen(transformer);
            }
            return new Instance<>(identity, combined);
        }

        /**
         * Applies a <u>{@link Consumer}</u> to a value and returns the same value.
         * This enables <b>fluent mutation</b>: configuring a builder in an expression context
         * (e.g. inside {@code return} or as a constructor argument) rather than as a statement.
         * <i>Pattern known as "tap" or "apply" in functional libraries.</i>
         * @param val     The value to configure.
         * @param appender The configuration operation.
         * @param <T>     The value type.
         * @return The same {@code val} instance after configuration.
         */
        private static <T> @NotNull T apply(@NotNull T val, @NotNull Consumer<T> appender)
        {
            appender.accept(val);
            return val;
        }
    }
}
