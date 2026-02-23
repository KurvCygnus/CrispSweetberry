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
public final class KilnRecipeSerializer implements RecipeSerializer<KilnRecipe>
{
    /**
     * <b><u>{@link com.mojang.serialization.MapCodec MapCodec}</u></b> is the protagonist of <b>local serialization</b>.<br>
     * <b><i>It defines the format of recipe JSON</b></i>.
     */
    private final MapCodec<KilnRecipe> mapCodec;
    /**
     * <b><u>{@link net.minecraft.network.codec.StreamCodec StreamCodec}</b></u> is the director of <b>network sync</b>.<br>
     * <b><i>It makes sure that data from server won't go wrong</b></i>.
     */
    private final StreamCodec<RegistryFriendlyByteBuf, KilnRecipe> streamCodec;
    
    /**
     * The constructor method to <b>define the codecs themselves</b>.
     */
    public KilnRecipeSerializer()
    {
        this.mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(KilnRecipe::ingredient),
            ItemStack.CODEC.fieldOf("result").forGetter(KilnRecipe::result),
            Codec.DOUBLE.fieldOf("cookTickRateMultiFactor").orElse(1D).forGetter(KilnRecipe::processFactor),
            Codec.FLOAT.fieldOf("experience").orElse(0F).forGetter(KilnRecipe::experience),
            Codec.BOOL.fieldOf("isBanned").orElse(true).forGetter(KilnRecipe::isBanned)
            ).apply(instance, KilnRecipe::new)
        );
        
        //* "fromNetwork" and "toNetwork" correspond to I/O respectively.
        //! WARN: You should make sure that the content of both two methods are synchronized all the time when adding new fields.
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
    
    @Override public @NotNull MapCodec<KilnRecipe> codec() { return this.mapCodec; }
    
    @Override public @NotNull StreamCodec<RegistryFriendlyByteBuf, KilnRecipe> streamCodec() { return this.streamCodec; }
    
    //? TODO: Group implementation for JEI/REI compatibility.
    private void toNetwork(RegistryFriendlyByteBuf buffer, @NotNull KilnRecipe recipe)
    {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient());
        ItemStack.STREAM_CODEC.encode(buffer, recipe.result());
        buffer.writeDouble(recipe.processFactor());
        buffer.writeFloat(recipe.experience());
        buffer.writeBoolean(recipe.isBanned());
    }
    
    private @NotNull KilnRecipe fromNetwork(RegistryFriendlyByteBuf buffer)
    {
        final Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        final ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
        final double processFactor = buffer.readDouble();
        final float experience = buffer.readFloat();
        final boolean isBanned = buffer.readBoolean();
        
        return new KilnRecipe(ingredient, stack, processFactor, experience, isBanned);
    }
}
