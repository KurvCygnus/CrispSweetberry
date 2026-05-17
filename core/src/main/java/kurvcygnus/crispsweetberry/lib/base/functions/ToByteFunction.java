//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.functions;

/**
 * A specialized <u>{@link java.util.function.Function Function}</u> for primitive type {@code byte}.
 * @author Kurv Cygnus
 * @see ToByteBiFunction
 * @since 1.0 Release
 */
@FunctionalInterface public interface ToByteFunction<T> { byte applyAsByte(T value); }
