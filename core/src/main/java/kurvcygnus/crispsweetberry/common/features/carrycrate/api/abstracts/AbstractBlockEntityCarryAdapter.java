//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.ICarrySerializable;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.ICarryTickable;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class AbstractBlockEntityCarryAdapter<E extends BlockEntity> implements ICarryTickable, ICarrySerializable
{
    protected final E blockEntity;
    
    public AbstractBlockEntityCarryAdapter(@NotNull E blockEntity) 
    {
        Objects.requireNonNull(blockEntity, "Param \"blockEntity\" must not be null!");
        
        this.blockEntity = blockEntity;
    }
}
