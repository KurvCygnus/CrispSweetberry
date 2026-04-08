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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.OverweightEffect;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateConstants.MAX_ACCEPTABLE_FACTOR;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
final class CarryCrateInventoryMonitorEvent
{
    @SubscribeEvent static void gamemodeMonitor(@NotNull PlayerEvent.PlayerChangeGameModeEvent event)
        { OverweightEffect.updateFactorAndEffect(event.getEntity(), null, TriState.DEFAULT); }
    
    @SubscribeEvent static void pickupPreMonitor(@NotNull ItemEntityPickupEvent.Pre event)
    {
        final ItemStack itemStack = getCrateWithCheck(event.getItemEntity()::getItem);
        
        if(itemStack == null)
            return;
        
        final Player player = event.getPlayer();
        final boolean isInteractable = !(player.isCreative() || player.isSpectator());
        final float carryFactor = player.getData(CarryCrateRegistries.CARRY_FACTOR.get());
        
        if(!(carryFactor >= MAX_ACCEPTABLE_FACTOR))
            return;
        
        FunctionalUtils.doIf(isInteractable, () -> event.setCanPickup(TriState.FALSE));
    }
    
    /**
     * @implNote Using <u>{@link ItemEntityPickupEvent.Post}</u> instead of <u>{@link ItemEntityPickupEvent.Pre}</u> since
     * the later one is fired as long as the drop is in the reach of player, which causes a disaster.
     */
    @SubscribeEvent static void pickupPostMonitor(@NotNull ItemEntityPickupEvent.Post event)
    {
        final ItemStack itemstack = getCrateWithCheck(event::getCurrentStack);
        
        if(itemstack == null)
            return;
        
        final Player player = event.getPlayer();
        final boolean isInteractable = !(player.isCreative() || player.isSpectator());
        final float carryFactor = player.getData(CarryCrateRegistries.CARRY_FACTOR.get());
        
        if(carryFactor >= MAX_ACCEPTABLE_FACTOR)
        {
            FunctionalUtils.doIf(isInteractable, () -> dropItem(player, itemstack));
            return;
        }
        
        final CarryData data = itemstack.get(CarryCrateRegistries.CARRY_CRATE_DATA.value());
        Objects.requireNonNull(data, "Param \"data\" must not be null!");
        
        OverweightEffect.updateFactorAndEffect(player, data, TriState.TRUE, () -> dropItem(player, itemstack));
    }
    
    @SubscribeEvent static void tossMonitor(@NotNull ItemTossEvent event)
    {
        final ItemStack itemstack = getCrateWithCheck(event.getEntity()::getItem);
        
        if(itemstack == null)
            return;
        
        final Player player = event.getPlayer();
        final CarryData data = itemstack.get(CarryCrateRegistries.CARRY_CRATE_DATA.value());
        Objects.requireNonNull(data, "Param \"data\" must not be null!");
        
        OverweightEffect.updateFactorAndEffect(player, data, TriState.FALSE);
    }
    
    private static @Nullable ItemStack getCrateWithCheck(@NotNull Supplier<ItemStack> stackSupplier)
    {
        final ItemStack itemstack = stackSupplier.get();
        if(itemstack.isEmpty() || !itemstack.is(CarryCrateRegistries.CARRY_CRATE_ITEM.value()) || !itemstack.has(CarryCrateRegistries.CARRY_CRATE_DATA.value()))
            return null;
        
        return itemstack;
    }
    
    private static void dropItem(@NotNull Player player, @NotNull ItemStack itemstack)
    {
        final BlockPos pos = player.getOnPos();
        Containers.dropItemStack(player.level(), pos.getX(), pos.getY(), pos.getZ(), itemstack);
    }
}
