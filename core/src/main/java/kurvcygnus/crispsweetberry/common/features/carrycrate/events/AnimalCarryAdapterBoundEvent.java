//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.events;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.entity.AdaptiveAnimalCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.registry.CarryRegistryManager;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
final class AnimalCarryAdapterBoundEvent
{
    private static final MarkLogger LOGGER = MarkLogger.marklessLogger(LogUtils.getLogger());
    
    @SuppressWarnings("unchecked")//! Unsafe casting, but with try-catch ;)
    @SubscribeEvent static void register(@NotNull ServerStartedEvent event)
    {
        final RegistryAccess access = event.getServer().registryAccess();
        access.registryOrThrow(Registries.ENTITY_TYPE).stream().
            filter(e -> 
                {
                    if(!e.getCategory().equals(MobCategory.CREATURE))
                        return false;
                    
                    final AABB aabb = e.getSpawnAABB(0D, 0D, 0D);
                    
                    return aabb.getXsize() * aabb.getYsize() * aabb.getZsize() <= AdaptiveAnimalCarryAdapter.MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME;
                }
            ).
            forEach(
                e -> 
                {
                    try
                    {
                        final EntityType<? extends Animal> animal = (EntityType<? extends Animal>) e;
                        
                        CarryRegistryManager.INSTANCE.register(animal, AdaptiveAnimalCarryAdapter::new);
                    }
                    catch(ClassCastException exception) { LOGGER.debug("Entity \"{}\" is not an animal. Skipped.", e.getDescriptionId(), exception); }
                }
            );
    }
}
