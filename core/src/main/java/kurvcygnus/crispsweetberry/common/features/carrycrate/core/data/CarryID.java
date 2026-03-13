//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record CarryID(@NotNull String id, @NotNull String uuid)
{
    public static final Codec<CarryID> CODEC = RecordCodecBuilder.create(inst -> 
        inst.group(
            Codec.STRING.fieldOf("id").forGetter(CarryID::id),
            Codec.STRING.fieldOf("uuid").forGetter(CarryID::uuid)
        ).apply(inst, CarryID::new)
    );
    
    public static final StreamCodec<ByteBuf, CarryID> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, CarryID::id,
        ByteBufCodecs.STRING_UTF8, CarryID::uuid,
        CarryID::new
    );
    
    public CarryID
    {
        Objects.requireNonNull(id, "Param \"id\" must not be null!");
        Objects.requireNonNull(uuid, "Param \"uuid\" must not be null!");
    }
}
