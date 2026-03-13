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
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryFactor;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

//? TODO: Cover more edge cases.
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
final class CarryCrateInventoryMonitorEvent
{
    private static final float OVERWEIGHT_FACTOR = 1F;
    private static final float MAX_ACCEPTABLE_FACTOR = 1.5F;
    private static final Lazy<MobEffectInstance> OVERWEIGHT_INSTANCE = Lazy.of(
        () -> new MobEffectInstance(CarryCrateRegistries.OVERWEIGHT, MobEffectInstance.INFINITE_DURATION)
    );
    
    @SubscribeEvent static void pickupMonitor(@NotNull ItemEntityPickupEvent.Pre event)
    {
        final ItemStack itemstack = event.getItemEntity().getItem();
        
        if(itemstack.isEmpty() || !itemstack.is(CarryCrateRegistries.CARRY_CRATE_ITEM.value()) || !itemstack.has(CarryCrateRegistries.CARRY_CRATE_DATA.value()))
            return;
        
        final Player player = event.getPlayer();
        final float carryFactor = player.getData(CarryCrateRegistries.CARRY_FACTOR.get()).factor();
        
        if(carryFactor >= MAX_ACCEPTABLE_FACTOR)
        {
            event.setCanPickup(TriState.FALSE);
            return;
        }
        
        final CarryData carryData = itemstack.get(CarryCrateRegistries.CARRY_CRATE_DATA.value());
        Objects.requireNonNull(carryData, "Param \"carryData\" must not be null!");
        
        final float newFactor = carryFactor + calculateFactor(carryData);
        
        if(newFactor > MAX_ACCEPTABLE_FACTOR)
        {
            event.setCanPickup(TriState.FALSE);
            return;
        }
        
        player.setData(CarryCrateRegistries.CARRY_FACTOR.get(), new CarryFactor(newFactor));
        
        if(newFactor >= OVERWEIGHT_FACTOR)
        {
            player.addEffect(OVERWEIGHT_INSTANCE.get());
            return;
        }
        
        player.removeEffect(CarryCrateRegistries.OVERWEIGHT);
    }
    
    @SubscribeEvent static void tossMonitor(@NotNull ItemTossEvent event)
    {
        final ItemStack itemstack = event.getEntity().getItem();
        
        if(itemstack.isEmpty() || !itemstack.is(CarryCrateRegistries.CARRY_CRATE_ITEM.value()) || !itemstack.has(CarryCrateRegistries.CARRY_CRATE_DATA.value()))
            return;
        
        final Player player = event.getPlayer();
        final float oldFactor = player.getData(CarryCrateRegistries.CARRY_FACTOR.get()).factor();
        
        final CarryData data = itemstack.get(CarryCrateRegistries.CARRY_CRATE_DATA.value());
        Objects.requireNonNull(data, "Param \"data\" must not be null!");
        final float newFactor = Math.max(0F, oldFactor - calculateFactor(data));
        
        player.setData(CarryCrateRegistries.CARRY_FACTOR.get(), new CarryFactor(newFactor));
        
        if(newFactor <= OVERWEIGHT_FACTOR)
            player.removeEffect(CarryCrateRegistries.OVERWEIGHT);
    }
    
    @SubscribeEvent static void itemMoveIntercept(@NotNull PlayerContainerEvent event)
    {
        
    }
    
    private static float calculateFactor(@NotNull CarryData carryData) { return carryData.causesOverweight() ? 0.5F : 1F; }
}
