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
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine;
import kurvcygnus.crispsweetberry.common.features.carrycrate.events.CarryCrateCopyProcessor;
import kurvcygnus.crispsweetberry.lib.base.extensions.BaseNestedPrinter;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import kurvcygnus.crispsweetberry.lib.base.lang.IVault;
import kurvcygnus.crispsweetberry.utils.constants.SerializationTemplates;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A data object that represents the identity of boxed Carry Crate.
 *
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
@ApiStatus.Internal
public final class CarryID extends BaseNestedPrinter<CarryID>
{
    private static final Function<CarryID, String> ID_GETTER = i -> i.id;
    private static final Function<CarryID, String> UUID_GETTER = i -> i.uuid;
    
    private static final Codec<CarryID> CODEC = RecordCodecBuilder.create(
        inst -> inst.group(
            Codec.STRING.fieldOf("id").forGetter(ID_GETTER),
            Codec.STRING.fieldOf("uuid").forGetter(UUID_GETTER)
        ).apply(inst, CarryID::new)
    );
    private static final StreamCodec<ByteBuf, CarryID> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.STRING_UTF8, ID_GETTER,
        ByteBufCodecs.STRING_UTF8, UUID_GETTER,
        CarryID::new
    );
    
    public static final Supplier<DataComponentType<CarryID>> SERIALIZATION_DEF = SerializationTemplates.buildSerializeTemplate(CODEC, STREAM_CODEC);
    
    @ApiStatus.Internal public static final
    IVault<
        BiFunction<
            String,
            String,
            CarryID
        >,
        Optional<Event>
    > __$1NT3RNAL_R3ST0R3$__ =
        IVault.ofAccessLimited(
            CarryID::new,
            Optional.empty(),
            ($, __) ->
                __.map(___ -> ___ instanceof ServerStartedEvent || ___ instanceof ScreenEvent.MouseButtonPressed.Pre).orElse(false),
            CarryEngine.class,
            CarryCrateCopyProcessor.class
        );
    
    private static final @NotNull @Unmodifiable INestedFieldMap<CarryID> FIELD_MAP = INestedPrintable.buildFieldMap(
        map -> { map.put("Recovery ID", ID_GETTER); map.put("UUID", UUID_GETTER); },
        2
    );
    
    public final @NotNull String id;
    public final @NotNull String uuid;
    
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
    
    @Override public boolean equals(Object obj)
    {
        return obj == this || obj instanceof CarryID that &&
            Objects.equals(this.id, that.id) &&
            Objects.equals(this.uuid, that.uuid);
    }
    
    @Override public int hashCode() { return Objects.hash(id, uuid); }
    
    @Override public @NotNull @Unmodifiable INestedFieldMap<CarryID> getFields() { return FIELD_MAP; }
}
