package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

public record CalculationResult(
        double currentRealProgress,
        double currentVisualProgress, 
        ResultType resultType,
        ProgressTrend trend
        ) {}
