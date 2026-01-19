package kurvcygnus.crispsweetberry.common.features.kiln.events;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.utils.constants.MiscConstants;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;

/**
 * The event which <b>fills the content with kiln recipes.</b>
 * @see KilnBlockEntity Usage
 * @since CSB 1.0 Release
 * @author Kurv
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID)
public final class KilnRecipeCacheEvent
{
    private static final HashMap<Item, NonNullList<KilnRecipe>> KILN_CACHED_RECIPES = new HashMap<>();
    private static final HashMap<Item, NonNullList<BlastingRecipe>> BANNED_RECIPES = new HashMap<>();
    
    private static final Logger logger = LogUtils.getLogger();
    
    /**
     * The core of <b>the adaptive recipes for kiln</b>.
     * @param event Event <b>{@code ServerStartedEvent}</b> is actually the event that triggers when the server has fully started and all recipes are loaded.<br>
     * <b><i>So it's obvious that this is the best timing for getting recipes, as they must have been prepared in that time.</b></i>
     */
    @SubscribeEvent
    public static void getKilnRecipes(final @NotNull ServerStartedEvent event)
    {
        logger.info("Getting Kiln Recipes...");
        
        KILN_CACHED_RECIPES.clear();
        
        RecipeManager manager = event.getServer().getRecipeManager();
        
        HashMap<Item, NonNullList<SmokingRecipe>> tempSmokingRecipes = new HashMap<>();
        HashMap<Item, NonNullList<SmeltingRecipe>> tempKilnRecipes = new HashMap<>();
        
        logger.debug("Getting Smoker Recipes...");
        streamRecipes(tempSmokingRecipes, manager, RecipeType.SMOKING);
        
        logger.debug("Getting Blast Furnace(Banned) Recipes...");
        streamRecipes(BANNED_RECIPES, manager, RecipeType.BLASTING);
        
        logger.debug("Starting filtering kiln recipes...");
        manager.getAllRecipesFor(RecipeType.SMELTING).forEach(recipeHolder ->
            {
                SmeltingRecipe recipe = recipeHolder.value();
                
                for(Ingredient ingredient: recipe.getIngredients())
                {
                    logger.debug("Start hooking ingredient {}", ingredient);
                    
                    for(ItemStack stack: ingredient.getItems())
                    {
                        Item item = stack.getItem();
                        
                        if(BANNED_RECIPES.containsKey(item))
                            continue;
                        
                        logger.debug("Filtered recipe {} for kiln", recipe);
                        
                        tempKilnRecipes.computeIfAbsent(item, i -> NonNullList.create()).
                            add(recipe);
                    }
                }
            }
        );
        logger.debug("Finished filtering kiln recipes. Start filtering unique mod recipes...");
        
        logger.debug("Finished filtering kiln recipes. Start conversion...");
        
        HashMap<Item, NonNullList<KilnRecipe>> completedKilnRecipesCacheList = new HashMap<>();
        
        filterRecipes(completedKilnRecipesCacheList, tempKilnRecipes, event.getServer().registryAccess());
        //* Smoker recipes are intentionally applied after smelting recipes to override them for the same input item.
        //! This is NOT redundant. Some mod will add smoker-only recipes.
        filterRecipes(completedKilnRecipesCacheList, tempSmokingRecipes, event.getServer().registryAccess());
        
        logger.debug("Conversion finished. Continue to put recipes into map...");
        
        KILN_CACHED_RECIPES.putAll(completedKilnRecipesCacheList);
        
        logger.info("Kiln recipe caching is finished!");
    }
    
    /**
     * An encapsulated method for <b>getting recipes for smoker and blast furnace<b>.
     */
    private static <R extends AbstractCookingRecipe> void streamRecipes
    (HashMap<Item, NonNullList<R>> targetMap, RecipeManager manager, RecipeType<R> recipeType)
    {
        manager.getAllRecipesFor(recipeType).forEach(recipeHolder ->
            {
                R recipe = recipeHolder.value();
                
                for(Ingredient ingredient: recipe.getIngredients())
                {
                    logger.debug("Start hooking ingredient {} for {} stream", ingredient.toString(), recipeType);
                    for(ItemStack stack: ingredient.getItems())
                    {
                        Item item = stack.getItem();
                        
                        targetMap.computeIfAbsent(item, i -> NonNullList.create()).
                            add(recipe);
                        
                        logger.debug("Streamed recipe {} in the temp Hashmap of {}", item, recipeType);
                    }
                }
            }
        );
    }
    
    /**
     * An encapsulated method for <b>filtering, and converting recipes to <u>{@link KilnRecipe}</u></b>.
     */
    private static <R extends AbstractCookingRecipe> void filterRecipes(HashMap<Item, NonNullList<KilnRecipe>> targetMap,
        HashMap<Item, NonNullList<R>> convertMap, RegistryAccess access)
    {
        convertMap.forEach((item, list) ->
            list.forEach(recipe ->
                {
                    for(Ingredient ingredient: recipe.getIngredients())
                    {
                        logger.debug("Start converting ingredient {} to KilnRecipe", ingredient.toString());
                        
                        KilnRecipe convertedRecipe = new KilnRecipe(
                            ingredient,
                            recipe.getResultItem(access),
                            calculateProcessFactor(recipe.getCookingTime(), recipe instanceof SmokingRecipe),
                            recipe.getExperience()
                        );
                        
                        targetMap.computeIfAbsent(item, i -> NonNullList.create()).
                            add(convertedRecipe);
                        
                        logger.debug("Finished converting ingredient {} to KilnRecipe", ingredient);
                    }
                }
            )
        );
    }
    
    private static double calculateProcessFactor(int cookingTime, boolean isSmokingRecipe)
        { return Math.max(0.05D, (double) cookingTime / MiscConstants.FURNACE_SMELTING_TIME) * (isSmokingRecipe ? 1.25D: 1D); }
    //!                     ↑ Maybe some mod will introduce short cooking time recipes into the game,
    //!                       so we should make sure at least processFactor is always bigger than 0D.
    
    public static HashMap<Item, NonNullList<KilnRecipe>> getKilnCachedRecipes() { return KILN_CACHED_RECIPES; }
    
    public static HashMap<Item, NonNullList<BlastingRecipe>> getBannedRecipes() { return BANNED_RECIPES; }
}
