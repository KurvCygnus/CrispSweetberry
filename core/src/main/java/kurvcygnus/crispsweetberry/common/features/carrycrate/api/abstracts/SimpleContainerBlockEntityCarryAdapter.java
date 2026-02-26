//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.jetbrains.annotations.NotNull;

//? TODO: BIG CHEST.
public class SimpleContainerBlockEntityCarryAdapter<E extends BaseContainerBlockEntity> extends AbstractBlockEntityCarryAdapter<E>
{
    public SimpleContainerBlockEntityCarryAdapter(@NotNull E blockEntity) { super(blockEntity); }
    
    @Override public void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) 
    {
        final CompoundTag data = this.blockEntity.saveCustomOnly(registries);
        tag.merge(data);
    }
    
    @Override public void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) { this.blockEntity.loadCustomOnly(tag, registries); }
    
    @Override public final void onCarriedSequence(@NotNull CarriedContext context) {}
    
    @Override public final void carryTick(@NotNull ServerLevel level, long carryingTime, @NotNull CarriedContext context) {}
}
