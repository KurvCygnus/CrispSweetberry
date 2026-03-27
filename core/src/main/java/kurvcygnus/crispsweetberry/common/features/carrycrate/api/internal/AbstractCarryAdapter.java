//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions.ICarriableLifecycle;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions.ICarryTickable;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions.ICarryVerifiable;
import org.jetbrains.annotations.ApiStatus;

/**
 * This is the basic of all carry adapters.<br>
 * It features {@code penaltyRate} definition, and ticking logic.
 * @since 1.0 Release
 * @see AbstractBlockCarryAdapter Block Base Adapter
 * @see AbstractBlockEntityCarryAdapter BlockEntity Base Adapter
 * @see AbstractEntityCarryAdapter Entity Base Adapter
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public abstract class AbstractCarryAdapter<T extends CarryData.CarryDataBaseHolder> implements ICarriableLifecycle<T>, ICarryTickable, ICarryVerifiable
{
}
