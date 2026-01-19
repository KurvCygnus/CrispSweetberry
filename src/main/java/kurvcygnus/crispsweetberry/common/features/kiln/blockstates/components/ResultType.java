package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

public enum ResultType
{ 
    CONTINUE,
    BALANCING,
    SKIP,
    INVALID,
    BLAST_TIP;
    
    public static ResultType toEnum(int index) { return ResultType.values()[index]; }
}
