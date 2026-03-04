//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.registry;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface ICarryRegistry
{
    <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>> 
    void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    );
    
    <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> 
    void registerUniversal(
        @NotNull Set<BlockEntityType<? extends E>> blockEntityTypes,
        @NotNull ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    );
    
    <B extends Block, A extends AbstractBlockCarryAdapter<B>>
    void register(
        @NotNull B block,
        @NotNull ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory
    );
    
    <B extends Block, A extends AbstractBlockCarryAdapter<? extends B>> 
    void registerUniversal(
        @NotNull Set<? extends B> blocks,
        @NotNull ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory
    );
    
    <E extends LivingEntity, A extends AbstractEntityCarryAdapter<E>>
    void register(
        @NotNull EntityType<E> entityType,
        @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory
    );
    
    <E extends LivingEntity, A extends AbstractEntityCarryAdapter<? extends E>>
    void registerUniversal(
        @NotNull Set<EntityType<? extends E>> entityTypes,
        @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory
    );
    
    @FunctionalInterface interface ICarryBlockEntityAdapterFactory<E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>>
    extends IBaseCarryAdapterFactory<E, A> { @Override A create(E blockEntity); }
    
    @FunctionalInterface interface ICarryBlockAdapterFactory<B extends Block, A extends AbstractBlockCarryAdapter<? extends B>>
    extends IBaseCarryAdapterFactory<B, A> { @Override A create(B block); }
    
    @FunctionalInterface interface ICarryEntityAdapterFactory<E extends LivingEntity, A extends AbstractEntityCarryAdapter<? extends E>> 
    extends IBaseCarryAdapterFactory<E, A> { @Override A create(E entity); }
    
    @FunctionalInterface interface IBaseCarryAdapterFactory<C, A extends AbstractCarryAdapter> { A create(C object); }
}
