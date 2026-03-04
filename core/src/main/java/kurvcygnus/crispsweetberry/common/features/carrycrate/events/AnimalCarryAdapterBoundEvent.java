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

import java.util.Objects;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
final class AnimalCarryAdapterBoundEvent
{
    private static final MarkLogger LOGGER = MarkLogger.marklessLogger(LogUtils.getLogger());
    
    @SuppressWarnings("unchecked")//! Unsafe casting, but with try-catch ;)
    @SubscribeEvent static void register(@NotNull ServerStartedEvent event)
    {
        final RegistryAccess access = event.getServer().registryAccess();
        LOGGER.info("Start hooking entity registry...");
        
        access.registryOrThrow(Registries.ENTITY_TYPE).stream().
            filter(entityType -> 
                {
                    if(!Objects.equals(entityType.getCategory(), MobCategory.CREATURE))
                        return false;
                    
                    LOGGER.debug("Captured entity \"{}\" as friendly entity.", entityType.getDescriptionId());
                    
                    final AABB aabb = entityType.getSpawnAABB(0D, 0D, 0D);
                    final double entityVolume = aabb.getXsize() * aabb.getYsize() * aabb.getZsize();
                    final boolean isAcceptable = entityVolume <= AdaptiveAnimalCarryAdapter.MAX_ACCEPTABLE_ENTITY_HEIGHT_VOLUME;
                    
                    LOGGER.debug(
                        "Entity \"{}\" {}.",
                        entityType.getDescriptionId(),
                        isAcceptable ? 
                            "accepted" :
                            "rejected. Its volume doesn't meet the condition. Volume: %f".
                                formatted(entityVolume)
                    );
                    
                    return isAcceptable;
                }
            ).
            forEach(
                entityType ->
                {
                    try
                    {
                        final EntityType<? extends Animal> animal = (EntityType<? extends Animal>) entityType;
                        
                        CarryRegistryManager.INSTANCE.register(animal, AdaptiveAnimalCarryAdapter::new);
                        LOGGER.debug("Accepted animal \"{}\".", entityType.getDescriptionId());
                    }
                    catch(ClassCastException exception) { LOGGER.debug("Entity \"{}\" is not an animal. Skipped.", entityType.getDescriptionId(), exception); }
                }
            );
        
        LOGGER.info("Finished hooking entity registry.");
    }
}
