//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.entities;

import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries.*;

public final class GlowStickEntity extends AbstractThrownTorchEntity
{
    private static final int MAX_BOUNCE_TIME = 8;
    
    public GlowStickEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> entityType, @NotNull Level level) { super(entityType, level); }
    
    public GlowStickEntity(double x, double y, double z, @NotNull Level level) { super(GLOW_STICK_ENTITY.get(), x, y, z, level); }
    
    public GlowStickEntity(@NotNull LivingEntity shooter, @NotNull Level level) { super(GLOW_STICK_ENTITY.get(), shooter, level); }
    
    public static final EntityDataAccessor<Byte> BOUNCE_COUNTER = SynchedEntityData.defineId(GlowStickEntity.class, EntityDataSerializers.BYTE);
    
    @Override protected void addNewSynchedData(SynchedEntityData.@NotNull Builder builder) { builder.define(BOUNCE_COUNTER, (byte) 0); }
    
    @Override protected void onHitBlock(@NotNull BlockHitResult result)
    {
        if(this.level().isClientSide)
            return;
        
        final Vec3 motionVector = this.getDeltaMovement();
        final BlockPos hitPos = result.getBlockPos();
        final Direction hitSide = result.getDirection();
        final @Nullable BlockPos placementPos;
        final @Nullable BlockState stateToPlace;
        
        if(hitSide == Direction.UP)
        {
            placementPos = hitPos.above();
            stateToPlace = getFloorInitState();
        }
        else
        {
            placementPos = result.getBlockPos();
            stateToPlace = null;
            addBounceCounter();
            
            if(getBounceCounter() < MAX_BOUNCE_TIME)
            {
                final Vec3 normal = Vec3.atLowerCornerOf(hitSide.getNormal());
                final Vec3 reflected = motionVector.subtract(normal.scale(2 * motionVector.dot(normal)));
                this.setDeltaMovement(reflected.x * 0.5, reflected.y * 0.8, reflected.z * 0.5);
                this.level().playSound(
                    null,
                    getOnPos(),
                    SoundEvents.SLIME_BLOCK_STEP,
                    SoundSource.AMBIENT,
                    0.5F,
                    1.2F
                );
                return;
            }
        }
        
        if(getBounceCounter() >= MAX_BOUNCE_TIME)
        {
            playSound(getDestroySound(), SoundSource.BLOCKS, SoundConstants.LOUD_SOUND_VOLUME);
            displayDestroyParticle();
        }
        else if(stateToPlace != null && tryPlaceTorch(stateToPlace, placementPos))
            playSound(getPlaceSound(), SoundSource.BLOCKS, SoundConstants.LOUD_SOUND_VOLUME);
    }
    
    @Override protected boolean shouldDiscard(@NotNull HitResult result) { return !this.level().isClientSide && getBounceCounter() >= MAX_BOUNCE_TIME; }
    
    private byte getBounceCounter() { return this.entityData.get(BOUNCE_COUNTER); }
    
    private void addBounceCounter() { this.entityData.set(BOUNCE_COUNTER, (byte) (this.entityData.get(BOUNCE_COUNTER) + 1)); }
    
    @Override protected @NotNull BlockState getFloorInitState() 
    {
        return this.isInWater() ? 
            this.getFloorTorchBlock().defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true) :
            this.getFloorTorchBlock().defaultBlockState();
    }
    
    @Override protected @NotNull Item getDefaultItem() { return GLOWSTICK_ITEM.value(); }
    
    @Override protected @NotNull AbstractTemporaryTorchBlock<?> getFloorTorchBlock() { return GLOW_STICK_BLOCK.get(); }
    
    @Override @SuppressWarnings("DataFlowIssue")//! Glowstick doesn't have wall variant. And passing null will reveal issues as early as possible.
    protected @NotNull AbstractTemporaryWallTorchBlock<?> getWallTorchBlock() { return null; }
    
    @Override protected @NotNull ParticleOptions @NotNull [] getLongerParticleStateList() { return new ParticleOptions[] {ParticleTypes.GLOW_SQUID_INK}; }
    
    @Override protected @NotNull ParticleOptions getShorterParticle() { return ParticleTypes.GLOW_SQUID_INK; }
    
    @Override protected boolean shouldCheckLiquids() { return false; }
    
    @Override protected boolean shouldLitMob() { return false; }
    
    @Override protected @NotNull SoundEvent getPlaceSound() { return SoundEvents.SLIME_BLOCK_PLACE; }
    
    @Override protected @NotNull SoundEvent getDestroySound() { return SoundEvents.SLIME_BLOCK_BREAK; }
    
    @Override protected boolean isReplaceable(@NotNull Block posBlock, @NotNull BlockState posBlockState) { return false; }
}
