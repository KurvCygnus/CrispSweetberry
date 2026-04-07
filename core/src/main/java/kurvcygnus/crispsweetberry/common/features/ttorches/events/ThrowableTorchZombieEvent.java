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
import kurvcygnus.crispsweetberry.utils.base.datastructure.CrispRangeMap;
import kurvcygnus.crispsweetberry.utils.base.datastructure.CrispRanger;
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

import static kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity.HIT_STD_EXTEND_FIRE_TICKS;
import static kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity.HIT_STD_MAX_TICKS;

/**
 * This event makes zombie can summon with <u>{@link kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableTorchItem Throwable Torch}</u>, 
 * making the game experience more immersive.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
final class ThrowableTorchZombieEvent
{
    private static final CrispRanger DEEPEST_RANGE = CrispRanger.closed(-64, -24);
    private static final CrispRanger DEEP_RANGE = CrispRanger.closed(-23, 16);
    private static final CrispRanger NORMAL_RANGE = CrispRanger.closed(17, 320);
    
    private static final CrispRangeMap<Integer> DEPTH_RANDOM_MAPPER = CrispRangeMap.create(
        map ->
        {
            map.put(DEEPEST_RANGE, 7);
            map.put(DEEP_RANGE, 5);
            map.put(NORMAL_RANGE, 3);
        },
        CrispRangeMap.THROW
    );
    
    @SubscribeEvent static void onNewZombieSpawn(@NotNull EntityJoinLevelEvent event)
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
            
            final int count = random.nextInt(DEPTH_RANDOM_MAPPER.getValueOrThrow(entity.getBlockY())) + 1;
            
            final ItemStack throwableTorch = TTorchRegistries.THROWABLE_TORCH.value().getDefaultInstance();
            
            throwableTorch.setCount(count);
            
            zombie.setItemInHand(InteractionHand.MAIN_HAND, throwableTorch);
            zombie.setDropChance(EquipmentSlot.MAINHAND, 1F);
        }
    }
    
    @SubscribeEvent static void throwableTorchLit(@NotNull LivingDamageEvent.Pre event)
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
