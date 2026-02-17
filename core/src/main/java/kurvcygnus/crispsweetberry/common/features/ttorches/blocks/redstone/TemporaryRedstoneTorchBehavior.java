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
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.REDSTONE_LIT;

public final class TemporaryRedstoneTorchBehavior extends AbstractTemporaryTorchBehavior
{
    private boolean signalSent = false;
    
    public <T extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> TemporaryRedstoneTorchBehavior(@NotNull Lazy<T> torchBlock)
        { super(torchBlock); }
    
    /**
     * <b>Returns the signal this block emits in the given direction</b>.
     * <p>
     * <b>NOTE</b>: directions in redstone signal related methods are backwards, so this method
     * checks for the signal emitted in the <i>opposite</i> direction of the one given.
     * </p>
     */
    public int getSignal(@NotNull BlockState blockState, @NotNull Direction side) { return blockState.getValue(REDSTONE_LIT) && Direction.UP != side ? 15 : 0; }
    
    @Override
    protected boolean isRelitable() { return false; }
}
