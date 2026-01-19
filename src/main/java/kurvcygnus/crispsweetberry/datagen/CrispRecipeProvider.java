package kurvcygnus.crispsweetberry.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class CrispRecipeProvider extends RecipeProvider
{
    public CrispRecipeProvider(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> registries)
    {
        super(output, registries);
    }
}
