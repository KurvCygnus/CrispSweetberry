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
import org.jetbrains.annotations.NotNull;

public interface IAtomicCarriable
{
    default void onCarriedSequence(@NotNull CarriedContext context) { }
    
    void onPlacedProcess(@NotNull ServerLevel level, long elapsedTime, @NotNull CarriedContext context);
    
    record CarriedContext(
        @NotNull ServerLevel level,
        @NotNull BlockPos pos,
        @NotNull ServerPlayer player
    ) { }
}
