//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.mixins;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.IVanillaBrewingStandAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BrewingStandBlockEntity.class)
public abstract class VanillaBrewingStandCarryInjection implements IVanillaBrewingStandAccessor
{
    @Accessor @Override public abstract @NotNull NonNullList<ItemStack> getItems();
    @Accessor @Override public abstract int getBrewTime();
    @Accessor @Override public abstract int getFuel();
    @Accessor @Override public abstract @NotNull Item getIngredient();
    @Accessor @Override public abstract boolean @NotNull [] getLastPotionCount();
    @Accessor @Override public abstract void setBrewTime(@Range(from = 0, to = MAX_BREWING_TIME) int brewTime);
    @Accessor @Override public abstract void setFuel(@Range(from = 0, to = MAX_FUEL) int fuel);
    @Accessor @Override public abstract void setIngredient(@NotNull Item ingredient);
    @Accessor @Override public abstract void setLastPotionCount(boolean @NotNull [] lastPotionCount);
    
    @Invoker @Override public abstract void callLoadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries);
    @Invoker @Override public abstract void callSaveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries);
}
