package kurvcygnus.crispsweetberry.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import static net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import static net.neoforged.neoforge.common.ModConfigSpec.Builder;

//? TODO: Bound with GUI
public final class CrispConfig
{
    private CrispConfig() { throw new IllegalAccessError(); }
    
    private static final Builder BUILDER = new Builder();
    
    public static final BooleanValue KILN_BE_DEBUG;
    public static final BooleanValue KILN_BE_CAL_DEBUG;
    public static final BooleanValue KILN_EVENT_DEBUG;
    
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
        
        KILN_EVENT_DEBUG = BUILDER.
            comment("Kiln: Event Debug Log Display Toggle").
            translation("crispsweetberry.config.debug.kiln_event").
            define("kilnEventDebug", false);
    }
    
    public static final ModConfigSpec SPEC = BUILDER.build();
}
