//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.entities;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.sync.SoulFireTagPayloads;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries.*;

/**
 * This is the entity part of ttorch series, soul fire variant.
 * <br><br>
 * Soul fire variant has a unique feature, with <u>{@link net.minecraft.world.level.block.SoulFireBlock SoulFireBlock}</u> enhanced:<br>
 * The soul fire, now is real, which could deals unextinguishable fire, whose implementation can be found at 
 * <u>{@link kurvcygnus.crispsweetberry.common.features.ttorches.mixin here}</u>.
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.blocks.soul.TemporarySoulTorchBlock Floor Torch
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.blocks.soul.TemporarySoulWallTorchBlock Wall Torch
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableSoulTorchItem Item
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.ThrownSoulTorchRenderer Renderer
 * @since 1.0 Release
 */
public final class ThrownSoulTorchEntity extends AbstractThrownTorchEntity
{
    private static final ParticleOptions[] SOUL_LONGER_PARTICLE_STATE_LIST = { ParticleTypes.SOUL_FIRE_FLAME };
    
    public ThrownSoulTorchEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> entityType, @NotNull Level level) { super(entityType, level); }
    
    public ThrownSoulTorchEntity(double x, double y, double z, @NotNull Level level) { super(THROWN_SOUL_TORCH.get(), x, y, z, level); }
    
    public ThrownSoulTorchEntity(@NotNull LivingEntity shooter, @NotNull Level level) { super(THROWN_SOUL_TORCH.get(), shooter, level); }
    
    @Override
    protected void onHitEntitySequence(@NotNull Entity entity) 
    {
        if(!entity.level().isClientSide)
            return;
        
        if(entity.getPersistentData().contains(TTorchUtilCollection.SOUL_FIRE_PERSISTENT_TAG))
            return;
        
        entity.getPersistentData().putByte(TTorchUtilCollection.SOUL_FIRE_PERSISTENT_TAG, (byte) 1);
        PacketDistributor.sendToPlayersTrackingEntity(entity, new SoulFireTagPayloads.SoulFireTagPayload(entity.getId(), true));
    }
    
    @Override protected @NotNull Item getDefaultItem() { return THROWABLE_SOUL_TORCH.value(); }
    
    @Override protected boolean shouldCheckLiquids() { return false; }
    
    @Override protected @NotNull ParticleOptions @NotNull[] getLongerParticleStateList() { return SOUL_LONGER_PARTICLE_STATE_LIST; }
    
    @Override protected @NotNull ParticleOptions getShorterParticle() { return ParticleTypes.SOUL; }
    
    @Override protected @NotNull AbstractTemporaryTorchBlock<?> getFloorTorchBlock() { return TEMPORARY_SOUL_TORCH.value(); }
    
    @Override protected @NotNull AbstractTemporaryWallTorchBlock<?> getWallTorchBlock() { return TEMPORARY_SOUL_WALL_TORCH.value(); }
}
