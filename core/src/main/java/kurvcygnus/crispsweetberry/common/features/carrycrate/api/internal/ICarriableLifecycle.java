//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal;

import org.jetbrains.annotations.Range;

/**
 * This represents the lifecycle part of carry crate system.
 * <br><br>
 * <h3><b>About Penalty Rate:</b></h3>
 * <br>
 * Penalty Rate is used by Carry Crate, which is a <u>{@link net.minecraft.world.item.BlockItem BlockItem}</u>, despite it has durability.
 * <br>
 * Carry Crate will gradually went break when it holds content, and its durability decides the chance of content drop.<br>
 * <b>And Penalty Rate decides the speed of durability's decreasement</b>, it represents the ticks that how much ticks will 1 durability spent.
 * <br>
 * <b>The smaller Penalty Rate is, the faster durability drops</b>.
 * @since 1.0 Release
 * @see kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity.ISimpleBlockEntityPenaltyLogic Container Item Based Penalty Logic Implementation
 * @author Kurv Cygnus
 */
public interface ICarriableLifecycle
{
    int DEFAULT_PENALTY_RATE = 20;
    int NO_PENALTY = 0;
    
    @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate();
    
    default boolean causesOverweight() { return true; }
}
