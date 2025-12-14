package kurvmod.crispsweetberry.events.init;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.items.CrispItems;
import kurvmod.crispsweetberry.utils.annotations.NoCreativeBus;
import kurvmod.crispsweetberry.utils.annotations.TakeCreativeBus;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.lang.reflect.Field;

import static kurvmod.crispsweetberry.CrispSweetberry.LOGGER;

/**
 * The executor of annotation <b>{@code @TakeCreativeBus}</b>.<br>
 * <b>It automatically registers every item in {@code CrispItems.class} which presents {@code @TakeCreativeBus} to designated tabs.</b>
 * @since CSB Release 1.0
 * @author Kurv
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID, value = Dist.CLIENT)
public final class CrispCreativeTabsRegistryEvent
{
    /**
     * The method that implements this class's function.
     */
    @SubscribeEvent
    public static void tabRegistryEvent(final BuildCreativeModeTabContentsEvent event)
    {
        /*
           The list of classes to scan for fields annotated with @TakeCreativeBus.
         * Notices that fields in these classes must be static (e.g. public static final, this is the best practice for registries)
         * for reflection to work with "field.get(null)".
        */
        final Class<?>[] loadList = { CrispItems.class };
        TakeCreativeBus annotation;
        
        LOGGER.info("Starting registration of creative mode tab contents...");
        
        for(final Class<?> clazz: loadList)
            for(Field field: clazz.getDeclaredFields())
            {
                field.setAccessible(true);
                
                if(!field.isAnnotationPresent(TakeCreativeBus.class) || field.isAnnotationPresent(NoCreativeBus.class))
                    continue;
                
                annotation = field.getAnnotation(TakeCreativeBus.class);
                
                if(event.getTabKey().equals(annotation.tabGroup().toCreativeTab()))
                    //* Passing null instead of variable clazz is correct and effective way in reflection.
                    try { event.accept((ItemLike) field.get(null)); }
                    catch(IllegalAccessException error) { LOGGER.error(error.getMessage()); }
                    finally { LOGGER.info("Attempting to register {} to tab {}", field.getName(), event.getTabKey().toString().toLowerCase()); }
            }
    }
}
