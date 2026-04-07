//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync;

import io.netty.buffer.ByteBuf;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassPlayerStateInjection;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.constants.ExampleSlotConstants;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * This is the collection of spyglass's item exchange sync stuff.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class SpyglassPayloads
{
    public static final String ORIGINAL_SLOT_TAG = DefinitionUtils.createPersistentTag("spyglass_exchange_slot_index");
    
    /**
     * This is a custom packet, which is used for telling server to emulate player's item use state, and item exchange.
     * @author Kurv Cygnus
     * @apiNote This is a <b>Client to Server packet</b>, which is registered at
     * <u>{@link #registerPacket(RegisterPayloadHandlersEvent)}</u>.<br>
     * <b><i>Misuse will lead to game crash</i></b>.
     * @since 1.0 Release
     */
    public record SpyglassPayload(boolean isPressed) implements CustomPacketPayload
    {
        public static final Type<SpyglassPayload> TYPE = new Type<>(DefinitionUtils.getModNamespacedLocation("qol/spyglass/packet"));
        
        public static final StreamCodec<ByteBuf, SpyglassPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL,
            SpyglassPayload::isPressed,
            SpyglassPayload::new
        );
        
        @Override public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
    }
    
    /**
     * This handles player's using attachTag declaration, and the logic of item exchange.
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @see kurvcygnus.crispsweetberry.common.qol.spyglass.server.events.SpyglassItemBoundaryCheckEvents Boundary Cases Handle
     * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemUsingInjection Essential Input Intercept
     * @see SpyglassPlayerStateInjection Essential Input Emulation
     * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassUsePoseInjection Visual Essential Mixin
     * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemDegreeFixInjection Item Model Degree Fix
     * @see kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent Client Zoom Implementation
     */
    static final class SpyglassPayloadHandler
    {
        private static void handleData(@NotNull SpyglassPayloads.SpyglassPayload data, @NotNull IPayloadContext context)
        {
            context.enqueueWork(() ->
                {
                    final ServerPlayer player = (ServerPlayer) context.player();
                    final Inventory inventory = player.getInventory();
                    
                    if(data.isPressed())
                    {
                        final int slot = inventory.findSlotMatchingItem(Items.SPYGLASS.getDefaultInstance());
                        
                        if(slot != ExampleSlotConstants.ERROR && slot != Inventory.SLOT_OFFHAND)
                        {
                            player.getPersistentData().putInt(ORIGINAL_SLOT_TAG, slot);
                            
                            final ItemStack spyglass = inventory.getItem(slot);
                            final ItemStack oldOffhand = player.getOffhandItem();
                            
                            player.setItemInHand(InteractionHand.OFF_HAND, spyglass);
                            player.awardStat(Stats.ITEM_USED.get(Items.SPYGLASS));
                            inventory.setItem(slot, oldOffhand);
                            player.startUsingItem(InteractionHand.OFF_HAND);
                        }
                    }
                    else if(player.getPersistentData().contains(ORIGINAL_SLOT_TAG))
                    {
                        player.stopUsingItem();
                        final int originalSlot = player.getPersistentData().getInt(ORIGINAL_SLOT_TAG);
                        
                        final ItemStack currentOffhand = player.getOffhandItem();
                        final ItemStack itemInOriginalSlot = inventory.getItem(originalSlot);
                        
                        player.setItemInHand(InteractionHand.OFF_HAND, itemInOriginalSlot);
                        inventory.setItem(originalSlot, currentOffhand);
                        
                        player.getPersistentData().remove(ORIGINAL_SLOT_TAG);
                    }
                }
            );
        }
    }
    
    @SubscribeEvent static void registerPacket(@NotNull RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar("1.0 Release");
        
        registrar.playToServer(
            SpyglassPayload.TYPE,
            SpyglassPayload.CODEC,
            SpyglassPayloadHandler::handleData
        );
    }
}
