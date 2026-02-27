//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.block;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarriableLifecycle;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.block.ICarryStackable;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.ICarryReactable;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;

/**
 * This is the basic of Block Adapters, which doesn't include any usable logics.
 *
 * @param <B> The block this adapter takes responsibility of.
 * @author Kurv Cygnus
 * @see SimpleBlockCarryAdapter Utility Adapter
 * @since 1.0 Release
 */
public abstract class AbstractBlockCarryAdapter<B extends Block> implements ICarryStackable, ICarriableLifecycle, ICarryReactable
{
    protected final B block;
    
    public AbstractBlockCarryAdapter(@NotNull B block)
    {
        Objects.requireNonNull(block, "Param \"block\" must not be null!");
        this.block = block;
    }
    
    @Override
    public abstract @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate();
    
    @Override
    public abstract @Range(from = 0, to = Integer.MAX_VALUE) int getAcceptableCount();
}
