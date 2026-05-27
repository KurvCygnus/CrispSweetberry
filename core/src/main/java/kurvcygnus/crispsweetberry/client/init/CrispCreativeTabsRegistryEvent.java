//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.client.init;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import kurvcygnus.crispsweetberry.utils.core.RegisterToTab;
import kurvcygnus.crispsweetberry.utils.core.TabEntry;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The executor of annotation <b><u>{@link RegisterToTab @RegisterToTab}</u></b>.<br>
 * <b>It automatically registers every entry that presents {@link RegisterToTab @RegisterToTab} to designated tabs.</b>
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE, value = Dist.CLIENT)
public final class CrispCreativeTabsRegistryEvent
{
    private static final IMarkLogger LOGGER = IMarkLogger.withMarkerSuffixes("TAB_REGISTRY");
    
    @SubscribeEvent public static void tabRegistryEvent(final @NotNull BuildCreativeModeTabContentsEvent event)
    {
        final @Nullable var entries = CrispSweetberry.TAB_LOOKUP.tryGet(event).get(event.getTabKey());
        
        if(entries == null)
        {
            LOGGER.warn("Registry entry is null. Skipped.");
            return;
        }
        
        for(final TabEntry entry: entries)
        {
            final Item item = entry.itemSupplier().get();
            
            //* Yes, using `==` to compare is legal.
            //* Firstly, [[ResourceKey]] didn't implement [[Object#equals]](which is terrible, this also happens on [[EntityType]]).
            //* Secondly, don't forget that most value holders in Minecraft are CONSTANTS,
            //* whose does have a fixed hashcode, thus can be compared with `==`, because `==` is a CPU command, it is slightly faster.
            if(entry.tab() == event.getTabKey())
            {
                event.accept(item);
                LOGGER.debug("Registered item \"{}\" to tab \"{}\".", item.getDefaultInstance().getDisplayName(), entry.tab());
            }
        }
    }
}
