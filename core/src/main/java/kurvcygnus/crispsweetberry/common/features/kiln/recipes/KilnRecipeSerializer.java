//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * This is the heart of <b>jsonifying</b> the <b>recipe of kiln</b>.
 * @see KilnRecipe Recipe Part
 * @see KilnRecipeType Type Declaration
 * @see KilnRecipeInput Recipe Input Part
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
public enum KilnRecipeSerializer implements RecipeSerializer<KilnRecipe>
{
    INST;
    
    private static final MapCodec<KilnRecipe> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(KilnRecipe::ingredient),
            ItemStack.CODEC.fieldOf("result").forGetter(KilnRecipe::result),
            Codec.DOUBLE.fieldOf("cookTickRateMultiFactor").orElse(1.).forGetter(KilnRecipe::processFactor),
            Codec.FLOAT.fieldOf("experience").orElse(0F).forGetter(KilnRecipe::experience),
            Codec.BOOL.fieldOf("isBanned").orElse(true).forGetter(KilnRecipe::isBanned)
        ).apply(instance, KilnRecipe::new)
    );
    
    private static final StreamCodec<RegistryFriendlyByteBuf, KilnRecipe> STREAM_CODEC =
        StreamCodec.of(KilnRecipeSerializer::toNetwork, KilnRecipeSerializer::fromNetwork);
    
    @Override public @NotNull MapCodec<KilnRecipe> codec() { return CODEC; }
    
    @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, KilnRecipe> streamCodec() { return STREAM_CODEC; }
    
    //? TODO: Group implementation for JEI/REI compatibility.
    private static void toNetwork(@NotNull RegistryFriendlyByteBuf buffer, @NotNull KilnRecipe recipe)
    {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
        ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
        buffer.writeDouble(recipe.processFactor());
        buffer.writeFloat(recipe.experience());
        buffer.writeBoolean(recipe.isBanned());
    }
    
    private static @NotNull KilnRecipe fromNetwork(@NotNull RegistryFriendlyByteBuf buffer)
    {
        final var ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        final var stack = ItemStack.STREAM_CODEC.decode(buffer);
        final double processFactor = buffer.readDouble();
        final float experience = buffer.readFloat();
        final boolean isBanned = buffer.readBoolean();
        
        return new KilnRecipe(ingredient, stack, processFactor, experience, isBanned);
    }
}
