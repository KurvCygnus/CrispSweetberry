package kurvcygnus.crispsweetberry.datagen;

import kurvcygnus.crispsweetberry.common.features.coins.abstracts.ICoinType;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinTypes;
import kurvcygnus.crispsweetberry.datagen.shared.BaseCoinRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class VanillaCoinRecipeProvider extends BaseCoinRecipeProvider
{
    public VanillaCoinRecipeProvider(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> registries) { super(output, registries); }
    
    @Override
    protected @NotNull List<? extends ICoinType<?>> getCoinTypeList() { return Arrays.asList(VanillaCoinTypes.VALUES); }
}
