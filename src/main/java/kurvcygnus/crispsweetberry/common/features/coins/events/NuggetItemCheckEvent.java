package kurvcygnus.crispsweetberry.common.features.coins.events;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.CoinType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@EventBusSubscriber(modid = CrispSweetberry.MOD_ID)
public final class NuggetItemCheckEvent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void onTagsUpdated(@NotNull TagsUpdatedEvent event)
    {
        if(event.shouldUpdateStaticData())
        {
            LOGGER.info("Tags updated! Validating CoinType nugget tags...");
            
            try
            {
                CoinType.validateTags();
                LOGGER.debug("CoinType validation passed.");
            }
            catch(IllegalArgumentException e) { LOGGER.error("CoinType Validation Failed: {}", e.getMessage()); }
        }
    }
}
