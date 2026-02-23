//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.vanilla;

import kurvcygnus.crispsweetberry.common.features.coins.api.AbstractCoinStackBlock;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Kurv Cygnus
 * @apiNote Reference implementation for vanilla coin types.
 * @since 1.0 Release
 */
public final class VanillaCoinStackBlock extends AbstractCoinStackBlock<VanillaCoinTypes>
{
    public VanillaCoinStackBlock(@NotNull Lazy<VanillaCoinTypes> lazyCoinTypeSupplier)
        { super(Objects.requireNonNull(lazyCoinTypeSupplier.get(), "Field \"coinTypeSupplier\" must not be null!")); }
}
