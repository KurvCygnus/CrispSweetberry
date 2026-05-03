//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.mixins;

import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.utils.base.trait.IMixinCaster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerMenu.class)
public abstract class CarryContainerQuickMoveInterceptor implements IMixinCaster<AbstractContainerMenu>
{
    @Inject(method = "doClick", at = @At("HEAD"), cancellable = true)
    private void noQuickMove(int slotId, int button, @NotNull ClickType clickType, @NotNull Player player, @NotNull CallbackInfo callbackInfo)
    {
        if(_$csb_lib_testSelf(InventoryMenu.class::isInstance) || clickType != ClickType.QUICK_MOVE || slotId < 0)
            return;
        
        _$csb_lib_doWhenSelf(menu -> menu.getSlot(slotId).getItem().is(CarryCrateRegistries.CARRY_CRATE_ITEM.value()), callbackInfo::cancel);
    }
}
