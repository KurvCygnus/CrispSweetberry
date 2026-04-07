//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

/**
 * This package overlaps large, self-contained gameplay features.
 * A feature usually consists of:<ul>
 * <li>Blocks / Items</li>
 * <li>BlockEntities</li>
 * <li>Menus / Screens</li>
 * <li>Recipes and internal logic</li>
 * </ul>
 * <b>Features should be internally cohesive and should NOT directly depend on
 * the internal implementation of other features</b>.
 * @implNote DO NOT add {@link javax.annotation.ParametersAreNonnullByDefault @ParametersAreNonnullByDefault} in package-infos,
 * this will lead to potential issues, errors, and footguns.
 */
package kurvcygnus.crispsweetberry.common.features;