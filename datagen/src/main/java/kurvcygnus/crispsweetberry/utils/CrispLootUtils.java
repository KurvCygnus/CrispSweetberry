//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils;

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