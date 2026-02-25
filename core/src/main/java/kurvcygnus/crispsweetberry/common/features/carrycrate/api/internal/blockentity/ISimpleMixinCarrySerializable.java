//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public interface ISimpleMixinCarrySerializable extends ICarrySerializable
{
    @Override default void loadCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) { callLoadAdditional(tag, registries); }
    
    @Override default void saveCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) { callSaveAdditional(tag, registries); }
    
    void callLoadAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries);
    
    void callSaveAdditional(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries);
}
