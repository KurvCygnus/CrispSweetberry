//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.CarriableSimpleLogicCollection.ISimpleBlockEntityBreakLogic;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.CarriableSimpleLogicCollection.ISimpleBlockEntityPenaltyDropLogic;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.CarriableSimpleLogicCollection.ISimpleBlockEntityPenaltyLogic;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableVanillaBlockEntityAccessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

//? TODO: BIG CHEST TEST. NBT IO Logic may have issues.

/**
 * This is simple implementation for most containers, like <u>{@link net.minecraft.world.level.block.entity.ChestBlockEntity Chest}</u>, 
 * <u>{@link net.minecraft.world.level.block.entity.BarrelBlockEntity Barrel}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @param <E> The blockEntity this adapter takes responsibility of.
 */
public class SimpleContainerBlockEntityCarryAdapter<E extends BaseContainerBlockEntity>
extends AbstractBlockEntityCarryAdapter<E> implements ISimpleBlockEntityPenaltyLogic<E>, ISimpleBlockEntityBreakLogic<E>, ISimpleBlockEntityPenaltyDropLogic<E>
{
    public SimpleContainerBlockEntityCarryAdapter(@NotNull BlockEntity blockEntity) { super(blockEntity); }
    
    @Override public void saveCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries, @NotNull E blockEntity)
    {
        final CompoundTag data = blockEntity.saveCustomOnly(registries);
        tag.merge(data);
    }
    
    @Override public void loadCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries, @NotNull E blockEntity) 
        { blockEntity.loadCustomOnly(tag, registries); }
    
    @Override public final void onCarriedSequence(@NotNull CarriedContext context, @NotNull E blockEntity) {}
    
    @Override public @NotNull NonNullList<ItemStack> getItems(@NotNull E blockEntity) 
        { return ((CarriableVanillaBlockEntityAccessors.IBaseContainerAccessor) blockEntity).callGetItems(); }
    
    @Override public @NotNull Class<?> getSupportedType() { return BaseContainerBlockEntity.class; }
}
