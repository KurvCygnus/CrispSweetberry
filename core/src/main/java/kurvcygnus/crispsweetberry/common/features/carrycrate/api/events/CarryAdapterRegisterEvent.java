//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.events;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.registry.ICarryRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class CarryAdapterRegisterEvent extends Event implements ICarryRegistry
{
    private final ICarryRegistry carryRegistry;
    
    public CarryAdapterRegisterEvent(@NotNull ICarryRegistry carryRegistry) { this.carryRegistry = carryRegistry; }
    
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>>
    void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    ) { this.carryRegistry.register(blockEntityType, carryAdapterBlockEntityFactory); }
    
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> 
    void registerUniversal(
        @NotNull Set<BlockEntityType<? extends E>> blockEntityTypes,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    ) { this.carryRegistry.registerUniversal(blockEntityTypes, carryAdapterBlockEntityFactory); }
    
    @Override public <B extends Block, A extends AbstractBlockCarryAdapter<B>> 
    void register(
        @NotNull B block,
        @NotNull ICarryRegistry.ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory
    ) { this.carryRegistry.register(block, carryAdapterBlockAdapterFactory); }
    
    @Override public <B extends Block, A extends AbstractBlockCarryAdapter<? extends B>>
    void registerUniversal(
        @NotNull Set<? extends B> blocks,
        @NotNull ICarryRegistry.ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory
    ) { this.carryRegistry.registerUniversal(blocks, carryAdapterBlockAdapterFactory); }
    
    @Override public <E extends LivingEntity, A extends AbstractEntityCarryAdapter<E>>
    void register(
        @NotNull EntityType<E> entityType,
        @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory
    ) { this.carryRegistry.register(entityType, carryEntityAdapterFactory); }
    
    @Override public <E extends LivingEntity, A extends AbstractEntityCarryAdapter<? extends E>>
    void registerUniversal(
        @NotNull Set<EntityType<? extends E>> entityTypes,
        @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory
    ) { this.carryRegistry.registerUniversal(entityTypes, carryEntityAdapterFactory); }
}
