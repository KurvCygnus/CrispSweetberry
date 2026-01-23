package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.ProgressTrend;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.ResultType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record CalculationResult
    (double currentRealProgress, double currentVisualProgress, ResultType resultType, ProgressTrend trend)
        {
            @Contract("_, _ -> new")
            public static @NotNull CalculationResult unexpectedResult(double currentRealProgress, double currentVisualProgress)
            {
                return new CalculationResult(
                    currentRealProgress,
                    currentVisualProgress,
                    ResultType.INVALID,
                    ProgressTrend.NEUTRAL
                );
            }
        }
