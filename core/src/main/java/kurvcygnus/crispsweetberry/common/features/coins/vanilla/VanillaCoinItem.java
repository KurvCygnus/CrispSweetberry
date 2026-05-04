//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.vanilla;

import kurvcygnus.crispsweetberry.common.features.coins.api.AbstractCoinItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * @apiNote Reference implementation for vanilla coin types.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class VanillaCoinItem extends AbstractCoinItem<VanillaCoinType>
{
    private final @NotNull Supplier<VanillaCoinType> coinTypeSupplier;
    
    public VanillaCoinItem(@NotNull Supplier<VanillaCoinType> coinTypeSupplier)
    {
        this.coinTypeSupplier = coinTypeSupplier;
        Objects.requireNonNull(coinTypeSupplier, "Field \"coinTypeSupplier\" must not be null!");
    }
    
    @Override protected @NotNull VanillaCoinType initCoinType() { return coinTypeSupplier.get(); }
}
