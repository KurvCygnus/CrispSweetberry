package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import kurvcygnus.crispsweetberry.common.registries.CrispRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * This is the base of the existence of the whole kiln recipe.<br>
 * <b><i>It defines the data and behavior a kiln recipe contains</b></i>.
 * @see KilnRecipeSerializer Serializer
 * @see KilnRecipeType Type Declaration
 * @see KilnRecipeInput Recipe Input Part
 * @author Kurv Cygnus
 * @since 1.0 Release
 * 
 * @implNote Kiln WILL NOT support recipe unlock, or recipe book function.<br>
 * Reasons:<ul>
 * <li>
 *     Supporting recipe unlocking inherently requires full integration with <u>{@link net.minecraft.world.item.crafting.RecipeManager RecipeManager}</u>,
 *     whose implementation is completely different from 
 *     <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.events.KilnRecipeCacheEvent KilnRecipeCacheEvent}</u>,
 *     and the event itself is the heart of <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity KilnBlockEntity}</u>, 
 *     <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator KilnProcessCalculator}</u>, 
 *     would require a fundamental architectural redesign.
 * </li>
 * <li>
 *     The recipe that recipe book requires must be static, which is opposite to the concept of {@code KilnRecipe}, in consideration of both recipe 
 *     source and process calculation rules.
 * </li>
 * <li>
 *     Doing all of this means we also need to {@code implements} <u>{@link net.minecraft.world.inventory.RecipeBookMenu RecipeBookMenu}</u>
 *     in <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu KilnMenu}</u>, and 
 *     <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity kiln itself}</u> is a multi-slots container,
 *     which is obviously ultimately complex.
 * </li>
 * </ul>
 * <i><b>
 * In this case, consciously giving up certain vanilla integrations is a deliberate
 * and necessary design decision.
 * </b></i>
 */
public final class KilnRecipe implements Recipe<KilnRecipeInput>
{
    //  region
    //* Fields & Constructors
    private final Ingredient ingredient;
    private final ItemStack result;
    
    /**
     * The procession of kiln is percentage-based, this variable holds <b>the factor of influencing the final procession rate of kiln</b>,
     * which is calculated by cookTime.
     */
    private final double processFactor;
    private final float experience;
    
    public final RecipeState state;
    
    public enum RecipeState { NORMAL, NULL, TIP }
    
    /**
     * The contractor method for <b>creating a recipe</b>, and <b>serialization</b>.
     */
    public KilnRecipe(@NotNull Ingredient ingredient, @NotNull ItemStack result, double processFactor, float experience)
    {
        this.ingredient = ingredient;
        this.result = result;
        this.processFactor = processFactor;
        this.experience = experience;
        this.state = RecipeState.NORMAL;
    }
    
    private KilnRecipe(RecipeState state)
    {
        this.ingredient = Ingredient.EMPTY;
        this.result = ItemStack.EMPTY;
        this.processFactor = 0D;
        this.experience = 0F;
        this.state = state;
    }
    
    /**
     * @apiNote This static factory constructor method is specifically used for
     * <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity KilnBlockEntity}</u> 
     * to avoid the confusion of null recipe<i>(Boundary cases)</i> and no match recipe<i>(Makes sense)</i>.
     */
    public static @NotNull NonNullList<KilnRecipe> noRecipeList() { return NonNullList.withSize(1, new KilnRecipe(RecipeState.NULL)); }
    
    /**
     * @apiNote This static factory constructor method is specifically used for
     * <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity KilnBlockEntity}</u> 
     * to avoid the confusion of null recipe<i>(Boundary cases)</i> and no match recipe<i>(Makes sense)</i>.
     */
    public static @NotNull KilnRecipe noRecipe() { return new KilnRecipe(RecipeState.NULL); }
    
    /**
     * @apiNote This static factory constructor method is specifically used for
     * <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity KilnBlockEntity}</u> 
     * to tip players the fact that blasting recipes can't be handled by kiln.
     */
    public static @NotNull KilnRecipe tipRecipe() { return new KilnRecipe(RecipeState.TIP); }
    //endregion
    
    //  region
    //* Recipe Basics
    @Override
    public boolean matches(@NotNull KilnRecipeInput input, @NotNull Level level) { return this.ingredient.test(input.stack()); }
    
    @Override
    public @NotNull ItemStack assemble(@NotNull KilnRecipeInput input, HolderLookup.@NotNull Provider registries) { return this.result.copy(); }
    
    @Override
    public boolean canCraftInDimensions(int width, int height) { return true; }
    
    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) { return this.result.copy(); }
    
    @Override
    public @NotNull RecipeSerializer<?> getSerializer() { return CrispRecipes.KILN_SERIALIZER.get(); }
    
    @Override
    public @NotNull RecipeType<?> getType() { return KilnRecipeType.INSTANCE; }
    //endregion
    
    //  region
    //* Helpers & Getters
    @Override
    public String toString() { return String.format("Ingredient: %s, Result: %s, Factor: %f; ", ingredient, result, processFactor); }
    
    /**
     * @apiNote This is used for recipe validation.
     * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator#calculateRates Usage  
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")//! We shouldn't reverse method, the name of inverted method will lead to confusion.
    public static boolean isEmptyRecipe(@NotNull KilnRecipe recipe) { return recipe.state.equals(RecipeState.NULL); }
    
    public static boolean isTipRecipe(@NotNull KilnRecipe recipe) { return recipe.state.equals(RecipeState.TIP); }
    
    public Ingredient getIngredient() { return this.ingredient; }
    
    public ItemStack getResult() { return this.result; }
    
    public double getProcessFactor() { return this.processFactor; }
    
    public float getExperience() { return experience; }
    //endregion
}
