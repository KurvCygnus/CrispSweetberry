//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.entities;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ThrownRedstoneTorchEntity extends AbstractThrownTorchEntity
{
    public ThrownRedstoneTorchEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> entityType, @NotNull Level level) { super(entityType, level); }
    
    public ThrownRedstoneTorchEntity(double x, double y, double z, @NotNull Level level)
        { super(TTorchRegistries.THROWN_REDSTONE_TORCH.get(), x, y, z, level); }
    
    public ThrownRedstoneTorchEntity(@NotNull LivingEntity shooter, @NotNull Level level) 
        { super(TTorchRegistries.THROWN_REDSTONE_TORCH.get(), shooter, level); }
    
    @Override
    protected @NotNull Item getDefaultItem() { return TTorchRegistries.THROWABLE_REDSTONE_TORCH.value(); }
}
