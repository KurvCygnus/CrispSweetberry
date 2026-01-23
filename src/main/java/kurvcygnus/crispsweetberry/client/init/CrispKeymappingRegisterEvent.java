package kurvcygnus.crispsweetberry.client.init;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.CrispSweetberry.LOGGER;
import static kurvcygnus.crispsweetberry.CrispSweetberry.MOD_ID;
import static kurvcygnus.crispsweetberry.client.registries.CrispKeymap.SPYGLASS_ZOOM;

/**
 * The class that <b>handles the registration of key binds</b>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.client.registries.CrispKeymap Keybind Declaration
 */
@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public final class CrispKeymappingRegisterEvent
{
    @SubscribeEvent
    public static void registerKeyBinds(final @NotNull RegisterKeyMappingsEvent event)
    {
        LOGGER.info("Registering Keybinds...");
        event.register(SPYGLASS_ZOOM);
    }
}