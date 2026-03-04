//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.IBlockEntityCarryLifecycle;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public interface ISimpleBlockEntityPenaltyLogic<E extends BlockEntity> extends IBlockEntityCarryLifecycle<E>
{
    @NotNull NonNullList<ItemStack> getItems(@NotNull E blockEntity);
    
    default float getMiscFactor(@NotNull E blockEntity) { return 0F; }
    
    @Override default int getPenaltyRate(@NotNull E blockEntity)
    {
        final float itemTotalFactor = getItems(blockEntity).stream().
            map(i -> ((float) i.getCount() / i.getMaxStackSize())).
            reduce(Float::sum).
            orElse(0F);
        
        return (int) (DEFAULT_PENALTY_RATE / (1 + itemTotalFactor + getMiscFactor(blockEntity)));
    }
}
