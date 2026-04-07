//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AbstractEntityCarryAdapter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This is a view interface of Carry Registry's Manager.
 *
 * @author Kurv Cygnus
 * @apiNote This won't be directly used by externals, so if you want to see documents, please go to
 * <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.api.events.CarryAdapterRegisterEvent CarryAdapterRegisterEvent}</u>.
 * @implNote 
 *         You may think, <i>"Why do we keeping this? These generics have no actual usage, isn't it?"</i><br>
 *         <b>This is the facade of the whole registry module.</b> Despite generics don't have any actual usages, 
 *         they are the contract of this registry, and it doesn't actually affect the external usage, thus, we kept it.
 * @see kurvcygnus.crispsweetberry.common.features.carrycrate.api.events.CarryAdapterRegisterEvent Usage
 * @since 1.0 Release
 */
@ApiStatus.Internal
public interface ICarryRegistry
{
    <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>>
    void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    );
    
    <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>>
    void registerUniversal(
        @NotNull Set<? extends BlockEntityType<? extends E>> blockEntityTypes,
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
        @NotNull Set<? extends EntityType<? extends E>> entityTypes,
        @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory
    );
    
    @FunctionalInterface non-sealed interface ICarryBlockEntityAdapterFactory<E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>>
    extends IBaseCarryAdapterFactory<E, A> 
    {
        @Override @NotNull A create(E blockEntity);
        
        @Override default @NotNull CarryType getType() { return CarryType.BLOCK_ENTITY; }
    }
    
    @FunctionalInterface non-sealed interface ICarryBlockAdapterFactory<B extends Block, A extends AbstractBlockCarryAdapter<? extends B>>
    extends IBaseCarryAdapterFactory<B, A> 
    {
        @Override @NotNull A create(B block);
        
        @Override default @NotNull CarryType getType() { return CarryType.BLOCK; }
    }
    
    @FunctionalInterface non-sealed interface ICarryEntityAdapterFactory<E extends LivingEntity, A extends AbstractEntityCarryAdapter<? extends E>>
    extends IBaseCarryAdapterFactory<E, A>
    {
        @Override @NotNull A create(E entity);
        
        @Override default @NotNull CarryType getType() { return CarryType.ENTITY; }
    }
    
    sealed interface IBaseCarryAdapterFactory<C, A extends AbstractCarryAdapter<?>>
    {
        @NotNull A create(C object);
        @NotNull CarryType getType();
    }
}
