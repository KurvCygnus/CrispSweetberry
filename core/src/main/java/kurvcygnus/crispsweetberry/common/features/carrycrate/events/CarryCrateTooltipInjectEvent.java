//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
final class CarryCrateTooltipInjectEvent
{
    @SubscribeEvent static void editTooltip(@NotNull RenderTooltipEvent.Pre event)
    {
    }
}
