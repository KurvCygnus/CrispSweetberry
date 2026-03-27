//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.data;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Tuple<L, M, R>(@NotNull L left, @NotNull M middle, @NotNull R right)
{
    public Tuple
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(middle, "Param \"middle\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
    }
}
