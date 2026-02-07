package kurvcygnus.crispsweetberry.common.qol.spyglass;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.client.CrispClientLiterals;
import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
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
    
    @AutoI18n({
        "en_us -> Spyglass Quick Zoom",
        "lol_us -> I C U KWIKE UwU",
        "zh_cn -> 望远镜快速使用"
    })
    private static final Component SPYGLASS_DESCRIPTION_TEXT = Component.translatable("crispsweetberry.keybind.spyglass_zoom"); 
    
    private SpyglassClientRegistries() { throw new IllegalAccessError(); }
    
    public static final KeyMapping SPYGLASS_ZOOM = new KeyMapping(
        SPYGLASS_DESCRIPTION_TEXT.getString(),
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        CrispClientLiterals.CRISP_CONTROL_MENU_CATEGORY.getString()
    );
    
    @SubscribeEvent
    static void registerKeyBind(final @NotNull RegisterKeyMappingsEvent event)
    {
        LOGGER.info("Registering Spyglass Keybind...");
        event.register(SPYGLASS_ZOOM);
    }
}