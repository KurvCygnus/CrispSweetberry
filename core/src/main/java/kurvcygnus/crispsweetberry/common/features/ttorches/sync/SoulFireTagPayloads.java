//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.sync;

import io.netty.buffer.ByteBuf;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * This is the collection of soulFire mixins' CS sync stuff.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class SoulFireTagPayloads
{
    /**
     * This is a custom packet, which is used for <u>{@link TTorchUtilCollection#SOUL_FIRE_PERSISTENT_TAG}</u>'s attach, and deattach.
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @apiNote This is a <b>Server to Client packet</b>, which is registered at 
     * <u>{@link kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries TTorchRegistries}</u>.<br>
     * <b><i>Misuse will lead to game crash</i></b>.
     */
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
    
    /**
     * This handles the sync for soul fire's tag persistent.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public static final class SoulFireTagPayloadHandler
    {
        /**
         * Attaches, or deattaches the soul fire tag on the entity.
         */
        public static void attachTag(@NotNull SoulFireTagPayloads.SoulFireTagPayload packet, @NotNull IPayloadContext context)
        {
            context.enqueueWork(() ->
                {
                    final Entity target = context.player().level().getEntity(packet.targetId());
                    
                    if(target == null)
                        return;
                    
                    if(packet.attachTag())
                    {
                        if(target.getPersistentData().contains(TTorchUtilCollection.SOUL_FIRE_PERSISTENT_TAG))
                            return;
                        
                        target.getPersistentData().putByte(TTorchUtilCollection.SOUL_FIRE_PERSISTENT_TAG, (byte) 1);
                    }
                    else
                    {
                        if(!target.getPersistentData().contains(TTorchUtilCollection.SOUL_FIRE_PERSISTENT_TAG))
                            return;
                        
                        target.getPersistentData().remove(TTorchUtilCollection.SOUL_FIRE_PERSISTENT_TAG);
                    }
                }
            );
        }
    }
}
