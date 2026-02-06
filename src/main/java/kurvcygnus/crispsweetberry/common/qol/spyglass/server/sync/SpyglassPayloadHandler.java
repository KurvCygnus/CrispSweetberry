package kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class SpyglassPayloadHandler
{
    public static final String ORIGINAL_SLOT_TAG = "%s:spyglass_exchange_slot_index".formatted(CrispSweetberry.NAMESPACE);
    
    public static void handleData(@NotNull SpyglassPayload data, @NotNull IPayloadContext context)
    {
        context.enqueueWork(() ->
            {
                final ServerPlayer player = (ServerPlayer) context.player();
                final Inventory inventory = player.getInventory();
                
                if(data.isPressed())
                {
                    final int slot = inventory.findSlotMatchingItem(Items.SPYGLASS.getDefaultInstance());
                    
                    if(slot != ExampleSlotConstants.NAN && slot != Inventory.SLOT_OFFHAND)
                    {
                        player.getPersistentData().putInt(ORIGINAL_SLOT_TAG, slot);
                        
                        final ItemStack spyglass = inventory.getItem(slot);
                        final ItemStack oldOffhand = player.getOffhandItem();
                        
                        player.setItemInHand(InteractionHand.OFF_HAND, spyglass);
                        player.awardStat(Stats.ITEM_USED.get(Items.SPYGLASS));
                        inventory.setItem(slot, oldOffhand);
                        player.startUsingItem(InteractionHand.OFF_HAND);
                    }
                }
                else if(player.getPersistentData().contains(ORIGINAL_SLOT_TAG))
                {
                    player.stopUsingItem();
                    final int originalSlot = player.getPersistentData().getInt(ORIGINAL_SLOT_TAG);
                    
                    final ItemStack currentOffhand = player.getOffhandItem();
                    final ItemStack itemInOriginalSlot = inventory.getItem(originalSlot);
                    
                    player.setItemInHand(InteractionHand.OFF_HAND, itemInOriginalSlot);
                    inventory.setItem(originalSlot, currentOffhand);
                    
                    player.getPersistentData().remove(ORIGINAL_SLOT_TAG);
                }
            }
        );
    }
}
