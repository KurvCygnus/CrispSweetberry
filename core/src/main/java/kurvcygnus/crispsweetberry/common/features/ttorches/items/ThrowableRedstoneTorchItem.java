//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.items;

import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.ITRedstoneTorchExtensions;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownRedstoneTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.abstracts.AbstractThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.projectile.ITriProjectileFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * This is the item part of ttorch series, redstone variant.
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.TemporaryRedstoneTorchBlock Floor Torch
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.TemporaryRedstoneWallTorchBlock Wall Torch
 * @see ThrownRedstoneTorchEntity Entity
 * @since 1.0 Release
 */
public final class ThrowableRedstoneTorchItem extends AbstractThrowableTorchItem<ThrownRedstoneTorchEntity>
{
    private final ITRedstoneTorchExtensions.OxidizeState oxidizeState;
    private final boolean waxed;
    
    public ThrowableRedstoneTorchItem(ITRedstoneTorchExtensions.OxidizeState oxidizeState, boolean waxed) 
    {
        super(new Properties());
        this.oxidizeState = oxidizeState;
        this.waxed = waxed;
    }
    
    @Override protected @NotNull BiFunction<LivingEntity, Level, ThrownRedstoneTorchEntity> getPlayerUsedProjectile() 
        { return (s, l) -> new ThrownRedstoneTorchEntity(s, l, this.oxidizeState, this.waxed); }
    
    @Override protected @NotNull ITriProjectileFunction<ThrownRedstoneTorchEntity> getDispenserUsedProjectile() 
        { return (x, y, z, l) -> new ThrownRedstoneTorchEntity(x, y, z, l, this.oxidizeState, this.waxed); }
}
