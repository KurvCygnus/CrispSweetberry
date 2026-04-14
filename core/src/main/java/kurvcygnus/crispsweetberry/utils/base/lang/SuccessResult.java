//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

final class SuccessResult<T, E extends Throwable> implements IResult<T, E>
{
    private final @NotNull T value;
    
    SuccessResult(@NotNull T value)
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        this.value = value;
    }
    
    @Override public boolean isSucceed() { return true; }
    @Override public void ifSucceed(@NotNull Consumer<T> action) { action.accept(value); }
    
    @Override public @NotNull <U> IResult<U, E> map(@NotNull Function<? super T, ? extends U> mapper) { return new SuccessResult<>(mapper.apply(value)); }
    
    @Override public @NotNull <X extends Throwable> IResult<T, X> mapException(@NotNull Function<? super Throwable, ? extends X> mapper)
        { return new SuccessResult<>(value); }
    
    @Override public @NotNull <U> IResult<U, E> flatMap(@NotNull Function<? super T, ? extends IResult<U, E>> mapper) { return mapper.apply(value); }
    
    @Override public @NotNull T orThrow() { return value; }
    
    @Override public @NotNull T orElse(@NotNull T defaultValue) { return value; }
    
    @Override public @NotNull T orElseGet(@NotNull Function<? super E, ? extends T> mapper) { return value; }
    
    @Override public @NotNull <U> U fold(@NotNull Function<? super T, ? extends U> success, @NotNull Function<? super E, ? extends U> fail)
        { return success.apply(value); }
    
    @Override public boolean equals(@Nullable Object obj) { return this == obj || obj instanceof SuccessResult<?, ?> that && Objects.equals(value, that.value); }
    
    @Override public int hashCode() { return Objects.hash(value); }
    
    @Override public @NotNull String toString() { return "Result -> value: %s".formatted(value); }
}
