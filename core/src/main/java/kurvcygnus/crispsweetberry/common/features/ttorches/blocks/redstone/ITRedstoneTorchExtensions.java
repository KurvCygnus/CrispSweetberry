//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone;

import kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableRedstoneTorchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Optional;
import java.util.function.ToIntFunction;

/**
 * This is the extension collection of the ttorch series' redstone variant.<br>
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see TemporaryRedstoneTorchBlock Floor Torch
 * @see TemporaryRedstoneWallTorchBlock Wall Torch
 * @see TemporaryRedstoneTorchBehavior Behavior
 */
public final class ITRedstoneTorchExtensions
{
    /**
     * A simple implementation of vanilla's copper's lifecycle.
     * Since vanilla's mechanic relies on <b>{@link WeatheringCopper specific interface}</b>, we have to implement this instead of use vanilla's.
     */
    public enum OxidizeState implements StringRepresentable
    {
        NORMAL,
        EXPOSED,
        WEATHERED,
        OXIDIZED;
        
        private final int signalStrength;
        private final float explosionChance;
        
        OxidizeState() 
        {
            this.signalStrength = 15 - (this.ordinal() * 3);
            this.explosionChance = 0.25F + this.ordinal() * 0.1F;
        }
        
        @Override
        public @NotNull String getSerializedName() { return this.name().toLowerCase(); }
        
        public int getSignalStrength() { return signalStrength; }
        
        public float getExplosionChance() { return explosionChance; }
        
        public @NotNull Optional<OxidizeState> getNextState()
        {
            if(this.ordinal() + 1 == values().length)
                return Optional.empty();
            
            return Optional.of(values()[this.ordinal() + 1]);
        }
    }
    
    public static @NotNull DeferredHolder<net.minecraft.world.level.block.Block, TemporaryRedstoneTorchBlock> getRedstoneTorchBlock(
        @NotNull DeferredRegister<net.minecraft.world.level.block.Block> register,
        ITRedstoneTorchExtensions.@NotNull OxidizeState state,
        boolean waxed,
        @NotNull DeferredHolder<Item, ThrowableRedstoneTorchItem> throwableTorch
    )
    {
        return register.register(
            "%s%stemporary_redstone_torch".formatted(waxed ? "waxed_" : "", state.equals(OxidizeState.NORMAL) ? "" : "%s_".formatted(state.name().toLowerCase())),
            resourceLocation -> new TemporaryRedstoneTorchBlock(state, waxed, Lazy.of(throwableTorch))
        );
    }
    
    public static @NotNull DeferredHolder<net.minecraft.world.level.block.Block, TemporaryRedstoneWallTorchBlock> getRedstoneWallTorchBlock(
        @NotNull DeferredRegister<net.minecraft.world.level.block.Block> register,
        ITRedstoneTorchExtensions.@NotNull OxidizeState state,
        boolean waxed,
        @NotNull DeferredHolder<Item, ThrowableRedstoneTorchItem> throwableTorch
    )
    {
        return register.register(
            "%s%stemporary_redstone_wall_torch".formatted(waxed ? "waxed_" : "", state.equals(OxidizeState.NORMAL) ? "" : "%s_".formatted(state.name().toLowerCase())),
            resourceLocation -> new TemporaryRedstoneWallTorchBlock(state, waxed, Lazy.of(throwableTorch))
        );
    }
    
    /**
     * Carries the basic constants and methods that both block and behavior needs.
     */
    interface Shared
    {
        BooleanProperty REDSTONE_LIT = BooleanProperty.create("lit");
        BooleanProperty WAXED = BooleanProperty.create("wax");
        EnumProperty<OxidizeState> OXIDIZE_STATE = EnumProperty.create("oxidize_state", OxidizeState.class);
        
        void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random);
        
        @NotNull ItemStack getCloneItemStack(
            @NotNull BlockState state,
            @NotNull HitResult target,
            @NotNull LevelReader level,
            @NotNull BlockPos pos,
            @NotNull Player player
        );
        
        @Range(from = 0, to = 15) int getDirectSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction side);
    }
    
    /**
     * Carries the data that is only used for Block.
     */
    interface Block
    {
        int REDSTONE_MAX_BRIGHTNESS = 7;
        int REDSTONE_MIN_BRIGHTNESS = 0;
        int REDSTONE_TORCH_SIGNAL_SEND_DELAY = 2;
        
        ToIntFunction<BlockState> REDSTONE_BRIGHTNESS_FORMULA = bs -> 
            bs.getValue(Shared.REDSTONE_LIT) ? Math.max(0, REDSTONE_MAX_BRIGHTNESS - bs.getValue(Shared.OXIDIZE_STATE).ordinal()) : REDSTONE_MIN_BRIGHTNESS;
    }
    
    /**
     * Carries the data that is only used for Behavior.
     */
    interface Behavior
    {
        int getSignal(@NotNull BlockState blockState, @NotNull Direction side);
        
        @NotNull BlockState getNextStateBlock(boolean isWallTorch, @NotNull BlockState state);
    }
}
