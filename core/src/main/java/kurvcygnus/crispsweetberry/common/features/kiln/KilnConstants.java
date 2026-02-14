//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln;

import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.utils.ui.collects.CrispIntRanger;
import net.minecraft.network.chat.Component;

/**
 * This class mainly maintains slots contants of Kiln.
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
public final class KilnConstants
{
    private KilnConstants() { throw new IllegalAccessError("Class \"KilnConstants\" is not meant to be instantized!"); }
    
    @AutoI18n({
        "en_us = Kiln",
        "lol_us = IN DA WARM BOKZ",
        "zh_cn = 窑炉"
    })
    public static final Component CONTAINER__KILN = Component.translatable("crispsweetberry.container.kiln");
    
    @AutoI18n({
        "en_us = Total heat changed. Rebalancing heat distribution...",
        "lol_us = DA BIG BOKZ IZ TRYIN' 2 BEE NOT OP",
        "zh_cn = 总输入热量改变. 正在重分配热量..."
    })
    public static final Component UI__WIDGET__KILN_INFO_COOLDOWN = Component.translatable("crispsweetberry.ui.widget.kiln_info_cooldown");
    
    @AutoI18n({
        "en_us = Some materials are blasting recipe.\nKiln can't process such items that requires high heat.",
        "lol_us = BIG BOKZ KIN'T TAK THIZ!!!",
        "zh_cn = 部分原材料属于高炉原配方.\n窑炉无法处理高热量要求的原材料."
    })
    public static final Component UI__WIDGET__KILN_TIP_BLAST = Component.translatable("crispsweetberry.ui.widget.kiln_tip_blast");
    
    public static final int KILN_SLOT_COUNT_FOR_EACH_TYPE = 3;
    public static final int KILN_DEFAULT_SIZE = KILN_SLOT_COUNT_FOR_EACH_TYPE * 2;
    
    public static final int KILN_INPUT_START_INDEX = 0;
    public static final int KILN_INPUT_END_INDEX = 2;
    
    public static final int KILN_OUTPUT_START_INDEX = 3;
    public static final int KILN_OUTPUT_END_INDEX = 5;
    
    private static final int KILN_BACKPACK_START_INDEX = 6;
    private static final int KILN_BACKPACK_END_INDEX = 32;
    
    private static final int KILN_HOTBAR_START_INDEX = 33;
    private static final int KILN_HOTBAR_END_INDEX = 41;
    
    public static final CrispIntRanger KILN_INPUT_SLOTS_RANGE = CrispIntRanger.closed(KILN_INPUT_START_INDEX, KILN_INPUT_END_INDEX);
    public static final CrispIntRanger KILN_OUTPUT_SLOTS_RANGE = CrispIntRanger.closed(KILN_OUTPUT_START_INDEX, KILN_OUTPUT_END_INDEX);
    public static final CrispIntRanger KILN_BACKPACK_SLOTS_RANGE = CrispIntRanger.closed(KILN_BACKPACK_START_INDEX, KILN_BACKPACK_END_INDEX);
    public static final CrispIntRanger KILN_HOTBAR_SLOTS_RANGE = CrispIntRanger.closed(KILN_HOTBAR_START_INDEX, KILN_HOTBAR_END_INDEX);
    public static final CrispIntRanger KILN_INVENTORY_SLOTS_RANGE = CrispIntRanger.closed(KILN_BACKPACK_SLOTS_RANGE.getMin(), KILN_HOTBAR_SLOTS_RANGE.getMax());
}
