//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

/**
 * This package contains Quality of Life (QoL) improvements and small functional tweaks.<br>
 * <br>
 * Each QoL improvement should be treated as an independent domain logic unit,<br>
 * whose sub-package usually consists of:
 * <ul>
 * <li>Internal logic and event handlers (e.g. {@code mixins} or {@code events})</li>
 * <li>Domain-specific API or constants</li>
 * <li>Client-side visual feedback (nested under .client)</li>
 * </ul>
 * <b>QoL features should be self-contained and must NOT have direct hard dependencies
 * on the internal implementation of other QoL or features.</b>
 * <br>
 * <i>The goal is to keep these small improvements modular, making them easy to toggle, maintain.</i>
 * <br>
 * @implNote DO NOT add {@link javax.annotation.ParametersAreNonnullByDefault @ParametersAreNonnullByDefault} in package-infos,
 * this will lead to potential issues, errors, and footguns.
 */
package kurvcygnus.crispsweetberry.common.qol;