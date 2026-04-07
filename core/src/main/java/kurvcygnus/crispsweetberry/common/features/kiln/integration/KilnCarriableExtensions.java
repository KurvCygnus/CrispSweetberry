//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.integration;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.CalculationResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnEnumCollections.*;
import static kurvcygnus.crispsweetberry.utils.constants.ExampleSlotConstants.ERROR;

public final class KilnCarriableExtensions
{
    public interface IBlockEntityBridge
    {
        @CheckReturnValue @NotNull KilnBlockEntityContext onCarriedSequence(
            @NotNull ServerLevel level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @NotNull ServerPlayer player
        );
        
        void onPlacedProcess(@NotNull ServerLevel level, long carryingTime, @NotNull KilnBlockEntityContext context, @NotNull BlockPos pos);
    }
    
    public interface ICalculatorBridge
    {
        @CheckReturnValue double onCarriedSequence();
        
        @CheckReturnValue @NotNull AtomicCalculationResult statelessCalculate(@NotNull CalculationContext context);
    }
    
    public record KilnBlockEntityContext(boolean isLit, double realRate) {}
    
    public record CalculationContext(
        long carryingTime,
        double realProgress,
        double visualProgress,
        double realRate,
        @NotNull ProcessionState state
    ) {}
    
    public record AtomicCalculationResult(int theoreticalProcessRound, @NotNull CalculationResult calculationResult) 
    {
        public static @NotNull AtomicCalculationResult withNoProduct(
            double realProgress,
            double visualProgress,
            @NotNull LogicalResult logicalResult,
            @NotNull VisualTrend trend
        ) { return new AtomicCalculationResult(ERROR, new CalculationResult(realProgress, visualProgress, logicalResult, trend)); }
    }
}
