//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.lang;

import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public record Pair<L, R>(@NotNull L left, @NotNull R right) implements Map.Entry<L, R>, INestedPrintable<Pair<L, R>>
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
    
    public <LU, RU> @NotNull Pair<LU, RU> dualMap(@NotNull Function<? super L, ? extends LU> leftMapper, @NotNull Function<? super R, ? extends RU> rightMapper)
    {
        Objects.requireNonNull(leftMapper, "Param \"leftMapper\" must not be null!");
        Objects.requireNonNull(rightMapper, "Param \"rightMapper\" must not be null!");
        return new Pair<>(leftMapper.apply(left), rightMapper.apply(right));
    }
    
    public <LU, RU> @NotNull Pair<LU, RU> flatMap(@NotNull BiFunction<? super L, ? super R, ? extends Pair<LU, RU>> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left, this.right);
    }
    
    public @NotNull Pair<R, L> swap() { return new Pair<>(right, left); }
    
    public @NotNull Map.Entry<L, R> asEntry() { return Map.Entry.copyOf(this); }
    
    public @NotNull @Unmodifiable Map<L, R> asMap() { return Map.of(this.left, this.right); }
    
    @Contract(value = "_ -> fail", pure = true) @Override public R setValue(@Nullable R value)
        { throw new UnsupportedOperationException("This is an immutable Class. Value mutation is not allowed!"); }
    
    @Override public @NotNull String toString() { return toNestedString(); }
    
    @Override public @NotNull @Unmodifiable Map<String, Function<Pair<L, R>, @Nullable Object>> getFields()
        { return INestedPrintable.buildFieldMap(new Pair<>("left", Pair::left), new Pair<>("right", Pair::right)); }
}
