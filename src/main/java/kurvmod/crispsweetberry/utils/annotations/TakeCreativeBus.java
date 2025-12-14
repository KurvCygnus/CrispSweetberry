package kurvmod.crispsweetberry.utils.annotations;

import kurvmod.crispsweetberry.events.init.CrispCreativeTabsRegistryEvent;
import kurvmod.crispsweetberry.ui.CrispCreativeTabs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for automatically creative mode tab's registry.<br>
 * <b><i>You can register one thing to multiple tabs by this annotation</b></i>.
 * @see CrispCreativeTabsRegistryEvent Annotation Executor
 * @since CSB 1.0 Release
 * @author Kurv
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TakeCreativeBus
{
    TabType tabGroup() default TabType.CRISPY;
    
    /**
     * The enum for <b>uniform CreativeModeTypes</b>.<br>
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
