package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

public enum ProgressTrend
{
    INCREASE,
    DECREASE,
    NEUTRAL;
    
    public static ProgressTrend toEnum(int index) { return ProgressTrend.values()[index]; }
}
