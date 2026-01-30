package kurvcygnus.crispsweetberry.common.features.coins.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.abstracts.AbstractCoinItem;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinItem;
import kurvcygnus.crispsweetberry.utils.ui.collects.CrispIntRanger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

//? TODO: Cover more cases.

/**
 * This event is the actual role that makes experience mechanic work.
 *
 * @author Kurv Cygnus
 * @see VanillaCoinItem Base Coin
 * @since 1.0 Release
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID)
public final class CoinExperienceEvent
{
    private static final int INVENTORY_INPUT_SLOT_START_INDEX = 1;
    private static final int INVENTORY_INPUT_SLOT_END_INDEX = 4;
    private static final int CRAFTING_TABLE_INPUT_SLOT_START_INDEX = 1;
    private static final int CRAFTING_TABLE_INPUT_SLOT_END_INDEX = 9;
    public static final int UNIVERSAL_RESULT_SLOT_INDEX = 0;
    
    private static final CrispIntRanger INVENTORY_INPUT_SLOTS_RANGE = CrispIntRanger.closed(INVENTORY_INPUT_SLOT_START_INDEX, INVENTORY_INPUT_SLOT_END_INDEX);
    private static final CrispIntRanger CRAFTING_TABLE_INPUT_SLOTS_RANGE = CrispIntRanger.closed(CRAFTING_TABLE_INPUT_SLOT_START_INDEX, CRAFTING_TABLE_INPUT_SLOT_END_INDEX);
    
    @SubscribeEvent
    static void craftPreCheck(@NotNull PlayerContainerEvent event)
    {
        final AbstractContainerMenu menu = event.getContainer();
        
        if(menu instanceof InventoryMenu || menu instanceof CraftingMenu)
        {
            final ItemStack result = event.getContainer().getSlot(UNIVERSAL_RESULT_SLOT_INDEX).getItem();
            
            if(result.getItem() instanceof AbstractCoinItem<?> coinItem)
            {
                Player player = event.getEntity();
                
                if(player.totalExperience < coinItem.getCoinType().getExperience())//? TODO: Tip players about this.
                    event.getContainer().getSlot(UNIVERSAL_RESULT_SLOT_INDEX).set(ItemStack.EMPTY);//* If player has not enough exp for coin, hide result.
            }
        }
    }
    
    @SubscribeEvent
    static void craftCheck(@NotNull PlayerEvent.ItemCraftedEvent event)
    {
        final Item item = event.getCrafting().getItem();
        final Player player = event.getEntity();
        final Level level = player.level();
        
        if(level.isClientSide)
            return;
        
        if(item instanceof AbstractCoinItem<?> coin)
            player.giveExperiencePoints(-coin.getCoinType().getExperience());
        else switch(event.getInventory())
        {
            case InventoryMenu ignored -> checkSlotsAndDispenseExp(event, INVENTORY_INPUT_SLOTS_RANGE);
            case CraftingMenu ignored -> checkSlotsAndDispenseExp(event, CRAFTING_TABLE_INPUT_SLOTS_RANGE);
            default -> {}
        }
        
    }
    
    private static void checkSlotsAndDispenseExp(@NotNull PlayerEvent.ItemCraftedEvent event, @NotNull CrispIntRanger ranger)
    {
        final ItemStack result = event.getInventory().getItem(UNIVERSAL_RESULT_SLOT_INDEX);
        int coinCount = 0;
        
        for(int inputIndex: ranger)
        {
            final ItemStack material = event.getInventory().getItem(inputIndex);
            
            if(material.getItem() instanceof AbstractCoinItem<?> coin)
            {
                coinCount++;
                if(inputIndex == ranger.getMax() && coinCount == 1 && result.getItem() == coin.getCoinType().nuggetItem())//? TODO: Refactor this with recipe check.
                {
                    Player player = event.getEntity();
                    ServerLevel level = (ServerLevel) player.level();
                    
                    //*                                                         No penalty, coins will be super OP. ↓
                    ExperienceOrb.award(level, player.position(), (int) (coin.getCoinType().getExperience() * coin.getCoinType().getPenaltyRate()));
                    return;
                }
            }
            else if(material != ItemStack.EMPTY)
                return;
        }
    }
}
