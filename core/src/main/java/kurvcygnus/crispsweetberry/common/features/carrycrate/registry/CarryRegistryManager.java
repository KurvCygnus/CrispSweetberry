//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.registry;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.registry.ICarryRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public enum CarryRegistryManager implements ICarryRegistry
{
    INSTANCE;
    
    private boolean freezeRecoveryAccess = false;
    
    private final HashMap<
        BlockEntityType<? extends BlockEntity>,
        ICarryBlockEntityAdapterFactory<? extends BlockEntity, ? extends AbstractBlockEntityCarryAdapter<? extends BlockEntity>>
        >
        blockEntityRegistry = new HashMap<>();
    
    private final HashMap<ResourceLocation, ICarryBlockEntityAdapterFactory<? extends BlockEntity, ? extends AbstractBlockEntityCarryAdapter<? extends BlockEntity>>>
        recoveryBlockEntityRegistry = new HashMap<>();
    
    private final HashMap<
        Block,
        ICarryBlockAdapterFactory<? extends Block, ? extends AbstractBlockCarryAdapter<? extends Block>>
        >
        blockRegistry = new HashMap<>();
    
    private final HashMap<ResourceLocation, ICarryBlockAdapterFactory<? extends Block, ? extends AbstractBlockCarryAdapter<? extends Block>>> 
        recoveryBlockRegistry = new HashMap<>();
    
    private final HashMap<
        EntityType<? extends LivingEntity>,
        ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<? extends LivingEntity>>
        >
        entityRegistry = new HashMap<>();
    
    private final HashMap<ResourceLocation, ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<? extends LivingEntity>>>
        recoveryEntityRegistry = new HashMap<>();
    
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>> void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    )
    {
        requireNonNull(blockEntityType, "Param \"blockEntityType\" must not be null!");
        requireNonNull(BlockEntityType.getKey(blockEntityType), "Param \"blockEntityType\"'s ResourceLocation must not be null!");
        requireNonNull(carryAdapterBlockEntityFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        if(blockEntityRegistry.containsKey(blockEntityType))
            throw new IllegalStateException("BlockEntity \"%s\" has already been bounded with an adapter!".formatted(blockEntityType.toString()));
        
        blockEntityRegistry.put(blockEntityType, carryAdapterBlockEntityFactory);
        recoveryBlockEntityRegistry.put(BlockEntityType.getKey(blockEntityType), carryAdapterBlockEntityFactory);
    }
    
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> void registerUniversal(
        @NotNull Set<BlockEntityType<? extends E>> blockEntityTypes,
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, A> carryAdapterBlockEntityFactory
    )
    {
        requireNonNull(blockEntityTypes, "Param \"blockEntityTypes\" must not be null!");
        requireNonNull(carryAdapterBlockEntityFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final BlockEntityType<? extends E> blockEntityType : blockEntityTypes)
        {
            requireNonNull(blockEntityType, "\"blockEntityType\" must not be null!");
            requireNonNull(BlockEntityType.getKey(blockEntityType), "Param \"blockEntityType\"'s ResourceLocation must not be null!");
            if(blockEntityRegistry.containsKey(blockEntityType))
                throw new IllegalStateException("BlockEntity \"%s\" has already been bounded with an adapter!".formatted(blockEntityType.toString()));
            
            blockEntityRegistry.put(blockEntityType, carryAdapterBlockEntityFactory);
            recoveryBlockEntityRegistry.put(BlockEntityType.getKey(blockEntityType), carryAdapterBlockEntityFactory);
        }
    }
    
    @Override public <B extends Block, A extends AbstractBlockCarryAdapter<B>> 
    void register(@NotNull B block, @NotNull ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory)
    {
        requireNonNull(block, "Param \"block\" must not be null!");
        requireNonNull(BuiltInRegistries.BLOCK.getKey(block), "Param \"block\"'s ResourceLocation must not be null!");
        requireNonNull(carryAdapterBlockAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        if(blockRegistry.containsKey(block))
            throw new IllegalStateException("Block \"%s\" has already been bounded with an adapter!".formatted(block.getDescriptionId()));
        
        blockRegistry.put(block, carryAdapterBlockAdapterFactory);
        recoveryBlockRegistry.put(BuiltInRegistries.BLOCK.getKey(block), carryAdapterBlockAdapterFactory);
    }
    
    @Override public <B extends Block, A extends AbstractBlockCarryAdapter<? extends B>> 
    void registerUniversal(@NotNull Set<? extends B> blocks, @NotNull ICarryBlockAdapterFactory<B, A> carryAdapterBlockAdapterFactory)
    {
        requireNonNull(blocks, "Param \"blocks\" must not be null!");
        requireNonNull(carryAdapterBlockAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final B block: blocks)
        {
            requireNonNull(block, "Param \"block\" must not be null!");
            requireNonNull(BuiltInRegistries.BLOCK.getKey(block), "Param \"block\"'s ResourceLocation must not be null!");
            if(blockRegistry.containsKey(block))
                throw new IllegalStateException("Block \"%s\" has already been bounded with an adapter!".formatted(block.getDescriptionId()));
            
            blockRegistry.put(block, carryAdapterBlockAdapterFactory);
            recoveryBlockRegistry.put(BuiltInRegistries.BLOCK.getKey(block), carryAdapterBlockAdapterFactory);
        }
    }
    
    @Override public <E extends LivingEntity, A extends AbstractEntityCarryAdapter<E>>
    void register(@NotNull EntityType<E> entityType, @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory)
    {
        requireNonNull(entityType, "Param \"entityType\" must not be null!");
        requireNonNull(EntityType.getKey(entityType), "Param \"entityType\"'s ResourceLocation must not be null!");
        requireNonNull(carryEntityAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        if(entityRegistry.containsKey(entityType))
            throw new IllegalStateException("Entity \"%s\" has already been bounded with an adapter!".formatted(entityType.getDescriptionId()));
        
        entityRegistry.put(entityType, carryEntityAdapterFactory);
        recoveryEntityRegistry.put(EntityType.getKey(entityType), carryEntityAdapterFactory);
    }
    
    @Override public <E extends LivingEntity, A extends AbstractEntityCarryAdapter<? extends E>> 
    void registerUniversal(@NotNull Set<EntityType<? extends E>> entityTypes, @NotNull ICarryEntityAdapterFactory<E, A> carryEntityAdapterFactory)
    {
        requireNonNull(entityTypes, "Param \"entityTypes\" must not be null!");
        requireNonNull(carryEntityAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final EntityType<? extends E> entityType: entityTypes)
        {
            requireNonNull(entityType, "Param \"entityType\" must not be null!");
            requireNonNull(EntityType.getKey(entityType), "Param \"entityType\"'s ResourceLocation must not be null!");
            
            if(entityRegistry.containsKey(entityType))
                throw new IllegalStateException("Entity \"%s\" has already been bounded with an adapter!".formatted(entityType.getDescriptionId()));
            
            entityRegistry.put(entityType, carryEntityAdapterFactory);
            recoveryEntityRegistry.put(EntityType.getKey(entityType), carryEntityAdapterFactory);
        }
    }
    
    public @NotNull HashMap<
        BlockEntityType<? extends BlockEntity>,
        ICarryBlockEntityAdapterFactory<? extends BlockEntity, ? extends AbstractBlockEntityCarryAdapter<?>>
        >
    getBlockEntityRegistry() { return blockEntityRegistry; }
    
    public @NotNull HashMap<
        Block,
        ICarryBlockAdapterFactory<? extends Block, ? extends AbstractBlockCarryAdapter<? extends Block>>
        > 
    getBlockRegistry() { return blockRegistry; }
    
    public @NotNull HashMap<
        EntityType<? extends LivingEntity>,
        ICarryEntityAdapterFactory<? extends LivingEntity, ? extends AbstractEntityCarryAdapter<? extends LivingEntity>>
        > 
    getEntityRegistry() { return entityRegistry; }
    
    public @NotNull HashMap<
        ResourceLocation,
        ICarryBlockEntityAdapterFactory<
            ? extends BlockEntity,
            ? extends AbstractBlockEntityCarryAdapter<? extends BlockEntity>
            >
        > 
    getRecoveryBlockEntityRegistry()
    {
        if(this.freezeRecoveryAccess)
            throw new IllegalStateException("Accessing frozen recovery registries after listeners' recovery is not allowed!");
        return recoveryBlockEntityRegistry;
    }
    
    public @NotNull HashMap<
        ResourceLocation,
        ICarryBlockAdapterFactory<
            ? extends Block,
            ? extends AbstractBlockCarryAdapter<? extends Block>
            >
        >
    getRecoveryBlockRegistry()
    {
        if(this.freezeRecoveryAccess)
            throw new IllegalStateException("Accessing frozen recovery registries after listeners' recovery is not allowed!");
        
        return recoveryBlockRegistry;
    }
    
    public @NotNull HashMap<
        ResourceLocation,
        ICarryEntityAdapterFactory<
            ? extends LivingEntity,
            ? extends AbstractEntityCarryAdapter<? extends LivingEntity>
            >
        >
    getRecoveryEntityRegistry()
    {
        if(this.freezeRecoveryAccess)
            throw new IllegalStateException("Accessing frozen recovery registries after listeners' recovery is not allowed!");
        
        return recoveryEntityRegistry;
    }
    
    public void freezeRecoveryAccess() 
    {
        if(this.freezeRecoveryAccess)
            return;
        
        this.freezeRecoveryAccess = true;
        this.recoveryBlockRegistry.clear();
        this.recoveryEntityRegistry.clear();
        this.recoveryBlockEntityRegistry.clear();
    }
}
