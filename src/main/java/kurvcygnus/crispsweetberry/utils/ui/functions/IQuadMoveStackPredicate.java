package kurvcygnus.crispsweetberry.utils.ui.functions;

import net.minecraft.world.item.ItemStack;

/**
 * A simple functional interface which is mainly used for {@code AbstractMenu#quickMoveStack()} method.
 * @implNote Method {@code AbstractMenu#quickMoveStack()} has {@code protected} access, so using a
 * functional interface is a must to make a universal utility.
 * @see kurvcygnus.crispsweetberry.utils.ui.CrispUIUtils#moveStackByRanger Usage
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
@FunctionalInterface
public interface IQuadMoveStackPredicate
{
    boolean test(ItemStack interactStack, int minIndex, int maxIndex, boolean reverseDirection);
}
