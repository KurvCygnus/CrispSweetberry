package kurvcygnus.crispsweetberry.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import static net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import static net.neoforged.neoforge.common.ModConfigSpec.Builder;

public class CrispConfig
{
    private static final Builder BUILDER = new Builder();
    
    public static final BooleanValue KILN_BE_DEBUG;
    public static final BooleanValue KILN_BE_CAL_DEBUG;
    
    static
    {
        KILN_BE_DEBUG = BUILDER.
            comment("Kiln: BlockEntity Debug Log Display Toggle").
            translation("crispsweetberry.config.configDebug.kiln_be").
            define("kilnBEDebug", false);
        
        KILN_BE_CAL_DEBUG = BUILDER.
            comment("Kiln: Calculator Debug Log Display Toggle").
            translation("crispsweetberry.config.configDebug.kiln_cal").
            define("kilnBECalDebug", false);
    }
    
    public static final ModConfigSpec SPEC = BUILDER.build();
}
