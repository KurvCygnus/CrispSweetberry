//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarriableLifecycle;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ISimpleBlockEntityPenaltyLogic extends ICarriableLifecycle
{
    @NotNull NonNullList<ItemStack> getItems();
    
    default float getMiscFactor() { return 0F; }
    
    @Override default int getPenaltyRate()
    {
        final float itemTotalFactor = getItems().stream().
            map(i -> ((float) i.getCount() / i.getMaxStackSize())).
            reduce(Float::sum).
            orElse(0F);
        
        return (int) (DEFAULT_PENALTY_RATE / (1 + itemTotalFactor + getMiscFactor()));
    }
}
