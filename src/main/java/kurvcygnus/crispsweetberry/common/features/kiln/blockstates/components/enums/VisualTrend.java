package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums;

public enum VisualTrend
{
    NORMAL,
    BALANCE,
    BURST,
    TIP,
    NONE;
    
    public static VisualTrend toEnum(int index) { return VisualTrend.values()[index]; }
}
