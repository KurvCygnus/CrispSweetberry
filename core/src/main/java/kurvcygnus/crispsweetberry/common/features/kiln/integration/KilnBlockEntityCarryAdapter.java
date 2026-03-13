//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.integration;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class KilnBlockEntityCarryAdapter extends AbstractBlockEntityCarryAdapter<KilnBlockEntity>
{
    private KilnCarriableExtensions.KilnBlockEntityContext context;
    
    public KilnBlockEntityCarryAdapter(@NotNull BlockEntity blockEntity) { super(blockEntity); }
    
    @Override public void onCarriedSequence(@NotNull CarriedContext context, @NotNull KilnBlockEntity blockEntity)
    {
        final BlockState state = context.level().getBlockState(context.pos());
        
        this.context = blockEntity.onCarriedSequence(context.level(), context.pos(), state, context.player());
    }
    
    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate(@NotNull KilnBlockEntity blockEntity)
    {
        final float itemTotalRate = blockEntity.getContainerItems().stream().
            map(i -> ((float) i.getCount() / i.getMaxStackSize())).
            reduce(Float::sum).
            orElse(0F);
        
        return (int) (DEFAULT_PENALTY_RATE / (1 + itemTotalRate));
    }
    
    @Override public void onPlacedProcess(@NotNull ServerLevel level, long elapsedTime, @NotNull CarriedContext context, @NotNull KilnBlockEntity blockEntity)
        { blockEntity.onPlacedProcess(level, elapsedTime, this.context, context.pos()); }
    
    @Override
    public void saveCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries, @NotNull KilnBlockEntity blockEntity)
    {
        final CompoundTag data = blockEntity.saveCustomOnly(registries);
        tag.merge(data);
    }
    
    @Override public void loadCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries, @NotNull KilnBlockEntity blockEntity)
        { blockEntity.loadCustomOnly(tag, registries); }
}
