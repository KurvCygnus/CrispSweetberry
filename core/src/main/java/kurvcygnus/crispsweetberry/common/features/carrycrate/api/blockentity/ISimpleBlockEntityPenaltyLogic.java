//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableBlockEntityExtensions;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * This interface provides a simple yet universal <u>{@link #getPenaltyRate() penaltyRate formula}</u> for blockEntity adapters.
 * @param <E> The blockEntity this adapter takes responsibility of.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public interface ISimpleBlockEntityPenaltyLogic<E extends BlockEntity> extends CarriableBlockEntityExtensions.IBlockEntityCarryLifecycle<E>
{
    /**
     * A abstract method to get item list, which is essential for penaltyRate's <u>{@link #getPenaltyRate() calculation}</u>.
     */
    @NotNull NonNullList<ItemStack> getItems(@NotNull E blockEntity);
    
    /**
     * Provides {@code blockEntity} for further customization on final penaltyRate.
     * @see BaseVanillaBrewingStandAdapter#getMiscFactor(BrewingStandBlockEntity) Use Example 
     */
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
