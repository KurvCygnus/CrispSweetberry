//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import org.jetbrains.annotations.NotNull;

/**
 * This is a simple implementation of enhanced vanilla <u>{@link net.minecraft.world.level.block.TorchBlock TorchBlock}</u>.<br>
 * It improves a bunch of stuff, which can be seen at <u>{@link AbstractGenericTorchBlock}</u>.
 * @implNote Since <u>{@link AbstractGenericTorchBlock}</u> is directly inherited from <u>{@link net.minecraft.world.level.block.TorchBlock TorchBlock}</u>, 
 * {@code AbstractTemporaryTorchBlock}'s implementation is actually as simple as this, which also doesn't need anything else.
 * @param <T> The detailed behavior of this torch block will bound to.
 * @author Kurv Cygnus
 * @since 1.0 Release
 * @see AbstractGenericTorchBlock Basic Abstraction
 * @see AbstractTemporaryWallTorchBlock Wall Torch implementation
 * @see AbstractTemporaryTorchBehavior Universal Behavior Abstraction
 */
public abstract class AbstractTemporaryTorchBlock<T extends AbstractTemporaryTorchBehavior> extends AbstractGenericTorchBlock<T>
{
    public AbstractTemporaryTorchBlock(@NotNull Properties properties, @NotNull T behavior) { super(properties, behavior, false); }
}
