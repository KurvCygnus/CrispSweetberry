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
import kurvcygnus.crispsweetberry.utils.base.trait.IMixinCaster;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SoulFireBlock;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection.SOUL_FIRE_PERSISTENT_TAG;
import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection.isLitBySoulFire;

/**
 * This mixin belongs to a part of vanilla <u>{@link SoulFireBlock}</u>'s enhancement, 
 * it processes anti-water logics, and the sequence of fire extinguish.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see SoulFireBurnSynchronizer Start Logic
 * @see SoulFireScreenVisualRender Screen Visual
 * @see SoulFireVisualEffectRender Appearance Visual
 * @see SoulFireTagPayloads.SoulFireTagPayloadHandler#attachTag Sync Handle 
 */
@Mixin(Entity.class)
public abstract class SoulFireExtinguishManager implements IMixinCaster<Entity>
{
    /**
     * Keeps entity's fire.<br>
     * Don't ask the name of this method. It's a secret to everybody.
     */
    @Inject(method = "clearFire", at = @At("HEAD"), cancellable = true)
    private void pkFire(@NotNull CallbackInfo callbackInfo)
    {
        final Entity entity = _$csb_lib_getSelf();

        if(_$csb_willExtinguish(entity))
            callbackInfo.cancel();
        else if(!entity.level().isClientSide && isLitBySoulFire(entity))
        {
            entity.getPersistentData().remove(SOUL_FIRE_PERSISTENT_TAG);

            PacketDistributor.sendToPlayersTrackingEntityAndSelf(
                entity,
                new SoulFireTagPayloads.SoulFireTagPayload(
                    entity.getId(),
                    false
                )
            );
        }
    }
    
    @Inject(method = "isFullyFrozen", at = @At("HEAD"), cancellable = true)
    private void wontBeColdAnymore(@NotNull CallbackInfoReturnable<Boolean> callbackInfoReturnable)
        { _$csb_lib_doWhenSelf(TTorchUtilCollection::isLitBySoulFire, () -> callbackInfoReturnable.setReturnValue(false)); }
    
    @Inject(method = "getTicksRequiredToFreeze", at = @At("HEAD"), cancellable = true)
    private void itsHot(@NotNull CallbackInfoReturnable<Integer> callbackInfoReturnable)//* Yes, I'middle lazy.
        { _$csb_lib_doWhenSelf(SoulFireExtinguishManager::_$csb_willExtinguish, () -> callbackInfoReturnable.setReturnValue(Integer.MAX_VALUE)); }
    
    @Inject(method = "baseTick", at = @At("HEAD"))
    private void clearUpTag(@NotNull CallbackInfo callbackInfo)
    {
        final Entity entity = _$csb_lib_getSelf();
        
        if(entity.level().isClientSide || entity.getRemainingFireTicks() > 0)
            return;
        
        entity.getPersistentData().remove(SOUL_FIRE_PERSISTENT_TAG);
        
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(
            entity,
            new SoulFireTagPayloads.SoulFireTagPayload(
                entity.getId(),
                false
            )
        );
    }
    
    @Inject(method = "setRemainingFireTicks", at = @At("HEAD"), cancellable = true)
    private void refuseSetFireTicks(@NotNull CallbackInfo callbackInfo)
    {
        final Entity entity = _$csb_lib_getSelf();
        
        if(entity.level().isClientSide)
            return;
        
        if(!entity.level().isClientSide && entity.getRemainingFireTicks() <= 0)
        {
            //! Don't send packets here. This will cause CS competitive state, which behaves as flashing sprites.
            entity.getPersistentData().remove(SOUL_FIRE_PERSISTENT_TAG);
            return;
        }
        
        if(_$csb_willExtinguish(entity))
            callbackInfo.cancel();
    }
    
    @Inject(method = "playEntityOnFireExtinguishedSound", at = @At("HEAD"), cancellable = true)
    private void interceptSoundPlay(@NotNull CallbackInfo callbackInfo) { _$csb_lib_doWhenSelf(SoulFireExtinguishManager::_$csb_willExtinguish, callbackInfo::cancel); }
    
    @Unique private static boolean _$csb_willExtinguish(@NotNull Entity entity) { return entity.isInWaterRainOrBubble() && isLitBySoulFire(entity); }
}
