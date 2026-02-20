//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries;
import kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin emulates the spyglass using attachTag.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see Player#isScoping() Source Target
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent Client Zoom Implementation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemUsingInjection Essential Input Intercept
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassUsePoseInjection Visual Essential Mixin
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemDegreeFixInjection Item Model Degree Fix
 * @see SpyglassPayloadHandler#handleData Serverside stuff
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.server.events.SpyglassItemBoundaryCheckEvents Boundary Cases Handle
 */
@Mixin(Player.class)
public final class SpyglassPlayerStateInjection
{
    @Inject(method = "isScoping", at = @At("RETURN"), cancellable = true)
    private void scopeInject(CallbackInfoReturnable<Boolean> cir)
    {
        if(SpyglassClientRegistries.SPYGLASS_ZOOM.isDown() && SpyglassQuickZoomEvent.isZooming())
            cir.setReturnValue(true);
    }
}
