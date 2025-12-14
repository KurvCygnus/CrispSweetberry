package kurvmod.crispsweetberry.misc;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public final class CrispKeymap
{
    public static final KeyMapping SPYGLASS_ZOOM = new KeyMapping(
      "crisp_spyglass_zoom",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        "crisp_in_game_keymappings"
    );
}
