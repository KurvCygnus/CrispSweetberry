//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.integration;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class KilnBlockEntityCarryAdapter extends AbstractBlockEntityCarryAdapter<KilnBlockEntity>
{
    private KilnCarriableExtensions.KilnBlockEntityContext context;
    
    public KilnBlockEntityCarryAdapter(@NotNull KilnBlockEntity blockEntity) { super(blockEntity); }
    
    @Override public void onCarriedSequence(@NotNull CarriedContext context)
    {
        final BlockState state = context.level().getBlockState(context.pos());
        
        this.context = this.blockEntity.onCarriedSequence(context.level(), context.pos(), state, context.player());
    }
    
    @Override public void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) { this.blockEntity.loadCustomOnly(tag, registries); }
    
    @Override public void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        final CompoundTag data = this.blockEntity.saveCustomOnly(registries);
        tag.merge(data);
    }
    
    @Override public void carryTick(@NotNull ServerLevel level, long carryingTime, @NotNull CarriedContext context)
        { this.blockEntity.carryTick(level, carryingTime, this.context, context.pos()); }
    
    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate()
    {
        final float itemTotalRate = this.blockEntity.getContainerItems().stream().
            map(i -> ((float) i.getCount() / i.getMaxStackSize())).
            reduce(Float::sum).
            orElse(0F);
        
        return (int) (DEFAULT_PENALTY_RATE / (1 + itemTotalRate));
    }
}
