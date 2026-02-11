//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.utils.ui.collects.CrispIntRanger;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity.HIT_STD_EXTEND_FIRE_TICKS;
import static kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity.HIT_STD_MAX_TICKS;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
final class ThrowableTorchZombieEvent
{
    private static final CrispIntRanger DEEPEST_RANGE = CrispIntRanger.closed(-64, -24);
    private static final CrispIntRanger DEEP_RANGE = CrispIntRanger.closed(-23, 16);
    private static final CrispIntRanger NORMAL_RANGE = CrispIntRanger.closed(17, 320);
    
    private static final List<CrispIntRanger> RANGERS = List.of(
        DEEPEST_RANGE,
        DEEP_RANGE,
        NORMAL_RANGE
    );
    
    @SubscribeEvent
    static void onNewZombieSpawn(@NotNull EntityJoinLevelEvent event)
    {
        if(event.isCanceled())
            return;
        
        final Entity entity = event.getEntity();
        
        if(entity instanceof Zombie zombie)
        {
            if(!zombie.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
                return;
            
            final Level level = event.getLevel();
            final RandomSource random = level.getRandom();
            
            if(random.nextFloat() > 0.05)
                return;
            
            final int count;
            
            switch(CrispIntRanger.inRangers(entity.getBlockY(), RANGERS))
            {
                case 0 -> count = random.nextInt(7) + 1;
                case 1 -> count = random.nextInt(5) + 1;
                default -> count = random.nextInt(3) + 1;
            }
            
            final ItemStack throwableTorch = TTorchRegistries.THROWABLE_TORCH.value().getDefaultInstance();
            
            throwableTorch.setCount(count);
            
            zombie.setItemInHand(InteractionHand.MAIN_HAND, throwableTorch);
            zombie.setDropChance(EquipmentSlot.MAINHAND, 1F);
        }
    }
    
    @SubscribeEvent
    static void throwableTorchLit(@NotNull LivingDamageEvent.Pre event)
    {
        final Entity entity = event.getSource().getEntity();
        
        if(entity instanceof Zombie zombie && zombie.getItemInHand(InteractionHand.MAIN_HAND).is(TTorchRegistries.THROWABLE_TORCH))
        {
            int fireTick = event.getEntity().getRemainingFireTicks();
            fireTick = Math.min(fireTick + HIT_STD_EXTEND_FIRE_TICKS, HIT_STD_MAX_TICKS);
            
            event.getEntity().setRemainingFireTicks(fireTick);
        }
    }
}
