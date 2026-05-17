//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.functions;

/**
 * A specialized <u>{@link java.util.function.BiFunction BiFunction}</u> for primitive type {@code char}.
 * @author Kurv Cygnus
 * @see ToCharFunction
 * @since 1.0 Release
 */
@FunctionalInterface public interface ToCharBiFunction<T, U> { char applyAsChar(T t, U u);}
