package kurvcygnus.crispsweetberry.common.misc.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.misc.items.CoinCollections;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

//? TODO: Cover more cases.

/**
 * This event is the actual role that makes experience mechanic work.
 *
 * @author Kurv Cygnus
 * @see CoinCollections Coin Families
 * @since 1.0 Release
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID)
public final class CoinExperienceEvent
{
    @SubscribeEvent
    static void craftCheck(@NotNull PlayerEvent.ItemCraftedEvent event)
    {
        final Item item = event.getCrafting().getItem();
        final Player player = event.getEntity();
        final Level level = player.level();
        
        if(level.isClientSide)
            return;
        
        if(item instanceof CoinCollections.AbstractCoinItem coin)
            ExperienceOrb.award((ServerLevel) level, player.position(), coin.getStoredExperience());
    }
}
