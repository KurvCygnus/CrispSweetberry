//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.qol.spyglass.client.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassPlayerStateInjection;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayload;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpyglassItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries.SPYGLASS_ZOOM;
import static kurvcygnus.crispsweetberry.utils.definitions.SoundConstants.NORMAL_SOUND_PITCH;
import static kurvcygnus.crispsweetberry.utils.definitions.SoundConstants.NORMAL_SOUND_VOLUME;
import static kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants.NAN;

/**
 * This handles clientside zoom stuff.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @implNote <h3><b>Some implementation Q&A:</b></h3>
 * <ul>
 *     <li>
 *         <b>Q: Why do we manually swap the spyglass to the off-hand instead of just simulating a use packet?</b><br>
 *         A: Minecraft's server is authoritative and stubborn. An item must be physically held in a hand to be "used". 
 *         We swap it to the off-hand to avoid interrupting the main-hand's interaction flow, but this requires 
 *         meticulous state tracking to return the item to its original slot once the key is released.
 *    </li>
 *    <li>
 *         <b>Q: Why not just call <u>{@link net.minecraft.client.multiplayer.MultiPlayerGameMode#useItem MultiPlayerGameMode.useItem}</u> and be done with it?</b><br>
 *         A: Because the game's zoom FOV logic is detached from the "item using" state in a way that assumes the 
 *         Right-Click mouse button is being held. Even if the server thinks you are using the item, the client-side 
 *         camera won't zoom unless we manually override the FOV modifier and inject into the <u>{@link Player#isScoping()}</u> check.
 *    </li>
 *    <li>
 *         <b>Q: Why do we trigger sounds and state changes on the client immediately instead of waiting for a server response?</b><br>
 *         A: Input latency. In Minecraft, 1 tick is 50ms. If we send a packet and wait for the server to swap the item 
 *         and sync it back, the player perceives a "heavy" delay. We perform a "client-side prediction" of the 
 *         zoom state and sound to make the response feel instantaneous, even though the "real" inventory swap 
 *         is happening on the server a few milliseconds later.
 *    </li>
 *    <li>
 *         <b>Q: What's with the <u>{@link SpyglassPlayerStateInjection SpyglassPlayerStateInjection}</u>?</b><br>
 *         A: This is the most counter-intuitive part. The game's <u>{@link Player#isScoping()}</u> method specifically checks 
 *         if the player is using a Spyglass AND if the <i>use-item keybind</i> (Right Click) is pressed. Since our 
 *         hotkey isn't Right Click, the vanilla logic returns {@code false}, breaking the overlay and 
 *         movement slowdown. We have to force-feed the game a {@code true} value when our hotkey is down.
 *    </li>
 *    <li>
 *         <b>Q: Why do we need to cancel {@code Minecraft.startUseItem()} in Mixins?</b><br>
 *         A: To prevent "input leakage." If a player is holding our zoom hotkey and accidentally right-clicks, 
 *         the game might try to use the main-hand item (like eating or placing a block) while zoomed. We use 
 *         <u>{@link kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemUsingInjection SpyglassItemUsingInjection}</u> to 
 *         effectively "mute" standard interactions while the quick-zoom is active.
 *    </li>
 * </ul>
 * @see SpyglassPayloadHandler#handleData Serverside stuff
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.server.events.SpyglassItemBoundaryCheckEvents Boundary Cases Handle
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemUsingInjection Essential Input Intercept
 * @see SpyglassPlayerStateInjection Essential Input Emulation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassUsePoseInjection Visual Essential Mixin
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemDegreeFixInjection Item Model Degree Fix
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE, value = Dist.CLIENT)
public final class SpyglassQuickZoomEvent
{
    private static final float SPYGLASS_MOVEMENT_FACTOR = 0.2F;
    private static ZoomState zoomState = ZoomState.IDLE;
    private static boolean hasSpyglass = false;
    private static boolean hotkeyPressed = false;
    
    private enum ZoomState
    {
        IDLE,
        ZOOMING,
        RELEASED
    }
    
    @SubscribeEvent
    static void spyglassZoom(@NotNull ClientTickEvent.Post event)
    {
        final Minecraft instance = Minecraft.getInstance();
        final LocalPlayer player = instance.player;
        
        if(instance.gameMode == null || player == null)
            return;
        
        if(player.isUsingItem() && player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof SpyglassItem)
                return;//! No repeat use.
        
        hotkeyPressed = SPYGLASS_ZOOM.isDown();
        
        hasSpyglass = player.getInventory().offhand.getFirst().is(Items.SPYGLASS) ||//! Slot match should be the later one, because it brings more performance penalty.
            player.getInventory().findSlotMatchingItem(Items.SPYGLASS.getDefaultInstance()) != NAN;
        
        if(isZooming())
            switch(zoomState)
            {
                case IDLE ->
                {
                    player.playSound(SoundEvents.SPYGLASS_USE, NORMAL_SOUND_VOLUME, NORMAL_SOUND_PITCH);
                    PacketDistributor.sendToServer(new SpyglassPayload(true));
                    instance.gameMode.useItem(player, InteractionHand.OFF_HAND);
                    zoomState = ZoomState.ZOOMING;
                }
                case ZOOMING -> {}
                case RELEASED -> zoomState = ZoomState.IDLE;
            }
        else switch(zoomState)
        {
            case ZOOMING ->
            {
                player.playSound(SoundEvents.SPYGLASS_STOP_USING, NORMAL_SOUND_VOLUME, NORMAL_SOUND_PITCH);
                zoomState = ZoomState.RELEASED;
                PacketDistributor.sendToServer(new SpyglassPayload(false));
            }
            case RELEASED -> zoomState = ZoomState.IDLE;
            default -> { }
        }
    }
    
    @SubscribeEvent
    static void zoom(@NotNull ComputeFovModifierEvent event)
    {
        if(!isZooming()) 
            return;
        
        final Minecraft instance = Minecraft.getInstance();
        
        if(!instance.options.getCameraType().isFirstPerson()) 
            return;
        
        event.setNewFovModifier(SpyglassItem.ZOOM_FOV_MODIFIER);
    }
    
    @SubscribeEvent
    static void speedEdit(@NotNull MovementInputUpdateEvent event)//? TODO: Behavior mismatch. Vanilla won't stop sprinting immediately after using.
    {
        if(!isZooming())
            return;
        
        event.getInput().leftImpulse *= SPYGLASS_MOVEMENT_FACTOR;
        event.getInput().forwardImpulse *= SPYGLASS_MOVEMENT_FACTOR;
        
        if(event.getEntity().isSprinting())
            event.getEntity().setSprinting(false);
    }
    
    public static boolean isZooming() { return hotkeyPressed && hasSpyglass; }
}
