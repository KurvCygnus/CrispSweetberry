package kurvcygnus.crispsweetberry.client.registries;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/**
 * @see kurvcygnus.crispsweetberry.client.init.CrispKeymappingRegisterEvent Actual Registry
 */
public final class CrispKeymap
{
    public static final KeyMapping SPYGLASS_ZOOM = new KeyMapping(
      "crispsweetberry.keybind.spyglass_zoom",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        "crispsweetberry.menu.control.title"
    );
}
