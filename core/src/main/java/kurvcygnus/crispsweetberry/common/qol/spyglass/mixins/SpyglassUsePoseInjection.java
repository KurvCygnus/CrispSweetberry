//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * This mixin makes the player who is using spyglass to do corresponded pose.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see PlayerRenderer Source Taget(Go to method getArmPose(), it is private and can't be accessed by Javadoc) 
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent Client Zoom Implementation
 * @see SpyglassPlayerStateInjection Essential Input Emulation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemUsingInjection Input Interception Stuff
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemDegreeFixInjection Item Model Degree Fix
 * @see SpyglassPayloadHandler#handleData Serverside Stuff
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.server.events.SpyglassItemBoundaryCheckEvents Boundary Cases Handle
 */
@Mixin(PlayerRenderer.class)
public final class SpyglassUsePoseInjection
{
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)
    private static void armPoseInject(@NotNull AbstractClientPlayer player, @NotNull InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> callback)
    {
        //! Explain: #getArmPose sets both two arms' pose. If no hand detect, both two hands will be SPYGLASS, which is similar to bow's pose.
        if(Objects.equals(hand, InteractionHand.OFF_HAND) && player.isScoping())
            callback.setReturnValue(HumanoidModel.ArmPose.SPYGLASS);
    }
}
