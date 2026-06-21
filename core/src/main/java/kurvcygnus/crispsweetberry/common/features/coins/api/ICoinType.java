//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.api;

import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * This is the contract of making a new coin variant.
 * @apiNote This self-bounded generic is NOT optional.
 * Without it, CoinItem / CoinStackItem / CoinStackBlock may reference different CoinType instances, causing silent logic corruption
 * that cannot be detected at compile time.
 * @param <C> <u>{@link ICoinType}</u> itself, here used <a href="https://en.wikipedia.org/wiki/Curiously_recurring_template_pattern">{@code F-bounded quantification}</a>
 * technique to make sure that a series of coin's detailed type must be same.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public interface ICoinType<C extends ICoinType<C>>
{
    @NotNull ResourceLocation getId();
    
    @NotNull AbstractCoinStackBlock<C> stackBlock();
    @NotNull AbstractCoinStackItem<C> stackItem();
    @NotNull AbstractCoinItem<C> coinItem();
    @NotNull Item nuggetItem();
    
    @Range(from = 1, to = Integer.MAX_VALUE) int getExperience();
    @Range(from = 0, to = 1) float getPenaltyRate();
    @Range(from = 0, to = (int) Float.MAX_VALUE) float getStrength();
    
    /**
     * @apiNote Mainly used for <u>{@link VanillaCoinType#COPPER copper}</u> and
     * <u>{@link VanillaCoinType#DIAMOND diamond}</u> coins' in-game accessibility.
     */
    default boolean shouldAppear() { return true; }
}
