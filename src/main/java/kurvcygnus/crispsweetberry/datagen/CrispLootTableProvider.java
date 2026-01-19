package kurvcygnus.crispsweetberry.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class CrispLootTableProvider extends LootTableProvider
{
    public CrispLootTableProvider(@NotNull PackOutput output,
        @NotNull Set<ResourceKey<LootTable>> requiredTables,
        @NotNull List<SubProviderEntry> subProviders,
        @NotNull CompletableFuture<HolderLookup.Provider> registries)
            {
                super(output, requiredTables, subProviders, registries);
            }
}
