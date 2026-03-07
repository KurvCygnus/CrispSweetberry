//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.block;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryBlockStackable;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

/**
 * This is the basic of Block Adapters, which doesn't include any usable logics.
 *
 * @param <B> The block this adapter takes responsibility of.
 * @author Kurv Cygnus
 * @see SimpleBlockCarryAdapter Utility Adapter
 * @since 1.0 Release
 */
public abstract class AbstractBlockCarryAdapter<B extends Block> extends AbstractCarryAdapter implements ICarryBlockStackable
{
    /**
     * @apiNote During <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter 
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code block} during <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u> 
     * will throw <u>{@link NullPointerException}</u></b>.
     */
    private final @Nullable B block;
    
    public AbstractBlockCarryAdapter(@Nullable B block) { this.block = block; }
    
    @Override public abstract @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getPenaltyRate();
    
    @Override public abstract @Range(from = 1, to = Integer.MAX_VALUE) int getAcceptableCount();
    
    /**
     * @apiNote During <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code #getBlock()} during <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>
     * will throw <u>{@link NullPointerException}</u>, and you shouldn't use this at most cases.</b>
     */
    protected final @NotNull B getBlock() 
    {
        Objects.requireNonNull(
            block,
            """
                Assertion failed: Field "block" happens to be null, this shouldn't be happen, which usually means
                method is called at improper time, with improper param. %s
                """.
                formatted(MiscConstants.FEEDBACK_MESSAGE)
        );
        
        return block;
    }
}
