package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A simple implementation of <u>{@link RecipeInput}</u>, used to define <u>{@link KilnRecipe}</u>'s detail.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public record KilnRecipeInput(@NotNull ItemStack stack) implements RecipeInput
{
    @Override
    public @NotNull ItemStack getItem(int index) { return stack; }
    
    @Override
    public int size() { return 1; }
}
