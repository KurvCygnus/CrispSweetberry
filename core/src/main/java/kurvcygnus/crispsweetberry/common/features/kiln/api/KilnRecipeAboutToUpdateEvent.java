//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.api;

import net.minecraft.core.NonNullList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * This is a custom event that fires at the end of vanilla recipe collection and conversion, the start of 
 * {@code KilnRecipeManager}'s recipe update, which allows you to modify the content of recipes.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see IKilnRecipeView
 */
public final class KilnRecipeAboutToUpdateEvent extends Event
{
    private final HashMap<Item, NonNullList<IKilnRecipeView>> kilnRecipes;
    
    public KilnRecipeAboutToUpdateEvent(@NotNull HashMap<Item, NonNullList<IKilnRecipeView>> kilnRecipes) { this.kilnRecipes = kilnRecipes; }
    
    public @NotNull HashMap<Item, NonNullList<IKilnRecipeView>> getKilnRecipes() { return kilnRecipes; }
    
    @CheckReturnValue
    public @NotNull Optional<HashMap<Item, NonNullList<IKilnRecipeView>>>
    getAllRecipesWithTag(@NotNull HashMap<Item, NonNullList<IKilnRecipeView>> collection, @NotNull TagKey<Item> tag)
    {
        Objects.requireNonNull(collection, "Param \"collection\" must not be null!");
        Objects.requireNonNull(tag, "Param \"tag\" must not be null!");
        
        final HashMap<Item, NonNullList<IKilnRecipeView>> filtered = new HashMap<>();
        
        collection.forEach((i, l) ->
            {
                if(!i.getDefaultInstance().is(tag))
                    return;
                
                filtered.put(i, l);
            }
        );
        
        if(filtered.isEmpty())
            return Optional.empty();
        
        return Optional.of(filtered);
    }
    
    @SafeVarargs @CheckReturnValue public final @NotNull Optional<HashMap<Item, NonNullList<IKilnRecipeView>>>
    getAllRecipesWithTag(@NotNull HashMap<Item, NonNullList<IKilnRecipeView>> collection, @NotNull TagKey<Item> @NotNull ... tags)
    {
        Objects.requireNonNull(collection, "Param \"collection\" must not be null!");
        Objects.requireNonNull(tags, "Param \"tag\" must not be null!");
        
        final HashMap<Item, NonNullList<IKilnRecipeView>> filtered = new HashMap<>();
        
        collection.forEach((i, l) ->
            {
                for(int index = 0, tagsLength = tags.length; index < tagsLength; index++)
                {
                    final TagKey<Item> tag = tags[index];
                    Objects.requireNonNull(tag, "Param \"tag\" must not be null! Null element starts at index %d.".formatted(index));
                    if(!i.getDefaultInstance().is(tag))
                        return;
                }
                
                filtered.put(i, l);
            }
        );
        
        if(filtered.isEmpty())
            return Optional.empty();
        
        return Optional.of(filtered);
    }
}
