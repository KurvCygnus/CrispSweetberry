//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import kurvcygnus.crispsweetberry.common.features.kiln.api.IKilnRecipeView;
import kurvcygnus.crispsweetberry.common.features.kiln.api.KilnRecipeAboutToUpdateEvent;
import kurvcygnus.crispsweetberry.common.features.kiln.integration.KilnJEICompat;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The holder of <u>{@link KilnRecipe}</u>.
 *
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.kiln.KilnRecipeCacheEvent Recipe Collect Event
 * @since 1.0 Release
 */
public enum KilnRecipeManager
{
    /**
     * Recipe manager should always exist in the whole lifecycle of game, so using singleton enum is a good choice.
     */
    INST;
    
    private Map<Item, NonNullList<KilnRecipe>> recipes;
    private Map<Item, NonNullList<KilnRecipe>> bannedRecipes;
    private Map<Item, NonNullList<KilnRecipe>> normalRecipes;
    private List<KilnRecipe> recipesList;
    
    public void updateRecipes(@NotNull HashMap<Item, NonNullList<KilnRecipe>> newRecipeMap)
    {
        final HashMap<Item, NonNullList<IKilnRecipeView>> postMap = new HashMap<>();
        
        newRecipeMap.forEach(
            (i, l) ->
            {
                final NonNullList<IKilnRecipeView> views = NonNullList.create();
                views.addAll(l);
                postMap.put(i, views);
            }
        );
        
        final KilnRecipeAboutToUpdateEvent event = new KilnRecipeAboutToUpdateEvent(postMap);
        
        NeoForge.EVENT_BUS.post(event);
        
        newRecipeMap.clear();
        
        event.getKilnRecipes().forEach((i, l) ->
            {
                final NonNullList<KilnRecipe> rebuiltInstances = NonNullList.create();
                
                l.forEach(v ->
                    {
                        final KilnRecipe instance = new KilnRecipe(
                            v.ingredient(),
                            v.result(),
                            v.processFactor(),
                            v.experience(),
                            v.isBanned()
                        );
                        
                        rebuiltInstances.add(instance);
                    }
                );
                
                newRecipeMap.put(i, rebuiltInstances);
            }
        );
        
        newRecipeMap.values().forEach(l -> l.removeIf(Objects::isNull));
        
        //? TODO: Better filter.
        final var totalRecipes = newRecipeMap.entrySet().stream().collect(
            Collectors.partitioningBy(
            entry -> entry.getValue().stream().anyMatch(KilnRecipe::isBanned),
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
            )
        );
        
        recipes = Collections.unmodifiableMap(newRecipeMap);
        bannedRecipes = Collections.unmodifiableMap(totalRecipes.get(true));
        normalRecipes = Collections.unmodifiableMap(totalRecipes.get(false));
        
        recipesList = recipes.values().stream().
            flatMap(Collection::stream).
            toList();
        
        KilnJEICompat.INST.pushRecipesToJEI();
    }
    
    public @NotNull Map<Item, NonNullList<KilnRecipe>> getRecipes() { return recipes; }
    
    public @NotNull Map<Item, NonNullList<KilnRecipe>> getNormalRecipes() { return normalRecipes; }
    
    public @NotNull Map<Item, NonNullList<KilnRecipe>> getBannedRecipes() { return bannedRecipes; }
    
    public @NotNull List<KilnRecipe> getRecipesList() { return recipesList; }
}
