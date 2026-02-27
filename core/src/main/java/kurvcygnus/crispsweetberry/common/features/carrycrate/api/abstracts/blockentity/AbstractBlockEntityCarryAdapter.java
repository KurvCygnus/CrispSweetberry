//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarriableLifecycle;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.ICarryReactable;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.ICarrySerializable;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.ICarryTickable;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This is the basic of BlockEntity Adapters, which doesn't include any usable logics.
 * @param <E> The blockEntity this adapter takes responsibility of.
 * @apiNote <b>BlockEntity's adapter is independent.</b><br>
 * Unlike vanilla design, blockEntity's adapter doesn't relies on its corresponded Block, 
 * thus, registering blockEntity itself only is the proper way to do compat.
 * @author Kurv Cygnus
 * @see BaseVanillaFurnaceSeriesAdapter Furnace Series Adapter
 * @see BaseVanillaBrewingStandAdapter Brewing Stand Adapter
 * @see SimpleContainerBlockEntityCarryAdapter Universal Storge Container Adapter
 * @since 1.0 Release
 */
public abstract class AbstractBlockEntityCarryAdapter<E extends BlockEntity> 
implements ICarryTickable, ICarrySerializable, ICarriableLifecycle, ICarryReactable
{
    protected final E blockEntity;
    
    public AbstractBlockEntityCarryAdapter(@NotNull E blockEntity) 
    {
        Objects.requireNonNull(blockEntity, "Param \"blockEntity\" must not be null!");
        
        this.blockEntity = blockEntity;
    }
}
