//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.items;

import kurvcygnus.crispsweetberry.common.features.ttorches.entities.GlowStickEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.abstracts.AbstractThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.base.functions.ITriProjectileFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

/**
 * This is the item part of ttorch series, glowstick variant.
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.blocks.glowstick.GlowStickBlock Glowstick Block
 * @see GlowStickEntity Entity
 * @since 1.0 Release
 */
public final class GlowStickItem extends AbstractThrowableTorchItem<GlowStickEntity>
{
    public GlowStickItem() { super(new Properties()); }
    
    @Override protected @NotNull BiFunction<LivingEntity, Level, GlowStickEntity> getPlayerUsedProjectile() { return GlowStickEntity::new; }
    
    @Override protected @NotNull ITriProjectileFunction<GlowStickEntity> getDispenserUsedProjectile() { return GlowStickEntity::new; }
}
