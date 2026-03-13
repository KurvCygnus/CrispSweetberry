//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.soul;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * This is the soul fire variant of ttorch series.
 *
 * @author Kurv Cygnus
 * @see TemporarySoulTorchBehavior Logic Implementation
 * @see TemporarySoulTorchBlock Floor Torch
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownSoulTorchEntity Entity
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableSoulTorchItem Item
 * @since 1.0 Release
 */
public final class TemporarySoulWallTorchBlock extends AbstractTemporaryWallTorchBlock<TemporarySoulTorchBehavior>
{
    public TemporarySoulWallTorchBlock() 
    {
        super(
            TTorchUtilCollection.STANDARD_TEMPORARY_TORCH_PROPERTIES,
            new TemporarySoulTorchBehavior(Lazy.of(TTorchRegistries.TEMPORARY_SOUL_WALL_TORCH))
        );
    }
    
    @Override
    public @NotNull ParticleOptions getTorchParticle() { return ParticleTypes.SOUL_FIRE_FLAME; }
    
    @Override
    public @NotNull ParticleOptions getSubTorchParticle() { return ParticleTypes.SOUL; }
    
    @Override protected @NotNull Supplier<? extends AbstractGenericTorchBlock<TemporarySoulTorchBehavior>> getCodecConstruct() { return TemporarySoulWallTorchBlock::new; }
}
