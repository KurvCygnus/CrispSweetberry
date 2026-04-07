//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.CarryBlockEntityInteractHandler;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.CarryBlockInteractHandler;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.CarryEntityInteractHandler;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import kurvcygnus.crispsweetberry.utils.base.extension.StatedBlockPlaceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/**
 * A simple enum for type matching, factory querying, and serialization.
 *
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
@ApiStatus.Internal
public enum CarryType implements StringRepresentable
{
    @SuppressWarnings("DataFlowIssue")//! See [[CarryType#createHandler()]]. It grantees the null safety for BLOCK_ENTITY.
    BLOCK_ENTITY(
        CarryData.CarryBlockEntityDataHolder.CODEC,
        CarryData.CarryBlockEntityDataHolder.STREAM_CODEC,
        CarryBlockEntityInteractHandler::new,
        BlockEntityType.class
    ),
    @SuppressWarnings("DataFlowIssue")//! See [[CarryType#createHandler()]]. It grantees the null safety for BLOCK.
    BLOCK(
        CarryData.CarryBlockDataHolder.CODEC,
        CarryData.CarryBlockDataHolder.STREAM_CODEC,
        CarryBlockInteractHandler::new,
        Block.class
    ),
    @SuppressWarnings("DataFlowIssue")//! See [[CarryType#createHandler()]]. It grantees the null safety for ENTITY.
    ENTITY(
        CarryData.CarryEntityDataHolder.CODEC,
        CarryData.CarryEntityDataHolder.STREAM_CODEC,
        CarryEntityInteractHandler::new,
        EntityType.class
    );
    
    private final MapCodec<? extends CarryData.CarryDataBaseHolder> codec;
    private final StreamCodec<ByteBuf, ? extends CarryData.CarryDataBaseHolder> streamCodec;
    private final ICarryInteractHandlerFactory<?> handler;
    private final Class<?> boundClass;
    
    CarryType(
        @NotNull MapCodec<? extends CarryData.CarryDataBaseHolder> codec,
        @NotNull StreamCodec<ByteBuf, ? extends CarryData.CarryDataBaseHolder> streamCodec,
        @NotNull ICarryInteractHandlerFactory<?> handler,
        @NotNull Class<?> boundClass
    )
    {
        this.codec = codec;
        this.streamCodec = streamCodec;
        this.handler = handler;
        this.boundClass = boundClass;
    }
    
    public @NotNull AbstractCarryInteractHandler createHandler(
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull ItemStack carryCrate,
        @Nullable BlockPos targetPos,
        @Nullable BlockState targetState,
        @Nullable LivingEntity targetEntity,
        @Nullable BlockEntity targetBlockEntity,
        @Nullable UseOnContext context,
        @Nullable CarryID optionalUUID
    )
    {
        FunctionalUtils.doIf(
            this == BLOCK_ENTITY, () ->
                Objects.requireNonNull(targetBlockEntity, "Param \"targetBlockEntity\" must not be null!")
        );
        
        if(this != ENTITY)
        {
            Objects.requireNonNull(targetPos, "Param \"targetPos\" must not be null!");
            Objects.requireNonNull(targetState, "Param \"targetState\" must not be null!");
            Objects.requireNonNull(context, "Param \"context\" must not be null!");
        }
        else
            Objects.requireNonNull(targetEntity, "Param \"targetEntity\" must not be null!");
        
        return this.handler.create(
            level,
            player,
            carryCrate,
            targetPos,
            targetState,
            targetEntity,
            targetBlockEntity,
            context == null ?
                null :
                blockState -> new StatedBlockPlaceContext(context, blockState),
            optionalUUID
        );
    }
    
    @Override public @NotNull String getSerializedName() { return this.name().toLowerCase(); }
    
    public @NotNull MapCodec<? extends CarryData.CarryDataBaseHolder> codec() { return codec; }
    
    public @NotNull StreamCodec<ByteBuf, ? extends CarryData.CarryDataBaseHolder> streamCodec() { return streamCodec; }
    
    public @NotNull Class<?> boundClass() { return boundClass; }
    
    public static final Codec<CarryType> CODEC = StringRepresentable.fromEnum(CarryType::values);
    
    @FunctionalInterface
    interface ICarryInteractHandlerFactory<H extends AbstractCarryInteractHandler>
    {
        @NotNull H create(
            @NotNull ServerLevel level,
            @NotNull ServerPlayer player,
            @NotNull ItemStack carryCrate,
            @Nullable BlockPos targetPos,
            @Nullable BlockState targetState,
            @Nullable LivingEntity targetEntity,
            @Nullable BlockEntity targetBlockEntity,
            @Nullable Function<BlockState, StatedBlockPlaceContext> contextGenerator,
            @Nullable CarryID optionalUUID
        );
    }
}
