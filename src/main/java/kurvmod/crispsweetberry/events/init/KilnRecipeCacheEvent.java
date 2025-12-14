package kurvmod.crispsweetberry.events.init;

import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.HashMap;

import static kurvmod.crispsweetberry.CrispSweetberry.LOGGER;

/**
 * The event which <b>fills the content with kiln recipes.</b>
 * @since CSB 1.0 Release
 * @author Kurv
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID)
public final class KilnRecipeCacheEvent
{
    private static final HashMap<Item, AbstractCookingRecipe> KILN_RECIPES_CACHE = new HashMap<>();
    
    //  Kiln also accepts smoker's recipes, but as a cost, it's smelting speed will slow down.
    //* So we need to specifically get an independent list for such a situation.
    private static final HashMap<Item, AbstractCookingRecipe> SMOKER_RECIPES_CACHE = new HashMap<>();
    
    /**
     * The core of <b>the adaptive recipes for kiln</b>.
     * @param event Event <b>{@code ServerStartedEvent}</b> is actually the event that triggers when the player joining a world.<br>
     * <b><i>So it's obvious that this is the best timing for getting recipes, as they must have been prepared in that time.</b></i>
     */
    @SubscribeEvent
    public void getKilnRecipes(ServerStartedEvent event)
    {
        LOGGER.info("Getting Kiln Recipes...");
        
        KILN_RECIPES_CACHE.clear();
        SMOKER_RECIPES_CACHE.clear();
        
        RecipeManager manager = event.getServer().getRecipeManager();
        
        HashMap<Item, AbstractCookingRecipe> tempSmokingRecipes = new HashMap<>();
        HashMap<Item, AbstractCookingRecipe> forbiddenRecipes = new HashMap<>();
        HashMap<Item, AbstractCookingRecipe> tempKilnRecipes = new HashMap<>();
        
        LOGGER.debug("Getting Smoker Recipes...");
        streamRecipes(tempSmokingRecipes, manager, RecipeType.SMOKING);
        
        LOGGER.debug("Getting Blast Furnace(Banned) Recipes...");
        streamRecipes(forbiddenRecipes, manager, RecipeType.BLASTING);
        
        LOGGER.debug("Starting filtering kiln recipes...");
        manager.getAllRecipesFor(RecipeType.SMELTING).forEach(recipeHolder ->
            {
                AbstractCookingRecipe recipe = recipeHolder.value();
                Item recipeItem = recipeHolder.value().getIngredients().getFirst().getItems()[0].getItem();
                
                if(tempSmokingRecipes.containsKey(recipeItem) || forbiddenRecipes.containsKey(recipeItem))
                    return;
                
                LOGGER.debug("Filtered recipe {} for kiln", recipe);
                tempKilnRecipes.put(recipeItem, recipe);
            }
        );
        LOGGER.debug("Finishing filtering kiln recipes. Continue to put recipes into immutable maps...");
        
        KILN_RECIPES_CACHE.putAll(tempKilnRecipes);
        SMOKER_RECIPES_CACHE.putAll(tempSmokingRecipes);
        
        LOGGER.info("Kiln recipe caching is finished!");
    }
    
    /**
     * An encapsulated method for <b>getting recipes for smoker and blast furnace<b>.
     */
    private static void streamRecipes(HashMap<Item, AbstractCookingRecipe> targetMap, RecipeManager manager, RecipeType<?> recipeType)
    {
        manager.getAllRecipesFor(recipeType).forEach(recipeHolder ->
                {
                    Item recipeItem = recipeHolder.value().getIngredients().getFirst().getItems()[0].getItem();
                    AbstractCookingRecipe recipe = (AbstractCookingRecipe) recipeHolder.value();
                    
                    targetMap.put(recipeItem, recipe);
                }
            );
    }
    
    //Getters
    public static HashMap<Item, AbstractCookingRecipe> getKilnRecipesCache() { return KILN_RECIPES_CACHE; }
    
    public static HashMap<Item, AbstractCookingRecipe> getSmokerRecipesCache() { return SMOKER_RECIPES_CACHE; }
}
