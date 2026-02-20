//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SoulFireBlock;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.SOUL_FIRE_0;
import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.getTextureByResourceLocation;

/**
 * This mixin belongs to a part of vanilla <u>{@link SoulFireBlock}</u>'s enhancement, 
 * it makes sure that <u>{@link Entity}</u>'s appearance is blue fire, not the standard one.
 * <br><br>
 * Btw, it's compatible with resource packs UwU.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see SoulFireBurnInjection Start Logic
 * @see SoulFireExtinguishInjection Behave Logic
 * @see SoulFireScreenVisualInjection Screen Visual
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.sync.SoulFireTagPayloadHandler#attachTag Sync Handle 
 */
@Mixin(EntityRenderDispatcher.class)
public final class SoulFireVisualEffectInjection
{
    @Unique
    private static final ResourceLocation SOUL_FIRE_1 = ResourceLocation.withDefaultNamespace("block/soul_fire_1");
    
    @ModifyVariable(method = "renderFlame", at = @At("STORE"), ordinal = 0)
    private TextureAtlasSprite modifyFireSprite0(TextureAtlasSprite textureatlassprite, PoseStack poseStack, MultiBufferSource buffer, @NotNull Entity entity)
    {
        if(TTorchConstants.isLitBySoulFire(entity))
            return getTextureByResourceLocation(SOUL_FIRE_0);
            
        return textureatlassprite;
    }
    
    @ModifyVariable(method = "renderFlame", at = @At(value = "STORE"), ordinal = 1)
    private TextureAtlasSprite soulFireOverlayAlt(TextureAtlasSprite textureatlassprite1, PoseStack poseStack, MultiBufferSource buffer, @NotNull Entity entity)
    {
        if(TTorchConstants.isLitBySoulFire(entity))
            return getTextureByResourceLocation(SOUL_FIRE_1);
        
        return textureatlassprite1;
    }
}
