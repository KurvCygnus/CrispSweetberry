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

/**
 * This is the behavior of glowstick ttorch variant, which holds all behavior logics.
 * <br><br>
 * Major behavior differences are centered in
 * <u>{@link kurvcygnus.crispsweetberry.common.features.ttorches.entities.GlowStickEntity GlowStickEntity}</u>.
 *
 * @author Kurv Cygnus
 * @see GlowStickBlock Glowstick Block
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.entities.GlowStickEntity Entity
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.items.GlowStickItem Item
 * @since 1.0 Release
 */
public final class GlowStickBehavior extends AbstractTemporaryTorchBehavior
{
    public GlowStickBehavior(@NotNull Lazy<? extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> lazyTorchBlock) { super(lazyTorchBlock); }
    
    @Override protected @NotNull Item getThrowableTorchItem() { return TTorchRegistries.GLOWSTICK_ITEM.value(); }
}
