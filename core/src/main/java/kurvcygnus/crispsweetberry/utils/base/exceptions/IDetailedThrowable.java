//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.exceptions;

import org.jetbrains.annotations.NotNull;

public interface IDetailedThrowable<E extends StructuredException & IDetailedThrowable<E, T>, T> extends IStructuredThrowable
{
    @NotNull T causeData();
    
    @SuppressWarnings("unchecked")//! CRTP makes sure that this will be safe.
    default @NotNull E asException() { return (E) this; }
    
    default void throwSelf() throws E { throw asException(); }
    
    default @NotNull String getMessage() { return asException().getMessage(); }
    
    default @NotNull Throwable cause() { return asException().wrappedException(); }
}
