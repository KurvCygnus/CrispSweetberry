//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin intercepts the mouse inputs when player is using Spyglass.
 * which is same as Vanilla.
 * @implNote You may think, <b>"Why not use <u>{@link net.neoforged.neoforge.event.entity.player.PlayerInteractEvent PlayerInteractEvents}</u>?"</b><br>
 * The answer is NO, you shouldn't do this. Despite the interception of use input works, attack does not.<br>
 * <b>Most events can only intercept the result, thus, block destroy will still have SFX, anim, particles if we use events</b>.<br>
 * Mixins can do things definitely correct, so we use it on all these aspects.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see Minecraft Source Target(Go to method startUseItem() & startAttack() & continueAttack(), they're private and can't be accessed by Javadoc)
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent Client Zoom Implementation
 * @see SpyglassPlayerStateInjection Essential Input Emulation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassUsePoseInjection Visual Essential Mixin
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemDegreeFixInjection Item Model Degree Fix
 * @see SpyglassPayloads Serverside stuff
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.server.events.SpyglassItemBoundaryCheckEvents Boundary Cases Handle
 */
@Mixin(Minecraft.class)
public final class SpyglassItemUsingInjection
{
    @Shadow public MultiPlayerGameMode gameMode;
    
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void interceptInteract(@NotNull CallbackInfo callbackInfo)
    {
        if(!SpyglassQuickZoomEvent.isZooming())
            return;
        
        callbackInfo.cancel();
    }
    
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void interceptAttack(CallbackInfoReturnable<Boolean> callbackInfoReturnable)
    {
        if(!SpyglassQuickZoomEvent.isZooming())
            return;
        
        callbackInfoReturnable.setReturnValue(false);
    }
    
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onContinueAttack(boolean leftClick, CallbackInfo callbackInfo)
    {
        if(!SpyglassQuickZoomEvent.isZooming())
            return;
        
        if(gameMode != null)
            gameMode.stopDestroyBlock();
        
        callbackInfo.cancel();
    }
}
