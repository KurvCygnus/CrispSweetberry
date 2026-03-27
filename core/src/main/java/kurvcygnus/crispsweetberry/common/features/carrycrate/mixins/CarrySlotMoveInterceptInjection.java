//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.mixins;

import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public final class CarrySlotMoveInterceptInjection
{
    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void preventCarryCratePlacement(@NotNull ItemStack stack, @NotNull CallbackInfoReturnable<Boolean> cir)
    {
        if(!stack.is(CarryCrateRegistries.CARRY_CRATE_ITEM.value()))
            return;
        
        final Slot thisSlot = (Slot) (Object) this;
        
        if(!(thisSlot.container instanceof Inventory))
            cir.setReturnValue(false);
    }
}
