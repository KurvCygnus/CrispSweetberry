//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.client;

import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import net.minecraft.network.chat.Component;

public final class CrispClientLiterals
{
    private CrispClientLiterals() { throw new IllegalAccessError("Class \"CrispClientLiterals\" is not meant to be instantized!"); }
    
    public static final String CRISP_CONTROL_MENU_CATEGORY_KEY = "crispsweetberry.menu.control.title";
    
    @AutoI18n(value = {
        "en_us = Crisp Sweetberry-In Game Keymappings",
        "lol_us = TAZTY FRUT IN GAYM BUTTONZ",
        "zh_cn = 澄莓物语-游戏按键"
        },
        key = CRISP_CONTROL_MENU_CATEGORY_KEY
    )
    public static final Component CRISP_CONTROL_MENU_CATEGORY = Component.translatable(CRISP_CONTROL_MENU_CATEGORY_KEY);
}
