//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarriableLifecycle;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

public interface IBlockEntityCarryLifecycle<E extends BlockEntity> extends ICarriableLifecycle
{
    String FAIL_SUPER_FAST_MSG = """
        Assertion failed: Field "blockEntity" happens to be null, this shouldn't be happen, which usually means
        method is called at improper time, with improper param. %s
        """.
        formatted(MiscConstants.FEEDBACK_MESSAGE);
    
    @Override @Range(from = 0, to = Integer.MAX_VALUE) default int getPenaltyRate() 
    {
        Objects.requireNonNull(getBlockEntity(), FAIL_SUPER_FAST_MSG);
        return getPenaltyRate(getBlockEntity());
    }
    
    @Nullable E getBlockEntity();
    
    @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate(@NotNull E blockEntity);
}
