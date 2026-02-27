//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.carriables.block;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.block.SimpleBlockCarryAdapter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PowderSnowBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class PowderSnowCarryAdapter extends SimpleBlockCarryAdapter<Block>
{
    public PowderSnowCarryAdapter(@NotNull Block block) 
    {
        super(block);
        
        if(!(block instanceof PowderSnowBlock))
            throw new IllegalArgumentException("This is for PowderSnow block only!");
    }
    
    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate() { return NO_PENALTY; }
    
    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getAcceptableCount() { return 4; }
}
