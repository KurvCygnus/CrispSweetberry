//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.mixins;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableVanillaBlockEntityAccessors;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * This is a bridge class, which implements the accessor's methods to reveal {@code private} fields.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.SimpleContainerBlockEntityCarryAdapter Usage
 */
@Mixin(BaseContainerBlockEntity.class) @ApiStatus.Internal
public abstract class BaseContainerAccessor implements CarriableVanillaBlockEntityAccessors.IBaseContainerAccessor
{
    @Override @Invoker public abstract @NotNull NonNullList<ItemStack> callGetItems();
}
