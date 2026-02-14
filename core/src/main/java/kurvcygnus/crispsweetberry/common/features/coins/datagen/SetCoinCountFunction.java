//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.datagen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import kurvcygnus.crispsweetberry.common.features.coins.CoinRegistries;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * This is a custom <u>{@link LootItemFunction}</u> for coin's item loot drop logics.
 *
 * @author Kurv Cygnus
 * @since Release 1.0
 */
public record SetCoinCountFunction(List<LootItemCondition> predicates) implements LootItemFunction
{
    private static final int DROPS_COUNT_PER_LAYER = 9;
    
    public static final MapCodec<SetCoinCountFunction> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            LootItemCondition.DIRECT_CODEC.listOf().fieldOf("conditions").orElse(List.of()).forGetter(fun -> fun.predicates)
        ).apply(instance, SetCoinCountFunction::new)
    );
    
    public SetCoinCountFunction() { this(List.of()); }
    
    @Override
    public @NotNull LootItemFunctionType<? extends LootItemFunction> getType() { return CoinRegistries.SET_COIN_COUNT_FUNCTION.value(); }
    
    @Override
    public ItemStack apply(@NotNull ItemStack stack, @NotNull LootContext context)
    {
        final BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if(state == null) 
            return stack;
        
        final int layers = state.getValue(AbstractCoinStackBlock.LAYERS);
        
        final int finalCount = layers * DROPS_COUNT_PER_LAYER;
        
        stack.setCount(finalCount);
        
        return stack;
    }
}
