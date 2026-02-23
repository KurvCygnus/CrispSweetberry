//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloads;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin fixes the issue of incorrect degree of spyglass when player is using it by <u>{@link SpyglassQuickZoomEvent quick zooming}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see PlayerItemInHandLayer Source Target(Go to method renderArmWithItem(), it is protected and can't be accessed by Javadoc) 
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent Client Zoom Implementation
 * @see SpyglassPlayerStateInjection Essential Input Emulation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemUsingInjection Input Interception Stuff
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassUsePoseInjection Visual Essential Mixin
 * @see SpyglassPayloads Serverside Stuff
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.server.events.SpyglassItemBoundaryCheckEvents Boundary Cases Handle
 */
@Mixin(PlayerItemInHandLayer.class)
public abstract class SpyglassItemDegreeFixInjection
{
    @Shadow protected abstract void renderArmWithSpyglass
        (LivingEntity entity, ItemStack stack, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int combinedLight);
    
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void degreeAdjust(
        @NotNull LivingEntity livingEntity,
        ItemStack itemStack,
        ItemDisplayContext displayContext,
        HumanoidArm arm,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        @NotNull CallbackInfo callbackInfo
    )
    {
        //! LivingEntity#getUsedItemHand is unreliable.
        final boolean isOffhand = arm == (livingEntity.getMainArm() == HumanoidArm.LEFT ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
        
        //! Didn't use isZooming() here since dong that will make visual effect clientside only.
        if(livingEntity instanceof Player player && player.isScoping() && isOffhand)
        {
            renderArmWithSpyglass(livingEntity, itemStack, arm, poseStack, buffer, packedLight);
            callbackInfo.cancel();
        }
    }
}
