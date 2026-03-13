//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection.*;

/**
 * A simple implementation of <u>{@link AbstractTemporaryTorchBlock}</u>.
 * @author Kurv Cygnus
 * @see TemporaryTorchBehavior Behavior
 * @see TemporaryWallTorchBlock Wall Torch
 * @see AbstractGenericTorchBlock Basic Abstraction
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock Floor Torch Abstraction
 * @since 1.0 Release
 */
public final class TemporaryTorchBlock extends AbstractTemporaryTorchBlock<TemporaryTorchBehavior>
{
    public TemporaryTorchBlock() { super(STANDARD_TEMPORARY_TORCH_PROPERTIES, new TemporaryTorchBehavior(Lazy.of(TTorchRegistries.TEMPORARY_TORCH))); }
    
    @Override
    public @NotNull ParticleOptions getTorchParticle() { return DEFAULT_TEMP_TORCH_PARTICLE; }
    
    @Override
    public @NotNull ParticleOptions getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
    
    @Override protected @NotNull Supplier<? extends AbstractGenericTorchBlock<TemporaryTorchBehavior>> getCodecConstruct() { return TemporaryTorchBlock::new; }
}
