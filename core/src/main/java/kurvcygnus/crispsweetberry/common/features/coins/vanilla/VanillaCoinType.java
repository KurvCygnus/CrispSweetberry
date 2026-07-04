//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.vanilla;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.api.*;
import kurvcygnus.crispsweetberry.common.features.coins.events.NuggetItemCheckEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static kurvcygnus.crispsweetberry.common.features.coins.CoinRegistries.*;
import static net.minecraft.world.item.Items.GOLD_NUGGET;
import static net.minecraft.world.item.Items.IRON_NUGGET;

/**
 * Enum-like coin componentExecutionType definitions for vanilla ores.<br>
 * We intentionally do NOT use {@link Enum} here to allow <u>{@link net.neoforged.neoforge.common.util.Lazy lazy suppliers}</u> and more flexible registration behavior.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see ICoinType Contract Interface
 * @see BaseCoinType Basic Implementation
 */
public final class VanillaCoinType extends BaseCoinType<VanillaCoinType>
{
    private final boolean isOptional;
    
    private VanillaCoinType(
        @NotNull String id,
        @NotNull Supplier<? extends AbstractCoinStackBlock<VanillaCoinType>> blockSupplier,
        @NotNull Supplier<? extends AbstractCoinStackItem<VanillaCoinType>> stackSupplier,
        @NotNull Supplier<? extends AbstractCoinItem<VanillaCoinType>> coinSupplier,
        @NotNull Supplier<? extends Item> nuggetSupplier,
        @Range(from = 0, to = Integer.MAX_VALUE) int experience,
        @Range(from = 0, to = 1) float penaltyRate,
        @Range(from = 0, to = (long) Float.MAX_VALUE) float strength,
        boolean isOptional
    )
    {
        super(id, blockSupplier, stackSupplier, coinSupplier, nuggetSupplier, experience, penaltyRate, strength);
        this.isOptional = isOptional;
    }
    
    private VanillaCoinType(
        @NotNull String id,
        @NotNull Supplier<? extends AbstractCoinStackBlock<VanillaCoinType>> blockSupplier,
        @NotNull Supplier<? extends AbstractCoinStackItem<VanillaCoinType>> stackSupplier,
        @NotNull Supplier<? extends AbstractCoinItem<VanillaCoinType>> coinSupplier,
        @NotNull Supplier<? extends Item> nuggetSupplier,
        @Range(from = 0, to = Integer.MAX_VALUE) int experience,
        @Range(from = 0, to = 1) float penaltyRate,
        @Range(from = 0, to = (long) Float.MAX_VALUE) float strength
    )
    {
        super(id, blockSupplier, stackSupplier, coinSupplier, nuggetSupplier, experience, penaltyRate, strength);
        this.isOptional = false;
    }
    
    public static final Predicate<Supplier<? extends Item>> DEFAULT_CONDITION = Objects::nonNull;
    public static final Predicate<Supplier<? extends Item>> OPTIONAL = (supplier) -> supplier.get() != Items.AIR;
    
    public static final VanillaCoinType COPPER = new VanillaCoinType(
        "copper",
        COPPER_COIN_STACK_BLOCK,
        COPPER_COIN_STACK,
        COPPER_COIN,
        NuggetItemCheckEvent.copperNuggetSupplier,
        1,
        .7F,
        .5F,
        true
    );
    
    public static final VanillaCoinType IRON = new VanillaCoinType(
        "iron",
        IRON_COIN_STACK_BLOCK,
        IRON_COIN_STACK,
        IRON_COIN,
        IRON_NUGGET::asItem,
        3,
        .8F,
        1F
    );
    
    public static final VanillaCoinType GOLD = new VanillaCoinType(
        "gold",
        GOLD_COIN_STACK_BLOCK,
        GOLD_COIN_STACK,
        GOLD_COIN,
        GOLD_NUGGET::asItem,
        7,
        .85F,
        .8F
    );
    
    public static final VanillaCoinType DIAMOND = new VanillaCoinType(
        "diamond",
        DIAMOND_COIN_STACK_BLOCK,
        DIAMOND_COIN_STACK,
        DIAMOND_COIN,
        NuggetItemCheckEvent.diamondNuggetSupplier,
        10,
        .9F,
        1.5F
    );
    
    public static final @Unmodifiable List<VanillaCoinType> VALUES = List.of(COPPER, IRON, GOLD, DIAMOND);
    
    @Override protected @NotNull String initNamespace() { return CrispSweetberry.NAMESPACE; }
    
    protected @NotNull Predicate<Supplier<? extends Item>> initEnableCondition() { return this.isOptional ? DEFAULT_CONDITION : OPTIONAL; }
}