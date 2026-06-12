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
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A simple enum for type matching, factory querying, and serialization.
 *
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
@ApiStatus.Internal public enum CarryType implements StringRepresentable
{
    BLOCK_ENTITY(CarryData.CarryBlockEntityDataHolder.CODEC, CarryData.CarryBlockEntityDataHolder.STREAM_CODEC, BlockEntityType.class),
    BLOCK(CarryData.CarryBlockDataHolder.CODEC,              CarryData.CarryBlockDataHolder.STREAM_CODEC,       Block.class),
    ENTITY(CarryData.CarryEntityDataHolder.CODEC,            CarryData.CarryEntityDataHolder.STREAM_CODEC,      EntityType.class);
    
    final MapCodec<? extends CarryData.CarryDataBaseHolder> codec;
    final StreamCodec<ByteBuf, ? extends CarryData.CarryDataBaseHolder> streamCodec;
    public final Class<?> boundClass;
    
    CarryType(
        @NotNull MapCodec<? extends CarryData.CarryDataBaseHolder> codec,
        @NotNull StreamCodec<ByteBuf, ? extends CarryData.CarryDataBaseHolder> streamCodec,
        @NotNull Class<?> boundClass
    )
    {
        this.codec       = codec;
        this.streamCodec = streamCodec;
        this.boundClass  = boundClass;
    }
    
    @Override public @NotNull String getSerializedName() { return this.name().toLowerCase(); }
    
    static final Codec<CarryType> CODEC = StringRepresentable.fromEnum(CarryType::values);
}
