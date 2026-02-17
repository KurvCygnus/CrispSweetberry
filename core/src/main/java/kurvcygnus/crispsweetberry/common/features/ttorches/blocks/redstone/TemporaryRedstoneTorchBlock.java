//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.*;

public final class TemporaryRedstoneTorchBlock extends AbstractTemporaryTorchBlock<TemporaryRedstoneTorchBehavior>
{
    private TemporaryRedstoneTorchBlock(@Nullable Properties properties) { this(); }
    
    public TemporaryRedstoneTorchBlock()
    {
        super(
            BASIC_TEMP_TORCH_PROPERTIES.lightLevel(REDSTONE_BRIGHTNESS_FORMULA),
            new TemporaryRedstoneTorchBehavior(Lazy.of(TTorchRegistries.TEMPORARY_REDSTONE_TORCH))
        );
    }
    
    
    @Override
    public @NotNull ParticleOptions getTorchParticle() { return DustParticleOptions.REDSTONE; }
    
    @Override
    public @NotNull ParticleOptions getSubTorchParticle() { return DustParticleOptions.REDSTONE; }
    
    @Override protected void addExtraBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) { builder.add(REDSTONE_LIT); }
    
    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getStateLength() { return REDSTONE_TORCH_SIGNAL_SEND_DELAY; }
    
    @Override
    public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporaryRedstoneTorchBehavior>> codec() { return simpleCodec(TemporaryRedstoneTorchBlock::new); }
    
    @Override
    protected int getSignal(@NotNull BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side)
        { return this.behavior.getSignal(blockState, side); }
}
