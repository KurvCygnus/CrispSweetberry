//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.extension;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class StackableToolBlockItem<T extends StackableToolBlockItem<T>> extends BlockItem implements IStackableTool<T>
{
    public StackableToolBlockItem(@NotNull Block block, Properties properties)
        { super(Objects.requireNonNull(block, "Param \"block\" must not be null!"), properties); }
    
    @Override public boolean isBarVisible(@NotNull ItemStack stack) { return IStackableTool.super.isBarVisible(stack); }
    @Override public int getBarWidth(@NotNull ItemStack stack) { return IStackableTool.super.getBarWidth(stack); }
    @Override public int getBarColor(@NotNull ItemStack stack) { return IStackableTool.super.getBarColor(stack); }
}
