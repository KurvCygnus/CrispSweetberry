//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.BaseVanillaBrewingStandAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.BaseVanillaFurnaceSeriesAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.SimpleContainerBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableBlockEntityExtensions.ICarrySerializable;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * This is the collection of vanilla's blockEntity's unionData accessors, which involves <b>mixin</b>,
 * and won't be used by external at most situations.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public final class CarriableVanillaBlockEntityAccessors
{
    /**
     * The accessor of <u>{@link net.minecraft.world.level.block.entity.BaseContainerBlockEntity BaseContainerBlockEntity}</u>.
     * @apiNote If you want to get item list, please consider inherit your adapter from 
     * <u>{@link SimpleContainerBlockEntityCarryAdapter}</u>, 
     * and use <u>{@link SimpleContainerBlockEntityCarryAdapter#getItems(BaseContainerBlockEntity)}</u> instead.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public interface IBaseContainerAccessor { @NotNull NonNullList<ItemStack> callGetItems(); }
    
    /**
     * This is a bridge interface between mixin accessor and standard serialization logics.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public interface IMixinCarrySerializable extends ICarrySerializable
    {
        @Override default void loadCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) { callLoadAdditional(tag, registries); }
        
        @Override default void saveCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) { callSaveAdditional(tag, registries); }
        
        void callLoadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries);
        
        void callSaveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries);
    }
    
    /**
     * The accessor of <u>{@link net.minecraft.world.level.block.entity.BrewingStandBlockEntity BrewingStandBlockEntity}</u>.
     * @author Kurv Cygnus
     * @since 1.0 Release
     * @see BaseVanillaBrewingStandAdapter Base Brewing Stand Adapter
     */
    public interface IVanillaBrewingStandAccessor extends IMixinCarrySerializable
    {
        int MAX_FUEL = 20;
        int MAX_BREWING_TIME = 400;
        
        @NotNull NonNullList<ItemStack> getItems();
        
        int getBrewTime();
        
        int getFuel();
        
        @NotNull Item getIngredient();
        
        boolean @NotNull [] getLastPotionCount();
        
        void setBrewTime(@Range(from = 0, to = MAX_BREWING_TIME) int brewTime);
        
        void setFuel(@Range(from = 0, to = MAX_FUEL) int fuel);
        
        void setIngredient(@NotNull Item ingredient);
        
        void setLastPotionCount(boolean @NotNull [] lastPotionCount);
    }
    
    /**
     * The accessor of <u>{@link net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity AbstractFurnaceBlockEntity}</u>.
     * @author Kurv Cygnus
     * @since 1.0 Release
     * @see BaseVanillaFurnaceSeriesAdapter Base Vanilla Furnace Adapter
     */
    public interface IVanillaFurnaceSeriesAccessor extends IMixinCarrySerializable
    {
        @NotNull RecipeType<? extends AbstractCookingRecipe> getRecipeType();
        
        RecipeManager.@NotNull CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> getQuickCheck();
        
        int getLitTime();
        
        int getCookingProgress();
        
        @NotNull NonNullList<ItemStack> callGetItems();
        
        boolean callIsLit();
        
        int callGetBurnDuration(@NotNull ItemStack fuel);
        
        void callSetRecipeUsed(@NotNull RecipeHolder<?> recipeHolder);
        
        void setLitTime(int litTime);
        
        void setCookingProgress(@Range(from = 0, to = 100) int cookingProgress);
    }
}
