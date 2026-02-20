//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipeManager;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;

/**
 * The event handler responsible for <b>populating and refreshing the Kiln's internal recipe cache.</b>
 * <br>
 * Instead of querying the <u>{@link RecipeManager}</u> every tick (which is computationally expensive),
 * this class flattens and transforms valid Smelting and Smoking recipes into a optimized <u>{@link HashMap}</u>.
 * <br>
 * @author Kurv Cygnus
 * @see KilnBlockEntity Main Usage
 * @see KilnRecipe Recipe Implementation
 * @see KilnRecipeManager Kiln's own Recipe Manager
 * @since 1.0 Release
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class KilnRecipeCacheEvent
{
    private static final MarkLogger LOGGER = MarkLogger.marklessLogger(LogUtils.getLogger());
    
    /**
     * Triggers the initial cache population when the server finishes its startup sequence.
     * @implNote We pick this event as the <u>{@link RecipeManager}</u> has fully stitched together all JSON recipes from
     * mods and datapacks. This ensures our cache doesn't miss entries that are registered
     * late in the loading cycle.
     */
    @SubscribeEvent
    static void getKilnRecipes(final @NotNull ServerStartedEvent event) { collectRecipes(event.getServer().getRecipeManager(), event.getServer().registryAccess()); }
    
    /**
     * Registers a reload listener to handle dynamic changes to recipes during gameplay.
     * @implNote <h4><b>This alternative event exists because recipes in Minecraft are not static.</b></h4>
     * Players or server admins can trigger {@code /reload}
     * to update datapacks. Without this listener, the Kiln would continue using stale data
     * from the initial server start, leading to "ghost recipes" or crashes when the underlying
     * recipe objects no longer exist in the manager.
     */
    @SubscribeEvent static void onAddReloadListener(final @NotNull AddReloadListenerEvent event)
    {
        event.addListener((
            preparationBarrier,
            resourceManager,
            profilerIn,
            profilerOut,
            backgroundExecutor,
            gameExecutor
            ) ->
            preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(() ->
                collectRecipes(event.getServerResources().getRecipeManager(), event.getRegistryAccess()),
                gameExecutor
            )
        );
    }
    
    /**
     * The core logic for filtering and transforming vanilla cooking recipes into Kiln-compatible data.
     */
    private static void collectRecipes(@NotNull RecipeManager manager, @NotNull RegistryAccess registryAccess)
    {
        final StopWatch time = new StopWatch();
        time.start();
        
        try(MarkLogger.MarkerHandle handle = LOGGER.pushMarker("CACHE_START"))
        {
            LOGGER.info("Getting Kiln Recipes...");
            
            final HashMap<Item, NonNullList<SmokingRecipe>> tempSmokingRecipes = new HashMap<>();
            final HashMap<Item, NonNullList<BlastingRecipe>> tempBlastingRecipes = new HashMap<>();
            final HashMap<Item, NonNullList<SmeltingRecipe>> tempKilnRecipes = new HashMap<>();
            
            handle.changeMarker("SMOKER_PHASE");
            LOGGER.info("Collecting Smoker Recipes...");
            streamRecipes(tempSmokingRecipes, manager, RecipeType.SMOKING);
            configDebug("Collection ended, {} entries in total, content: {}", tempSmokingRecipes.size(), tempSmokingRecipes);
            
            handle.changeMarker("BLAST_PHASE");
            LOGGER.info("Collecting Blast Furnace(Banned) Recipes...");
            streamRecipes(tempBlastingRecipes, manager, RecipeType.BLASTING);
            configDebug("Collection ended, {} entries in total, content: {}", tempBlastingRecipes.size(), tempBlastingRecipes);
            
            
            handle.changeMarker("INITIAL_FILTER");
            LOGGER.info("Starting filtering kiln recipes...");
            manager.getAllRecipesFor(RecipeType.SMELTING).stream().map(RecipeHolder::value).forEach(
                recipe ->
                {
                    for(final Ingredient ingredient: recipe.getIngredients())
                    {
                        for(final ItemStack stack: ingredient.getItems())
                        {
                            final Item item = stack.getItem();
                            
                            configDebug("Accepted item \"{}\" as smelting recipe", stack.getDisplayName());
                            
                            tempKilnRecipes.computeIfAbsent(item, i -> NonNullList.create()).
                                add(recipe);
                        }
                    }
                }
            );
            
            handle.changeMarker("FINAL_FILTER");
            LOGGER.info("Finished filtering kiln recipes. Start conversion...");
            
            final HashMap<Item, NonNullList<KilnRecipe>> completedKilnRecipesCacheList = new HashMap<>();
            
            filterRecipes(completedKilnRecipesCacheList, tempKilnRecipes, registryAccess);
            //* Smoker, and blaster recipes are intentionally applied after smelting recipes to override them for the same input item.
            //! This is NOT redundant. Some mod will add smoker-only / blaster-only recipes.
            filterRecipes(completedKilnRecipesCacheList, tempSmokingRecipes, registryAccess);
            filterRecipes(completedKilnRecipesCacheList, tempBlastingRecipes, registryAccess);
            
            handle.changeMarker("EVENT_FINISHED");
            LOGGER.info("Kiln recipe caching finished in {} ms!", time.getTime());
            
            KilnRecipeManager.INSTANCE.updateRecipes(completedKilnRecipesCacheList);
        }
    }
    
    /**
     * An encapsulated method for <b>getting recipes for smoker and blast furnace<b>.
     */
    private static <R extends AbstractCookingRecipe> void streamRecipes
    (@NotNull HashMap<Item, NonNullList<R>> targetMap, @NotNull RecipeManager manager, @NotNull RecipeType<R> recipeType)
    {
        try(MarkLogger.MarkerHandle ignored = LOGGER.pushMarker("RECIPE_STREAM"))
        {
            manager.getAllRecipesFor(recipeType).stream().map(RecipeHolder::value).forEach(
                recipe ->
                {
                    for(final Ingredient ingredient: recipe.getIngredients())
                    {
                        for(final ItemStack stack: ingredient.getItems())
                        {
                            final Item item = stack.getItem();
                            
                            targetMap.computeIfAbsent(item, i -> NonNullList.create()).
                                add(recipe);
                        }
                    }
                    
                    configDebug("Completed a round of recipe collection, Ingredients: {}, current stream recipe type: {}",
                        recipe.getIngredients(), recipeType
                    );
                }
            );
        }
    }
    
    /**
     * An encapsulated method for <b>filtering, and converting recipes to <u>{@link KilnRecipe}</u></b>.
     */
    private static <R extends AbstractCookingRecipe> void filterRecipes
    (@NotNull HashMap<Item, NonNullList<KilnRecipe>> targetMap, @NotNull HashMap<Item, NonNullList<R>> convertMap, @NotNull RegistryAccess access)
    {
        convertMap.forEach((item, list) ->
            {
                if(!list.isEmpty())
                {
                    final String type;
                    switch(RecipeSourceType.getSourceType(list.getFirst()))
                    {
                        case SMOKING -> type = "Smoking";
                        case BLASTING -> type = "Blasting";
                        default -> type = "Skip";
                    }
                    
                    if(!type.equals("Skip") && targetMap.containsKey(item))
                    {
                        configDebug("Item {} found in cache, clearing old Smelting recipes to override with {}.", item, type);
                        targetMap.get(item).clear();
                    }
                }
                
                for(final R recipe: list)
                {
                    for(final Ingredient ingredient: recipe.getIngredients())
                    {
                        final KilnRecipe convertedRecipe = new KilnRecipe(
                            ingredient,
                            recipe.getResultItem(access),
                            calculateProcessFactor(
                                recipe.getCookingTime(),
                                RecipeSourceType.getSourceType(recipe)
                            ),
                            recipe.getExperience(),
                            recipe instanceof BlastingRecipe
                        );
                        
                        targetMap.computeIfAbsent(item, i -> NonNullList.create()).
                            add(convertedRecipe);
                    }
                }
            }
        );
    }
    
    private static double calculateProcessFactor(int cookingTime, @NotNull RecipeSourceType recipeSourceType)
    {
        //* Both Smoking and Smelting Recipe are hard-coded in vanilla Minecraft. 
        final int standardTime;
        final double penaltyRate;
        
        switch(recipeSourceType)
        {
            case SMOKING ->
            {
                standardTime = MiscConstants.ADVANCED_HEATING_CONTAINER_TIME;
                penaltyRate = 1.25D;
            }
            case BLASTING ->
            {
                standardTime = MiscConstants.ADVANCED_HEATING_CONTAINER_TIME;
                penaltyRate = 2.5D;
            }
            default ->
            {
                standardTime = MiscConstants.FURNACE_SMELTING_TIME;
                penaltyRate = 1D;
            }
        }
        
        //!                               Maybe some mod will introduce short cooking time recipes into the game,
        //!                             ↓ so we should make sure at least processFactor is always bigger than 0D.
        final double factor = Math.max(0.05D, (double) cookingTime / standardTime) * penaltyRate;
        
        configDebug("Type: {}, Time: {}, Factor: {}",
            recipeSourceType.name().toLowerCase(), cookingTime, factor
        );
        
        return factor;
    }
    
    private static void configDebug(@NotNull String message, Object @NotNull ... args) { LOGGER.when(CrispConfig.KILN_EVENT_DEBUG.get()).debug(message, args); }
    
    private enum RecipeSourceType
    {
        FURNACE,
        SMOKING,
        BLASTING;
        
        static <T> @NotNull RecipeSourceType getSourceType(@NotNull T recipeSource)
        {
            Objects.requireNonNull(recipeSource, "Param \"recipeSource\" must not be null!");
            
            return switch(recipeSource)
            {
                case SmokingRecipe ignored -> SMOKING;
                case BlastingRecipe ignored -> BLASTING;
                default -> FURNACE;
            };
        }
    }
}
