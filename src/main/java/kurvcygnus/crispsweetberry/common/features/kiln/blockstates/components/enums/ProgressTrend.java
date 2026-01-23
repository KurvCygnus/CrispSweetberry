package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums;

public enum ProgressTrend
{
    INCREASE,
    DECREASE,
    NEUTRAL;
    
    public static ProgressTrend toEnum(int index) { return ProgressTrend.values()[index]; }
}
