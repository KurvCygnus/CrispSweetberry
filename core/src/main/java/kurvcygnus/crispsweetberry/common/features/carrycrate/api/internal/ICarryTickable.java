//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public interface ICarryTickable
{
    /**
     * This method will be called during carrying.
     * @apiNote All <u>{@link AbstractCarryAdapter adapters}</u> are variables with short lifecycle. 
     * During the execution of {@code #carryTick(TickingContext)}, <b>adapters will be created with {@code null} values</b>.<br>
     * <b>So, do not use {@code AbstractBlockCarryAdapter.block}, {@code AbstractBlockEntityCarryAdapter.blockEntity} or 
     * {@code AbstractEntityCarryAdapter.entity} in this method, <u>{@link NullPointerException}</u> will be thrown</b>.
     */
    default void carryingTick(@NotNull TickingContext context) {}
    
    record TickingContext(@NotNull ItemStack carryCrate, @NotNull Level level, @NotNull Entity entity, int slotId) {}
}
