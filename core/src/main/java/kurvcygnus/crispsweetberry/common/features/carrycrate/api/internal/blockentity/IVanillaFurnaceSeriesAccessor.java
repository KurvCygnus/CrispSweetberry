//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface IVanillaFurnaceSeriesAccessor extends ISimpleMixinCarrySerializable
{
    @NotNull RecipeType<? extends AbstractCookingRecipe> getRecipeType();
    
    RecipeManager.@NotNull CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> getQuickCheck();
    
    int getLitTime();
    
    int getCookingProgress();
    
    @NotNull NonNullList<ItemStack> callGetItems();
    
    boolean callIsLit();
    
    int callGetBurnDuration(@NotNull ItemStack fuel);
    
    void callSetRecipeUsed(@NotNull RecipeHolder<?> recipeHolder);
    
    void setLitTime(int litTime);
    
    void setCookingProgress(@Range(from = 0, to = 100) int cookingProgress);
}
