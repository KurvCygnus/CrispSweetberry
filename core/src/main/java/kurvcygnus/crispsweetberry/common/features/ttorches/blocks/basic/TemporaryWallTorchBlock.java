//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.*;

/**
 * The simple implementation of <u>{@link AbstractTemporaryWallTorchBlock}</u>.
 *
 * @author Kurv Cygnus
 * @see TemporaryTorchBehavior Behavior
 * @see TemporaryTorchBlock Floor Torch
 * @see AbstractGenericTorchBlock Basic Abstraction
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock Wall Torch Abstraction
 * @since 1.0 Release
 */
public final class TemporaryWallTorchBlock extends AbstractTemporaryWallTorchBlock<TemporaryTorchBehavior>
{
    private TemporaryWallTorchBlock(@Nullable Properties properties) { this(); }
    
    public TemporaryWallTorchBlock() { super(STANDARD_TEMPORARY_TORCH_PROPERTIES, new TemporaryTorchBehavior(Lazy.of(TTorchRegistries.TEMPORARY_WALL_TORCH))); }
    
    @Override public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporaryTorchBehavior>> codec() { return simpleCodec(TemporaryWallTorchBlock::new); }
    
    @Override public @NotNull ParticleOptions getTorchParticle() { return DEFAULT_TEMP_TORCH_PARTICLE; }
    
    @Override public @NotNull ParticleOptions getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
}
