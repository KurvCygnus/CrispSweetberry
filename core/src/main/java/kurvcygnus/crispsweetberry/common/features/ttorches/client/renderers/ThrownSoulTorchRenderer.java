//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers;

import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.abstracts.AbstractThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownSoulTorchEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

/**
 * The renderer of ttorch series, soul fire variant.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see ThrownSoulTorchEntity Render Target
 */
public final class ThrownSoulTorchRenderer extends AbstractThrownTorchRenderer<ThrownSoulTorchEntity>
{
    public ThrownSoulTorchRenderer(EntityRendererProvider.@NotNull Context context) { super(context); }
    
    @Override protected @NotNull String getTextureName() { return "thrown_soul_torch"; }
}
