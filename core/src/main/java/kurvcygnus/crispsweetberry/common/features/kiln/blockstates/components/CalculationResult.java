//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import org.jetbrains.annotations.*;

import java.util.Map;
import java.util.function.Supplier;

/**
 * A simple value object for <u>{@link KilnProgressCalculator}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public record CalculationResult(
    @Range(from = 0, to = 1) double currentRealProgress,
    @Range(from = 0, to = 1) double currentVisualProgress, 
    @NotNull KilnEnumCollections.LogicalResult logicalResult,
    @NotNull KilnEnumCollections.VisualTrend trend
) implements INestedPrintable
{
    @Contract("_, _ -> new")
    public static @NotNull CalculationResult unexpectedResult(@Range(from = 0, to = 1) double currentRealProgress, @Range(from = 0, to = 1) double currentVisualProgress)
    {
        return new CalculationResult(
            currentRealProgress,
            currentVisualProgress,
            KilnEnumCollections.LogicalResult.INVALID,
            KilnEnumCollections.VisualTrend.NORMAL
        );
    }
    
    @Override public @NotNull @Unmodifiable Map<String, Supplier<@Nullable Object>> getFields()
    {
        return INestedPrintable.buildFieldMap(
            map ->
            {
                map.put("realProgress", this::currentRealProgress);
                map.put("visualProgress", this::currentVisualProgress);
                map.put("logicalResult", this.logicalResult::name);
                map.put("trend", this.trend::name);
            }
        );
    }
}
