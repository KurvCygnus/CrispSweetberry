//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class CarryCrateCopyProcessor
{
    private static final IMarkLogger LOGGER = IMarkLogger.markedLogger("CARRY_PERSISTENT");
    
    @SubscribeEvent static void onScreenMouseClick(ScreenEvent.MouseButtonPressed.@NotNull Pre event)
    {
        if(event.getButton() != GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
            return;
        
        final var mc = Minecraft.getInstance();
        
        if(!(mc.screen instanceof AbstractContainerScreen<?> containerScreen))
            return;
        
        final var hoveredSlot = containerScreen.getSlotUnderMouse();
        
        if(hoveredSlot == null || !hoveredSlot.hasItem())
            return;
        
        final var originalStack = hoveredSlot.getItem();
        
        if(
            !originalStack.is(CarryCrateRegistries.CARRY_CRATE_ITEM.value()) ||
            !originalStack.has(CarryCrateRegistries.CARRY_ID.value()) ||
            !originalStack.has(CarryCrateRegistries.CARRY_CRATE_DATA.value())
        ) return;
        
        LOGGER.debug("Stack \"{}\" meets the comndition. Taking over the Copy Logic.", originalStack);
        event.setCanceled(true);
        
        final var builder = CarryID.__$1NT3RNAL_R3ST0R3$__.tryGet(Optional.of(event));
        final var clonedStack = originalStack.copy();
        final var carryID = clonedStack.get(CarryCrateRegistries.CARRY_ID.get());
        final var data = clonedStack.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        
        //! Both two value non-nullability are granted by [[DataComponentHolder#has]].
        assert carryID != null;
        assert data != null;
        
        final var newID = builder.apply(carryID.id(), UUID.randomUUID().toString().replace("-", ""));
        final var type = data.carryType();
        
        LOGGER.debug("New CarryID generated: {}\nTrying to add it to listener lookup with CarryType {}.", newID, type);
        
        clonedStack.set(CarryCrateRegistries.CARRY_ID.get(), newID);
        CarryEngine.INSERT_ACCESS.tryGet(Optional.empty()).accept(type, carryID, newID);
        
        //! This event will only be fired during gaming, which means [[Minecraft#gameMode]] always exists.
        assert mc.gameMode != null;
        mc.gameMode.handleCreativeModeItemAdd(clonedStack, 36 + containerScreen.getMenu().getStateId());
    }
}
