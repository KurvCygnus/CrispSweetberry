//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate;

import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.world.item.BlockItem;

public final class CarryCrateItem extends BlockItem
{
    public static final int CARRY_CRATE_MAX_DURABILITY = 120;
    
    public CarryCrateItem() 
    {
        super(
            CarryCrateRegistries.CARRY_CRATE_BLOCK.value(),
            new Properties().
                durability(CARRY_CRATE_MAX_DURABILITY).
                setNoRepair().//* "durability" sets the max stack size to 1 implicitly, we need to adjust this.
                stacksTo(MiscConstants.STANDARD_MAX_STACK_SIZE)//* and for flexibility, we take maxStackCount as a param.
        );
    }
}
