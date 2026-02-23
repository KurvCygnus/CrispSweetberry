//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * This is a interface of {@code KilnRecipe}, providing all utilities that can, and should be used by externals.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see KilnRecipeAboutToUpdateEvent Usage
 */
public interface IKilnRecipeView
{
    @NotNull Ingredient ingredient();
    
    @NotNull ItemStack result();
    
    /**
     * Get the {@code processFactor} of a recipe.
     * @apiNote 1D stands as a normal recipe, the bigger it is, the process speed is slower.
     */
    @Range(from = 0, to = (long) Double.MAX_VALUE) double processFactor();
    
    @Range(from = 0, to = (long) Float.MAX_VALUE) float experience();
    
    boolean isBanned();
    
    @NotNull IKilnRecipeView withBanned();
    
    @NotNull IKilnRecipeView unBanned();
}
