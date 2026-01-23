package kurvcygnus.crispsweetberry.utils.ui;

import kurvcygnus.crispsweetberry.utils.ui.collects.CrispIntRanger;
import kurvcygnus.crispsweetberry.utils.ui.constants.SlotConstants;
import kurvcygnus.crispsweetberry.utils.ui.functions.IQuadMoveStackPredicate;
import kurvcygnus.crispsweetberry.utils.ui.functions.IQuadSlotSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

import static kurvcygnus.crispsweetberry.utils.ui.constants.SlotConstants.CORRECTION_INDEX;
import static kurvcygnus.crispsweetberry.utils.ui.constants.SlotConstants.SLOT_GAP;

/**
 * @since 1.0 Release
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
        @NotNull IQuadSlotSupplier<C, ? extends Slot> slotFactory, @NotNull Consumer<Slot> consumer) throws IllegalArgumentException
            {
                Objects.requireNonNull(container, "Container cannot be null!");
                Objects.requireNonNull(slotFactory, "SlotFactory cannot be null!");
                Objects.requireNonNull(consumer, "Consumer cannot be null!");
                
                if(rows <= 0 || cols <= 0)
                    throw new IllegalArgumentException(String.format(
                        "Variable \"rows\" and \"cols\" must both be a positive integer! Current value: rows: %d, cols: %d",
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
     * @apiNote <b>The original method's implementation logic uses <i>closedOpen</i> style, and this utility has fixed it to standard <i>closed</i> style</b>. 
     * <i>Don't forget about this!</i>
     * @implSpec <pre>{@code 
     *  moveStackByRanger(stack, ranger, flag, this::moveItemStackTo);
     * }</pre>
     * @see SlotConstants Furnace Layout Index Reference
     * @see CrispIntRanger Ranger
     */
    public static boolean moveStackByRanger
        (@NotNull ItemStack interactStack, @NotNull CrispIntRanger ranger, boolean reverseDirection, @NotNull IQuadMoveStackPredicate predicate)
            {
                Objects.requireNonNull(interactStack, "ItemStack cannot be null!");
                Objects.requireNonNull(ranger, "CrispIntRanger cannot be null!");
                Objects.requireNonNull(predicate, "Predicate cannot be null!");
                
                return predicate.test(interactStack, ranger.getMin(), ranger.getMax() + CORRECTION_INDEX, reverseDirection);
            }
}
