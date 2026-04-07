//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.core.registry;

import kurvcygnus.crispsweetberry.client.init.CrispCreativeTabsRegistryEvent;
import kurvcygnus.crispsweetberry.client.registries.CrispCreativeTabs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatically creative mode tab's registry.<br>
 * <b><i>It only works on items</i></b>.
 * @implSpec Usage Example:<br><pre>{@code
 *  @RegisterToTab(
 *  tabGroup = TabType.SpecifiedTabType,
 *  condition = bar// default: true
 *  )
 *  public static final Holder<Item> Foo = ...
 * }</pre>
 * <i><b>We strongly recommend using {@code public static final} to avoid potential problems</b></i>.
 * @see CrispCreativeTabsRegistryEvent#tabRegistryEvent(BuildCreativeModeTabContentsEvent)  Annotation Executor
 * @see kurvcygnus.crispsweetberry.CrispSweetberry#TAB_LOOKUP Info Source
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RegisterToTab
{
    TabType tabGroup() default TabType.CRISPY;
    boolean registerCondition() default true;
    
    /**
     * The enum to <b>uniform CreativeModeTabTypes</b>.<br>
     */
    enum TabType
    {
        BUILDING_BLOCKS(CreativeModeTabs.BUILDING_BLOCKS),
        COLORED_BLOCKS(CreativeModeTabs.COLORED_BLOCKS),
        NATURAL_BLOCKS(CreativeModeTabs.NATURAL_BLOCKS),
        FUNCTIONAL_BLOCKS(CreativeModeTabs.FUNCTIONAL_BLOCKS),
        REDSTONE_UTILITIES(CreativeModeTabs.REDSTONE_BLOCKS),
        TOOLS_AND_UTILITIES(CreativeModeTabs.TOOLS_AND_UTILITIES),
        COMBAT(CreativeModeTabs.COMBAT),
        INGREDIENTS(CreativeModeTabs.INGREDIENTS),
        FOOD_N_DRINKS(CreativeModeTabs.FOOD_AND_DRINKS),
        SPAWN_EGGS(CreativeModeTabs.SPAWN_EGGS),
        OP_BLOCKS(CreativeModeTabs.OP_BLOCKS),
        CRISPY(CrispCreativeTabs.CRISP_CREATIVE_TAB);
        
        private final ResourceKey<CreativeModeTab> tabResourceKey;
        
        TabType(ResourceKey<CreativeModeTab> tabResourceKey) { this.tabResourceKey = tabResourceKey; }
        
        public ResourceKey<CreativeModeTab> toCreativeTab() { return tabResourceKey; }
    }
}
