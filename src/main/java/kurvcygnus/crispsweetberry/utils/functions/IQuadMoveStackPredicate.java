package kurvcygnus.crispsweetberry.utils.functions;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IQuadMoveStackPredicate
{
    boolean predicate(ItemStack interactStack, int minIndex, int maxIndex, boolean reverseDirection);
}
