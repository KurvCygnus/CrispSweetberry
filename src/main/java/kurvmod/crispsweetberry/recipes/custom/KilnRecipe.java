package kurvmod.crispsweetberry.recipes.custom;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

//WIP
//DOC
//TODO
public final class KilnRecipe extends AbstractCookingRecipe
{
    public KilnRecipe(RecipeType<?> type, String group, CookingBookCategory category, Ingredient ingredient, ItemStack result, float experience)
    {
        super(type, group, CookingBookCategory.BLOCKS, ingredient, result, experience, 0);
    }
    
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return RecipeSerializer.SMELTING_RECIPE; }
}
