//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import org.jetbrains.annotations.ApiStatus;

/**
 * An internal enum used bu <u>{@link KilnBlockEntity}</u>.<br>
 * It is used to determine the final <u>{@link KilnBlockEntity.ProcessionState ProcessionState}</u> of <u>{@link KilnBlockEntity}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see KilnBlockEntity BlockEntity
 */
@ApiStatus.Internal
public enum InputState
{
    ALL_EMPTY,
    HAS_TIP,
    VALID
}
