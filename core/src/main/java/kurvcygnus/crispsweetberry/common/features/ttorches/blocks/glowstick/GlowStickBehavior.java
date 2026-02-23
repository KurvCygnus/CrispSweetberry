//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.glowstick;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBehavior;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

public final class GlowStickBehavior extends AbstractTemporaryTorchBehavior
{
    public GlowStickBehavior(@NotNull Lazy<? extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> lazyTorchBlock)
        { super(lazyTorchBlock); }
    
    @Override protected @NotNull Item getThrowableTorchItem() { return TTorchRegistries.GLOWSTICK_ITEM.value(); }
}
