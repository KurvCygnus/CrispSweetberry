//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.soul;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TemporarySoulTorchBlock extends AbstractTemporaryTorchBlock<TemporarySoulTorchBehavior>
{
    private TemporarySoulTorchBlock(@Nullable Properties properties) { this(); }
    
    public TemporarySoulTorchBlock() 
    { 
        super(
            TTorchConstants.STANDARD_TEMPORARY_TORCH_PROPERTIES,
            new TemporarySoulTorchBehavior(Lazy.of(TTorchRegistries.TEMPORARY_SOUL_TORCH))
        );
    }
    
    @Override public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporarySoulTorchBehavior>> codec() { return simpleCodec(TemporarySoulTorchBlock::new); }
    
    @Override public @NotNull ParticleOptions getTorchParticle() { return ParticleTypes.SOUL_FIRE_FLAME; }
    
    @Override public @NotNull ParticleOptions getSubTorchParticle() { return ParticleTypes.SOUL; }
}
