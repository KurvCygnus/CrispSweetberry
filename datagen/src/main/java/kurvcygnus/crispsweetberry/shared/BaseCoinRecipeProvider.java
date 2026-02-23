//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.shared;

import kurvcygnus.crispsweetberry.CrispRecipeProvider;
import kurvcygnus.crispsweetberry.common.features.coins.api.AbstractCoinItem;
import kurvcygnus.crispsweetberry.common.features.coins.api.AbstractCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.coins.api.AbstractCoinStackItem;
import kurvcygnus.crispsweetberry.common.features.coins.api.ICoinType;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * This a simple helper class to apply custom <u>{@link AbstractCoinStackBlock coin stack}</u> recipes.<br>
 * @apiNote <b>DataGen only accepts one <u>{@link RecipeProvider}</u>, so it's recommend to do 
 * <a href="https://en.wikipedia.org/wiki/Composition_over_inheritance"><u>{@code Composition}</u></a>
 * at your own main <u>{@link RecipeProvider}</u>, then use <u>{@link BaseCoinRecipeProvider#buildRecipes}</u> in it</b>.
 * @see CrispRecipeProvider A good example
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
public abstract class BaseCoinRecipeProvider extends RecipeProvider
{
    public BaseCoinRecipeProvider(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> registries) { super(output, registries); }
    
    @Override
    public final void buildRecipes(@NotNull RecipeOutput output)
    {
        for(int index = 0; index < getCoinTypeList().size(); index++)
        { 
            final ICoinType<?> coinType = getCoinTypeList().get(index);
            Objects.requireNonNull(coinType, "CoinType lists can not be null! Null element starts at index: " + index);
            
            if(!coinType.shouldAppear())
                continue;
            
            final AbstractCoinItem<?> coinItem = coinType.coinItem();
            final AbstractCoinStackItem<?> coinStackItem = coinType.stackItem();
            final Item nuggetItem = coinType.nuggetItem();
            final String id = coinType.getId().toString();
            
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, coinItem).
                requires(nuggetItem).
                unlockedBy("%s_coin_unlocked".formatted(id), has(nuggetItem)).
                unlockedBy("%s_coin_unlocked_by_loot".formatted(id), has(coinItem)).
                save(output);
            
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, nuggetItem).
                requires(coinItem).
                unlockedBy("%s_nugget_unlocked".formatted(id), has(coinItem)).
                save(output);
            
            ShapelessRecipeBuilder coinStackBlockRecipe = ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, coinStackItem);
            
            for(int quantity = 0; quantity < 9; quantity++)
                coinStackBlockRecipe = coinStackBlockRecipe.requires(coinItem);
            
            coinStackBlockRecipe.
                unlockedBy("%s_coin_stack_unlocked".formatted(id), has(coinItem)).
                unlockedBy("%s_coin_stack_unlocked_by_loot".formatted(id), has(coinStackItem)).
                save(output);
        }
    }
    
    protected abstract List<? extends ICoinType<?>> getCoinTypeList();
}
