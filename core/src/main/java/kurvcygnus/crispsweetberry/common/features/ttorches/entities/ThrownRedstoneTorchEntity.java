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
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.ITRedstoneTorchExtensions;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries.*;
import static kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock.FACING;

/**
 * This is the entity part of ttorch series, redstone variant.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.TemporaryRedstoneTorchBlock Floor Torch
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.TemporaryRedstoneWallTorchBlock Wall Torch
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableRedstoneTorchItem Item
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.ThrownRedstoneTorchRenderer Renderer
 */
public class ThrownRedstoneTorchEntity extends AbstractThrownTorchEntity
{
    private final ITRedstoneTorchExtensions.OxidizeState oxidizeState;
    private final boolean waxed;
    
    public ThrownRedstoneTorchEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> entityType, @NotNull Level level) 
    {
        super(entityType, level);
        this.oxidizeState = ITRedstoneTorchExtensions.OxidizeState.NORMAL;
        this.waxed = false;
    }
    
    public ThrownRedstoneTorchEntity(double x, double y, double z, @NotNull Level level, @NotNull ITRedstoneTorchExtensions.OxidizeState oxidizeState, boolean waxed) 
    { 
        super(THROWN_REDSTONE_TORCH.get(), x, y, z, level);
        this.oxidizeState = oxidizeState;
        this.waxed = waxed;
    }
    
    public ThrownRedstoneTorchEntity(@NotNull LivingEntity shooter, @NotNull Level level, @NotNull ITRedstoneTorchExtensions.OxidizeState oxidizeState, boolean waxed) 
    { 
        super(THROWN_REDSTONE_TORCH.get(), shooter, level);
        this.oxidizeState = oxidizeState;
        this.waxed = waxed;
    }
    
    @Override protected void onHitEntitySequence(@NotNull Entity entity) 
    {
        if(entity instanceof Creeper creeper)
        {
            if(creeper.level().getRandom().nextFloat() > oxidizeState.getExplosionChance() || creeper.isIgnited())
                return;
            
            creeper.ignite();
        }
    }
    
    @Override protected @NotNull BlockState getFloorInitState() 
        { return REDSTONE_TTORCH_LOOKUP.get(waxed).get(this.oxidizeState).get().defaultBlockState(); }
    
    @Override protected @NotNull BlockState getWallInitState(@NotNull Direction direction)
        { return WALL_REDSTONE_TTORCH_LOOKUP.get(waxed).get(this.oxidizeState).get().defaultBlockState().setValue(FACING, direction); }
    
    @Override protected @NotNull AbstractTemporaryTorchBlock<?> getFloorTorchBlock() { return TEMPORARY_REDSTONE_TORCH.get(); }
    
    @Override protected @NotNull AbstractTemporaryWallTorchBlock<?> getWallTorchBlock() { return TEMPORARY_REDSTONE_WALL_TORCH.get(); }
    
    @Override protected @NotNull Item getDefaultItem() { return EXPOSED_THROWABLE_REDSTONE_TORCH.value(); }
    
    @Override protected boolean shouldCheckLiquids() { return false; }
    
    @Override protected @NotNull ParticleOptions @NotNull [] getLongerParticleStateList() { return new ParticleOptions[] {DustParticleOptions.REDSTONE}; }
    
    @Override protected @NotNull ParticleOptions getShorterParticle() { return DustParticleOptions.REDSTONE; }
    
    @Override protected boolean shouldLitMob() { return false; }
    
    public @NotNull ITRedstoneTorchExtensions.OxidizeState getOxidizeState() { return oxidizeState; }
}
