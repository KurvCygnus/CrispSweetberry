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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * A data object that represents the identity of boxed Carry Crate.
 *
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
@ApiStatus.Internal
public final class CarryID implements INestedPrintable
{
    private static final Codec<CarryID> CODEC = RecordCodecBuilder.create(
        inst -> inst.group(
            Codec.STRING.fieldOf("id").forGetter(CarryID::id),
            Codec.STRING.fieldOf("uuid").forGetter(CarryID::uuid)
        ).apply(inst, CarryID::new)
    );
    private static final StreamCodec<ByteBuf, CarryID> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, CarryID::id,
        ByteBufCodecs.STRING_UTF8, CarryID::uuid,
        CarryID::new
    );
    
    public static final Supplier<DataComponentType<CarryID>> SERIALIZATION_DEF =
        DataComponentType.<CarryID>builder().persistent(CODEC).networkSynchronized(STREAM_CODEC)::build;
    
    private final @NotNull String id;
    private final @NotNull String uuid;
    
    private CarryID(@NotNull String id, @NotNull String uuid)
    {
        this.id = id;
        this.uuid = uuid;
    }
    
    public static @NotNull CarryID create(@NotNull ResourceLocation id, @NotNull UUID uuid)
    {
        return new CarryID(
            Objects.requireNonNull(id, "Param \"id\" must not be null!").toString(),
            Objects.requireNonNull(uuid, "Param \"uuid\" must not be null!").toString().replace("-", "")
        );
    }
    
    @ApiStatus.Internal
    public static @NotNull CarryID restore(
        @NotNull String id,
        @NotNull String uuid,
        @NotNull Map<CarryType, Map<CarryID, ? extends ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>>> access,
        @NotNull ServerStartedEvent access2
    )
    {
        Objects.requireNonNull(access, "External Usage is not allowed!");
        Objects.requireNonNull(access2, "External Usage is not allowed!");
        assert id != null : "Param \"id\" must not be null!";
        assert uuid != null : "Param \"uuid\" must not be null!";
        return new CarryID(id, uuid);
    }
    
    @Override public @NotNull String toString() { return toNestedString(); }
    
    public @NotNull String id() { return id; }
    
    public @NotNull String uuid() { return uuid; }
    
    @Override public boolean equals(Object obj)
    {
        return obj == this || obj instanceof CarryID that &&
            Objects.equals(this.id, that.id) &&
            Objects.equals(this.uuid, that.uuid);
    }
    
    @Override public int hashCode() { return Objects.hash(id, uuid); }
    
    @Override public @NotNull @Unmodifiable Map<String, Supplier<@Nullable Object>> getFields() { return Map.of("Recovery ID", this::id, "UUID", this::uuid); }
}
