package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

public record KilnRecipeInput(ItemStack stack, boolean isSmokerSlot) implements RecipeInput
{
    @Override
    public @NotNull ItemStack getItem(int index) { return stack; }
    
    @Override
    public int size() { return 1; }
}
