//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A simple implementation of <u>{@link RecipeInput}</u>, used to define <u>{@link KilnRecipe}</u>'s detail.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public record KilnRecipeInput(@NotNull ItemStack stack) implements RecipeInput
{
    @Override
    public @NotNull ItemStack getItem(int index) { return stack; }
    
    @Override
    public int size() { return 1; }
}
