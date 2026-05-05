//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.constants;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This is a simple collection of universal <u>{@link DataComponentType}</u> serialization and <u>{@link AttachmentType}</u> sync's simple templates.
 * @since 1.0 Release
 */
public final class SerializationTemplates
{
    private SerializationTemplates() { throw new IllegalAccessError("Class \"SerializationTemplates\" is not meant to be instantized!"); }
    
    public static final Supplier<DataComponentType<Integer>> INT_TEMPLATE = buildSerializeTemplate(Codec.INT, ByteBufCodecs.INT);
    
    public static final Supplier<DataComponentType<Float>> FLOAT_TEMPLATE = buildSerializeTemplate(Codec.FLOAT, ByteBufCodecs.FLOAT);
    
    public static final Supplier<DataComponentType<Long>> LONG_TEMPLATE = buildSerializeTemplate(Codec.LONG, ByteBufCodecs.VAR_LONG);
    
    public static final Supplier<DataComponentType<Byte>> BYTE_TEMPLATE = buildSerializeTemplate(Codec.BYTE, ByteBufCodecs.BYTE);
    
    public static final Supplier<DataComponentType<Boolean>> BOOLEAN_TEMPLATE = buildSerializeTemplate(Codec.BOOL, ByteBufCodecs.BOOL);
    
    public static final Supplier<DataComponentType<Double>> DOUBLE_TEMPLATE = buildSerializeTemplate(Codec.DOUBLE, ByteBufCodecs.DOUBLE);
    
    public static final Supplier<DataComponentType<String>> STRING_TEMPLATE = buildSerializeTemplate(Codec.STRING, ByteBufCodecs.STRING_UTF8);
    
    public static final Function<Supplier<Integer>, Supplier<AttachmentType<Integer>>> INT_DATA_SYNCER = buildSyncTemplate(Codec.INT, ByteBufCodecs.INT);
    
    public static final Function<Supplier<Float>, Supplier<AttachmentType<Float>>> FLOAT_DATA_SYNCER = buildSyncTemplate(Codec.FLOAT, ByteBufCodecs.FLOAT);
    
    public static final Function<Supplier<Long>, Supplier<AttachmentType<Long>>> LONG_DATA_SYNCER = buildSyncTemplate(Codec.LONG, ByteBufCodecs.VAR_LONG);
    
    public static final Function<Supplier<Byte>, Supplier<AttachmentType<Byte>>> BYTE_DATA_SYNCER = buildSyncTemplate(Codec.BYTE, ByteBufCodecs.BYTE);
    
    public static final Function<Supplier<Boolean>, Supplier<AttachmentType<Boolean>>> BOOLEAN_DATA_SYNCER = buildSyncTemplate(Codec.BOOL, ByteBufCodecs.BOOL);
    
    public static final Function<Supplier<Double>, Supplier<AttachmentType<Double>>> DOUBLE_DATA_SYNCER = buildSyncTemplate(Codec.DOUBLE, ByteBufCodecs.DOUBLE);
    
    public static final Function<Supplier<String>, Supplier<AttachmentType<String>>> STRING_DATA_SYNCER = buildSyncTemplate(Codec.STRING, ByteBufCodecs.STRING_UTF8);
    
    private static <T> @NotNull Supplier<DataComponentType<T>> buildSerializeTemplate(@NotNull Codec<T> codec, @NotNull StreamCodec<ByteBuf, T> streamCodec)
        { return DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec)::build; }
    
    private static <T> @NotNull Function<Supplier<T>, Supplier<AttachmentType<T>>> buildSyncTemplate(
        @NotNull Codec<T> codec,
        @NotNull StreamCodec<ByteBuf, T> streamCodec
    ) { return t -> AttachmentType.builder(t).serialize(codec).sync(streamCodec)::build; }
}