//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers;

import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.abstracts.AbstractThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.GlowStickEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

/**
 * The renderer of ttorch series, glowstick variant.
 * @author Kurv Cygnus
 * @see GlowStickEntity Render Target
 * @since 1.0 Release
 */
public final class GlowstickRenderer extends AbstractThrownTorchRenderer<GlowStickEntity>
{
    public GlowstickRenderer(EntityRendererProvider.@NotNull Context context) { super(context); }
    
    @Override
    protected @NotNull String getTextureName() { return "glowstick"; }
}
