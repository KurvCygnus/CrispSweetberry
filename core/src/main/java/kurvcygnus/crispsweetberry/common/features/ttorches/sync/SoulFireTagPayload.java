//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.sync;

import io.netty.buffer.ByteBuf;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record SoulFireTagPayload(int targetId, boolean attachTag) implements CustomPacketPayload
{
    public static final Type<SoulFireTagPayload> TYPE = new Type<>(CrispDefUtils.getModNamespacedLocation("features/ttorch/packet"));
    
    public static final StreamCodec<ByteBuf, SoulFireTagPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.INT, SoulFireTagPayload::targetId,
        ByteBufCodecs.BOOL, SoulFireTagPayload::attachTag,
        SoulFireTagPayload::new
    );
    
    @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
