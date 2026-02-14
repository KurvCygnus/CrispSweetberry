//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.qol.spyglass.server.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassPlayerStateInjection;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler.ORIGINAL_SLOT_TAG;

/**
 * This makes sure player's item won't be corrupted when they touched boundary cases, like logout, or death.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see SpyglassPayloadHandler#handleData Serverside stuff
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemUsingInjection Essential Input Intercept
 * @see SpyglassPlayerStateInjection Essential Input Emulation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassUsePoseInjection Visual Essential Mixin
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemDegreeFixInjection Item Model Degree Fix
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent Client Zoom Implementation
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class SpyglassItemBoundaryCheckEvents
{
    @SubscribeEvent
    static void deathCheck(@NotNull LivingDeathEvent event)
    {
        if(event.isCanceled())
            return;
        
        final LivingEntity entity = event.getEntity();
        
        if(!(entity instanceof Player player && player.getPersistentData().contains(ORIGINAL_SLOT_TAG)))
            return;
        
        emergencyCleanup(player, player.getPersistentData().getInt(ORIGINAL_SLOT_TAG));
    }
    
    @SubscribeEvent
    static void logoutCheck(@NotNull PlayerEvent.PlayerLoggedOutEvent event)
    {
        final Player player = event.getEntity();
        if(!player.getPersistentData().contains(ORIGINAL_SLOT_TAG))
            return;
        
        emergencyCleanup(player, player.getPersistentData().getInt(ORIGINAL_SLOT_TAG));
    }
    
    private static void emergencyCleanup(@NotNull Player player, int originalSlotIndex)
    {
        final Inventory playerInventory = player.getInventory();
        final ItemStack spyglass = playerInventory.offhand.getFirst();
        
        player.setItemInHand(InteractionHand.OFF_HAND, playerInventory.getItem(originalSlotIndex));
        playerInventory.setItem(originalSlotIndex, spyglass);
    }
}
