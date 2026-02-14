//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums;

import kurvcygnus.crispsweetberry.common.features.kiln.KilnContainerData;
import org.jetbrains.annotations.ApiStatus;

/**
 * An internal enum for <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnScreen Screen}</u>'s visual display effects.<br>
 * It is deduced by <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressCalculator#calculateRates Calculator}</u>, 
 * then synchronized to <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnScreen Screen}</u> by 
 * <u>{@link KilnContainerData ContainerData}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public enum VisualTrend
{
    NORMAL,
    BALANCE,
    BURST,
    TIP,
    NONE
}
