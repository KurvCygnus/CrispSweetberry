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
import kurvcygnus.crispsweetberry.lib.base.stream.Invoker;
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
    
    private Map<Item, List<KilnRecipe>> recipes;
    private Map<Item, List<KilnRecipe>> bannedRecipes;
    private Map<Item, List<KilnRecipe>> normalRecipes;
    private List<KilnRecipe> recipesList;
    
    public void updateRecipes(@NotNull Map<Item, List<KilnRecipe>> newRecipeMap)
    {
        final var postMap = new HashMap<Item, List<IKilnRecipeView>>();
        
        newRecipeMap.forEach(
            (item, list) ->
            {
                final var views = NonNullList.<IKilnRecipeView>create();
                views.addAll(list);
                postMap.put(item, views);
            }
        );
        
        final var event = new KilnRecipeAboutToUpdateEvent(postMap);
        
        NeoForge.EVENT_BUS.post(event);
        
        newRecipeMap.clear();
        
        event.getKilnRecipes().forEach(
            (item, list) ->
            {
                final var rebuiltInstances = NonNullList.<KilnRecipe>create();
                
                //! The only implementer of [[IKilnRecipeView]] is [[KilnRecipe]], and [[KilnRecipe]] is immutable,
                //! so casting is better than stupidly rebuilding instances.
                Invoker.unit(list).
                    //! [[IKilnRecipeView]] can be implemented by others, so we have to check whether the instance is legal.
                    filter(KilnRecipe.class::isInstance).
                    map(KilnRecipe.class::cast).
                    invoke(rebuiltInstances::add);
                
                newRecipeMap.put(item, rebuiltInstances);
            }
        );
        
        newRecipeMap.values().forEach(l -> l.removeIf(Objects::isNull));
        
        //? TODO: Better filter.
        final var totalRecipes = Invoker.unit(newRecipeMap.entrySet()).collect(
            Collectors.partitioningBy(
            entry ->
                entry.getValue().stream().anyMatch(KilnRecipe::isBanned),
                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
            )
        );
        
        recipes = Collections.unmodifiableMap(newRecipeMap);
        bannedRecipes = Collections.unmodifiableMap(totalRecipes.get(true));
        normalRecipes = Collections.unmodifiableMap(totalRecipes.get(false));
        
        recipesList = Invoker.unit(recipes.values()).
            destructIterableMap(list -> list).
            toList();
        
        KilnJEICompat.INST.pushRecipesToJEI();
    }
    
    public @NotNull Optional<List<KilnRecipe>> getRecipes(@NotNull Item item) { return Optional.ofNullable(recipes.get(item)); }
    
    public @NotNull Optional<List<KilnRecipe>> getNormalRecipes(@NotNull Item item) { return Optional.ofNullable(normalRecipes.get(item)); }
    
    public @NotNull Optional<List<KilnRecipe>> getBannedRecipes(@NotNull Item item) { return Optional.ofNullable(bannedRecipes.get(item)); }
    
    public @NotNull List<KilnRecipe> getRecipesList() { return recipesList; }
}
