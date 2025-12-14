package kurvmod.crispsweetberry.recipes;

import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CrispRecipes
{
    public static final DeferredRegister<RecipeType<?>> CRISP_RECIPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, CrispSweetberry.MOD_ID);
    
}
