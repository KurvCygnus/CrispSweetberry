//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.mixins;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection;
import kurvcygnus.crispsweetberry.common.features.ttorches.sync.SoulFireTagPayloads;
import kurvcygnus.crispsweetberry.lib.base.trait.IMixinCaster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin belongs to a part of vanilla <u>{@link SoulFireBlock}</u>'s enhancement, 
 * it adds tag to entity, starts the new behave logic.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownSoulTorchEntity Another tag trigger
 * @see SoulFireExtinguishManager Behave Logics
 * @see SoulFireScreenVisualRender Screen Visual
 * @see SoulFireVisualEffectRender Appearance Visual
 * @see SoulFireTagPayloads.SoulFireTagPayloadHandler#attachTag Sync handle 
 */
@Mixin(BaseFireBlock.class)
public abstract class SoulFireBurnSynchronizer implements IMixinCaster<BaseFireBlock>
{
    @Inject(method = "entityInside", at = @At("HEAD"))
    private void soulFire(BlockState state, @NotNull Level level, BlockPos pos, @NotNull Entity entity, @NotNull CallbackInfo callbackInfo)
    {
        if(level.isClientSide || !_$csb_lib_testSelf(SoulFireBlock.class::isInstance) || TTorchUtilCollection.isLitBySoulFire(entity))
            return;
        
        entity.getPersistentData().putByte(TTorchUtilCollection.SOUL_FIRE_PERSISTENT_TAG, (byte) 1);
        
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
            entity,
            new SoulFireTagPayloads.SoulFireTagPayload(
                entity.getId(),
                true
            )
        );
    }
}
