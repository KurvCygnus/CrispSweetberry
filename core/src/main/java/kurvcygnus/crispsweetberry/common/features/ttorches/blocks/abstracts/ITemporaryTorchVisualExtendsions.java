//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import net.minecraft.core.particles.ParticleOptions;
import org.jetbrains.annotations.NotNull;

/**
 * This handles the visual-related extension methods for <u>{@link AbstractGenericTorchBlock}</u>.
 *
 * @author Kurv Cygnus
 * @see AbstractGenericTorchBlock
 * @since 1.0 Release
 * @see ITemporaryTorchLifecycleExtensions Lifecycle Extensions
 */
public interface ITemporaryTorchVisualExtendsions
{
    @NotNull ParticleOptions getTorchParticle();
    @NotNull ParticleOptions getSubTorchParticle();
}
