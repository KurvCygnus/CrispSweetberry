//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.events;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.registry.ICarryEntityBlockRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class CarryAdapterRegisterEvent extends Event
{
    private final ICarryEntityBlockRegistry entityBlockRegistry;
    
    public CarryAdapterRegisterEvent(@NotNull ICarryEntityBlockRegistry entityBlockRegistry) { this.entityBlockRegistry = entityBlockRegistry; }
    
    public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>> void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryEntityBlockRegistry.ICarryAdapterBlockEntityFactory<E, A> carryAdapterBlockEntityFactory
    ) { this.entityBlockRegistry.register(blockEntityType, carryAdapterBlockEntityFactory); }
    
    public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> void registerUniversalBlockEntityAdapter(
        @NotNull List<BlockEntityType<? extends E>> blockEntityTypes,
        @NotNull ICarryEntityBlockRegistry.ICarryAdapterBlockEntityFactory<E, A> carryAdapterBlockEntityFactory
    ) { this.entityBlockRegistry.registerUniversalBlockEntityAdapter(blockEntityTypes, carryAdapterBlockEntityFactory); }
}
