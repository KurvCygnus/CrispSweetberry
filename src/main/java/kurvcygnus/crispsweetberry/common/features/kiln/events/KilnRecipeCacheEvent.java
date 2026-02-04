package kurvcygnus.crispsweetberry.common.features.kiln.events;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
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

/**
 * The event handler responsible for <b>populating and refreshing the Kiln's internal recipe cache.</b>
 * <br>
 * Instead of querying the <u>{@link RecipeManager}</u> every tick (which is computationally expensive),
 * this class flattens and transforms valid Smelting and Smoking recipes into a optimized <u>{@link HashMap}</u>.
 * <br>
 * @author Kurv Cygnus
 * @see KilnBlockEntity Main Usage
 * @see KilnRecipe Recipe Implementation
 * @since 1.0 Release
 */
@EventBusSubscriber(modid = CrispSweetberry.ID)
public final class KilnRecipeCacheEvent
{
    private static final HashMap<Item, NonNullList<KilnRecipe>> KILN_CACHED_RECIPES = new HashMap<>();
    private static final HashMap<Item, NonNullList<BlastingRecipe>> BANNED_RECIPES = new HashMap<>();
    
    private static final MarkLogger LOGGER = MarkLogger.getLogger(LogUtils.getLogger());
    
    /**
     * Triggers the initial cache population when the server finishes its startup sequence.
     * @implNote We pick this event as the <u>{@link RecipeManager}</u> has fully stitched together all JSON recipes from
     * mods and datapacks. This ensures our cache doesn't miss entries that are registered
     * late in the loading cycle.
     * <br>
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
    @SubscribeEvent
    static void onAddReloadListener(final @NotNull AddReloadListenerEvent event)
    {
        event.addListener((
            preparationBarrier,
            resourceManager,
            profilerIn,
            profilerOut,
            backgroundExecutor,
            gameExecutor
            ) ->
            preparationBarrier.wait(net.minecraft.util.Unit.INSTANCE).thenRunAsync(() ->
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
            
            KILN_CACHED_RECIPES.clear();
            BANNED_RECIPES.clear();
            
            final HashMap<Item, NonNullList<SmokingRecipe>> tempSmokingRecipes = new HashMap<>();
            final HashMap<Item, NonNullList<SmeltingRecipe>> tempKilnRecipes = new HashMap<>();
            
            handle.changeMarker("SMOKER_PHASE");
            LOGGER.info("Collecting Smoker Recipes...");
            streamRecipes(tempSmokingRecipes, manager, RecipeType.SMOKING);
            configDebug("Collection ended, {} entries in total, content: {}", tempSmokingRecipes.size(), tempSmokingRecipes);
            
            handle.changeMarker("BLAST_PHASE");
            LOGGER.info("Collecting Blast Furnace(Banned) Recipes...");
            streamRecipes(BANNED_RECIPES, manager, RecipeType.BLASTING);
            configDebug("Collection ended, {} entries in total, content: {}", BANNED_RECIPES.size(), BANNED_RECIPES);
            
            
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
                            
                            if(BANNED_RECIPES.containsKey(item))
                            {
                                configDebug("Filtered item \"{}\", reason: Belongs to Banned Recipes", stack.getDisplayName());
                                continue;
                            }
                            
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
            //* Smoker recipes are intentionally applied after smelting recipes to override them for the same input item.
            //! This is NOT redundant. Some mod will add smoker-only recipes.
            filterRecipes(completedKilnRecipesCacheList, tempSmokingRecipes, registryAccess);
            
            handle.changeMarker("RECIPE_CACHE");
            configDebug("Conversion finished. Continue to put recipes into the map...");
            
            KILN_CACHED_RECIPES.putAll(completedKilnRecipesCacheList);
            
            handle.changeMarker("EVENT_FINISHED");
            LOGGER.info("Kiln recipe caching finished in {} ms!", time.getTime());
        }
    }
    
    /**
     * An encapsulated method for <b>getting recipes for smoker and blast furnace<b>.
     */
    private static <R extends AbstractCookingRecipe> void streamRecipes
    (@NotNull HashMap<Item, NonNullList<R>> targetMap, @NotNull RecipeManager manager, @NotNull RecipeType<R> recipeType)
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
                
                try(MarkLogger.MarkerHandle ignored = LOGGER.pushMarker("RECIPE_STREAM"))
                {
                    configDebug("Completed a round of recipe collection, Ingredients: {}, current stream recipe type: {}",
                        recipe.getIngredients(), recipeType
                    );
                }
            }
        );
    }
    
    /**
     * An encapsulated method for <b>filtering, and converting recipes to <u>{@link KilnRecipe}</u></b>.
     */
    private static <R extends AbstractCookingRecipe> void filterRecipes
    (@NotNull HashMap<Item, NonNullList<KilnRecipe>> targetMap, @NotNull HashMap<Item, NonNullList<R>> convertMap, @NotNull RegistryAccess access)
    {
        convertMap.forEach((item, list) ->
            {
                if(!list.isEmpty() && list.getFirst() instanceof SmokingRecipe)
                {
                    if(targetMap.containsKey(item))
                    {
                        try(MarkLogger.MarkerHandle handle = LOGGER.pushMarker("FINAL_FILTER"))
                            { configDebug("Item {} found in cache, clearing old Smelting recipes to override with Smoking.", item); }
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
                            calculateProcessFactor(recipe.getCookingTime(), recipe instanceof SmokingRecipe),
                            recipe.getExperience()
                        );
                        
                        targetMap.computeIfAbsent(item, i -> NonNullList.create()).
                            add(convertedRecipe);
                    }
                }
            }
        );
    }
    
    private static double calculateProcessFactor(int cookingTime, boolean isSmokingRecipe)
    {
        //* Both Smoking and Smelting Recipe are hard-coded in vanilla Minecraft. 
        final int standardTime = isSmokingRecipe ? MiscConstants.SMOKER_SMOKING_TIME : MiscConstants.FURNACE_SMELTING_TIME;
        
        //!                               Maybe some mod will introduce short cooking time recipes into the game,
        //!                             ↓ so we should make sure at least processFactor is always bigger than 0D.
        final double factor = Math.max(0.05D, (double) cookingTime / standardTime) * (isSmokingRecipe ? 1.25D : 1D);
        
        try(MarkLogger.MarkerHandle ignored = LOGGER.pushMarker("FINAL_FILTER"))
        {
            configDebug("Type: {}, Time: {}, Factor: {}",
                isSmokingRecipe ? "Smoking" : "Smelting", cookingTime, factor
            );
        }
        
        return factor;
    }
    
    public static @NotNull HashMap<Item, NonNullList<KilnRecipe>> getKilnCachedRecipes() { return KILN_CACHED_RECIPES; }
    
    public static @NotNull HashMap<Item, NonNullList<BlastingRecipe>> getBannedRecipes() { return BANNED_RECIPES; }
    
    private static void configDebug(@NotNull String message, Object @NotNull ... args) { LOGGER.debugIf(CrispConfig.KILN_EVENT_DEBUG.get(), message, args); }
}
