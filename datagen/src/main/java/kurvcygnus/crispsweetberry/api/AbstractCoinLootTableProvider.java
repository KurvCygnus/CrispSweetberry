//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.api;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.ICoinType;
import kurvcygnus.crispsweetberry.common.features.coins.datagen.IsCrunchingCondition;
import kurvcygnus.crispsweetberry.common.features.coins.datagen.SetCoinCountFunction;
import kurvcygnus.crispsweetberry.utils.CrispLootUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * This a simple helper class to apply custom <u>{@link AbstractCoinStackBlock coin stack}</u> drops.<br>
 * If you have no demand to make custom behaviors, directly {@code extends} this, when write content list, then you are done.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public abstract class AbstractCoinLootTableProvider extends BlockLootSubProvider
{
    public AbstractCoinLootTableProvider(@NotNull HolderLookup.Provider registries) { super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), registries); }
    
    protected abstract @NotNull List<? extends AbstractCoinStackBlock<?>> initCoinBlockLists();
    
    protected void addCoinLoot(@NotNull AbstractCoinStackBlock<?> coinStackBlock, @NotNull ICoinType<?> coinType)
    {
        requireNonNull(coinStackBlock, "Param \"coinStackBlock\" must not be null!");
        requireNonNull(coinType, "Param \"coinType\" must not be null!");
        
        this.add(coinStackBlock, blockAttributes ->
            CrispLootUtils.initLootPool(() -> LootPool.lootPool().
                setRolls(ConstantValue.exactly(1F)).
                add(createAlternatives(coinType))
            )
        );
    }
    
    protected @NotNull AlternativesEntry.Builder createAlternatives(@NotNull ICoinType<?> coinType)
    {
        requireNonNull(coinType, "Param \"coinType\" must not be null!");
        
        return AlternativesEntry.alternatives(
            LootItem.
                lootTableItem(coinType.stackItem()).
                when(hasSilkTouch()),
            
            LootItem.
                lootTableItem(coinType.nuggetItem()).
                when(IsCrunchingCondition::new).
                apply(SetCoinCountFunction::new),//! Experience award is processed in Block itself, not lootTable.
            
            LootItem.
                lootTableItem(coinType.coinItem()).
                when(InvertedLootItemCondition.invert(IsCrunchingCondition::new)).
                apply(SetCoinCountFunction::new).
                apply(ApplyExplosionDecay.explosionDecay())
        );
    }
    
    @Override
    protected final void generate()
    {
        for(final Block block: getKnownBlocks())
        {
            final AbstractCoinStackBlock<?> coinStackBlock = (AbstractCoinStackBlock<?>) block;
             
            final ICoinType<?> coinType = coinStackBlock.getCoinType();
            
            addCoinLoot(coinStackBlock, coinType);
        }
    }
    
    @Override
    protected final @NotNull Iterable<Block> getKnownBlocks() { return initCoinBlockLists().stream().collect(Collectors.toUnmodifiableList()); }
}
