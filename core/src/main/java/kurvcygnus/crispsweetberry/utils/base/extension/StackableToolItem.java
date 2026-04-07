//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.extension;

import net.minecraft.world.item.Item;

public abstract class StackableToolItem<T extends StackableToolItem<T>> extends Item implements IStackableTool<T>
{
    public StackableToolItem(Properties properties) { super(properties); }
}
