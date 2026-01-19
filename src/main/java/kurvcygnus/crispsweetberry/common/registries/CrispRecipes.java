package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipeSerializer;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipeType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CrispRecipes
{
    private CrispRecipes() { throw new IllegalAccessError(); }
    
    public static final DeferredRegister<RecipeType<?>> CRISP_RECIPE_REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, CrispSweetberry.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> CRISP_SERIALIZER_REGISTER = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CrispSweetberry.MOD_ID);
    
    public static final DeferredHolder<RecipeType<?>, KilnRecipeType> KILN_RECIPE_TYPE = CRISP_RECIPE_REGISTER.register("kiln", () -> KilnRecipeType.INSTANCE);
    
    public static final DeferredHolder<RecipeSerializer<?>, KilnRecipeSerializer> KILN_SERIALIZER = CRISP_SERIALIZER_REGISTER.register("kiln", KilnRecipeSerializer::new);
}
