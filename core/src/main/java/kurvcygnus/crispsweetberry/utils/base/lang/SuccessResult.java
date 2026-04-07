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

final class SuccessResult<TValue, TException extends Throwable> implements IResult<TValue, TException>
{
    private final @NotNull TValue value;
    
    SuccessResult(@NotNull TValue value)
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        this.value = value;
    }
    
    @Override public boolean isSucceed() { return true; }
    @Override public void ifSucceed(@NotNull Consumer<TValue> action) { action.accept(value); }
    
    @Override public @NotNull <TNewValue> IResult<TNewValue, TException> map(@NotNull Function<? super TValue, ? extends TNewValue> mapper)
        { return new SuccessResult<>(mapper.apply(value)); }
    @Override public @NotNull <TNewException extends Throwable> IResult<TValue, TNewException> mapException(@NotNull Function<? super Throwable, ? extends TNewException> mapper)
        { return new SuccessResult<>(value); }
    @Override public @NotNull <TNewValue> IResult<TNewValue, TException> flatMap(@NotNull Function<? super TValue, ? extends IResult<TNewValue, TException>> mapper)
        { return mapper.apply(value); }
    
    @Override public @NotNull TValue orThrow() { return value; }
    @Override public @NotNull TValue orElse(@NotNull TValue defaultValue) { return value; }
    @Override public @NotNull TValue orElseGet(@NotNull Function<? super TException, ? extends TValue> mapper) { return value; }
    @Override public @NotNull <TNewValue> TNewValue fold(
        @NotNull Function<? super TValue, ? extends TNewValue> success,
        @NotNull Function<? super TException, ? extends TNewValue> fail
    ) { return success.apply(value); }
    
    @Override public boolean equals(@Nullable Object obj) { return obj instanceof SuccessResult<?, ?> that && Objects.equals(value, that.value); }
    
    @Override public int hashCode() { return Objects.hash(value); }
    
    @Override public @NotNull String toString() { return "Result -> value: %s".formatted(value); }
}
