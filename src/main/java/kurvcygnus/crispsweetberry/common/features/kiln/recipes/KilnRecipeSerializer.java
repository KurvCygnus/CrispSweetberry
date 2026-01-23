package kurvcygnus.crispsweetberry.common.features.kiln.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * This is the heart of <b>jsonifying</b> the <b>recipe of kiln</b>.
 * @see KilnRecipe Recipe Part
 * @see KilnRecipeType Type Declaration
 * @see KilnRecipeInput Recipe Input Part
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
public final class KilnRecipeSerializer implements RecipeSerializer<KilnRecipe>
{
    /**
     * <b><u>{@link com.mojang.serialization.MapCodec MapCodec}</u></b> is the protagonist of <b>local serialization</b>.<br>
     * <b><i>It defines the format of recipe JSON</b></i>.
     */
    private final MapCodec<KilnRecipe> mapCodec;
    /**
     * <b><u>{@link net.minecraft.network.codec.StreamCodec StreamCodec}</b></u> is the director of <b>network sync</b>.<br>
     * <b><i>It makes sure that data from server won't go wrong</b></i>.
     */
    private final StreamCodec<RegistryFriendlyByteBuf, KilnRecipe> streamCodec;
    
    /**
     * The constructor method to <b>define the codecs themselves</b>.
     */
    public KilnRecipeSerializer()
    {
        this.mapCodec = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(KilnRecipe::getIngredient),
            ItemStack.CODEC.fieldOf("result").forGetter(KilnRecipe::getResult),
            Codec.DOUBLE.fieldOf("cookTickRateMultiFactor").orElse(1D).forGetter(KilnRecipe::getProcessFactor),
            Codec.FLOAT.fieldOf("experience").orElse(0F).forGetter(KilnRecipe::getExperience)
            ).apply(instance, KilnRecipe::new)
        );
        
        //* "fromNetwork" and "toNetwork" correspond to I/O respectively.
        //! WARN: You should make sure that the content of both two methods are synchronized all the time when adding new fields.
        this.streamCodec = StreamCodec.of(this::toNetwork, this::fromNetwork);
    }
    
    @Override
    public @NotNull MapCodec<KilnRecipe> codec() { return this.mapCodec; }
    
    @Override
    public @NotNull StreamCodec<RegistryFriendlyByteBuf, KilnRecipe> streamCodec() { return this.streamCodec; }
    
    //? TODO: Group implementation for JEI/REI compatability.
    private void toNetwork(RegistryFriendlyByteBuf buffer, KilnRecipe recipe)
    {
        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.getIngredient());
        ItemStack.STREAM_CODEC.encode(buffer, recipe.getResult());
        buffer.writeDouble(recipe.getProcessFactor());
        buffer.writeFloat(recipe.getExperience());
    }
    
    private KilnRecipe fromNetwork(RegistryFriendlyByteBuf buffer)
    {
        Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
        ItemStack stack = ItemStack.STREAM_CODEC.decode(buffer);
        double processFactor = buffer.readDouble();
        float experience = buffer.readFloat();
        
        return new KilnRecipe(ingredient, stack, processFactor, experience);
    }
}
