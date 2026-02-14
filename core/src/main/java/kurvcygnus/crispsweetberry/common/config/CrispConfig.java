//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.config;

import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
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
    
    private static final String CONFIG_DEBUG_KILN_BE = "crispsweetberry.config.debug.kiln_be";
    private static final String CONFIG_DEBUG_KILN_CAL = "crispsweetberry.config.debug.kiln_cal";
    private static final String CONFIG_DEBUG_KILN_EVENT = "crispsweetberry.config.debug.kiln_event";
    
    @AutoI18n(value = {
        "en_us = [DEBUG ONLY] Kiln: BlockEntity Debug Log",
        "lol_us = [DAVZ] WARM BOZ: BlarkAntity Tezt Stuff",
        "zh_cn = [调试] 窑炉: BlockEntity 调试日志"
        },
        key = CONFIG_DEBUG_KILN_BE
    )
    public static final Component KILN_BE_DEBUG_TEXT = Component.translatable(CONFIG_DEBUG_KILN_BE);
    
    @AutoI18n(value = {
        "en_us = [DEBUG ONLY] Kiln: Calculator Debug Log",
        "lol_us = [DAVZ] WARM BOZ: Kaqlaytor Tezt Stuff",
        "zh_cn = [调试] 窑炉: Calculator 调试日志"
        },
        key = CONFIG_DEBUG_KILN_CAL
    )
    public static final Component KILN_BE_CAL_DEBUG_TEXT = Component.translatable(CONFIG_DEBUG_KILN_CAL);
    
    @AutoI18n(value = {
        "en_us = [DEBUG ONLY] Kiln: Cache Event Debug Log",
        "lol_us = [DAVZ] WARM BOZ: EVENT BUZ GOGOGO OwO",
        "zh_cn = [调试] 窑炉: 缓存事件调试日志"
        },
        key = CONFIG_DEBUG_KILN_EVENT
    )
    public static final Component KILN_EVENT_DEBUG_TEXT = Component.translatable(CONFIG_DEBUG_KILN_EVENT);
    
    static
    {
        KILN_BE_DEBUG = BUILDER.
            comment("Kiln: BlockEntity Debug Log Display Toggle").
            translation(CrispDefUtils.unwrapTextKey(KILN_BE_DEBUG_TEXT)).
            define("kilnBEDebug", false);
        
        KILN_BE_CAL_DEBUG = BUILDER.
            comment("Kiln: Calculator Debug Log Display Toggle").
            translation(CrispDefUtils.unwrapTextKey(KILN_BE_CAL_DEBUG_TEXT)).
            define("kilnBECalDebug", false);
        
        KILN_EVENT_DEBUG = BUILDER.
            comment("Kiln: Event Debug Log Display Toggle").
            translation(CONFIG_DEBUG_KILN_EVENT).
            define("kilnEventDebug", false);
    }
    
    public static final ModConfigSpec SPEC = BUILDER.build();
}
