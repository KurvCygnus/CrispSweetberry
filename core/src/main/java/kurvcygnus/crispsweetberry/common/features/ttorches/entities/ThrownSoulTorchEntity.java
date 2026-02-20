//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.entities;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.sync.SoulFireTagPayload;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public final class ThrownSoulTorchEntity extends AbstractThrownTorchEntity
{
    private static final ParticleOptions[] SOUL_LONGER_PARTICLE_STATE_LIST = { ParticleTypes.SOUL_FIRE_FLAME };
    
    public ThrownSoulTorchEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> entityType, @NotNull Level level) { super(entityType, level); }
    
    public ThrownSoulTorchEntity(double x, double y, double z, @NotNull Level level) { super(TTorchRegistries.THROWN_SOUL_TORCH.get(), x, y, z, level); }
    
    public ThrownSoulTorchEntity(@NotNull LivingEntity shooter, @NotNull Level level) { super(TTorchRegistries.THROWN_SOUL_TORCH.get(), shooter, level); }
    
    @Override
    protected void onHitEntitySequence(@NotNull Entity entity) 
    {
        if(!entity.level().isClientSide)
            return;
        
        if(!entity.getPersistentData().contains(TTorchConstants.SOUL_FIRE_PERSISTENT_TAG))
            PacketDistributor.sendToPlayersTrackingEntity(entity, new SoulFireTagPayload(entity.getId(), true));
    }
    
    @Override protected @NotNull Item getDefaultItem() { return TTorchRegistries.THROWABLE_SOUL_TORCH.value(); }
    
    @Override protected boolean shouldCheckLiquids() { return false; }
    
    @Override protected @NotNull ParticleOptions @NotNull[] getLongerParticleStateList() { return SOUL_LONGER_PARTICLE_STATE_LIST; }
    
    @Override protected @NotNull ParticleOptions getShorterParticle() { return ParticleTypes.SOUL; }
    
    @Override protected @NotNull AbstractTemporaryTorchBlock<?> getFloorTorchBlock() { return TTorchRegistries.TEMPORARY_SOUL_TORCH.value(); }
    
    @Override protected @NotNull AbstractTemporaryWallTorchBlock<?> getWallTorchBlock() { return TTorchRegistries.TEMPORARY_SOUL_WALL_TORCH.value(); }
}
