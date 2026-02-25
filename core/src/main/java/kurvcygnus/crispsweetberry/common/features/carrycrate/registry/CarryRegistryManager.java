//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.registry;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.registry.ICarryEntityBlockRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public enum CarryRegistryManager implements ICarryEntityBlockRegistry
{
    INSTANCE;
    
    private final HashMap<BlockEntityType<?>, ICarryAdapterFactory<?, ?>> blockEntityRegistry = new HashMap<>();
    
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<E>> void register(
        @NotNull BlockEntityType<E> blockEntityType,
        @NotNull ICarryAdapterFactory<E, A> carryAdapterFactory
    )
    {
        Objects.requireNonNull(blockEntityType, "Param \"blockEntityType\" must not be null!");
        Objects.requireNonNull(carryAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        if(blockEntityRegistry.containsKey(blockEntityType))
            throw new IllegalStateException("BlockEntity \"%s\" has already been bounded with an adapter!".formatted(blockEntityType.toString()));
        
        blockEntityRegistry.put(blockEntityType, carryAdapterFactory);
    }
    
    @Override public <E extends BlockEntity, A extends AbstractBlockEntityCarryAdapter<? extends E>> void registerUniversalBlockEntityAdapter(
        @NotNull List<BlockEntityType<E>> blockEntityTypes,
        @NotNull ICarryAdapterFactory<E, A> carryAdapterFactory
    )
    {
        Objects.requireNonNull(blockEntityTypes, "Param \"blockEntityTypes\" must not be null!");
        Objects.requireNonNull(carryAdapterFactory, "Param \"carryAdapterFactory\" must not be null!");
        
        for(final BlockEntityType<?> blockEntityType: blockEntityTypes)
        {
            if(blockEntityRegistry.containsKey(blockEntityType))
                throw new IllegalStateException("BlockEntity \"%s\" has already been bounded with an adapter!".formatted(blockEntityType.toString()));
            
            blockEntityRegistry.put(blockEntityType, carryAdapterFactory);
        }
    }
    
    
    public @NotNull HashMap<BlockEntityType<?>, ICarryAdapterFactory<?, ?>> getBlockEntityRegistry() { return blockEntityRegistry; }
}
