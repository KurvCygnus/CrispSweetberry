package kurvcygnus.crispsweetberry.common.qol.spyglass.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public final class SpyglassPayloadHandler
{
    private static final String ORIGINAL_SLOT_TAG = "ExchangeSlot";
    
    public static void handleData(@NotNull SpyglassPayload data, @NotNull IPayloadContext context)
    {
        context.enqueueWork(() ->
        {
            final ServerPlayer player = (ServerPlayer) context.player();
            final Inventory inventory = player.getInventory();
            
            if(data.isPressed())
            {
                final int slot = inventory.findSlotMatchingItem(Items.SPYGLASS.getDefaultInstance());
                
                if(slot != -1 && slot != Inventory.SLOT_OFFHAND)
                {
                    player.getPersistentData().putInt(ORIGINAL_SLOT_TAG, slot);
                    
                    final ItemStack spyglass = inventory.getItem(slot).copy();
                    final ItemStack oldOffhand = player.getOffhandItem().copy();
                    
                    player.setItemInHand(InteractionHand.OFF_HAND, spyglass);
                    inventory.setItem(slot, oldOffhand);
                }
            }
            else if(player.getPersistentData().contains(ORIGINAL_SLOT_TAG))
            {
                final int originalSlot = player.getPersistentData().getInt(ORIGINAL_SLOT_TAG);
                
                final ItemStack currentOffhand = player.getOffhandItem().copy();
                final ItemStack itemInOriginalSlot = inventory.getItem(originalSlot).copy();
                
                player.setItemInHand(InteractionHand.OFF_HAND, itemInOriginalSlot);
                inventory.setItem(originalSlot, currentOffhand);
                
                player.getPersistentData().remove(ORIGINAL_SLOT_TAG);
            }
        });
    }
}
