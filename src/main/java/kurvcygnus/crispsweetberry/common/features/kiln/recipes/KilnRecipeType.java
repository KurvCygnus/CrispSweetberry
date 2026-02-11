//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import net.minecraft.world.item.crafting.RecipeType;

public final class KilnRecipeType implements RecipeType<KilnRecipe>
{
    public static final KilnRecipeType INSTANCE = new KilnRecipeType();
    public static final String ID = "kiln";
}
