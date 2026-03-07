//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.mixins;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableVanillaBlockEntityAccessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractFurnaceBlockEntity.class) 
public abstract class VanillaFurnaceAccessor implements CarriableVanillaBlockEntityAccessors.IVanillaFurnaceSeriesAccessor
{
    @Invoker @Override public abstract boolean callIsLit();
    @Invoker @Override public abstract int callGetBurnDuration(@NotNull ItemStack fuel);
    @Invoker @Override public abstract void callSetRecipeUsed(@NotNull RecipeHolder<?> recipeHolder);
    @Invoker @Override public abstract void callLoadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries);
    @Invoker @Override public abstract void callSaveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries);
    @Invoker @Override @NotNull public abstract NonNullList<ItemStack> callGetItems();
    
    @Accessor @NotNull @Override public abstract RecipeType<? extends AbstractCookingRecipe> getRecipeType();
    @Accessor @Override public abstract RecipeManager.@NotNull CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> getQuickCheck();
    @Accessor @Override public abstract int getLitTime();
    @Accessor @Override public abstract int getCookingProgress();
    
    @Override @Accessor public abstract void setLitTime(int litTime);
    @Override @Accessor public abstract void setCookingProgress(@Range(from = 0, to = 100) int cookingProgress);
}
