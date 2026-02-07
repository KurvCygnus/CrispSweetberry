package kurvcygnus.crispsweetberry.common.config;

import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;

import static net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import static net.neoforged.neoforge.common.ModConfigSpec.Builder;

//? TODO: Bound with GUI

/**
 * This defines the option for Crisp Sweetberry.
 * @see kurvcygnus.crispsweetberry.common.config.gui.CrispConfigScreen Screen
 * @since Release 1.0
 * @author Kurv Cygnus
 */
public final class CrispConfig
{
    private CrispConfig() { throw new IllegalAccessError(); }
    
    private static final Builder BUILDER = new Builder();
    
    public static final BooleanValue KILN_BE_DEBUG;
    public static final BooleanValue KILN_BE_CAL_DEBUG;
    public static final BooleanValue KILN_EVENT_DEBUG;
    
    @AutoI18n({
        "en_us -> [DEBUG ONLY] Kiln: BlockEntity Debug Log",
        "lol_us -> [DAVZ] WARM BOZ: BlarkAntity Tezt Stuff",
        "zh_cn -> [调试] 窑炉: BlockEntity 调试日志"
    })
    public static final Component KILN_BE_DEBUG_TEXT = Component.translatable("crispsweetberry.config.debug.kiln_be");
    
    @AutoI18n({
        "en_us -> [DEBUG ONLY] Kiln: Calculator Debug Log",
        "lol_us -> [DAVZ] WARM BOZ: Kaqlaytor Tezt Stuff",
        "zh_cn -> [调试] 窑炉: Calculator 调试日志"
    })
    public static final Component KILN_BE_CAL_DEBUG_TEXT = Component.translatable("crispsweetberry.config.debug.kiln_cal");
    
    @AutoI18n({
        "en_us -> [DEBUG ONLY] Kiln: Cache Event Debug Log",
        "lol_us -> [DAVZ] WARM BOZ: EVENT BUZ GOGOGO OwO",
        "zh_cn -> [调试] 窑炉: 缓存事件调试日志"
    })
    public static final Component KILN_EVENT_DEBUG_TEXT = Component.translatable("crispsweetberry.config.debug.kiln_event");
    
    static
    {
        KILN_BE_DEBUG = BUILDER.
            comment("Kiln: BlockEntity Debug Log Display Toggle").
            translation(KILN_BE_DEBUG_TEXT.getString()).
            define("kilnBEDebug", false);
        
        KILN_BE_CAL_DEBUG = BUILDER.
            comment("Kiln: Calculator Debug Log Display Toggle").
            translation(KILN_BE_CAL_DEBUG_TEXT.getString()).
            define("kilnBECalDebug", false);
        
        KILN_EVENT_DEBUG = BUILDER.
            comment("Kiln: Event Debug Log Display Toggle").
            translation("crispsweetberry.config.debug.kiln_event").
            define("kilnEventDebug", false);
    }
    
    public static final ModConfigSpec SPEC = BUILDER.build();
}
