package kurvmod.crispsweetberry.events.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.fml.common.EventBusSubscriber; // 新增导入

import static kurvmod.crispsweetberry.CrispSweetberry.LOGGER;
import static kurvmod.crispsweetberry.misc.CrispKeymap.SPYGLASS_ZOOM;
import static kurvmod.crispsweetberry.CrispSweetberry.MOD_ID; // 确保导入 MOD_ID

/**
 * The class that <b>handles the registration of key binds</b>.
 * @since CSB Release 1.0
 * @author Kurv
 */
@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class CrispKeymappingRegisterEvent
{
    @SubscribeEvent
    public static void registerKeyBinds(RegisterKeyMappingsEvent event)
    {
        LOGGER.info("Registering Keybinds...");
        event.register(SPYGLASS_ZOOM);
    }
}