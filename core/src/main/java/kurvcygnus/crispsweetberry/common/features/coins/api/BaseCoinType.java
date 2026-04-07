//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static kurvcygnus.crispsweetberry.utils.FunctionalUtils.throwIf;

/**
 * This is a basic implementation of coin series' essential <u>{@link ICoinType contract}</u>,
 * it can be directly used as a template for custom stuff.
 * @apiNote If you want to implement optional coins, or just want to see some example to use, see 
 * {@link kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinTypes VanillaCoinTypes}.
 * @param <C> Self-restricted generic, <b>fill your implemented class as {@code C} and you you're good to go</b>.<br>
 * If you want to figure out what on earth is this, see comments in <u>{@link ICoinType}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public abstract class BaseCoinType<C extends ICoinType<C>> implements ICoinType<C>
{
    private final @NotNull String namespace;
    protected final @NotNull String id;
    protected final @NotNull Supplier<? extends AbstractCoinStackBlock<C>> blockSupplier;
    protected final @NotNull Supplier<? extends AbstractCoinStackItem<C>> stackSupplier;
    protected final @NotNull Supplier<? extends AbstractCoinItem<C>> coinSupplier;
    protected final @NotNull Supplier<? extends Item> nuggetSupplier;
    protected final int experience;
    protected final float penaltyRate;
    protected final float strength;
    protected final @NotNull Predicate<Supplier<? extends Item>> enableCondition;
    
    @SuppressWarnings("NonStrictComparisonCanBeEquality")//! Defensive check.
    protected BaseCoinType(
        @NotNull String id,
        @NotNull Supplier<? extends AbstractCoinStackBlock<C>> blockSupplier,
        @NotNull Supplier<? extends AbstractCoinStackItem<C>> stackSupplier,
        @NotNull Supplier<? extends AbstractCoinItem<C>> coinSupplier,
        @NotNull Supplier<? extends Item> nuggetSupplier,
        @Range(from = 0, to = Integer.MAX_VALUE) int experience,
        @Range(from = 0, to = 1) float penaltyRate,
        @Range(from = 0, to = (long) Float.MAX_VALUE) float strength
    )
    {
        requireNonNull(initNamespace(), "Param \"namespace\" must not be null");
        requireNonNull(id, "Param \"id\" must not be null!");
        requireNonNull(blockSupplier, "Param \"blockSupplier\" must not be null!");
        requireNonNull(stackSupplier, "Param \"stackSupplier\" must not be null!");
        requireNonNull(coinSupplier, "Param \"coinSupplier\" must not be null!");
        requireNonNull(nuggetSupplier, "Param \"nuggetSupplier\" must not be null!");
        throwIf(experience <= 0, "Param \"experience\" must be a positive integer!", IllegalArgumentException::new);
        throwIf(penaltyRate <= 0F || penaltyRate > 1F, "Param \"penaltyRate\" must be in range of (0F, 1F]!", IllegalArgumentException::new
        );
        throwIf(strength <= 0F, "Param \"strength\" must be a positive float!", IllegalArgumentException::new);
        requireNonNull(initEnableCondition(), "Param \"enableCondition\" must not be null!");
        
        this.namespace = initNamespace();
        this.id = id;
        this.blockSupplier = blockSupplier;
        this.stackSupplier = stackSupplier;
        this.coinSupplier = coinSupplier;
        this.nuggetSupplier = nuggetSupplier;
        this.experience = experience;
        this.penaltyRate = penaltyRate;
        this.strength = strength;
        this.enableCondition = initEnableCondition();
    }
    
    protected @NotNull Predicate<Supplier<? extends Item>> initEnableCondition() { return Objects::nonNull; }
    
    protected abstract @NotNull String initNamespace();
    
    public @NotNull String id() { return this.id; } 
    
    @Override public @NotNull ResourceLocation getId() { return ResourceLocation.fromNamespaceAndPath(namespace, id); }
    
    @Override public @NotNull AbstractCoinStackBlock<C> stackBlock() { return this.blockSupplier.get(); }
    
    @Override public @NotNull AbstractCoinStackItem<C> stackItem() { return this.stackSupplier.get(); }
    
    @Override public @NotNull AbstractCoinItem<C> coinItem() { return this.coinSupplier.get(); }
    
    @Override public @NotNull Item nuggetItem() { return nuggetSupplier.get(); }
    
    @Override public int getExperience() { return this.experience; }
    
    @Override public float getPenaltyRate() { return this.penaltyRate; }
    
    @Override public float getStrength() { return this.strength; }
    
    @Override public boolean shouldAppear() { return this.enableCondition.test(this.nuggetSupplier); }
    
    @Override public boolean equals(@Nullable Object object)
    {
        if(this == object)
            return true;
        if(object == null || getClass() != object.getClass())
            return false;
        
        final BaseCoinType<?> that = (BaseCoinType<?>) object;
        
        return experience == that.experience &&
            Float.compare(that.penaltyRate, penaltyRate) == 0 &&
            Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() { return hash(namespace, id); }
    
    @Override public @NotNull String toString() { return "CoinTypes[id=%s, experience=%d, strength=%f]".formatted(id, experience, strength); }
}
