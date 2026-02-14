//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic;

import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBehavior;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

/**
 * A simple implementation of <u>{@link AbstractTemporaryTorchBehavior}</u>, used by <u>{@link TemporaryTorchBlock}</u>, and <u>{@link TemporaryWallTorchBlock}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see TemporaryTorchBlock Floor Torch
 * @see TemporaryWallTorchBlock Wall Torch
 * @see AbstractGenericTorchBlock Basic Abstraction
 * @see AbstractTemporaryTorchBehavior Behavior Abstraction
 */
public final class TemporaryTorchBehavior extends AbstractTemporaryTorchBehavior
{
    public <T extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> TemporaryTorchBehavior(@NotNull Lazy<T> torchBlock)
        { super(torchBlock); }
    
    @Override
    protected boolean isRelitable() { return true; }
}
