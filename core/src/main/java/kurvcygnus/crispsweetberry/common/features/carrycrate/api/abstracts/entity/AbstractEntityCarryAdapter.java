//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.entity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarriableLifecycle;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.ICarryReactable;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public abstract class AbstractEntityCarryAdapter<E extends LivingEntity> implements ICarriableLifecycle, ICarryReactable
{
    protected final E entity;
    
    public AbstractEntityCarryAdapter(@NotNull E entity) 
    {
        Objects.requireNonNull(entity, "Param \"entity\" must not be null!");
        this.entity = entity;
    }
    
    @Override public abstract @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate();
}
