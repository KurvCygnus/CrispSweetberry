//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.SOUL_FIRE_PERSISTENT_TAG;

@Mixin(Entity.class)
public final class SoulFireExtinguishInjection
{
    @Inject(method = "extinguishFire", at = @At("HEAD"), cancellable = true)
    private void keepFire(CallbackInfo callbackInfo)
    {
        final Entity entity = (Entity)(Object)this;
        
        if(entity.isInWaterRainOrBubble() && entity.getPersistentData().contains(SOUL_FIRE_PERSISTENT_TAG))
            callbackInfo.cancel();
        else
            entity.getPersistentData().remove(SOUL_FIRE_PERSISTENT_TAG);
    }
}
