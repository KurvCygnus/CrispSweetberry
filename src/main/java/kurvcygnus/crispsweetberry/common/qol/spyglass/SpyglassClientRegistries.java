package kurvcygnus.crispsweetberry.common.qol.spyglass;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE, value = Dist.CLIENT)
public final class SpyglassClientRegistries
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private SpyglassClientRegistries() { throw new IllegalAccessError(); }
    
    public static final KeyMapping SPYGLASS_ZOOM = new KeyMapping(
        "crispsweetberry.keybind.spyglass_zoom",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        "crispsweetberry.menu.control.title"
    );
    
    @SubscribeEvent
    static void registerKeyBind(final @NotNull RegisterKeyMappingsEvent event)
    {
        LOGGER.info("Registering Spyglass Keybind...");
        event.register(SPYGLASS_ZOOM);
    }
}