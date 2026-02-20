//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import kurvcygnus.crispsweetberry.common.features.kiln.KilnRecipeCacheEvent;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries;
import kurvcygnus.crispsweetberry.common.features.kiln.api.IKilnRecipeView;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * This is the base of the existence of the whole kiln recipe.<br>
 * <b><i>It defines the data and behavior a kiln recipe contains</b></i>.
 *
 * @param processFactor The procession of kiln is percentage-based, this variable holds <b>the factor of influencing the final procession rate of kiln</b>,
 *                      which is calculated by cookTime.
 * @author Kurv Cygnus
 * @implNote Kiln WILL NOT support recipe unlock, or recipe book function.<br>
 * Reasons:<ul>
 * <li>
 * Supporting recipe unlocking inherently requires full integration with <u>{@link RecipeManager RecipeManager}</u>,
 * whose implementation is completely different from
 * <u>{@link KilnRecipeCacheEvent KilnRecipeCacheEvent}</u>,
 * and the event itself is the heart of <u>{@link KilnBlockEntity KilnBlockEntity}</u>,
 * <u>{@link KilnProgressCalculator KilnProcessCalculator}</u>,
 * would require a fundamental architectural redesign.
 * </li>
 * <li>
 * The recipe that recipe book requires must be static, which is opposite to the concept of {@code KilnRecipe}, in consideration of both recipe
 * source and process calculation rules.
 * </li>
 * <li>
 * Doing all of this means we also need to {@code implements} <u>{@link RecipeBookMenu RecipeBookMenu}</u>
 * in <u>{@link KilnMenu KilnMenu}</u>, and
 * <u>{@link KilnBlockEntity kiln itself}</u> is a multi-slots container,
 * which is obviously ultimately complex.
 * </li>
 * </ul>
 * <i><b>
 * In this case, consciously giving up certain vanilla integrations is a deliberate
 * and necessary design decision.
 * </b></i>
 * @see KilnRecipeSerializer Serializer
 * @see KilnRecipeType Type Declaration
 * @see KilnRecipeInput Recipe Input Part
 * @since 1.0 Release
 */
public record KilnRecipe(
    Ingredient ingredient,
    ItemStack result,
    double processFactor,
    float experience,
    boolean isBanned
) implements Recipe<KilnRecipeInput>, IKilnRecipeView
{
    public static final String GROUP_BUILDING_MATERIALS = "building_materials";
    public static final String GROUP_FOODS = "foods";
    public static final String GROUP_MATERIALS = "materials";
    public static final String GROUP_MISC = "misc";
    
    /**
     * The contractor method for <b>creating a recipe</b>, and <b>serialization</b>.
     */
    public KilnRecipe(@NotNull Ingredient ingredient, @NotNull ItemStack result, double processFactor, float experience, boolean isBanned)
    {
        this.ingredient = ingredient;
        this.result = result;
        this.processFactor = processFactor;
        this.experience = experience;
        this.isBanned = isBanned;
    }
    
    @Override
    public @NotNull KilnRecipe withBanned()
    {
        return new KilnRecipe(
            this.ingredient,
            this.result,
            this.processFactor,
            this.experience,
            true
        );
    }
    
    @Override
    public @NotNull KilnRecipe unBanned()
    {
        return new KilnRecipe(
            this.ingredient,
            this.result,
            this.processFactor,
            this.experience,
            false
        );
    }
    
    @Override
    public boolean matches(@NotNull KilnRecipeInput input, @NotNull Level level) { return this.ingredient.test(input.stack()); }
    
    @Override
    public @NotNull ItemStack assemble(@NotNull KilnRecipeInput input, HolderLookup.@NotNull Provider registries) { return this.result.copy(); }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }
    
    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) { return this.result.copy(); }
    
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return KilnRegistries.KILN_SERIALIZER.get(); }
    
    @Override
    public @NotNull RecipeType<?> getType() { return KilnRecipeType.INSTANCE; }
    
    @Override
    public @NotNull Ingredient ingredient() { return this.ingredient; }
    
    @Override
    public @NotNull ItemStack result() { return this.result.copy(); }
}
