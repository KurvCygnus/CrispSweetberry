//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.api;

import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * This is a custom event that fires at the end of vanilla recipe collection and conversion, the start of 
 * {@code KilnRecipeManager}'s recipe update, which allows you to modify the content of recipes.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see IKilnRecipeView
 */
public final class KilnRecipeAboutToUpdateEvent extends Event
{
    private final Map<Item, List<IKilnRecipeView>> kilnRecipes;
    
    public KilnRecipeAboutToUpdateEvent(@NotNull Map<Item, List<IKilnRecipeView>> kilnRecipes) { this.kilnRecipes = kilnRecipes; }
    
    public @NotNull Map<Item, List<IKilnRecipeView>> getKilnRecipes() { return kilnRecipes; }
    
    @CheckReturnValue public @NotNull Optional<Map<Item, List<IKilnRecipeView>>> getAllRecipesWithTag(@NotNull TagKey<Item> tag)
    {
        Objects.requireNonNull(tag, "Param \"tag\" must not be null!");
        
        final var filtered = new HashMap<Item, List<IKilnRecipeView>>();
        
        getKilnRecipes().forEach(
            (item, list) ->
            {
                if(!item.getDefaultInstance().is(tag))
                    return;
                
                filtered.put(item, list);
            }
        );
        
        if(filtered.isEmpty())
            return Optional.empty();
        
        return Optional.of(filtered);
    }
    
    @SafeVarargs @CheckReturnValue public final @NotNull Optional<Map<Item, List<IKilnRecipeView>>> getAllRecipesWithTag(
        @NotNull TagKey<Item> @NotNull ... tags
    )
    {
        Objects.requireNonNull(tags, "Param \"tag\" must not be null!");
        
        final var filtered = new HashMap<Item, List<IKilnRecipeView>>();
        
        getKilnRecipes().forEach(
            (item, list) ->
            {
                for(int index = 0, tagsLength = tags.length; index < tagsLength; index++)
                {
                    final var tag = tags[index];
                    Objects.requireNonNull(tag, TextUtils.format("Param \"tag\" must not be null! Null element starts at index {}.", index));
                    if(!item.getDefaultInstance().is(tag))
                        return;
                }
                
                filtered.put(item, list);
            }
        );
        
        if(filtered.isEmpty())
            return Optional.empty();
        
        return Optional.of(filtered);
    }
}
