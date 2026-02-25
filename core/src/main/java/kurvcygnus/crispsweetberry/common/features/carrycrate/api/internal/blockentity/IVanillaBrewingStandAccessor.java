//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface IVanillaBrewingStandAccessor extends ISimpleMixinCarrySerializable
{
    int MAX_FUEL = 20;
    int MAX_BREWING_TIME = 400;
    
    @NotNull NonNullList<ItemStack> getItems();
    
    int getBrewTime();
    
    int getFuel();
    
    @NotNull Item getIngredient();
    
    boolean @NotNull [] getLastPotionCount();
    
    void setBrewTime(@Range(from = 0, to = MAX_BREWING_TIME) int brewTime);
    
    void setFuel(@Range(from = 0, to = MAX_FUEL) int fuel);
    
    void setIngredient(@NotNull Item ingredient);
    
    void setLastPotionCount(boolean @NotNull [] lastPotionCount);
}
