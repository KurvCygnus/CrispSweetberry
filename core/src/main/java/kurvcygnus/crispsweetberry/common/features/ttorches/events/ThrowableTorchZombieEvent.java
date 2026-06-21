//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.events;

import com.google.errorprone.annotations.DoNotCall;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.lib.base.datastructure.RangeMap;
import kurvcygnus.crispsweetberry.lib.base.datastructure.Ranger;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
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
    private static final Ranger DEEPEST_RANGE = Ranger.closed(-64, -24);
    private static final Ranger DEEP_RANGE = Ranger.closed(-23, 16);
    private static final Ranger NORMAL_RANGE = Ranger.closed(17, 320);
    
    private static final RangeMap<Integer> DEPTH_RANDOM_MAPPER = RangeMap.create(
        map ->
        {
            map.put(DEEPEST_RANGE, 7);
            map.put(DEEP_RANGE, 5);
            map.put(NORMAL_RANGE, 3);
        },
        RangeMap.THROW
    );
    
    @SubscribeEvent @DoNotCall static void onNewZombieSpawn(@NotNull EntityJoinLevelEvent event)
    {
        if(event.isCanceled())
            return;
        
        final var entity = event.getEntity();
        
        if(entity instanceof Zombie zombie)
        {
            if(!zombie.getItemInHand(InteractionHand.MAIN_HAND).isEmpty())
                return;
            
            final var level = event.getLevel();
            final var random = level.getRandom();
            
            if(random.nextFloat() > 0.05)
                return;
            
            final int count = random.nextInt(DEPTH_RANDOM_MAPPER.getValueOrThrow(entity.getBlockY())) + 1;
            
            final var throwableTorch = TTorchRegistries.THROWABLE_TORCH.value().getDefaultInstance();
            
            throwableTorch.setCount(count);
            
            zombie.setItemInHand(InteractionHand.MAIN_HAND, throwableTorch);
            zombie.setDropChance(EquipmentSlot.MAINHAND, 1F);
        }
    }
    
    @SubscribeEvent @DoNotCall static void throwableTorchLit(@NotNull LivingDamageEvent.Pre event)
    {
        final var entity = event.getSource().getEntity();
        
        if(entity instanceof Zombie zombie && zombie.getItemInHand(InteractionHand.MAIN_HAND).is(TTorchRegistries.THROWABLE_TORCH))
        {
            final int fireTick = Math.min(event.getEntity().getRemainingFireTicks() + HIT_STD_EXTEND_FIRE_TICKS, HIT_STD_MAX_TICKS);
            event.getEntity().setRemainingFireTicks(fireTick);
        }
    }
}
