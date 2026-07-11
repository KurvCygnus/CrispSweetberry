//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.util;

public interface IChecker
{
    @FunctionalInterface interface OfNoArg<E extends Throwable> { void check() throws E; }
    
    @FunctionalInterface interface OfOneArg<T, E extends Throwable> { void check(T value) throws E; }
}
