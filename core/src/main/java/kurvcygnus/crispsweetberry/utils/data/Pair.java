//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.data;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public record Pair<L, R>(@NotNull L left, @NotNull R right) implements Map.Entry<L, R>
{
    public Pair
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
    }
    
    @Override public @NotNull L getKey() { return this.left; }
    
    @Override public @NotNull R getValue() { return this.right; }
    
    @Contract(value = "_ -> fail", pure = true) @Override public R setValue(@NotNull R value) { throw new UnsupportedOperationException(); }
}
