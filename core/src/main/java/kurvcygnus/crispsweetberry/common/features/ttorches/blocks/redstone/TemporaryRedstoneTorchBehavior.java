//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone;

import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Optional;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries.REDSTONE_TTORCH_LOOKUP;
import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries.WALL_REDSTONE_TTORCH_LOOKUP;
import static kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock.FACING;

/**
 * This is the behavior of redstone ttorch variant, which holds all behavior logics.
 * <br><br>
 * Redstone TTorch's behavior is quite different from <u>{@link net.minecraft.world.level.block.RedstoneTorchBlock vanilla}</u>:
 * <ul>
 *     <li>
 *         <b>Variable Signal Strength:</b> Unlike vanilla redstone torches which always emit a signal of 15, 
 *         the signal strength here is determined by the {@link ITRedstoneTorchExtensions.OxidizeState OxidizeState}. 
 *         As the torch oxidizes, the signal strength drops in increments of 3 (15, 12, 9, 6).
 *     </li>
 *     <li>
 *         <b>One-Shot Behavior:</b> This torch acts as a temporary power source. Once placed, it will eventually 
 *         transition to an unlit state or oxidize. It does not respond to block updates to "re-light" like a 
 *         vanilla torch does when its supporting block loses power.
 *     </li>
 *     <li>
 *         <b>No Burn-out Logic:</b> Vanilla torches have a complex "Toggle" system ({@code RECENT_TOGGLES}) 
 *         to prevent infinite loops by burning out if toggled more than 8 times in 60 ticks. This behavior 
 *         removes that logic entirely as the torch's lifespan is governed by oxidation and scheduled ticks.
 *     </li>
 *     <li>
 *         <b>Oxidation & Maintenance:</b> The block implements a copper-like aging system. It can be 
 *         {@link net.minecraft.world.item.HoneycombItem waxed} to lock its current signal strength or 
 *         scraped with an {@link net.minecraft.world.item.AxeItem axe} to revert oxidation stages.
 *     </li>
 * </ul>
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see ITRedstoneTorchExtensions Extension Interfaces
 * @see TemporaryRedstoneTorchBlock Floor Torch
 * @see TemporaryRedstoneWallTorchBlock Wall Torch
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownRedstoneTorchEntity Entity
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableRedstoneTorchItem Item
 */
public final class TemporaryRedstoneTorchBehavior
extends AbstractTemporaryTorchBehavior implements ITRedstoneTorchExtensions.Behavior, ITRedstoneTorchExtensions.Shared
{
    public <T extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> TemporaryRedstoneTorchBehavior(@NotNull Lazy<T> torchBlock)
        { super(torchBlock); }
    
    @Override protected void onPlaceSequence(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState)
    {
        for(final Direction direction: Direction.values())
            level.updateNeighborsAt(pos.relative(direction), this.getTorchBlock());
    }
    
    /**
     * Called only once to stop sending signals.
     */
    @Override public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        if(!oldState.getValue(REDSTONE_LIT))
            return;
        
        level.setBlockAndUpdate(pos, oldState.setValue(REDSTONE_LIT, false));
        
        for(final Direction direction: Direction.values())
            level.updateNeighborsAt(pos.relative(direction), this.getTorchBlock());
    }
    
    /**
     * <b>Returns the signal this block emits in the given direction</b>.
     * @apiNote <u>{@link Direction Directions}</u> in redstone signal related methods are backwards, so this method
     * checks for the signal emitted in the <i>opposite</i> direction of the one given.
     * </p>
     */
    @Override public int getSignal(@NotNull BlockState blockState, @NotNull Direction side) 
    {
        if(!blockState.getValue(REDSTONE_LIT) || this.getTorchBlock().isWallTorch() && blockState.getValue(FACING) == side || side == Direction.UP)
            return 0;
        
        return blockState.getValue(OXIDIZE_STATE).getSignalStrength();
    }
    
    @Override public @Range(from = 0, to = 15) int 
    getDirectSignal(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction side)
        { return side == Direction.DOWN ? this.getSignal(state, side) : 0; }
    
    @Override protected boolean isRelitable() { return false; }
    
    @Override//* Minecraft requires the block's item to be confirmed before runtime, so we can only do this.
    protected @NotNull Item getThrowableTorchItem() { return this.getTorchBlock().getThrowableTorchItem(); }
    
    @Override public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        if(state.getValue(WAXED))
            return;
        
        level.setBlockAndUpdate(pos, getNextStateBlock(this.getTorchBlock().isWallTorch(), state));
    }
    
    @Override public @NotNull BlockState getNextStateBlock(boolean isWallTorch, @NotNull BlockState state)
    {
        final Optional<ITRedstoneTorchExtensions.OxidizeState> oxidizeState = state.getValue(OXIDIZE_STATE).getNextState();
        
        return oxidizeState.map(value -> isWallTorch ?
            WALL_REDSTONE_TTORCH_LOOKUP.get(false).get(value).value().defaultBlockState() :
            REDSTONE_TTORCH_LOOKUP.get(false).get(value).value().defaultBlockState()).orElse(state);
    }
    
    @Override public @NotNull ItemStack getCloneItemStack
    (@NotNull BlockState state, @NotNull HitResult target, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull Player player)
        { return new ItemStack(getThrowableTorchItem()); }
    
    @Override public @NotNull ItemInteractionResult useItemOn(
        @NotNull ItemStack stack,
        @NotNull BlockState state,
        @NotNull Level level,
        @NotNull BlockPos pos,
        @NotNull Player player,
        @NotNull InteractionHand hand
    )
    {
        final Item itemInHand = stack.getItem();
        final boolean waxed = state.getValue(WAXED);
        final boolean isAddingWax = !waxed && itemInHand instanceof HoneycombItem;
        final boolean isRemovingWax = waxed && itemInHand instanceof AxeItem;
        
        if(!isAddingWax && !isRemovingWax)
            return ItemInteractionResult.FAIL;
        
        final ITRedstoneTorchExtensions.OxidizeState oxidizeState = state.getValue(OXIDIZE_STATE);
        
        level.playSound(null, pos, waxed ? SoundEvents.AXE_WAX_OFF : SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        if(!level.isClientSide)
        {
            final BlockState stateToChange = this.getTorchBlock().isWallTorch() ?
                WALL_REDSTONE_TTORCH_LOOKUP.get(waxed).get(oxidizeState).get().defaultBlockState() :
                REDSTONE_TTORCH_LOOKUP.get(waxed).get(oxidizeState).get().defaultBlockState();// I hate copper series.
            
            level.setBlockAndUpdate(pos, stateToChange);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, stateToChange));
            
            if(waxed)
            {
                level.levelEvent(player, 3004, pos, 0);// IDK what's this, I also don't want to find out what is this.
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            else
            {
                level.levelEvent(player, 3003, pos, 0);// IDK what's this, I also don't want to find out what is this.
                stack.shrink(1);
            }
        }
            
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
