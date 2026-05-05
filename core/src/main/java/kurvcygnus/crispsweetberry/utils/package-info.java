//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

/**
 * This package overlaps useful, reusable and reliable classes, methods and constants.<br>
 * Which means <b>the content inside shouldn't rely on any data, state or context</b>.<br>
 * Only <b>constants can be related to context meaning, and the range should be at Vanilla, out of range is NOT allowed</b>.<br>
 * <i>If you found any method or constant is not universal and have context, it belongs to its user, not here</i>.<br>
 * On contrast, if they are rather universal, whose usage is not limited in this project, you should take them to
 * <u>{@link kurvcygnus.crispsweetberry.lib lib package}</u>.
 * @implNote DO NOT add {@link javax.annotation.ParametersAreNonnullByDefault @ParametersAreNonnullByDefault} in package-infos,
 * this will lead to potential issues, errors, and footguns.<br>
 */
package kurvcygnus.crispsweetberry.utils;