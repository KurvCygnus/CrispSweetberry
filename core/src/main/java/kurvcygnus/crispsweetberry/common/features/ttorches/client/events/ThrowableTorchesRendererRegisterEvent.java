//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.client.events;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.ThrownSoulTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.ThrownTorchRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * The class that <b>handles custom entity's renderer registration</b>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE, value = Dist.CLIENT)
public final class ThrowableTorchesRendererRegisterEvent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void registerRenderEvents(final EntityRenderersEvent.@NotNull RegisterRenderers event)
    {
        LOGGER.info("Registering EntityRenderers...");
        event.registerEntityRenderer(TTorchRegistries.THROWN_TORCH.get(), ThrownTorchRenderer::new);
        event.registerEntityRenderer(TTorchRegistries.THROWN_SOUL_TORCH.get(), ThrownSoulTorchRenderer::new);
    }
}
