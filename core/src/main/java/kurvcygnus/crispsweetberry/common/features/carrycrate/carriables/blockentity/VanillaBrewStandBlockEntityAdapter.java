//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.carriables.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.BaseVanillaBrewingStandAdapter;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.NotNull;

public final class VanillaBrewStandBlockEntityAdapter extends BaseVanillaBrewingStandAdapter<BrewingStandBlockEntity>
{
    public VanillaBrewStandBlockEntityAdapter(@NotNull BrewingStandBlockEntity blockEntity) { super(blockEntity); }
}
