//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.client.init;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import kurvcygnus.crispsweetberry.utils.registry.objects.TabEntry;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The executor of annotation <b><u>{@link RegisterToTab @RegisterToTab}</u></b>.<br>
 * <b>It automatically registers every entry that presents {@link RegisterToTab @RegisterToTab} to designated tabs.</b>
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE, value = Dist.CLIENT)
public final class CrispCreativeTabsRegistryEvent
{
    private static final MarkLogger LOGGER = MarkLogger.withMarkerSuffixes(LogUtils.getLogger(), "TAB_REGISTRY");
    
    @SubscribeEvent public static void tabRegistryEvent(final @NotNull BuildCreativeModeTabContentsEvent event)
    {
        final List<TabEntry> entries = CrispSweetberry.TAB_LOOKUP.get(event.getTabKey());
        
        if(entries == null)
        {
            try(MarkLogger.MarkerHandle ignored = LOGGER.pushMarker("TAB_REGISTRY_FETAL")) { LOGGER.warn("Registry entry is null. Skipped."); }
            return;
        }
        
        for(final TabEntry entry: entries)
        {
            if(!entry.condition())
            {
                LOGGER.debug("Skipped the registration of {} as its condition hasn't met.", entry.itemSupplier().get().getDefaultInstance().getDisplayName());
                continue;
            }
            
            final Item item = entry.itemSupplier().get();
            
            if(entry.tab() == event.getTabKey())
            {
                event.accept(item);
                LOGGER.debug("Registered item \"{}\" to tab \"{}\".", item.getDefaultInstance().getDisplayName(), entry.tab());
            }
        }
    }
}
