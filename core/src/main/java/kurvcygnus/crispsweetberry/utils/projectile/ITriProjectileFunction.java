//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.projectile;

import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that produces a projectile that is spawned by item.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@FunctionalInterface
public interface ITriProjectileFunction<E extends ThrowableItemProjectile>
{
    @NotNull E apply(double x, double y, double z, Level level);
}
