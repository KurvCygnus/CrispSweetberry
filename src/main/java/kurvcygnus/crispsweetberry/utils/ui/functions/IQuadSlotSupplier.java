package kurvcygnus.crispsweetberry.utils.ui.functions;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

/**
 * A functional interface to make creating slot for container menu more simple.
 * @implNote Method {@code AbstractMenu#addSlot()} has {@code protected} access, so using a
 * functional interface is a must to make a universal utility.
 * @param <C> The container that slots will be bound to.
 * @param <S> The slot that will be added to container.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.utils.ui.CrispUIUtils#addGridSlots Usage 
 */
@FunctionalInterface
public interface IQuadSlotSupplier<C extends Container, S extends Slot>
{
    S create(C container, int index, int x, int y);
}
