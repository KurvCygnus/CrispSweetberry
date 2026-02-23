//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry;

import kurvcygnus.crispsweetberry.common.features.coins.api.ICoinType;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinTypes;
import kurvcygnus.crispsweetberry.shared.BaseCoinRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

final class VanillaCoinRecipeProvider extends BaseCoinRecipeProvider
{
    VanillaCoinRecipeProvider(@NotNull PackOutput output, @NotNull CompletableFuture<HolderLookup.Provider> registries) { super(output, registries); }
    
    @Override
    protected @NotNull List<? extends ICoinType<?>> getCoinTypeList() { return Arrays.asList(VanillaCoinTypes.VALUES); }
}
