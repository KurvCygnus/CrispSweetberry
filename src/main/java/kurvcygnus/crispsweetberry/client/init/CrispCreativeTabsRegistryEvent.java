package kurvcygnus.crispsweetberry.client.init;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.utils.registry.annotations.BanFromTabRegistry;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * The executor of annotation <b><u>{@link RegisterToTab @RegisterToTab}</u></b>.<br>
 * <b>It automatically registers every item in {@code CrispItems.class} which presents {@link RegisterToTab @RegisterToTab} to designated tabs.</b>
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID, value = Dist.CLIENT)
public final class CrispCreativeTabsRegistryEvent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void tabRegistryEvent(final @NotNull BuildCreativeModeTabContentsEvent event)
    {
        //  The list of classes to scan for fields annotated with @RegisterToTab.
        //! Notices that fields in these classes must be static (e.g. public static final, this is the best practice for registries)
        //! for reflection to work with "field.get(null)".
        RegisterToTab annotation;
        
        LOGGER.info("[TAB_REGISTRY] Start the registration of creative mode tab contents...");
        
        for(ModFileScanData.AnnotationData data: CrispSweetberry.ANNOTATION_CACHE)
        {
            try
            {
                Class<?> clazz = Class.forName(data.clazz().getClassName());
                Field field = clazz.getDeclaredField(data.memberName());
                field.setAccessible(true);
                
                if(!field.isAnnotationPresent(RegisterToTab.class) || field.isAnnotationPresent(BanFromTabRegistry.class))
                    continue;
                
                annotation = field.getAnnotation(RegisterToTab.class);
                
                if(!annotation.registerCondition())
                    continue;
                
                if(event.getTabKey().equals(annotation.tabGroup().toCreativeTab()))
                {
                    //* Passing "null" instead of variable "clazz" is correct and effective way in reflection.
                    try { event.accept((ItemLike) field.get(null)); }
                    catch(IllegalAccessException error) { LOGGER.error(error.getMessage()); }
                    finally { LOGGER.debug("[TAB_REGISTRY] Attempting to register {} to tab {}", field.getName(), event.getTabKey().toString().toLowerCase()); }
                }
            }
            catch(ClassNotFoundException e) { LOGGER.error("[TAB_REGISTRY] Failed to find class, details: {}", e.getMessage()); }
            catch(NoSuchFieldException e) { LOGGER.error("[TAB_REGISTRY] Failed to find field, details: {}", e.getMessage()); }
            finally { LOGGER.debug("[TAB_REGISTRY] Go to next entry..."); }
        }
        
        LOGGER.info("[TAB_REGISTRY] Finish the registration of creative mode tab contents!");
    }
}
