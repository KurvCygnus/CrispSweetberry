//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.integration;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.CalculationResult;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.LogicalResult;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.VisualTrend;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants.NAN;

public final class KilnCarriableExtensions
{
    public interface IKilnCarriableBlockEntityBridge
    {
        @CheckReturnValue @NotNull KilnBlockEntityContext onCarriedSequence(
            @NotNull ServerLevel level,
            @NotNull BlockPos pos,
            @NotNull BlockState state,
            @NotNull ServerPlayer player
        );
        
        void carryTick(@NotNull ServerLevel level, long carryingTime, @NotNull KilnBlockEntityContext context, @NotNull BlockPos pos);
    }
    
    public interface IKilnCarriableCalculatorBridge
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
        @NotNull KilnBlockEntity.ProcessionState state
    ) {}
    
    public record AtomicCalculationResult(int theoreticalProcessRound, @NotNull CalculationResult calculationResult) 
    {
        public static @NotNull AtomicCalculationResult withNoProduct(
            double realProgress,
            double visualProgress,
            @NotNull LogicalResult logicalResult,
            @NotNull VisualTrend trend
        ) { return new AtomicCalculationResult(NAN, new CalculationResult(realProgress, visualProgress, logicalResult, trend)); }
    }
}
