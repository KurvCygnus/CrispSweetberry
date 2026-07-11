//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln;

import com.google.errorprone.annotations.DoNotCall;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipeManager;
import kurvcygnus.crispsweetberry.lib.base.stream.Invoker;
import kurvcygnus.crispsweetberry.lib.base.trait.IMappedEnum;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
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
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private KilnRecipeCacheEvent() { throw new IllegalAccessError("Class \"KilnRecipeCacheEvent\" is not meant to be instantized!"); }
    
    private static final IMarkLogger LOGGER = IMarkLogger.configuredLogger(
        IMarkLogger.allowWhen(Level.DEBUG, IMarkLogger.ConditionSituation.EQUAL, CrispConfig.KILN_EVENT_DEBUG)
    );
    
    /**
     * Triggers the initial cache population when the server finishes its startup sequence.
     * @implNote We pick this event as the <u>{@link RecipeManager}</u> has fully stitched together all JSON recipes from
     * mods and datapacks. This ensures our cache doesn't miss entries that are registered
     * late in the loading cycle.
     */
    @SubscribeEvent @DoNotCall static void getKilnRecipes(final @NotNull ServerStartedEvent event)
        { collectRecipes(event.getServer().getRecipeManager(), event.getServer().registryAccess()); }
    
    /**
     * Registers a reload listener to handle dynamic changes to recipes during gameplay.
     * @implNote <h4><b>This alternative event exists because recipes in Minecraft are not static.</b></h4>
     * Players or server admins can trigger {@code /reload}
     * to update datapacks. Without this listener, the Kiln would continue using stale data
     * from the initial server start, leading to "ghost recipes" or crashes when the underlying
     * recipe objects no longer exist in the manager.
     */
    @SubscribeEvent @DoNotCall static void onAddReloadListener(final @NotNull AddReloadListenerEvent event)
    {
        event.addListener((
            preparationBarrier,
            resourceManager,
            profilerIn,
            profilerOut,
            backgroundExecutor,
            gameExecutor
            ) ->
            preparationBarrier.wait(Unit.INSTANCE).thenRunAsync(
                () -> collectRecipes(event.getServerResources().getRecipeManager(), event.getRegistryAccess()),
                gameExecutor
            )
        );
    }
    
    /**
     * The core logic for filtering and transforming vanilla cooking recipes into Kiln-compatible data.
     */
    private static void collectRecipes(@NotNull RecipeManager manager, @NotNull RegistryAccess registryAccess)
    {
        final var time = new StopWatch();
        time.start();
        
        try(final var handle = LOGGER.pushMarker("CACHE_START"))
        {
            LOGGER.info("Getting Kiln Recipes...");
            
            final var tempSmokingRecipes = new HashMap<Item, List<SmokingRecipe>>();
            final var tempBlastingRecipes = new HashMap<Item, List<BlastingRecipe>>();
            final var tempKilnRecipes = new HashMap<Item, List<SmeltingRecipe>>();
            
            handle.changeMarker("SMOKER_PHASE");
            LOGGER.info("Collecting Smoker Recipes...");
            streamRecipes(tempSmokingRecipes, manager, RecipeType.SMOKING);
            LOGGER.debug("Collection ended, {} entries in total, content: {}", tempSmokingRecipes.size(), tempSmokingRecipes);
            
            handle.changeMarker("BLAST_PHASE");
            LOGGER.info("Collecting Blast Furnace(Banned) Recipes...");
            streamRecipes(tempBlastingRecipes, manager, RecipeType.BLASTING);
            LOGGER.debug("Collection ended, {} entries in total, content: {}", tempBlastingRecipes.size(), tempBlastingRecipes);
            
            
            handle.changeMarker("INITIAL_FILTER");
            LOGGER.info("Starting filtering kiln recipes...");
            Invoker.unit(manager.getAllRecipesFor(RecipeType.SMELTING)).map(RecipeHolder::value).invoke(
                recipe ->
                {
                    for(final var ingredient: recipe.getIngredients())
                    {
                        for(final var stack: ingredient.getItems())
                        {
                            final var item = stack.getItem();
                            
                            LOGGER.debug("Accepted item \"{}\" as smelting recipe", stack.getDisplayName().getString());
                            
                            tempKilnRecipes.computeIfAbsent(item, i -> NonNullList.create()).
                                add(recipe);
                        }
                    }
                }
            );
            
            handle.changeMarker("FINAL_FILTER");
            LOGGER.info("Finished filtering kiln recipes. Start conversion...");
            
            final var completedKilnRecipesCacheList = new HashMap<Item, List<KilnRecipe>>();
            
            filterRecipes(completedKilnRecipesCacheList, tempKilnRecipes, registryAccess);
            //* Smoker, and blaster recipes are intentionally applied after smelting recipes to override them for the same input item.
            //! This is NOT redundant. Some mod will add smoker-only / blaster-only recipes.
            filterRecipes(completedKilnRecipesCacheList, tempSmokingRecipes, registryAccess);
            filterRecipes(completedKilnRecipesCacheList, tempBlastingRecipes, registryAccess);
            
            handle.changeMarker("EVENT_FINISHED");
            LOGGER.info("Kiln recipe caching finished in {} ms!", time.getTime());
            
            KilnRecipeManager.INST.updateRecipes(completedKilnRecipesCacheList);
        }
    }
    
    /**
     * An encapsulated method for <b>getting recipes for smoker and blast furnace<b>.
     */
    private static <R extends AbstractCookingRecipe> void streamRecipes(
        @NotNull Map<Item, List<R>> targetMap,
        @NotNull RecipeManager manager,
        @NotNull RecipeType<R> recipeType
    )
    {
        try(final var ignored = LOGGER.pushMarker("RECIPE_STREAM"))
        {
            Invoker.unit(manager.getAllRecipesFor(recipeType)).map(RecipeHolder::value).invoke(
                recipe ->
                {
                    final var ingredients = recipe.getIngredients();
                    
                    Invoker.unit(ingredients).
                        destructArrayMap(Ingredient::getItems).
                        map(ItemStack::getItem).
                        invoke(item -> targetMap.computeIfAbsent(item, i -> NonNullList.create()).add(recipe));
                    
                    LOGGER.debug(
                        "Completed a round of recipe collection, Ingredients: {}, current stream recipe type: {}",
                        ingredients,
                        recipeType
                    );
                }
            );
        }
    }
    
    /**
     * An encapsulated method for <b>filtering, and converting recipes to <u>{@link KilnRecipe}</u></b>.
     */
    private static <R extends AbstractCookingRecipe> void filterRecipes(
        @NotNull Map<Item, List<KilnRecipe>> targetMap,
        @NotNull Map<Item, List<R>> convertMap,
        @NotNull RegistryAccess access
    )
    {
        convertMap.forEach(
            (item, list) ->
            {
                if(!list.isEmpty())
                {
                    final var type = RecipeSourceType.getSourceType(list.getFirst());
                    
                    if(!type.equals(RecipeSourceType.FURNACE) && targetMap.containsKey(item))
                    {
                        LOGGER.debug("Item {} found in cache, clearing old Smelting recipes to override with {}.", item, type.name());
                        targetMap.get(item).clear();
                    }
                }
                
                for(final R recipe: list)
                {
                    for(final var ingredient: recipe.getIngredients())
                    {
                        final var convertedRecipe = new KilnRecipe(
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
                standardTime = KilnConstants.ADVANCED_HEATING_CONTAINER_TIME;
                penaltyRate = 1.25;
            }
            case BLASTING ->
            {
                standardTime = KilnConstants.ADVANCED_HEATING_CONTAINER_TIME;
                penaltyRate = 2.5;
            }
            default ->
            {
                standardTime = KilnConstants.FURNACE_SMELTING_TIME;
                penaltyRate = 1.;
            }
        }
        
        //!                               Maybe some mod will introduce short cooking time recipes into the game,
        //!                             ↓ so we should make sure at least processFactor is always bigger than "0." .
        final double factor = Math.max(.05, (double) cookingTime / standardTime) * penaltyRate;
        
        LOGGER.debug(
            "Type: {}, Time: {}, Factor: {}",
            recipeSourceType.name().toLowerCase(),
            cookingTime,
            factor
        );
        
        return factor;
    }
    
    private enum RecipeSourceType implements IMappedEnum<Class<?>, RecipeSourceType>
    {
        FURNACE(SmeltingRecipe.class),
        SMOKING(SmokingRecipe.class),
        BLASTING(BlastingRecipe.class);
        
        private static final Map<Class<?>, RecipeSourceType> LOOKUP = IMappedEnum.constructLookup(RecipeSourceType.class);
        
        private final Class<?> boundClass;
        
        RecipeSourceType(@NotNull Class<?> boundClass) { this.boundClass = boundClass; }
        
        static @NotNull RecipeSourceType getSourceType(@NotNull Object recipeSource)
        {
            Objects.requireNonNull(recipeSource, "Param \"recipeSource\" must not be null!");
            return LOOKUP.get(recipeSource.getClass());
        }
        
        @Override public @NotNull Class<?> getKey() { return this.boundClass; }
    }
}
