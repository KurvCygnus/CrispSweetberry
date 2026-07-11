//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.api.ICoinType;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinType;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

//? TODO: Compatibility for muti nuggets

/**
 * This event checks the validation of all <u>{@link VanillaCoinType}</u>' nugget item,
 * and also check whether <u>{@link VanillaCoinType#COPPER Copper}</u> and <u>{@link VanillaCoinType#DIAMOND diamond}</u> coins
 * should exist.
 * @since Release 1.0
 * @author Kurv Cygnus
 * @see Tags.Items#NUGGETS Tag
 * @apiNote This can be used as a template for custom <u>{@link ICoinType CoinType}</u>'s validation and 
 * existence determination.
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class NuggetItemCheckEvent
{
    private static final IMarkLogger LOGGER = IMarkLogger.withMarkerSuffixes("NUGGET_CHECK");
    
    public static Supplier<Item> copperNuggetSupplier = () -> Items.AIR;
    public static Supplier<Item> diamondNuggetSupplier = () -> Items.AIR;
    
    @SubscribeEvent(priority = EventPriority.LOWEST) static void onTagsUpdated(@NotNull TagsUpdatedEvent event)
    {
        if(!event.shouldUpdateStaticData())
            return;
        
        LOGGER.debug("Tags updated! Start searching copper & diamond's nugget...");
        
        final var itemRegistry = event.getRegistryAccess().registryOrThrow(Registries.ITEM);
        
        final var nuggetTag = itemRegistry.getTag(Tags.Items.NUGGETS);
        
        nuggetTag.ifPresent(
            holders ->
            {
                copperNuggetSupplier = () -> Items.AIR;
                diamondNuggetSupplier = () -> Items.AIR;
                
                for(final var holder: holders)
                    holder.unwrapKey().ifPresent(key ->
                        {
                            final var id = key.location();
                            final var path = id.getPath();
                            
                            if(path.contains("copper_nugget"))//! Currently, this is the best way to find correspond item with no exceptions allowed.
                                copperNuggetSupplier = holder::value;
                            else if(path.contains("diamond_nugget"))
                                diamondNuggetSupplier = holder::value;
                        }
                    );
            }
        );
        
        LOGGER.info("Validating CoinType nugget tags...");
        
        try
        {
            for(final var type: VanillaCoinType.VALUES)
            {
                final var nuggetStack = type.nuggetItem().getDefaultInstance();
                
                if(!nuggetStack.is(Tags.Items.NUGGETS))
                    LOGGER.warn(
                        "Invalid definition for {}: Item {} is not in the Nuggets tag!",
                        type.id().toUpperCase(), nuggetStack.getItemHolder().getRegisteredName()
                    );
            }
        }
        catch(IllegalArgumentException e) { LOGGER.error("CoinType Validation Failed: {}", e.getMessage()); }
        
        LOGGER.debug("CoinType validation completed.");
    }
}
