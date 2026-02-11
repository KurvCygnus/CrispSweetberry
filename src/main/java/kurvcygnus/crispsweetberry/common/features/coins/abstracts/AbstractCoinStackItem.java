//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins.abstracts;

import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinStackItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * This is the basic of stacked coin items, it is only meant to be thrown away, destroyed or got placed, 
 * it can't be disassembled into <u>{@link AbstractCoinItem coins}</u>.
 *
 * @author Kurv Cygnus
 * @implNote The reason we keep <u>{@link AbstractCoinStackItem}</u> and <u>{@link VanillaCoinStackItem VanillaCoinStackItem}</u>
 * separated is that we are considering using annotation processors to solve boilerplate problems in future developing.
 * @see VanillaCoinStackItem Universal Implementation
 * @since 1.0 Release
 */
public abstract class AbstractCoinStackItem<C extends ICoinType<C>> extends BlockItem
{
    private C coinType = null;
    
    /**
     * @apiNote Lazy initialization is required here to avoid registry order issues.<br>
     * {@link ICoinType} references Item/Block,
     * while Item/Block also need to resolve their CoinType.<br>
     * Eager initialization will cause <u>{@link NullPointerException null access}</u> or <u>{@link IllegalStateException incomplete registration}</u>.
     */
    private final @NotNull Lazy<C> lazyCoinTypeSupplier = Lazy.of(this::initCoinType);
    
    @SuppressWarnings("unused")//! Only for vanilla CODEC.
    private AbstractCoinStackItem(@Nullable Block block, @Nullable Properties properties) { this(); }
    
    @SuppressWarnings("DataFlowIssue")//! To avoid footgun, we use lazy load with overrides.
    protected AbstractCoinStackItem() { super(null, new Properties().stacksTo(16)); }
    
    /**
     * @implNote  This method is used by <u>{@link BlockItem}</u> to markedLogger the instance of block.<br>
     * We override this to implement lazy loading block to avoid footgun.
     */
    @Override
    public @NotNull Block getBlock() { return this.getCoinType().stackBlock(); }
    
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
            return Objects.requireNonNull(coinType, "Field \"coinType\" was initialized as null!");
        }
        
        return this.coinType;
    }
}
