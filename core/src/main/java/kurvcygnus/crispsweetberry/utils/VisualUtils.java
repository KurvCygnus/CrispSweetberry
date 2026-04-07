//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils;

import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * @since 1.0 Release
 */
public final class VisualUtils
{
    private VisualUtils() { throw new IllegalAccessError("Class \"VisualUtils\" is not meant to be instantized!"); }
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static void addParticles(@NotNull Level level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, ParticleOptions @NotNull ... particleOptions)
    {
        Objects.requireNonNull(level, "Level is null, please make sure this method is called at the proper time!");
        
        if(xSpeed < 0 || ySpeed < 0 || zSpeed < 0)
            LOGGER.warn("Using negative speed value is not recommended. Speeds: x: {}, y: {}, z: {}", xSpeed, ySpeed, zSpeed);
        
        for(int index = 0; index < particleOptions.length; index++)
        {
            final ParticleOptions particle = particleOptions[index];
            Objects.requireNonNull(particle, "Particle can not be null! Null particle at index: " + index);
            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
