package kurvcygnus.crispsweetberry.client.init;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.registries.CrispItems;
import kurvcygnus.crispsweetberry.utils.annotations.BanFromTabRegistry;
import kurvcygnus.crispsweetberry.utils.annotations.RegisterToTab;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * The executor of annotation <b><u>{@link RegisterToTab @RegisterToTab}</u></b>.<br>
 * <b>It automatically registers every item in {@code CrispItems.class} which presents {@link RegisterToTab @RegisterToTab} to designated tabs.</b>
 * @since CSB Release 1.0
 * @author Kurv
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID, value = Dist.CLIENT)
public final class CrispCreativeTabsRegistryEvent
{
    private static final Logger logger = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void tabRegistryEvent(final @NotNull BuildCreativeModeTabContentsEvent event)
    {
        //  The list of classes to scan for fields annotated with @RegisterToTab.
        //! Notices that fields in these classes must be static (e.g. public static final, this is the best practice for registries)
        //! for reflection to work with "field.get(null)".
        final Class<?>[] loadList = { CrispItems.class };
        RegisterToTab annotation;
        
        logger.info("Start the registration of creative mode tab contents...");
        
        for(final Class<?> clazz: loadList)
            for(var field: clazz.getDeclaredFields())
            {
                field.setAccessible(true);
                
                if(!field.isAnnotationPresent(RegisterToTab.class) || field.isAnnotationPresent(BanFromTabRegistry.class))
                    continue;
                
                annotation = field.getAnnotation(RegisterToTab.class);
                
                if(event.getTabKey().equals(annotation.tabGroup().toCreativeTab()))
                    //* Passing "null" instead of variable "clazz" is correct and effective way in reflection.
                    try { event.accept((ItemLike) field.get(null)); }
                    catch(IllegalAccessException error) { logger.error(error.getMessage()); }
                    finally { logger.info("Attempting to register {} to tab {}", field.getName(), event.getTabKey().toString().toLowerCase()); }
            }
    }
}
