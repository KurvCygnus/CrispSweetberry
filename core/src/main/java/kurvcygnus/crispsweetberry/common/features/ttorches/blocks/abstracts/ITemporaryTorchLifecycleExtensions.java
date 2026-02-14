//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * This handles the lifecycle-related extensions method for <u>{@link AbstractGenericTorchBlock}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see AbstractGenericTorchBlock
 * @see ITemporaryTorchVisualExtendsions Visual Extensions
 */
public interface ITemporaryTorchLifecycleExtensions
{
    @Range(from = 0, to = Integer.MAX_VALUE) int getStateLength();
    boolean isStillBright(@NotNull BlockState state);
}
