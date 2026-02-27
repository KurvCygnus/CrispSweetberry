//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection.*;
import static kurvcygnus.crispsweetberry.utils.definitions.SoundConstants.NORMAL_SOUND_PITCH;
import static kurvcygnus.crispsweetberry.utils.definitions.SoundConstants.NORMAL_SOUND_VOLUME;
import static kurvcygnus.crispsweetberry.utils.projectile.ProjectileConstants.*;
import static net.minecraft.world.level.block.WallTorchBlock.FACING;

/**
 * This is an universal behavior component, which can be used both for <u>{@link AbstractTemporaryTorchBlock}</u> and <u>{@link AbstractTemporaryWallTorchBlock}</u>.
 * <br>
 * It'll determine the behavior automatically based on <u>{@link #torchBlock}</u>'s flag, {@code isWallTorch}.
 *
 * @author Kurv Cygnus
 * @implNote Since Minecraft is lifecycle-sensitive, directly use <u>{@link #torchBlock}</u> will end up getting a <u>{@link NullPointerException NPE}</u>,
 * so we used <u>{@link Lazy}</u> to avoid this.
 * @see AbstractGenericTorchBlock Basic Torch Abstraction
 * @see AbstractTemporaryTorchBlock Floor Torch implementation
 * @see AbstractTemporaryWallTorchBlock Wall Torch implementation
 * @since 1.0 Release
 */
public abstract class AbstractTemporaryTorchBehavior
{
    private AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior> torchBlock = null;
    private final Lazy<? extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> lazyTorchBlock;
    private boolean isStateLengthLegal = false;
    
    public AbstractTemporaryTorchBehavior(@NotNull Lazy<? extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> lazyTorchBlock)
    {
        Objects.requireNonNull(lazyTorchBlock, "Param \"lazy\" must not be null!");
        this.lazyTorchBlock = lazyTorchBlock;
    }
    
    /**
     * @implNote TTorch series' blocks all need burnout behavior. Using <u>{@link net.minecraft.world.level.block.EntityBlock EntityBlock}</u> with
     * <u>{@link net.minecraft.world.level.block.entity.BlockEntity BlockEntity}</u> is bad, and more importantly, brings extra performance penalty,
     * so we used <u>{@link Level#scheduleTick(BlockPos, Block, int)}</u> instead.
     */
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState)
    {
        final int stateLength = this.getTorchBlock().getStateLength();
        
        if(!isStateLengthLegal)
        {
            //noinspection NonStrictComparisonCanBeEquality
            CrispFunctionalUtils.throwIf(
                stateLength <= 0,//! Defensive check.
                () -> new IllegalArgumentException("The attachTag length of tempo torches should be a positive integer! Current length: %d".formatted(stateLength))
            );
            isStateLengthLegal = true;
        }
        
        if(state.is(oldState.getBlock()))
            return;
        
        level.scheduleTick(pos, this.getTorchBlock(), stateLength);
        
        this.onPlaceSequence(state, level, pos, oldState);
    }
    
    protected void onPlaceSequence(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState) { }
    
    public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
    @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand)
    {
        if(!isRelitable() || this.getTorchBlock().isStillBright(state))
            return ItemInteractionResult.FAIL;
        
        final Item itemInHand = stack.getItem();
        
        if(!canLitStuff(stack, itemInHand))
            return ItemInteractionResult.FAIL;
        
        final boolean isDamageable = stack.isDamageableItem();
        final float damageableItemPitch = level.getRandom().nextFloat() * 0.4F + 0.8F;
        
        level.playSound(null, pos, isDamageable ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE,
            SoundSource.BLOCKS, NORMAL_SOUND_VOLUME, isDamageable ? damageableItemPitch : NORMAL_SOUND_PITCH
        );
        
        if(!level.isClientSide)
        {
            level.setBlockAndUpdate(pos, state.setValue(LIGHT_PROPERTY, LightState.FULL_BRIGHT));
            
            if(isDamageable)
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            else
                stack.consume(1, player);
        }
        
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    
    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        final double verticalParticleSpeed = (random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        final LightState oldLightState = oldState.getValue(LIGHT_PROPERTY);
        final BlockState newState = oldState.setValue(LIGHT_PROPERTY, oldLightState.getNextState());
        
        //! Terminates this method if the attachTag is already dark.
        if(oldLightState == LightState.DARK)
            return;
        
        if(!level.isClientSide)
        {
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, TORCH_BURNING_OUT_VOL, SoundConstants.NORMAL_SOUND_PITCH);
        }
        else
            level.addParticle(this.getTorchBlock().getSubTorchParticle(), pos.getX(), pos.getY(), pos.getZ(),
                X_NO_SPEED, verticalParticleSpeed, Z_NO_SPEED
            );
        
        //* Wait for next attachTag's change.
        level.scheduleTick(pos, this.getTorchBlock(), this.getTorchBlock().getStateLength());
    }
    
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, boolean isWallTorch)
    {
        //! Dark attachTag means the torch has already burned out, so of course wo should directly terminate this when the attachTag is DARK.
        if(state.getValue(LIGHT_PROPERTY) == LightState.DARK)
            return;
        
        double xPos = (double) pos.getX() + HORIZONTAL_TORCH_OFFSET_VALUE;
        double yPos = (double) pos.getY() + VERTICAL_TORCH_OFFSET_VALUE;
        double zPos = (double) pos.getZ() + HORIZONTAL_TORCH_OFFSET_VALUE;
        
        if(isWallTorch)//* Wall torch's particle position is different from standard one, of course.
        {
            final Direction direction = state.getValue(FACING).getOpposite();
            
            xPos += HORIZONTAL_WALL_TORCH_OFFSET_VALUE * (double) direction.getStepX();
            yPos += VERTICAL_WALL_TORCH_OFFSET_VALUE;
            zPos += HORIZONTAL_WALL_TORCH_OFFSET_VALUE * (double) direction.getStepZ();
        }
        
        if(level.isClientSide)
        {
            if(this.getTorchBlock().isStillBright(state))
                level.addParticle(this.getTorchBlock().getTorchParticle(), xPos, yPos, zPos, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
            level.addParticle(this.getTorchBlock().getSubTorchParticle(), xPos, yPos, zPos, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
        }
    }
    
    protected static boolean canLitStuff(@NotNull ItemStack stack, Item itemInHand)
    {
        return stack.is(ItemTags.CREEPER_IGNITERS) ||
            stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT) ||
            itemInHand instanceof FlintAndSteelItem ||
            itemInHand instanceof FireChargeItem;
    }
    
    protected AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior> getTorchBlock()
    {
        if(torchBlock == null)
        {
            this.torchBlock = this.lazyTorchBlock.get();
            return this.torchBlock;
        }
        
        return this.torchBlock;
    }
    
    protected boolean isRelitable() { return true; }
    
    protected abstract @NotNull Item getThrowableTorchItem();
}
