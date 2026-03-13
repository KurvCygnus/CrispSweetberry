//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.abstracts.AbstractThrowableTorchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.function.Supplier;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection.BASIC_TEMP_TORCH_PROPERTIES;

/**
 * This is the redstone variant of ttorch series, features signal passing.
 * @author Kurv Cygnus
 * @see TemporaryRedstoneTorchBehavior Logic Implementation
 * @see ITRedstoneTorchExtensions Extension Interfaces
 * @see TemporaryRedstoneTorchBlock Floor Torch
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownRedstoneTorchEntity Entity
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableRedstoneTorchItem Item
 * @since 1.0 Release
 */
public final class TemporaryRedstoneWallTorchBlock
extends AbstractTemporaryWallTorchBlock<TemporaryRedstoneTorchBehavior> implements ITRedstoneTorchExtensions.Block, ITRedstoneTorchExtensions.Shared
{
    private final Lazy<? extends AbstractThrowableTorchItem<?>> throwableTorch;
    
    public TemporaryRedstoneWallTorchBlock
    (@NotNull ITRedstoneTorchExtensions.OxidizeState oxidizeState, boolean waxed, @NotNull Lazy<? extends AbstractThrowableTorchItem<?>> throwableTorch)
    {
        super(
            BASIC_TEMP_TORCH_PROPERTIES.lightLevel(REDSTONE_BRIGHTNESS_FORMULA),
            new TemporaryRedstoneTorchBehavior(Lazy.of(TTorchRegistries.TEMPORARY_REDSTONE_WALL_TORCH))
        );
        this.registerDefaultState(this.stateDefinition.any().
            setValue(REDSTONE_LIT, true).
            setValue(OXIDIZE_STATE, oxidizeState).
            setValue(WAXED, waxed)
        );
        this.throwableTorch = throwableTorch;
    }
    
    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getStateLength() { return REDSTONE_TORCH_SIGNAL_SEND_DELAY; }
    
    @Override protected @NotNull Supplier<? extends AbstractGenericTorchBlock<TemporaryRedstoneTorchBehavior>> getCodecConstruct()
    {
        return () -> new TemporaryRedstoneWallTorchBlock(
            ITRedstoneTorchExtensions.OxidizeState.NORMAL,
            false,
            Lazy.of(TTorchRegistries.THROWABLE_REDSTONE_TORCH)
        );
    }
    
    @Override protected void addExtraStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) 
        { builder.add(REDSTONE_LIT, OXIDIZE_STATE, WAXED); }
    
    @Override public @NotNull ParticleOptions getTorchParticle() { return DustParticleOptions.REDSTONE; }
    
    @Override public @NotNull ParticleOptions getSubTorchParticle() { return DustParticleOptions.REDSTONE; }
    
    @Override protected int getSignal(@NotNull BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side)
        { return this.behavior.getSignal(blockState, side); }
    
    @Override public @Range(from = 0, to = 15) int
    getDirectSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction side)
        { return this.behavior.getDirectSignal(state, level, pos, side); }
    
    @Override public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { this.behavior.randomTick(state, level, pos, random); }
    
    @Override public @NotNull ItemStack getCloneItemStack(
        @NotNull BlockState state,
        @NotNull HitResult target,
        @NotNull LevelReader level,
        @NotNull BlockPos pos,
        @NotNull Player player
    ) { return this.behavior.getCloneItemStack(state, target, level, pos, player); }
    
    @Override public boolean isSignalSource(@NotNull BlockState state) { return true; }
    
    @Override public @NotNull Item getThrowableTorchItem() { return this.throwableTorch.get(); }
}
