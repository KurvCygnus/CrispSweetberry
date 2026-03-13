//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.CarriableSimpleLogicCollection;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableVanillaBlockEntityAccessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

//? TODO: BIG CHEST TEST. NBT IO Logic may have issues.
public class SimpleContainerBlockEntityCarryAdapter<E extends BaseContainerBlockEntity>
extends AbstractBlockEntityCarryAdapter<E>
implements CarriableSimpleLogicCollection.ISimpleBlockEntityPenaltyLogic<E>, CarriableSimpleLogicCollection.ISimpleBlockEntityBreakLogic<E>
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
