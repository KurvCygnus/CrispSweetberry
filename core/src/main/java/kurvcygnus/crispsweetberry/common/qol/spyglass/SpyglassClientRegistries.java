//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.qol.spyglass;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.client.CrispClientLiterals;
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
    
    private static final String SPYGLASS_DESCRIPTION_KEY = "crispsweetberry.keybind.spyglass_zoom";
    
    @AutoI18n({
        "en_us = Spyglass Quick Zoom",
        "lol_us = I C U KWIKE UwU",
        "zh_cn = 望远镜快速使用"
    })
    private static final Component SPYGLASS_DESCRIPTION_TEXT = Component.translatable(SPYGLASS_DESCRIPTION_KEY); 
    
    private SpyglassClientRegistries() { throw new IllegalAccessError(); }
    
    public static final KeyMapping SPYGLASS_ZOOM = new KeyMapping(
        SPYGLASS_DESCRIPTION_KEY,
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_Z,
        CrispClientLiterals.CRISP_CONTROL_MENU_CATEGORY_KEY
    );
    
    @SubscribeEvent
    static void registerKeyBind(final @NotNull RegisterKeyMappingsEvent event)
    {
        LOGGER.info("Registering Spyglass Keybind...");
        event.register(SPYGLASS_ZOOM);
    }
}