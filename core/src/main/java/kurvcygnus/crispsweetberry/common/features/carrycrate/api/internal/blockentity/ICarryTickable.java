//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public interface ICarryTickable
{
    default void onCarriedSequence(@NotNull ICarryTickable.CarriedContext context) {}
    
    void carryTick(@NotNull ServerLevel level, long carryingTime);
    
    record CarriedContext(
        @NotNull ServerLevel level,
        @NotNull BlockPos pos,
        @NotNull BlockState state,
        @NotNull ServerPlayer player
    ) {}
}
