//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.soul;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBehavior;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.ITRedstoneTorchExtensions;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.TemporaryRedstoneTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone.TemporaryRedstoneWallTorchBlock;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;

/**
 * This is the behavior of soul fire ttorch variant, which holds all behavior logics.
 * <br><br>
 * Major behavior differences are centered in 
 * <u>{@link kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownSoulTorchEntity ThrownSoulTorchEntity}</u>. 
 * @author Kurv Cygnus
 * @see ITRedstoneTorchExtensions Extension Interfaces
 * @see TemporaryRedstoneTorchBlock Floor Torch
 * @see TemporaryRedstoneWallTorchBlock Wall Torch
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownRedstoneTorchEntity Entity
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableRedstoneTorchItem Item
 * @since 1.0 Release
 */
public final class TemporarySoulTorchBehavior extends AbstractTemporaryTorchBehavior
{
    public TemporarySoulTorchBehavior(@NotNull Lazy<? extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> lazyTorchBlock)
        { super(lazyTorchBlock); }
    
    @Override protected @NotNull Item getThrowableTorchItem() { return TTorchRegistries.THROWABLE_SOUL_TORCH.value(); }
}
