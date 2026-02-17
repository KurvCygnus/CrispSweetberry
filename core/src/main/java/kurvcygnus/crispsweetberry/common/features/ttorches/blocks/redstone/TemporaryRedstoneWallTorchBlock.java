//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class TemporaryRedstoneWallTorchBlock extends AbstractTemporaryWallTorchBlock<TemporaryRedstoneTorchBehavior>
{
    public TemporaryRedstoneWallTorchBlock(@NotNull Properties properties, @NotNull TemporaryRedstoneTorchBehavior behavior)
    {
        super(properties, behavior);
    }
    
    @Override
    public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporaryRedstoneTorchBehavior>> codec()
    {
        return null;
    }
    
    @Override
    public boolean isStillBright(@NotNull BlockState state)
    {
        return false;
    }
    
    @Override
    public @NotNull ParticleOptions getTorchParticle()
    {
        return null;
    }
    
    @Override
    public @NotNull ParticleOptions getSubTorchParticle()
    {
        return null;
    }
}
