//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.utils.ui.CrispUIUtils;
import kurvcygnus.crispsweetberry.utils.ui.collects.CrispRanger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

/**
 * @since 1.0 Release
 */
public final class CarryCrateConstants
{
    private CarryCrateConstants() { throw new IllegalAccessError("Class \"CarryCrateConstants\" is not meant to be instantized!"); }
    
    public static final float OVERWEIGHT_FACTOR = 1F;
    public static final float MAX_ACCEPTABLE_FACTOR = 1.5F;
    
    public static final int CARRY_CRATE_MAX_DURABILITY = 120;
    public static final int PENALTY_QUANTITY = 5;
    
    private static final CrispRanger LOW_DURABILITY_RANGE = CrispRanger.closedOpen(0, CARRY_CRATE_MAX_DURABILITY / 3);
    private static final CrispRanger MEDIUM_DURABILITY_RANGE = CrispRanger.closedOpen(CARRY_CRATE_MAX_DURABILITY / 3, CARRY_CRATE_MAX_DURABILITY * 2 / 3);
    private static final CrispRanger HIGH_DURABILITY_RANGE = CrispRanger.closed(CARRY_CRATE_MAX_DURABILITY * 2 / 3, CARRY_CRATE_MAX_DURABILITY);
    
    public static final List<CrispRanger> DURABILITY_RANGERS = List.of(
        LOW_DURABILITY_RANGE,
        MEDIUM_DURABILITY_RANGE,
        HIGH_DURABILITY_RANGE
    );
    
    public static final int LOW_DURA_INDEX = 0;
    public static final int MEDIUM_DURA_INDEX = 1;
    public static final int HIGH_DURA_INDEX = 2;
    
    @AutoI18n({
        "en_us = On the verge of collapse...",
        "lol_us = NOOOOO QAQ",
        "zh_cn = 命悬一纸..."
    })
    public static final Component UI__CARRY_CRATE__LOW_DURABILITY_DESCRIPTION = CrispUIUtils.dimmedItalicText(
        "%s.ui.carry_crate.low_durability_description".formatted(CrispSweetberry.NAMESPACE)
    );
    
    @AutoI18n({
        "en_us = Showing some character",
        "lol_us = got sum wrinkuls",
        "zh_cn = 略显沧桑"
    })
    public static final Component UI__CARRY_CRATE__MEDIUM_DURABILITY_DESCRIPTION = CrispUIUtils.dimmedItalicText(
        "%s.ui.carry_crate.medium_durability_description".formatted(CrispSweetberry.NAMESPACE)
    );
    
    @AutoI18n({
        "en_us = Sturdy as a rock",
        "lol_us = HAERD",
        "zh_cn = 稳固如初"
    })
    public static final Component UI__CARRY_CRATE__HIGH_DURABILITY_DESCRIPTION = CrispUIUtils.dimmedItalicText(
        "%s.ui.carry_crate.high_durability_description".formatted(CrispSweetberry.NAMESPACE)
    );
    
    @AutoI18n({
        "en_us = Content:&ensp;",
        "lol_us = THINZ:&ensp;",
        "zh_cn = 内容:&ensp;"
    })
    public static final MutableComponent UI__CARRY_CRATE__CONTENT_PREFIX = CrispUIUtils.dimmedText(
        "%s.ui.carry_crate.content_prefix".formatted(CrispSweetberry.NAMESPACE)
    );
    
    @AutoI18n({
        "en_us = Contains&ensp;",
        "lol_us = I HAV&ensp;",
        "zh_cn = 装了&ensp;"
    })
    public static final MutableComponent UI_CARRY_CRATE__LAYER_PREFIX = CrispUIUtils.dimmedText(
        "%s.ui.carrycrate.layer_prefix".formatted(CrispSweetberry.NAMESPACE)
    );
    
    @AutoI18n({
        "en_us = &ensp;layers",
        "lol_us = &ensp;UwU",
        "zh_cn = &ensp;份"
    })
    public static final MutableComponent UI_CARRY_CRATE__LAYER_SUFFIX = CrispUIUtils.dimmedText(
        "%s.ui.carrycrate.layer_suffix".formatted(CrispSweetberry.NAMESPACE)
    );
}