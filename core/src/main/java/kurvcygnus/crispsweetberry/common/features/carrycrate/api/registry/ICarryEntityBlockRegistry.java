//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.registry;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.AbstractBlockEntityCarryAdapter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICarryEntityBlockRegistry
{
    <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>> 
    void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryAdapterBlockEntityFactory<E, A> carryAdapterBlockEntityFactory
    );
    
    <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> 
    void registerUniversalBlockEntityAdapter(
        @NotNull List<BlockEntityType<? extends E>> blockEntityType,
        @NotNull ICarryAdapterBlockEntityFactory<E, A> carryAdapterBlockEntityFactory
    );
    
    @FunctionalInterface 
    interface ICarryAdapterBlockEntityFactory<E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> 
    {
        A create(@NotNull E blockEntity);
    }
}
