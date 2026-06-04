//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * This mixin just does one simple hack: Making external able to access <u>{@link BlockSnapshot}</u>'s contractor.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@Mixin(BlockSnapshot.class)
public interface IBlockSnapshotCreator
{
    @ApiStatus.Internal @Invoker("<init>") @Contract("_, _, _, _, _, _ -> new")
    static @NotNull BlockSnapshot _$csb_callInit(
        @NotNull ResourceKey<Level> dim,
        @NotNull LevelAccessor level,
        @NotNull BlockPos pos,
        @NotNull BlockState state,
        @Nullable CompoundTag nbt,
        int flags
    )
    {
        //noinspection Contract
        throw new AssertionError("UwU");
        //! In runtime, this logic will be replaced with real constructor. So we shall suppress it,
        //! in order to avoid the [[SuppressWarnings]] pollution.
    }
}
