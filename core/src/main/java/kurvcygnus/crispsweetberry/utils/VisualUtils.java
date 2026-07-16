//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * @since 1.0 Release
 */
public final class VisualUtils
{
    private VisualUtils() { throw new IllegalAccessError("Class \"VisualUtils\" is not meant to be instantized!"); }
    
    public static void addParticles(
        @NotNull Level level,
        double x,
        double y,
        double z,
        double xSpeed,
        double ySpeed,
        double zSpeed,
        @NotNull ParticleOptions @NotNull ... particleOptions
    ) { for(final var particle: particleOptions) level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed); }
}
