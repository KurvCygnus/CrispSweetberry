package kurvcygnus.crispsweetberry.datagen.utils;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public final class CrispLootUtils
{
    private CrispLootUtils() { throw new IllegalAccessError(); }
    
    public static @NotNull LootTable.Builder initLootPool(@NotNull Supplier<LootPool.Builder> pool)
    {
        Objects.requireNonNull(pool, "Param \"pool\" must not be null!");
        return LootTable.lootTable().withPool(pool.get());
    }
}