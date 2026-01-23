package kurvcygnus.crispsweetberry.utils.ui.functions;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

/**
 * A functional interface to make creating slot for container menu more simple.
 * @param <C> The container that slots will be bound to.
 * @param <S> The slot that will be added to container.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@FunctionalInterface
public interface IQuadSlotSupplier<C extends Container, S extends Slot>
{
    S create(C container, int index, int x, int y);
}
