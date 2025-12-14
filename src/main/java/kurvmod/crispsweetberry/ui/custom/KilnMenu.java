package kurvmod.crispsweetberry.ui.custom;

import kurvmod.crispsweetberry.events.init.KilnRecipeCacheEvent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static kurvmod.crispsweetberry.utils.CrispConstants.SlotConstants.*;

//DOC
//WIP
//TODO
public class KilnMenu extends AbstractContainerMenu
{
    public static final int KILN_DEFAULT_SLOT_SIZE = 6;
    protected Level level;
    private final Container container;
    private final ContainerLevelAccess access;
    
    protected KilnMenu(@Nullable MenuType<?> menuType, int containerId, Container container, ContainerLevelAccess access)
    {
        super(menuType, containerId);
        this.container = container;
        this.access = access;
    }
    
    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index)
    {
        ItemStack temporaryStack = ItemStack.EMPTY;
        this.level = player.level();
        Slot slot = this.slots.get(index);
        
        if(slot.hasItem())
        {
            ItemStack interactStack = slot.getItem();
            temporaryStack = interactStack.copy();
            
            switch(index)
            {
                case OUTPUT_SLOT ->
                {
                    if(!this.moveItemStackTo(interactStack, BACKPACK_SLOT_START_INDEX, BACKPACK_PLUS_BAR_SLOT_END_INDEX, true))
                        return ItemStack.EMPTY;
                    
                    slot.onQuickCraft(interactStack, temporaryStack);
                }
                
                case INPUT_SLOT ->
                {
                    if(!this.moveItemStackTo(interactStack, BACKPACK_SLOT_START_INDEX, BACKPACK_PLUS_BAR_SLOT_END_INDEX_EXCLUSIVE, false))
                        return ItemStack.EMPTY;
                }
                
                default ->
                {
                    if((this.canSmelt(interactStack) || this.canSmoke(interactStack)) &&
                        !tryMoveStackToInputSlot(interactStack))
                        return ItemStack.EMPTY;
                    else if(index >= BACKPACK_SLOT_START_INDEX && index < BACKPACK_SLOT_END_INDEX_EXCLUSIVE &&
                        !tryMoveStackToBarSlot(interactStack))
                        return ItemStack.EMPTY;
                    else if(index >= BAR_SLOT_START_INDEX && index < BAR_SLOT_END_INDEX_EXCLUSIVE &&
                        !tryMoveStackToInventorySlot(interactStack))
                        return ItemStack.EMPTY;
                }
            }
            
            if(interactStack.isEmpty())
                slot.setByPlayer(ItemStack.EMPTY);
            else
                slot.setChanged();
            
            if(interactStack.getCount() == temporaryStack.getCount())
                return ItemStack.EMPTY;
            
            slot.onTake(player, interactStack);
        }
        
        return temporaryStack;
    }
    
    /**
     * <b><i>WARNING</i></b>:
     * <p>
     * The <b>actual validity logic must be implemented in the underlying container</b>
     * (e.g. a {@code BlockEntity} such as <b>{@code KilnBlockEntity}</b>),
     * <b>if you are using {@code container.stillvaild()}</b>.
     * </p>
     * <p>
     * {@code AbstractContainerMenu} only <b>forwards</b> this check and is responsible
     * for UI interaction, <b>not world state validation</b>.
     * </p>
     * <p>
     * Failing to override {@code stillValid(Player)} in the container may cause
     * <b>desynchronization or improper menu behavior</b>.
     * </p>
     * <p><i>
     * Honestly, I have to say the official NeoForge source code and doc are both sucks.
     * </p></i>
     */
    @Override
    public boolean stillValid(@NotNull Player player) { return this.container.stillValid(player); }
    
    /**
     * For readability, this has been encapsulated into a single method.
     */
    private boolean tryMoveStackToBarSlot(ItemStack interactStack)
        { return this.moveItemStackTo(interactStack, BAR_SLOT_START_INDEX, BAR_SLOT_END_INDEX_EXCLUSIVE, false); }
    
    /**
     * For readability, this has been encapsulated into a single method.
     */
    private boolean tryMoveStackToInputSlot(ItemStack interactStack)
        { return this.moveItemStackTo(interactStack, INPUT_SLOT, INPUT_SLOT_END_INDEX_EXCLUSIVE, false); }
    
    /**
     * For readability, this has been encapsulated into a single method.
     */
    private boolean tryMoveStackToInventorySlot(ItemStack interactStack)
        { return this.moveItemStackTo(interactStack, BACKPACK_SLOT_START_INDEX, BACKPACK_SLOT_END_INDEX_EXCLUSIVE, false); }
    
    private boolean canSmelt(@NotNull ItemStack itemstack) { return KilnRecipeCacheEvent.getKilnRecipesCache().containsKey(itemstack.getItem()); }
    
    private boolean canSmoke(@NotNull ItemStack itemstack) { return KilnRecipeCacheEvent.getSmokerRecipesCache().containsKey(itemstack.getItem()); }
}
