//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers;

import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.abstracts.AbstractThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownTorchEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

/**
 * The <b>renderer of thrown torch</b>.
 * @see ThrownTorchEntity Render Target
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class ThrownTorchRenderer extends AbstractThrownTorchRenderer<ThrownTorchEntity>
{
    public ThrownTorchRenderer(@NotNull EntityRendererProvider.Context context) { super(context); }
    
    @Override
    protected @NotNull String getTextureName() { return "thrown_torch"; }
}