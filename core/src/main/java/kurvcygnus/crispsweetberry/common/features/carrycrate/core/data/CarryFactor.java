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

public record CarryFactor(float factor)
{
    public static final Codec<CarryFactor> CODEC = RecordCodecBuilder.create(
        inst -> inst.group(Codec.FLOAT.fieldOf("factor").forGetter(CarryFactor::factor)).
            apply(inst, CarryFactor::new)
    );
    
    public static final StreamCodec<ByteBuf, CarryFactor> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        CarryFactor::factor,
        CarryFactor::new
    );
}
