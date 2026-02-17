//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.items;

import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownSoulTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.abstracts.AbstractThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.projectile.ITriProjectileFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public final class ThrowableSoulTorchItem extends AbstractThrowableTorchItem<ThrownSoulTorchEntity>
{
    public ThrowableSoulTorchItem() { super(new Properties()); }
    
    @Override
    protected @NotNull BiFunction<LivingEntity, Level, ThrownSoulTorchEntity> getPlayerUsedProjectile() { return ThrownSoulTorchEntity::new; }
    
    @Override
    protected @NotNull ITriProjectileFunction<ThrownSoulTorchEntity> getDispenserUsedProjectile() { return ThrownSoulTorchEntity::new; }
}
