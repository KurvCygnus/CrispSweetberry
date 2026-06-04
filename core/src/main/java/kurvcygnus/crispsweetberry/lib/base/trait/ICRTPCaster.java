//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.trait;

import com.google.errorprone.annotations.DoNotCall;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICRTPCaster<T extends ICRTPCaster<T, E>, E>
{
    @SuppressWarnings("unchecked")//! CRTP has granted the safety.
    @ApiStatus.Internal default @NotNull E getSelf() { return (E) this; }
    
    /**
     * @implNote This is useless, literally. It is existed to suppress "unused" warnings,
     * since generic arg {@code T} is <b>only</b> used to restrict implementer's type.
     */
    @SuppressWarnings("unused") @DoNotCall @Deprecated(forRemoval = true) private @Nullable T dummy() { return null; }
}
