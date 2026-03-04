//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal;

/**
 * This is the basic of all carry adapters.<br>
 * It features {@code penaltyRate} definition, and ticking logic.
 * @since 1.0 Release
 * @see kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.block.AbstractBlockCarryAdapter Block Base Adapter
 * @see kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity.AbstractBlockEntityCarryAdapter BlockEntity Base Adapter
 * @see kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.entity.AbstractEntityCarryAdapter Entity Base Adapter
 * @author Kurv Cygnus
 */
public abstract class AbstractCarryAdapter implements ICarriableLifecycle, ICarryTickable
{
}
