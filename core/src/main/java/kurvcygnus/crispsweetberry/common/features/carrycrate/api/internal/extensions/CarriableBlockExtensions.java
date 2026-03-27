//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.SimpleBlockCarryAdapter;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Range;

/**
 * A collection class holds the all unique abilities of <u>{@link AbstractBlockCarryAdapter block adapters}</u>.
 * @since 1.0 Release
 * @see CarriableExtensions Basics
 * @see CarriableBlockEntityExtensions BlockEntity Basics
 * @see AbstractBlockCarryAdapter Base Block Adapter
 */
@ApiStatus.Internal
public final class CarriableBlockExtensions
{
    /**
     * This interface holds the ability of <b>boxing multi blocks for a single carry crate</b>.
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @see AbstractBlockCarryAdapter Base Block Adapter
     */
    public interface ICarryBlockStackable
    {
        /**
         * Gets the max count of this adapter's <u>{@link net.minecraft.world.level.block.Block bounded block}</u> 
         * can take.<br><br>
         * 
         * The higher it is, the more carry crate could take.
         * @apiNote {@code 1} is the default value of 
         * <u>{@link SimpleBlockCarryAdapter Universal Block Adapter}</u>.
         */
        @Range(from = 1, to = Integer.MAX_VALUE) int getAcceptableCount();
    }
}
