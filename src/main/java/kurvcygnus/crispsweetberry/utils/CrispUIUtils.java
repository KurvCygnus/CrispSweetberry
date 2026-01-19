package kurvcygnus.crispsweetberry.utils;

import kurvcygnus.crispsweetberry.utils.collects.CrispIntRanger;
import kurvcygnus.crispsweetberry.utils.functions.IQuadMoveStackPredicate;
import kurvcygnus.crispsweetberry.utils.functions.IQuadSlotSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static kurvcygnus.crispsweetberry.utils.constants.SlotConstants.CORRECTION_INDEX;
import static kurvcygnus.crispsweetberry.utils.constants.SlotConstants.SLOT_GAP;

/**
 * @since CSB Release 1.0
 */
public final class CrispUIUtils
{
    private CrispUIUtils() { throw new IllegalAccessError(); }
    
    /**
     * A utility method to <b>batch initialize and add slots in a grid layout</b> to a menu.<br>
     * This is commonly used for universal UI components like <b>player inventories and hotbars</b>.
     * @apiNote Please make sure both {@code rows} and {@code cols} are positive integers. Or else this method will
     * throw <u>{@link IllegalArgumentException}</u>.
     */
    public static <C extends Container> void addGridSlots(@NotNull C container, int startIndex, int startX, int startY, int rows, int cols,
        @NotNull IQuadSlotSupplier<C, ? extends Slot> slotFactory, @NotNull Consumer<Slot> consumer)
            {
                if(rows <= 0 || cols <= 0)
                    throw new IllegalArgumentException(String.format(
                        "Variable \"rows\" and \"cols\" must be a positive integer! Current value: rows: %d, cols: %d",
                            rows, cols)
                    );
                
                for(int row = 0; row < rows; row++)
                    for(int col = 0; col < cols; col++)
                    {
                        final int SLOT_INDEX = startIndex + (col + row * cols);
                        final int X_POS = startX + col * SLOT_GAP;
                        final int Y_POS = startY + row * SLOT_GAP;
                        
                        consumer.accept(slotFactory.create(container, SLOT_INDEX, X_POS, Y_POS));
                    }
            }
    
    /**
     * A utility method to make {@code #moveItemStackTo()} in the <u>{@link net.minecraft.world.inventory.AbstractContainerMenu containers}</u> more simple.
     * @apiNote The original method's implementation logic uses <i>closedOpen</i> style, and this utility has fixed it to standard <i>closed</i> style. 
     * <i>Don't forget about this!</i>
     * @implSpec <pre>{@code 
     *  moveStackByRanger(stack, ranger, flag, this::moveItemStackTo);
     * }</pre>
     * @see kurvcygnus.crispsweetberry.utils.constants.SlotConstants Furnace Layout Index Reference
     * @see CrispIntRanger Ranger
     */
    public static boolean moveStackByRanger
        (@NotNull ItemStack interactStack, @NotNull CrispIntRanger ranger, boolean reverseDirection, @NotNull IQuadMoveStackPredicate predicate)
            { return predicate.predicate(interactStack, ranger.getMin(), ranger.getMax() + CORRECTION_INDEX, reverseDirection); }
}
