//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.carriables.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.BaseVanillaFurnaceSeriesAdapter;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import org.jetbrains.annotations.NotNull;

public final class VanillaFurnaceBlockEntitySeriesAdapterCollection
{
    public static final class FurnaceBlockEntityAdapter extends BaseVanillaFurnaceSeriesAdapter<FurnaceBlockEntity>
        { public FurnaceBlockEntityAdapter(@NotNull FurnaceBlockEntity blockEntity) { super(blockEntity); }}
    
    public static final class BlastFurnaceBlockEntityAdapter extends BaseVanillaFurnaceSeriesAdapter<BlastFurnaceBlockEntity>
        { public BlastFurnaceBlockEntityAdapter(@NotNull BlastFurnaceBlockEntity blockEntity) { super(blockEntity); }}
    
    public static final class SmokerBlockEntityAdapter extends BaseVanillaFurnaceSeriesAdapter<SmokerBlockEntity>
        { public SmokerBlockEntityAdapter(@NotNull SmokerBlockEntity blockEntity) { super(blockEntity); }}
}
