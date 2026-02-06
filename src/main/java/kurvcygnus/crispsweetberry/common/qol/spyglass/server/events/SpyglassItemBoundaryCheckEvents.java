package kurvcygnus.crispsweetberry.common.qol.spyglass.server.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler.ORIGINAL_SLOT_TAG;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class SpyglassItemBoundaryCheckEvents
{
    @SubscribeEvent
    static void deathCheck(@NotNull LivingDeathEvent event)
    {
        if(event.isCanceled())
            return;
        
        final LivingEntity entity = event.getEntity();
        
        if(!(entity instanceof Player player && player.getPersistentData().contains(ORIGINAL_SLOT_TAG)))
            return;
        
        final int originalSlotIndex = player.getPersistentData().getInt(ORIGINAL_SLOT_TAG);
        emergencyCleanup(player, originalSlotIndex);
    }
    
    @SubscribeEvent
    static void logoutCheck(@NotNull PlayerEvent.PlayerLoggedOutEvent event)
    {
        final Player player = event.getEntity();
        if(!player.getPersistentData().contains(ORIGINAL_SLOT_TAG))
            return;
        
        final int originalSlotIndex = player.getPersistentData().getInt(ORIGINAL_SLOT_TAG);
        emergencyCleanup(player, originalSlotIndex);
    }
    
    private static void emergencyCleanup(@NotNull Player player, int originalSlotIndex)
    {
        final Inventory playerInventory = player.getInventory();
        final ItemStack spyglass = playerInventory.offhand.getFirst();
        
        player.setItemInHand(InteractionHand.OFF_HAND, playerInventory.getItem(originalSlotIndex));
        playerInventory.setItem(originalSlotIndex, spyglass);
    }
}
