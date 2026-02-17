//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.mixin;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public final class SoulFireBurnInjection
{
    @Inject(method = "entityInside", at = @At("HEAD"))
    private void soulFire(BlockState state, Level level, BlockPos pos, @NotNull Entity entity, @NotNull CallbackInfo callbackInfo)
    {
        final BaseFireBlock block = (BaseFireBlock)(Object) this;
        
        if(block instanceof SoulFireBlock)
            entity.getPersistentData().putByte(TTorchConstants.SOUL_FIRE_PERSISTENT_TAG, (byte) 1);
    }
}
