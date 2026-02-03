package kurvcygnus.crispsweetberry.client.init;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinItem;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinStackItem;
import kurvcygnus.crispsweetberry.utils.registry.TabEntry;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

/**
 * The executor of annotation <b><u>{@link RegisterToTab @RegisterToTab}</u></b>.<br>
 * <b>It automatically registers every entry that presents {@link RegisterToTab @RegisterToTab} to designated tabs.</b>
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.ID, value = Dist.CLIENT)
public final class CrispCreativeTabsRegistryEvent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void tabRegistryEvent(final @NotNull BuildCreativeModeTabContentsEvent event)
    {
        LOGGER.info("[TAB_REGISTRY] Start the registration of creative mode tab contents...");
        
        final List<TabEntry> entries = CrispSweetberry.TAB_LOOKUP.get(event.getTabKey());
        
        if(entries != null)
        {
            for(final TabEntry entry: entries)
            {
                if(!entry.condition())
                {
                    LOGGER.debug("[TAB_REGISTRY] Skipped the registration of {} as its condition hasn't met.", entry.itemSupplier().get().getDefaultInstance().getDisplayName());
                    continue;
                }
                
                final Item item = entry.itemSupplier().get();
                
                if(item instanceof AbstractCoinItem<?> coin && !coin.getCoinType().shouldAppear())
                {
                    LOGGER.debug("[TAB_REGISTRY] Skipped the registration of coin {} as it shouldn't appear.", coin.getDefaultInstance().getDisplayName());
                    continue;
                }
                
                if(item instanceof AbstractCoinStackItem<?> stack && !stack.getCoinType().shouldAppear())
                {
                    LOGGER.debug("[TAB_REGISTRY] Skipped the registration of coinStack {} as it shouldn't appear.", stack.getDefaultInstance().getDisplayName());
                    continue;
                }
                
                event.accept(item);
            }
        }
        
        LOGGER.info("[TAB_REGISTRY] Finish the registration of creative mode tab contents!");
    }
}
