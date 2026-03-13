//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.block;

import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * This is a simple abstraction of <u>{@link SimpleBlockCarryAdapter}</u>, with the support of <b>{@code layer}</b>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @param <B> The block this adapter takes responsibility of. 
 */
public class SimpleLayeredBlockCarryAdapter<B extends Block> extends SimpleBlockCarryAdapter<B>
{
    protected static final int VANILLA_LAYER = 8;
    
    public SimpleLayeredBlockCarryAdapter(@NotNull Block block) { super(block); }
    
    protected @Range(from = DEFAULT_ACCEPTABLE_COUNT, to = Integer.MAX_VALUE) int getLayer() { return VANILLA_LAYER; }
    
    @Override public @Range(from = 1, to = Integer.MAX_VALUE) int getAcceptableCount() { return super.getAcceptableCount() * getLayer(); }
}
