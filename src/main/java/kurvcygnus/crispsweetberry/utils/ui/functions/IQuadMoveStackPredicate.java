package kurvcygnus.crispsweetberry.utils.ui.functions;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IQuadMoveStackPredicate
{
    boolean test(ItemStack interactStack, int minIndex, int maxIndex, boolean reverseDirection);
}
