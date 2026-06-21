//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate;

import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.lib.base.datastructure.RangeMap;
import kurvcygnus.crispsweetberry.lib.base.datastructure.Ranger;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.UIUtils;
import kurvcygnus.crispsweetberry.utils.constants.ComponentConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Supplier;

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
    
    @AutoI18n({
        "en_us = On the verge of collapse...",
        "lol_us = NOOOOO QAQ",
        "zh_cn = 命悬一纸..."
    })
    public static final Component UI__CARRY_CRATE__LOW_DURABILITY_DESCRIPTION = UIUtils.dimmedItalicText(
        DefinitionUtils.namespacedDotId(ComponentConstants.UI, ComponentConstants.CARRY_CRATE, "low_durability_description")
    );
    
    @AutoI18n({
        "en_us = Showing some character",
        "lol_us = got sum wrinkuls",
        "zh_cn = 略显沧桑"
    })
    public static final Component UI__CARRY_CRATE__MEDIUM_DURABILITY_DESCRIPTION = UIUtils.dimmedItalicText(
        DefinitionUtils.namespacedDotId(ComponentConstants.UI, ComponentConstants.CARRY_CRATE, "medium_durability_description")
    );
    
    @AutoI18n({
        "en_us = Sturdy as a rock",
        "lol_us = HAERD",
        "zh_cn = 稳固如初"
    })
    public static final Component UI__CARRY_CRATE__HIGH_DURABILITY_DESCRIPTION = UIUtils.dimmedItalicText(
        DefinitionUtils.namespacedDotId(ComponentConstants.UI, ComponentConstants.CARRY_CRATE, "high_durability_description")
    );
    
    private static final Ranger LOW_DURABILITY_RANGE = Ranger.closedOpen(0, CARRY_CRATE_MAX_DURABILITY / 3);
    private static final Ranger MEDIUM_DURABILITY_RANGE = Ranger.closedOpen(CARRY_CRATE_MAX_DURABILITY / 3, CARRY_CRATE_MAX_DURABILITY * 2 / 3);
    private static final Ranger HIGH_DURABILITY_RANGE = Ranger.closed(CARRY_CRATE_MAX_DURABILITY * 2 / 3, CARRY_CRATE_MAX_DURABILITY);
    
    public static final RangeMap<Component> DESCRIPTION_DISPATCHER = RangeMap.create(
        map ->
        {
            map.put(LOW_DURABILITY_RANGE, UI__CARRY_CRATE__LOW_DURABILITY_DESCRIPTION);
            map.put(MEDIUM_DURABILITY_RANGE, UI__CARRY_CRATE__MEDIUM_DURABILITY_DESCRIPTION);
            map.put(HIGH_DURABILITY_RANGE, UI__CARRY_CRATE__HIGH_DURABILITY_DESCRIPTION);
        },
        RangeMap.THROW
    );
    
    //* Using [[Supplier]] instead of [[MutableComponent]] directly to avoid footgun.
    //* As its name says, [[MutableComponent]] is mutable, which means using mutate methods like
    //* [[MutableComponent#append]], [[MutableComponent#withStyle]] will change its own text value.
    //* In such a case, [[Component#copy]] is a solution, but [[Supplier]] is obviously better than that,
    //* since it always produces the expected [[MutableComponent]], no [[Component#copy]] required.
    
    @AutoI18n({
        "en_us = Content:~@s",
        "lol_us = THINZ:~@s",
        "zh_cn = 内容:~@s"
    })
    public static final Supplier<MutableComponent> UI__CARRY_CRATE__CONTENT_PREFIX = () ->
        UIUtils.dimmedText(DefinitionUtils.namespacedDotId(ComponentConstants.UI, ComponentConstants.CARRY_CRATE, "content_prefix"));
    
    @AutoI18n({
        "en_us = Contains~@s",
        "lol_us = I HAV~@s",
        "zh_cn = 装了"
    })
    public static final Supplier<MutableComponent> UI__CARRY_CRATE__LAYER_PREFIX = () ->
        UIUtils.dimmedText(DefinitionUtils.namespacedDotId(ComponentConstants.UI, ComponentConstants.CARRY_CRATE, "layer_prefix"));
    
    @AutoI18n({
        "en_us = ~@slayers",
        "lol_us = ~@sUwU",
        "zh_cn = 份"
    })
    public static final Supplier<MutableComponent> UI__CARRY_CRATE__LAYER_SUFFIX = () ->
        UIUtils.dimmedText(DefinitionUtils.namespacedDotId(ComponentConstants.UI, ComponentConstants.CARRY_CRATE, "layer_suffix"));
}