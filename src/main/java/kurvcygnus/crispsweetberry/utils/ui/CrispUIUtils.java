package kurvcygnus.crispsweetberry.utils.ui;

import kurvcygnus.crispsweetberry.utils.ui.collects.CrispIntRanger;
import kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants;
import kurvcygnus.crispsweetberry.utils.ui.functions.IQuadMoveStackPredicate;
import kurvcygnus.crispsweetberry.utils.ui.functions.IQuadSlotSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

import static kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants.CORRECTION_INDEX;
import static kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants.SLOT_GAP;

/**
 * A collection of some simple helpers for <u>{@link net.minecraft.world.inventory.AbstractContainerMenu UI stuff}</u>.
 * @apiNote Being tortured by {@code AbstractMenu#moveItemStackTo()}?<br>
 * <u>{@link CrispIntRanger#inRangers CrispIntRanger#inRangers()}</u> may help you out.
 * @since 1.0 Release
 */
public final class CrispUIUtils
{
    private CrispUIUtils() { throw new IllegalAccessError(); }
    
    /**
     * A utility method to <b>batch initialize and add slots in a grid layout</b> to a menu.<br>
     * This is commonly used for universal UI components like <b>player inventories and hotbars</b>.
     * @implSpec Example:<pre>{@code 
     *  addGridSlots(
     *             inventory, 
     *             BACKPACK_SLOTS_RANGE.getMin(),
     *             INVENTORY_SLOTS_START_X_POS,
     *             INVENTORY_SLOTS_START_Y_POS,
     *             INVENTORY_SLOTS_TOTAL_ROWS,
     *             INVENTORY_SLOTS_TOTAL_COLS,
     *             Slot::new,
     *             this::addSlot
     *         );
     * }</pre>
     * All constants can be found at <u>{@link ExampleSlotConstants}</u>.
     * @apiNote Please make sure that both {@code rows} and {@code cols} are unsigned. Or else this method will
     * throw <u>{@link IllegalArgumentException}</u>.
     */
    public static <C extends Container> void addGridSlots(@NotNull C container, int startIndex, int startX, int startY, int rows, int cols,
        @NotNull IQuadSlotSupplier<C, ? extends Slot> slotFactory, @NotNull Consumer<Slot> consumer) throws IllegalArgumentException
            {
                Objects.requireNonNull(container, "Param \"container\" cannot be null!(1st param)");
                Objects.requireNonNull(slotFactory, "Param \"slotFactory\" cannot be null!(7th param)");
                Objects.requireNonNull(consumer, "Param \"consumer\" cannot be null!(8th param)");
                
                if(rows <= 0 || cols <= 0)
                    throw new IllegalArgumentException("Variable \"rows\" and \"cols\" must both be a positive integer! Current value: rows: %d, cols: %d".
                        formatted(rows, cols)
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
     * @implSpec Example:<pre>{@code 
     *  moveStackByRanger(stack, ranger, flag, this::moveItemStackTo);
     * }</pre>
     * @see ExampleSlotConstants Furnace Layout Index Reference
     * @see CrispIntRanger Ranger
     */
    public static boolean moveStackByRanger
        (@NotNull ItemStack interactStack, @NotNull CrispIntRanger ranger, boolean reverseDirection, @NotNull IQuadMoveStackPredicate predicate)
            {
                Objects.requireNonNull(interactStack, "Param \"interactStack\" cannot be null!(1st param)");
                Objects.requireNonNull(ranger, "Param \"ranger\" cannot be null!(2nd param)");
                Objects.requireNonNull(predicate, "Param \"predicate\" cannot be null!(4th param)");
                
                return predicate.test(interactStack, ranger.getMin(), ranger.getMax() + CORRECTION_INDEX, reverseDirection);
            }
}
