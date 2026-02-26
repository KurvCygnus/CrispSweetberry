//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.integration;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;

public final class KilnBlockEntityCarryAdapter extends AbstractBlockEntityCarryAdapter<KilnBlockEntity>
{
    private KilnCarriableExtensions.KilnBlockEntityContext context;
    
    public KilnBlockEntityCarryAdapter(@NotNull KilnBlockEntity blockEntity) { super(blockEntity); }
    
    @Override public void onCarriedSequence(@NotNull CarriedContext context)
        { this.context = this.blockEntity.onCarriedSequence(context.level(), context.pos(), context.state(), context.player()); }
    
    @Override public void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) { this.blockEntity.loadCustomOnly(tag, registries); }
    
    @Override public void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        final CompoundTag data = this.blockEntity.saveCustomOnly(registries);
        tag.merge(data);
    }
    
    @Override public void carryTick(@NotNull ServerLevel level, long carryingTime, @NotNull CarriedContext context)
        { this.blockEntity.carryTick(level, carryingTime, this.context, context.pos()); }
}
