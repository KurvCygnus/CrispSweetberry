//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.api;

import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This is the basic of coin items, which can store experience, with penalty as costs.
 * @implNote The reason we keep <u>{@link AbstractCoinItem}</u> and <u>{@link VanillaCoinItem VanillaCoinItem}</u>
 * separated is that we are considering using annotation processors to solve boilerplate problems in future developing.
 * @see VanillaCoinItem Vanilla Implementation
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public abstract class AbstractCoinItem<C extends ICoinType<C>> extends Item
{
    private C coinType = null;
    
     /**
     * @apiNote Lazy initialization is required here to avoid registry order issues.<br>
     * {@link ICoinType} references Item/Block,
     * while Item/Block also need to resolve their CoinType.<br>
     * Eager initialization will cause <u>{@link NullPointerException null access}</u> or <u>{@link IllegalStateException incomplete registration}</u>.
     */
    private final @NotNull Lazy<C> lazyCoinTypeSupplier = Lazy.of(this::initCoinType);
    
    @SuppressWarnings("unused")//! Only for vanilla CODEC stuff.
    private AbstractCoinItem(@Nullable Properties properties) { this(); }
    
    public AbstractCoinItem() { super(new Properties()); }
    
    /**
     * @apiNote Lazy initialization is required here to avoid registry order issues.<br>
     * {@link ICoinType} references Item/Block,
     * while Item/Block also need to resolve their CoinType.<br>
     * Eager initialization will cause <u>{@link NullPointerException null access}</u> or <u>{@link IllegalStateException incomplete registration}</u>.
     */
    protected abstract @NotNull C initCoinType();
    
    public final @NotNull C getCoinType()
    {
        if(this.coinType == null)
        {
            this.coinType = lazyCoinTypeSupplier.get();
            return Objects.requireNonNull(coinType, "Field \"getCoinType\" is initialized as null!");
        }
        
        return this.coinType;
    }
}
