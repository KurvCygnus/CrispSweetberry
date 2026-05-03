//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.lang;

import kurvcygnus.crispsweetberry.utils.base.extensions.INestedPrintable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public record Pair<L, R>(@NotNull L left, @NotNull R right) implements Map.Entry<L, R>, Serializable, INestedPrintable
{
    public Pair
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
    }
    
    @Override public @NotNull L getKey() { return this.left; }
    
    @Override public @NotNull R getValue() { return this.right; }
    
    public <U> @NotNull U mapLeft(@NotNull Function<? super L, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left);
    }
    
    public <U> @NotNull U mapRight(@NotNull Function<? super R, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.right);
    }
    
    public @NotNull Pair<R, L> swap() { return new Pair<>(right, left); }
    
    public @NotNull Map.Entry<L, R> asEntry() { return Map.Entry.copyOf(this); }
    
    public @NotNull @Unmodifiable Map<L, R> asMap() { return Map.of(this.left, this.right); }
    
    @Contract(value = "_ -> fail", pure = true) @Override public R setValue(@Nullable R value)
        { throw new UnsupportedOperationException("This is an immutable Class. Value mutation is not allowed!"); }
    
    @Override public @NotNull String toString() { return toNestedString(); }
    
    @Override public @NotNull @Unmodifiable Map<@NotNull String, @Nullable Object> getFields()
    {
        return Map.of(
            "left", left,
            "right", right
        );
    }
}
