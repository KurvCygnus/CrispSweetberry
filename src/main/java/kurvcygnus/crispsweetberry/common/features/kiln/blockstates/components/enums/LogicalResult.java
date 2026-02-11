//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums;

import org.jetbrains.annotations.ApiStatus;

/**
 * An internal enum for <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator KilnProgressCalculator}</u>.<br>
 * It is used for <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity BlockEntity}</u>'s result procession.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator#calculateRates Usage
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity#serverTick Result variants
 */
@ApiStatus.Internal
public enum LogicalResult
{
    CONTINUE,
    BALANCING,
    SKIP,
    INVALID,
    BLAST_TIP
}
