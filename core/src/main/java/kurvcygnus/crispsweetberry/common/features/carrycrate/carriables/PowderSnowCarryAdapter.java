//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.carriables;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.SimpleBlockCarryAdapter;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * This adapter adds the support of <u>{@link PowderSnowBlock}</u>, featuring multi-block boxing, 
 * no penalty to carry crate and no side effects.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class PowderSnowCarryAdapter extends SimpleBlockCarryAdapter<PowderSnowBlock>
{
    public PowderSnowCarryAdapter(@NotNull PowderSnowBlock block) { super(block); }
    
    @Override public @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getPenaltyRate() { return NO_PENALTY; }
    
    @Override public @Range(from = 1, to = Integer.MAX_VALUE) int getAcceptableCount() { return 4; }
    
    @Override public boolean causesOverweight() { return false; }
}
