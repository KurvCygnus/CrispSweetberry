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

final class FailureResult<T, E extends Throwable> implements IResult<T, E>
{
    private final @NotNull E exception;
    
    FailureResult(@NotNull E exception)
    {
        Objects.requireNonNull(exception, "Param \"exception\" must not be null!");
        this.exception = exception;
    }
    
    @Override public boolean isSucceed() { return false; }
    @Override public void ifFailure(@NotNull Consumer<E> action) { action.accept(exception); }
    
    @Override public @NotNull <U> IResult<U, E> map(@NotNull Function<? super T, ? extends U> mapper) { return new FailureResult<>(exception); }
    
    @Override public @NotNull <X extends Throwable> IResult<T, X> mapException(@NotNull Function<? super Throwable, ? extends X> mapper)
        { return new FailureResult<>(mapper.apply(exception)); }
    
    @Override public @NotNull <U> IResult<U, E> flatMap(@NotNull Function<? super T, ? extends IResult<U, E>> mapper)
        { return new FailureResult<>(exception); }
    
    @Override public @NotNull T orThrow() throws E { throw exception; }
    
    @Override public @NotNull T orElse(@NotNull T defaultValue) { return defaultValue; }
    
    @Override public @NotNull T orElseGet(@NotNull Function<? super E, ? extends T> mapper) { return mapper.apply(exception); }
    
    @Override public @NotNull <U> U fold(@NotNull Function<? super T, ? extends U> success, @NotNull Function<? super E, ? extends U> fail)
        { return fail.apply(exception); }
    
    @Override public boolean equals(@Nullable Object obj)
        { return this == obj || obj instanceof FailureResult<?, ?> that && Objects.equals(this.exception, that.exception); }
    
    @Override public int hashCode() { return Objects.hash(exception); }
    
    @Override public @NotNull String toString() { return "Result -> exception: %s".formatted(exception); }
}
