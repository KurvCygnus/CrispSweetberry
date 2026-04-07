//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

/**
 * This package overlaps all client-side related code.<br>
 * <b>Rendering, visual effects, UI, key bindings, and all related events, registries are all client-side elements</b>.<br>
 * <b>Client-only code MUST NOT be referenced by any class under {@link kurvcygnus.crispsweetberry.common common}</b>.<br><br>
 * NOTE:<br>
 * <i>Client-Server side separate rule doesn't apply to <u>{@link kurvcygnus.crispsweetberry.common.features features}</u>, 
 * <u>{@code kurvcygnus.crispsweetberry.datagen}</u>
 * and <u>{@link kurvcygnus.crispsweetberry.common.config config}</u> packages.</i>
 * @implNote DO NOT add {@link javax.annotation.ParametersAreNonnullByDefault @ParametersAreNonnullByDefault} in package-infos,
 * this will lead to potential issues, errors, and footguns.
 */
package kurvcygnus.crispsweetberry.client;