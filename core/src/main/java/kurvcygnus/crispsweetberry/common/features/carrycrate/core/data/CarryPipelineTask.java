//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core.data;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.utils.base.extension.StatedBlockPlaceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@ApiStatus.Internal
public final class CarryPipelineTask
{
    private final @NotNull CarryType type;
    private final @NotNull TriState listener;
    private final @NotNull TriState component;
    private final @NotNull TriState target;
    private final @NotNull ItemStack crate;
    private final @NotNull ServerLevel level;
    private final @NotNull ServerPlayer player;
    private final @NotNull BlockPos interactPos;
    private final @Nullable CarryData data;
    private final @Nullable CarryID id;
    private final @Nullable LivingEntity entity;
    private final @Nullable BlockEntityType<?> blockEntityType;
    private final @NotNull BiConsumer<CarryID, ICarryRegistry.IBaseCarryAdapterFactory<?, ?>> listenerInsert;
    private final @NotNull Consumer<CarryID> listenerRemove;
    private final @NotNull Consumer<ServerLevel> markDirty;
    private final @Nullable Function<BlockState, StatedBlockPlaceContext> placeContext;
    private @NotNull InteractionResult result;
    
    public CarryPipelineTask(
        @NotNull CarryType type,
        @NotNull TriState listener,
        @NotNull TriState component,
        @NotNull TriState target,
        @NotNull ItemStack crate,
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull BlockPos interactPos,
        @Nullable CarryData data,
        @Nullable CarryID id,
        @Nullable LivingEntity entity,
        @Nullable BlockEntityType<?> blockEntityType,
        @NotNull BiConsumer<CarryID, ICarryRegistry.IBaseCarryAdapterFactory<?, ?>> listenerInsert,
        @NotNull Consumer<CarryID> listenerRemove,
        @NotNull Consumer<ServerLevel> markDirty,
        @Nullable Function<BlockState, StatedBlockPlaceContext> placeContext,
        @NotNull InteractionResult result
    )
    {
        this.type = type;
        this.listener = listener;
        this.component = component;
        this.target = target;
        this.crate = crate;
        this.level = level;
        this.player = player;
        this.interactPos = interactPos;
        this.data = data;
        this.id = id;
        this.entity = entity;
        this.blockEntityType = blockEntityType;
        this.listenerInsert = listenerInsert;
        this.listenerRemove = listenerRemove;
        this.markDirty = markDirty;
        this.placeContext = placeContext;
        this.result = result;
    }
    
    private @NotNull CarryPipelineTask assignResultAndReturn(@NotNull InteractionResult result)
    {
        Objects.requireNonNull(result, "Param \"result\" must not be null!");
        this.result = result;
        return this;
    }
    
    public @NotNull CarryPipelineTask success() { return assignResultAndReturn(InteractionResult.SUCCESS); }
    
    public @NotNull CarryPipelineTask fail() { return assignResultAndReturn(InteractionResult.FAIL); }
    
    public @NotNull CarryPipelineTask pass() { return assignResultAndReturn(InteractionResult.PASS); }
    
    public @NotNull CarryType type() { return type; }
    
    public @NotNull TriState listener() { return listener; }
    
    public @NotNull TriState component() { return component; }
    
    public @NotNull TriState target() { return target; }
    
    public @NotNull ItemStack crate() { return crate; }
    
    public @NotNull ServerLevel level() { return level; }
    
    public @NotNull ServerPlayer player() { return player; }
    
    public @NotNull BlockPos interactPos() { return interactPos; }
    
    public @Nullable CarryData data() { return data; }
    
    public @Nullable CarryID id() { return id; }
    
    public @Nullable LivingEntity entity() { return entity; }
    
    public @Nullable BlockEntityType<?> blockEntityType() { return blockEntityType; }
    
    public @NotNull BiConsumer<CarryID, ICarryRegistry.IBaseCarryAdapterFactory<?, ?>> listenerInsert() { return listenerInsert; }
    
    public @NotNull Consumer<CarryID> listenerRemove() { return listenerRemove; }
    
    public @Nullable Function<BlockState, StatedBlockPlaceContext> placeContext() { return placeContext; }
    
    public @NotNull InteractionResult result() { return result; }
    
    public void markDirty() { this.markDirty.accept(level); }
    
    @Override public boolean equals(@Nullable Object obj)
    {
        return this == obj ||
            obj instanceof CarryPipelineTask that &&
                type == that.type &&
                listener == that.listener &&
                component == that.component &&
                target == that.target &&
                Objects.equals(crate, that.crate) &&
                Objects.equals(level, that.level) &&
                Objects.equals(player, that.player) &&
                Objects.equals(interactPos, that.interactPos) &&
                Objects.equals(data, that.data) &&
                Objects.equals(id, that.id) &&
                Objects.equals(entity, that.entity) &&
                Objects.equals(blockEntityType, that.blockEntityType) &&
                Objects.equals(listenerInsert, that.listenerInsert) &&
                Objects.equals(listenerRemove, that.listenerRemove) &&
                Objects.equals(markDirty, that.markDirty) &&
                Objects.equals(placeContext, that.placeContext) &&
                result == that.result;
    }
    
    @Override public int hashCode()
    {
        return Objects.hash(
            type,
            listener,
            component,
            target,
            crate,
            level,
            player,
            interactPos,
            data,
            id,
            entity,
            blockEntityType,
            listenerInsert,
            listenerRemove,
            markDirty,
            placeContext,
            result
        );
    }
    
    @Override public @NotNull String toString()
    {
        return """
            
            CarryPipelineTask
            {
                type: %s
                listener: %s
                component: %s
                target: %s
                crate: %s
                level: %s
                player: %s
                interactPos: %s
                data: %s
                id: %s
                entity: %s
                blockEntityType: %s
                listenerInsert: %s
                listenerRemove: %s
                markDirty: %s
                placeContext: %s
                result: %s
            }
            """.
            formatted(
                type,
                listener,
                component,
                target,
                crate,
                level,
                player,
                interactPos,
                data,
                id,
                entity,
                blockEntityType,
                listenerInsert,
                listenerRemove,
                markDirty,
                placeContext,
                result
            );
    }
}
