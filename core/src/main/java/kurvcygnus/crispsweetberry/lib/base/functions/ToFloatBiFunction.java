//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.functions;

import java.io.Serializable;

/**
 * A specialized <u>{@link java.util.function.BiFunction BiFunction}</u> for primitive type {@code float}.
 * @author Kurv Cygnus
 * @see ToFloatFunction
 * @since 1.0 Release
 */
@FunctionalInterface public interface ToFloatBiFunction<T, U> extends Serializable { float applyAsFloat(T t, U u);}
