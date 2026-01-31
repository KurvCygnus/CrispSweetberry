package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.LogicalResult;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.VisualTrend;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A simple value object for <u>{@link KilnProgressCalculator}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public record CalculationResult(double currentRealProgress, double currentVisualProgress, LogicalResult logicalResult, VisualTrend trend)
{
    @Contract("_, _ -> new")
    public static @NotNull CalculationResult unexpectedResult(double currentRealProgress, double currentVisualProgress)
    {
        return new CalculationResult(
            currentRealProgress,
            currentVisualProgress,
            LogicalResult.INVALID,
            VisualTrend.NORMAL
        );
    }
}
