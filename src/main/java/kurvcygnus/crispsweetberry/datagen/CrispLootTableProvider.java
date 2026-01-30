package kurvcygnus.crispsweetberry.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class CrispLootTableProvider extends LootTableProvider
{
    public CrispLootTableProvider(@NotNull PackOutput output, @NotNull List<SubProviderEntry> subProviders,
        @NotNull CompletableFuture<HolderLookup.Provider> registries)
            {
                super(output, Collections.emptySet(), subProviders, registries);
            }
}
