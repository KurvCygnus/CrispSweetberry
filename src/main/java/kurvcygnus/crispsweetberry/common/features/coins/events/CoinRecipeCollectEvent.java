//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.events;

import com.google.common.collect.HashBiMap;
import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinItem;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class CoinRecipeCollectEvent
{
    private static final MarkLogger LOGGER = MarkLogger.withMarkerSuffixes(LogUtils.getLogger(), "COIN_RECIPE");
    
    private static final HashBiMap<Item, Item> NUGGET_TO_COIN_RECIPES = HashBiMap.create();
    private static final HashBiMap<Item, Item> COIN_TO_NUGGET_RECIPES = HashBiMap.create();
    private static final HashBiMap<Item, Item> COIN_TO_STACK_RECIPES = HashBiMap.create();
    
    @SubscribeEvent
    static void collectCoinRecipes(final @NotNull ServerAboutToStartEvent event) { collectRecipes(event.getServer().getRecipeManager(), event.getServer().registryAccess()); }
    
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
    
    private static void collectRecipes(@NotNull RecipeManager manager, @NotNull RegistryAccess registryAccess)
    {
        LOGGER.debug("Start collecting coins recipes...");
        
        NUGGET_TO_COIN_RECIPES.clear();
        COIN_TO_NUGGET_RECIPES.clear();
        COIN_TO_STACK_RECIPES.clear();
        
        manager.getAllRecipesFor(RecipeType.CRAFTING).stream().map(RecipeHolder::value).forEach(recipe ->
            {
                final @Nullable List<Ingredient> ingredients = recipe.getIngredients().stream().filter(i -> !i.isEmpty()).toList();
                
                if(ingredients.isEmpty() || ingredients.getFirst().getItems().length == 0)
                    return;
                
                final ItemStack materialSample = ingredients.getFirst().getItems()[0].copy();
                final ItemStack resultItem = recipe.getResultItem(registryAccess);
                
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
        )
        ;
    }
    
    public static @NotNull HashBiMap<Item, Item> getCoinCraftRecipes() { return NUGGET_TO_COIN_RECIPES; }
     
    public static @NotNull HashBiMap<Item, Item> getCoinDisassembleRecipes() { return COIN_TO_NUGGET_RECIPES; }
    
    public static @NotNull HashBiMap<Item, Item> getStackCraftRecipes() { return COIN_TO_STACK_RECIPES; }
}
