//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Main event for generating JSON data.
 * @since 1.0 Release
 * @implNote <u>{@link GatherDataEvent}</u> doesn't belong to neither <u>{@link net.neoforged.api.distmarker.Dist#CLIENT ClientSide}</u> nor 
 * <u>{@link net.neoforged.api.distmarker.Dist#DEDICATED_SERVER ServerSide}</u>, so just remind that adding these params to 
 * <u>{@link EventBusSubscriber}</u> will cause problems(will be silent at some cases), don't do this.
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class CrispDataGenEvent
{
    @SubscribeEvent
    static void dataGenerate(@NotNull GatherDataEvent event)
    {
        final DataGenerator generator = event.getGenerator();
        final PackOutput output = generator.getPackOutput();
        final ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        final CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        
        generator.addProvider(
            event.includeServer(),
            new CrispRecipeProvider(output, lookupProvider)
        );
        
        generator.addProvider(
            event.includeClient(),
            new CrispBlockstateProvider(output, existingFileHelper)
        );
        
        generator.addProvider(
            event.includeServer(),
            new LootTableProvider(
                output,
                Collections.emptySet(),
                List.of(
                    new LootTableProvider.SubProviderEntry(
                        CrispBlockLootSubProvider::new,
                        LootContextParamSets.BLOCK
                    ),
                    new LootTableProvider.SubProviderEntry(
                        VanillaCoinLootTableProvider::new,
                        LootContextParamSets.BLOCK
                    )
                ),
                lookupProvider
            )
        );
    }
}
