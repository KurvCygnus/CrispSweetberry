//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.exceptions;

public interface ITransitionalThrowable<E extends StructuredException & ITransitionalThrowable<E, T, R>, T, R> extends IDetailedThrowable<E, T>
{
    R rollback();
}
