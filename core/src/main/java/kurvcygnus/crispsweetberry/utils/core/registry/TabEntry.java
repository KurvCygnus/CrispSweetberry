//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.core.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A simple unionData holder for <u>{@link kurvcygnus.crispsweetberry.client.init.CrispCreativeTabsRegistryEvent tab registry}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public record TabEntry(@NotNull Supplier<? extends Item> itemSupplier, @NotNull ResourceKey<CreativeModeTab> tab, boolean condition)
{
    public TabEntry
    {
        Objects.requireNonNull(itemSupplier, "Param \"itemSupplier\" must not be null!");
        Objects.requireNonNull(tab, "Param \"tab\" must not be null!");
    }
}
