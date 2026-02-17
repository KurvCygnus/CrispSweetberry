//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import kurvcygnus.crispsweetberry.common.features.kiln.integration.KilnJEICompat;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.BlastingRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * The holder of <u>{@link KilnRecipe}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.kiln.KilnRecipeCacheEvent Recipe Collect Event
 */
public enum KilnRecipeManager
{
    /**
     * Recipe manager should always exist in the whole lifecycle of game, so using singleton enum is a good choice.
     */
    INSTANCE;
    
    private Map<Item, NonNullList<KilnRecipe>> recipes;
    private Map<Item, NonNullList<BlastingRecipe>> bannedRecipes;
    private List<KilnRecipe> recipesList;
    private List<BlastingRecipe> bannedRecipesList;
    
    public void updateRecipes(@NotNull HashMap<Item, NonNullList<KilnRecipe>> newRecipeMap, @NotNull HashMap<Item, NonNullList<BlastingRecipe>> newBannedRecipeMap)
    {
        recipes.clear();
        recipesList.clear();
        bannedRecipes.clear();
        bannedRecipesList.clear();
        
        recipes = Collections.unmodifiableMap(newRecipeMap);
        bannedRecipes = Collections.unmodifiableMap(newBannedRecipeMap);
        
        recipesList = recipes.values().stream().
            flatMap(Collection::stream).
            toList();
        
        bannedRecipesList = bannedRecipes.values().stream().
            flatMap(Collection::stream).
            toList();
        
        KilnJEICompat.INSTANCE.pushRecipesToJEI();
    }
    
    public @NotNull Map<Item, NonNullList<KilnRecipe>> getRecipes() { return recipes; }
    
    public @NotNull Map<Item, NonNullList<BlastingRecipe>> getBannedRecipes() { return bannedRecipes; }
    
    public @NotNull List<KilnRecipe> getRecipesList() { return recipesList; }
    
    public @NotNull List<BlastingRecipe> getBannedRecipesList() { return bannedRecipesList; }
}
