//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers;

import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.abstracts.AbstractThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownRedstoneTorchEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

/**
 * The renderer of ttorch series, redstone variant.
 *
 * @author Kurv Cygnus
 * @see ThrownRedstoneTorchEntity Render Target
 * @since 1.0 Release
 */
public final class ThrownRedstoneTorchRenderer extends AbstractThrownTorchRenderer<ThrownRedstoneTorchEntity>
{
    public ThrownRedstoneTorchRenderer(EntityRendererProvider.@NotNull Context context) { super(context); }
    
    @Override protected void appendTextureName(@NotNull StringBuilder path, @NotNull ThrownRedstoneTorchEntity entity, @NotNull FacingTuple tuple)
        { path.append("_%s_%s_%s".formatted(entity.getOxidizeState().name(), tuple.horizontalFacing().getAlias(), tuple.verticalFacing().getAlias())); }
    
    @Override protected @NotNull String getTextureName() { return "thrown_redstone_torch"; }
    
    @Override protected boolean hasStateVariation() { return false; }
}
