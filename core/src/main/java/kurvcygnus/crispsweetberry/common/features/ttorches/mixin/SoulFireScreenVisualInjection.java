//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection;
import kurvcygnus.crispsweetberry.common.features.ttorches.sync.SoulFireTagPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.SoulFireBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection.SOUL_FIRE_0;
import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection.getTextureByResourceLocation;

/**
 * This mixin belongs to a part of vanilla <u>{@link SoulFireBlock}</u>'s enhancement, 
 * it proves the screen visual effect.
 * <br><br>
 * Btw, it's compatible with resource packs UwU.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see SoulFireBurnInjection Start Logic
 * @see SoulFireExtinguishInjection Behave Logic
 * @see SoulFireVisualEffectInjection Appearance Visual
 * @see SoulFireTagPayloads.SoulFireTagPayloadHandler#attachTag Sync Handle 
 */
@Mixin(ScreenEffectRenderer.class)
public final class SoulFireScreenVisualInjection
{
    @ModifyVariable(method = "renderFire", at = @At("STORE"), ordinal = 0)
    private static TextureAtlasSprite renderSoulFire(TextureAtlasSprite textureatlassprite, @NotNull Minecraft minecraft, PoseStack poseStack)
    {
        final @Nullable Player player = minecraft.player;
        
        if(player == null)
            return textureatlassprite;
        
        if(TTorchUtilCollection.isLitBySoulFire(player))
            return getTextureByResourceLocation(SOUL_FIRE_0);
        
        return textureatlassprite;
    }
}
