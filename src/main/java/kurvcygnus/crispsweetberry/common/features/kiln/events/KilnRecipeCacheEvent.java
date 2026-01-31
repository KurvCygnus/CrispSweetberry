package kurvcygnus.crispsweetberry.common.features.kiln.events;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.utils.misc.CrispLogUtils;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.apache.commons.lang3.time.StopWatch;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;

/**
 * The event which <b>fills the content with kiln recipes.</b>
 *
 * @author Kurv Cygnus
 * @see KilnBlockEntity Usage
 * @since 1.0 Release
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID)
public final class KilnRecipeCacheEvent
{
    private static final HashMap<Item, NonNullList<KilnRecipe>> KILN_CACHED_RECIPES = new HashMap<>();
    private static final HashMap<Item, NonNullList<BlastingRecipe>> BANNED_RECIPES = new HashMap<>();
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * The core of <b>the adaptive recipes for kiln</b>.
     *
     * @param event Event <b>{@code ServerStartedEvent}</b> is actually the event that triggers when the server has fully started and all recipes are loaded.<br>
     *              <b><i>So it's obvious that this is the best timing for getting recipes, as they must have been prepared in that time.</b></i>
     */
    @SubscribeEvent
    static void getKilnRecipes(final @NotNull ServerStartedEvent event)
    {
        StopWatch time = new StopWatch();
        time.start();
        
        LOGGER.info("[CACHE_START] Getting Kiln Recipes...");
        
        KILN_CACHED_RECIPES.clear();
        
        RecipeManager manager = event.getServer().getRecipeManager();
        
        HashMap<Item, NonNullList<SmokingRecipe>> tempSmokingRecipes = new HashMap<>();
        HashMap<Item, NonNullList<SmeltingRecipe>> tempKilnRecipes = new HashMap<>();
        
        LOGGER.info("[SMOKER_PHASE] Collecting Smoker Recipes...");
        streamRecipes(tempSmokingRecipes, manager, RecipeType.SMOKING);
        configDebug("[SMOKER_PHASE] Collection ended, {} entries in total, content: {}", tempSmokingRecipes.size(), tempSmokingRecipes);
        
        LOGGER.info("[BLAST_PHASE] Collecting Blast Furnace(Banned) Recipes...");
        streamRecipes(BANNED_RECIPES, manager, RecipeType.BLASTING);
        configDebug("[BLAST_PHASE] Collection ended, {} entries in total, content: {}", BANNED_RECIPES.size(), BANNED_RECIPES);
        
        LOGGER.info("[INITIAL_FILTER] Starting filtering kiln recipes...");
        for(RecipeHolder<SmeltingRecipe> recipeHolder: manager.getAllRecipesFor(RecipeType.SMELTING))
        {
            SmeltingRecipe recipe = recipeHolder.value();
            
            for(Ingredient ingredient: recipe.getIngredients())
            {
                for(ItemStack stack: ingredient.getItems())
                {
                    Item item = stack.getItem();
                    
                    if(BANNED_RECIPES.containsKey(item))
                    {
                        configDebug("[INITIAL_FILTER] Filtered item \"{}\", reason: Belongs to Banned Recipes", stack.getDisplayName());
                        continue;
                    }
                    
                    configDebug("[INITIAL_FILTER] Accepted item \"{}\" as smelting recipe", stack.getDisplayName());
                    
                    tempKilnRecipes.computeIfAbsent(item, i -> NonNullList.create()).
                        add(recipe);
                }
            }
        }
        LOGGER.info("[FINAL_FILTER] Finished filtering kiln recipes. Start conversion...");
        
        HashMap<Item, NonNullList<KilnRecipe>> completedKilnRecipesCacheList = new HashMap<>();
        
        filterRecipes(completedKilnRecipesCacheList, tempKilnRecipes, event.getServer().registryAccess());
        //* Smoker recipes are intentionally applied after smelting recipes to override them for the same input item.
        //! This is NOT redundant. Some mod will add smoker-only recipes.
        filterRecipes(completedKilnRecipesCacheList, tempSmokingRecipes, event.getServer().registryAccess());
        
        configDebug("[RECIPE_CACHE] Conversion finished. Continue to put recipes into the map...");
        
        KILN_CACHED_RECIPES.putAll(completedKilnRecipesCacheList);
        
        LOGGER.info("[EVENT_FINISHED] Kiln recipe caching finished in {} ms!", time.getTime());
    }
    
    /**
     * An encapsulated method for <b>getting recipes for smoker and blast furnace<b>.
     */
    private static <R extends AbstractCookingRecipe> void streamRecipes
    (HashMap<Item, NonNullList<R>> targetMap, @NotNull RecipeManager manager, RecipeType<R> recipeType)
    {
        for(RecipeHolder<R> recipeHolder: manager.getAllRecipesFor(recipeType))
        {
            R recipe = recipeHolder.value();
            
            for(Ingredient ingredient: recipe.getIngredients())
            {
                for(ItemStack stack: ingredient.getItems())
                {
                    Item item = stack.getItem();
                    
                    targetMap.computeIfAbsent(item, i -> NonNullList.create()).
                        add(recipe);
                }
            }
            
            configDebug("[RECIPE_STREAM] Completed a round of recipe collection, Ingredients: {}, current stream recipe type: {}",
                recipe.getIngredients(), recipeType
            );
        }
    }
    
    /**
     * An encapsulated method for <b>filtering, and converting recipes to <u>{@link KilnRecipe}</u></b>.
     */
    private static <R extends AbstractCookingRecipe> void filterRecipes
    (HashMap<Item, NonNullList<KilnRecipe>> targetMap, @NotNull HashMap<Item, NonNullList<R>> convertMap, RegistryAccess access)
    {
        convertMap.forEach((item, list) ->
            {
                if(!list.isEmpty() && list.getFirst() instanceof SmokingRecipe)
                {
                    if(targetMap.containsKey(item))
                    {
                        configDebug("[FINAL_FILTER] Item {} found in cache, clearing old Smelting recipes to override with Smoking.", item);
                        targetMap.get(item).clear();
                    }
                }
                
                for(R recipe: list)
                {
                    for(Ingredient ingredient: recipe.getIngredients())
                    {
                        KilnRecipe convertedRecipe = new KilnRecipe(
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
        configDebug("[FINAL_FILTER] Type: {}, Time: {}, Factor: {}",
            isSmokingRecipe ? "Smoking" : "Smelting", cookingTime, factor
        );
        
        return factor;
    }
    
    public static HashMap<Item, NonNullList<KilnRecipe>> getKilnCachedRecipes() { return KILN_CACHED_RECIPES; }
    
    public static HashMap<Item, NonNullList<BlastingRecipe>> getBannedRecipes() { return BANNED_RECIPES; }
    
    private static void configDebug(String message, Object @NotNull ... args) { CrispLogUtils.logIf(CrispConfig.KILN_EVENT_DEBUG.get(), () -> LOGGER.debug(message, args)); }
}
