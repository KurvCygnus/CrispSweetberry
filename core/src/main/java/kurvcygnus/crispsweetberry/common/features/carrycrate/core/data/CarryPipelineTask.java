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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView;
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

/**
 * This is a mutable data class which is used in the
 * <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine#interact(CarryInteractContextCollection.ICarryInteractContext) CarryEngine#interact(ICarryInteractContext)}</u>'s
 * post-process phase.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @implNote
 * <h4><b>Some simple Q&A:</b></h4>
 * <ul>
 *     <li>
 *         Isn't this data class <b>too bloat</b>? It has 16 fields, that's not just a few!
 *         <hr>
 *         It's not, because all of these fields are essential in interact logics. <b>Choping these fields into
 *         smaller fields only makes the post-process's context passing more troublesome. Also, field {@code listenerInsert},
 *         {@code listenerRemove} and {@code placeContext} has already got more decoupling effort than directly passing those raw types.</b>
 *     </li>
 *     <li>
 *         Why not using <b><u>{@link Record}</u></b>, or at least <b>immutable data class</b>?
 *         <hr>
 *         The post-process logic implementation is a typical example of <u><a href="https://fsharpforfunandprofit.com/rop/"><b>Railway Oriented Programming</b> </a></u>, which produces some use-then-discard objects,
 *         thus, if this is immutable, mutating {@code result} will creating a new object, which obviously increases the stress of garbage collector, so
 *         we decide to take mutable class, which can be passed as a reference, with no those problems.
 *     </li>
 * </ul>
 */
@ApiStatus.Internal
public final class CarryPipelineTask
{
    private final CarryType type;
    private final TriState listener;
    private final TriState component;
    private final TriState target;
    private final ItemStack crate;
    private final ServerLevel level;
    private final ServerPlayer player;
    private final BlockPos interactPos;
    private final @Nullable CarryData data;
    private final @Nullable CarryID id;
    private final @Nullable LivingEntity entity;
    private final @Nullable BlockEntityType<?> blockEntityType;
    private final BiConsumer<CarryID, ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> listenerInsert;
    private final Consumer<CarryID> listenerRemove;
    private final @Nullable Function<BlockState, StatedBlockPlaceContext> placeContext;
    private InteractionResult result;
    
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
        @NotNull BiConsumer<CarryID, ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> listenerInsert,
        @NotNull Consumer<CarryID> listenerRemove,
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
        this.placeContext = placeContext;
        this.result = result;
    }
    
    private @NotNull CarryPipelineTask assignResultAndReturn(@NotNull InteractionResult result)
    {
        Objects.requireNonNull(result, "Param \"result\" must not be null!");
        this.result = result;
        return this;
    }
    
    //! Using SUCCESS may lead to [[ItemStack#shrink]].
    public @NotNull CarryPipelineTask success() { return assignResultAndReturn(InteractionResult.SUCCESS_NO_ITEM_USED); }
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
    public @NotNull BiConsumer<CarryID, ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> listenerInsert() { return listenerInsert; }
    public @NotNull Consumer<CarryID> listenerRemove() { return listenerRemove; }
    public @Nullable Function<BlockState, StatedBlockPlaceContext> placeContext() { return placeContext; }
    public @NotNull InteractionResult result() { return result; }
    
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
                result
            );
    }
}
