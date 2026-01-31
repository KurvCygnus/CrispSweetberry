package kurvcygnus.crispsweetberry.common.features.kiln.client.ui;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Modified slot for Kiln Block.
 * @see KilnMenu Usage
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public final class KilnInputSlot extends Slot
{
    /**
     * @apiNote Using explicit new field instead of default <u>{@link Slot#container}</u> as
     * Kiln itself is completely a unique variant of furnace, and using casting is semantically counterintuitive.<br>
     * <i>Also, this implementation doesn't impact performance much compare to use casting.</i>
     */
    private final KilnBlockEntity container;
    
    public KilnInputSlot(@NotNull KilnBlockEntity container, int slot, int x, int y)
    {
        super(container, slot, x, y);
        this.container = container;
    }
    
    @Override//*                                                                                        ↓ For tip display.
    public boolean mayPlace(@NotNull ItemStack stack) { return container.canSmelt(stack) || container.isBanned(stack); }
}
