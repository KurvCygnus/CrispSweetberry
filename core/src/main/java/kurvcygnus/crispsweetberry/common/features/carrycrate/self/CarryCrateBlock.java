//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.self;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateConstants;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

//? TODO: Open & Close state, color variants, Piston compression in future releases.
//? TODO: Now: Add fragile feature.
/**
 * This is the block version of Carry Crate.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class CarryCrateBlock extends HorizontalDirectionalBlock
{
    private static final IntegerProperty DURABILITY = IntegerProperty.create("durability", 0, CarryCrateConstants.CARRY_CRATE_MAX_DURABILITY);
    
    public CarryCrateBlock()
    {
        super(BlockBehaviour.Properties.of().
            destroyTime(0.1F).
            explosionResistance(0.1F).
            ignitedByLava().
            sound(SoundType.SCAFFOLDING)
        );
        
        this.registerDefaultState(this.stateDefinition.any().
            setValue(FACING, Direction.NORTH).
            setValue(DURABILITY, CarryCrateConstants.CARRY_CRATE_MAX_DURABILITY)
        );
    }
    
    @Override protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) { builder.add(FACING, DURABILITY); }
    
    @Override public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        final int durability = Objects.requireNonNullElse(
            context.getItemInHand().get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()),
            CarryCrateConstants.CARRY_CRATE_MAX_DURABILITY
        );
        
        return this.defaultBlockState().
            setValue(FACING, context.getHorizontalDirection().getOpposite())
            .setValue(DURABILITY, durability);
    }
    
    @Override protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder params)
    {
        final List<ItemStack> stacks = super.getDrops(state, params);
        
        //! Do not change this for loop to stream.
        //! Stream mostly returns an immutable collection, which may untrackable, and obsolete crash issues with other mods.
        for(final ItemStack itemStack: stacks)
            if(itemStack.is(this.asItem()))
                itemStack.set(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get(), state.getValue(DURABILITY));
        
        return stacks;
    }
    
    @Override protected @NotNull MapCodec<CarryCrateBlock> codec() { return simpleCodec(FunctionalUtils.noArgCodec(CarryCrateBlock::new)); }
}
