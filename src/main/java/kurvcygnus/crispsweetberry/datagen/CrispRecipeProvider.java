//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.datagen;

import kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.registries.CrispItems;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class CrispRecipeProvider extends RecipeProvider
{
    private final VanillaCoinRecipeProvider coinRecipeProvider;
    
    CrispRecipeProvider(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> registries) 
    {
        super(output, registries);
        this.coinRecipeProvider = new VanillaCoinRecipeProvider(output, registries);
    }
    
    @Override
    protected void buildRecipes(@NotNull RecipeOutput output)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KilnRegistries.KILN.value()).
            pattern("TTT").
            pattern("TFT").
            pattern("BCB").
            define('T', Items.TERRACOTTA).
            define('F', Items.FURNACE).
            define('B', Items.BRICKS).
            define('C', Items.CAMPFIRE).//? TODO: Tags
            unlockedBy("kiln_unlocked", has(Items.FURNACE)). //? TODO
            save(output, "kiln_std");
        
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, KilnRegistries.KILN.value()).//? Use this dumb impl for now.
            pattern("TTT").
            pattern("TFT").
            pattern("BCB").
            define('T', Items.TERRACOTTA).
            define('F', Items.FURNACE).
            define('B', Items.BRICKS).
            define('C', Items.SOUL_CAMPFIRE).
            unlockedBy("kiln_unlocked", has(Items.FURNACE)).
            save(output, "kiln_soul");
        
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, CrispItems.CARRY_CRATE.value(), 2).
            pattern("PPP").
            pattern("PSP").
            pattern("PPP").
            define('P', Items.PAPER).
            define('S', Tags.Items.STRINGS).
            unlockedBy("carry_crate_unlocked", inventoryTrigger(
                ItemPredicate.Builder.item().of(Tags.Items.CHESTS).build(),
                ItemPredicate.Builder.item().of(Items.PAPER).build()
            )).
            save(output);
        
        ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, CrispItems.HONEY_BERRY.value(), 3).
            requires(Items.SUGAR).
            requires(Tags.Items.FOODS_BERRY).
            requires(Tags.Items.FOODS_BERRY).
            requires(Tags.Items.FOODS_BERRY).
            requires(Tags.Items.DRINKS_HONEY).
            unlockedBy("csb_unlocked", inventoryTrigger(
                ItemPredicate.Builder.item().of(Tags.Items.FOODS_BERRY).build(),
                ItemPredicate.Builder.item().of(Tags.Items.DRINKS_HONEY).build()
            )).
            save(output);
        
        ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, TTorchRegistries.THROWABLE_TORCH.value(), 9).
            requires(ItemTags.COALS).
            requires(ItemTags.COALS).
            requires(ItemTags.COALS).
            requires(Items.STICK).
            unlockedBy("throwable_torch_unlocked_standard", has(Items.TORCH)).
            unlockedBy("throwable_torch_unlocked_nether_standard",  has(Items.SOUL_TORCH)).
            unlockedBy("throwable_torch_unlocked_from_loot", has(TTorchRegistries.THROWABLE_TORCH.value())).
            save(output);
        
        coinRecipeProvider.buildRecipes(output);
    }
}
