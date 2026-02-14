//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone;

import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBehavior;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

public final class TemporaryRedstoneTorchBehavior extends AbstractTemporaryTorchBehavior
{
    private boolean signalSent = false;
    
    public <T extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> TemporaryRedstoneTorchBehavior(@NotNull Lazy<T> torchBlock)
        { super(torchBlock); }
    
    @Override
    protected boolean isRelitable() { return false; }
}
