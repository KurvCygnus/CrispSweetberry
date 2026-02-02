package kurvcygnus.crispsweetberry.common.features.coins.events;

import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

@EventBusSubscriber(modid = CrispSweetberry.MOD_ID)
public final class CoinRecipeCollectEvent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private static final HashBiMap<Item, Item> NUGGET_TO_COIN_RECIPES = HashBiMap.create();
    private static final HashBiMap<Item, Item> COIN_TO_NUGGET_RECIPES = HashBiMap.create();
    private static final HashBiMap<Item, Item> COIN_TO_STACK_RECIPES = HashBiMap.create();
    
    @SubscribeEvent
    static void collectCoinRecipes(final @NotNull ServerAboutToStartEvent event)
    {
        LOGGER.debug("[COIN_RECIPE] Start collecting coins recipes...");
        
        NUGGET_TO_COIN_RECIPES.clear();
        COIN_TO_NUGGET_RECIPES.clear();
        
        final RecipeManager manager = event.getServer().getRecipeManager();
        
        manager.getAllRecipesFor(RecipeType.CRAFTING).stream().map(RecipeHolder::value).forEach(recipe -> 
            {
                final @Nullable List<Ingredient> ingredients = recipe.getIngredients().stream().filter(i -> !i.isEmpty()).toList();
                
                if(ingredients.isEmpty() || ingredients.getFirst().getItems().length == 0)
                    return;
                
                final ItemStack materialSample = ingredients.getFirst().getItems()[0].copy();
                final ItemStack resultItem = recipe.getResultItem(event.getServer().registryAccess());
                
                if(ingredients.size() == 1)
                {
                    if(materialSample.getItem() instanceof AbstractCoinItem<?> coinItem && resultItem.is(coinItem.getCoinType().nuggetItem()))
                        COIN_TO_NUGGET_RECIPES.put(materialSample.getItem(), resultItem.getItem());
                    else if(resultItem.getItem() instanceof AbstractCoinItem<?> coinItem && materialSample.is(coinItem.getCoinType().nuggetItem()))
                        NUGGET_TO_COIN_RECIPES.put(materialSample.getItem(), resultItem.getItem());
                }
                else if(ingredients.size() == 9)
                {
                    final boolean isAllSame = ingredients.stream().allMatch(i -> i.test(materialSample));
                    
                    if(isAllSame && materialSample.getItem() instanceof AbstractCoinItem<?> coinItem && resultItem.is(coinItem.getCoinType().stackItem()))
                        COIN_TO_STACK_RECIPES.put(materialSample.getItem(), resultItem.getItem());
                }
            }
        );
    }
    
    public static @NotNull HashBiMap<Item, Item> getCoinCraftRecipes() { return NUGGET_TO_COIN_RECIPES; }
     
    public static @NotNull HashBiMap<Item, Item> getCoinDisassembleRecipes() { return COIN_TO_NUGGET_RECIPES; }
    
    public static @NotNull HashBiMap<Item, Item> getStackCraftRecipes() { return COIN_TO_STACK_RECIPES; }
}
