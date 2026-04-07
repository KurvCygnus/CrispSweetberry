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

final class FailureResult<TValue, TException extends Throwable> implements IResult<TValue, TException>
{
    private final @NotNull TException exception;
    
    FailureResult(@NotNull TException exception)
    {
        Objects.requireNonNull(exception, "Param \"exception\" must not be null!");
        this.exception = exception;
    }
    
    @Override public boolean isSucceed() { return false; }
    @Override public void ifFailure(@NotNull Consumer<TException> action) { action.accept(exception); }
    
    @Override public @NotNull <TNewValue> IResult<TNewValue, TException> map(@NotNull Function<? super TValue, ? extends TNewValue> mapper)
        { return new FailureResult<>(exception); }
    @Override public @NotNull <TNewException extends Throwable> IResult<TValue, TNewException> mapException(@NotNull Function<? super Throwable, ? extends TNewException> mapper)
        { return new FailureResult<>(mapper.apply(exception)); }
    @Override public @NotNull <TNewValue> IResult<TNewValue, TException> flatMap(@NotNull Function<? super TValue, ? extends IResult<TNewValue, TException>> mapper)
        { return new FailureResult<>(exception); }
    
    @Override public @NotNull TValue orThrow() throws TException { throw exception; }
    @Override public @NotNull TValue orElse(@NotNull TValue defaultValue) { return defaultValue; }
    @Override public @NotNull TValue orElseGet(@NotNull Function<? super TException, ? extends TValue> mapper) { return mapper.apply(exception); }
    @Override public @NotNull <TNewValue> TNewValue fold(
        @NotNull Function<? super TValue, ? extends TNewValue> success,
        @NotNull Function<? super TException, ? extends TNewValue> fail
    ) { return fail.apply(exception); }
    
    @Override public boolean equals(@Nullable Object obj) { return obj instanceof FailureResult<?,?> that && Objects.equals(this.exception, that.exception); }
    
    @Override public int hashCode() { return Objects.hash(exception); }
    
    @Override public @NotNull String toString() { return "Result -> exception: %s".formatted(exception); }
}
