//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.self;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//? TODO

public final class CarryCrateBlock extends HorizontalDirectionalBlock
{
    //! Only for CODC.
    public CarryCrateBlock(@Nullable Properties properties) { this(); }
    
    public CarryCrateBlock()
    {
        super(BlockBehaviour.Properties.of().
            destroyTime(0.1F).
            explosionResistance(0.1F).
            ignitedByLava().
            sound(SoundType.SCAFFOLDING)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    @Override protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) { builder.add(FACING); }
    
    @Override public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
        { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    
    @Override protected @NotNull MapCodec<CarryCrateBlock> codec() { return simpleCodec(CarryCrateBlock::new); }
}
