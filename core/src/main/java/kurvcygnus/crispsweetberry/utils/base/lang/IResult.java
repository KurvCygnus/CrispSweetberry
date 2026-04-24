//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.lang;

import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A simple implementation of <u><a href="https://en.wikipedia.org/wiki/Monad_(functional_programming)">Either Monad</a></u> from <b>Functional Programming</b>.
 * @param <T> The type of data this holder holds.
 * @param <E> The type of exception this holder holds.
 * @author Kurv Cygnus
 * @since 1.0 Release
 * @apiNote Due to Java's type system, generic {@code E} can be burden at most situations.<br>
 * In that case, you can try using <u>{@link StructuredException}</u> instead.
 */
public sealed interface IResult<T, E extends Throwable> permits FailureResult, SuccessResult
{
    static <T, E extends Throwable> @NotNull IResult<T, E> of(@NotNull @Flow(targetIsContainer = true) T value) { return new SuccessResult<>(value); }
    
    static <T, E extends Throwable> @NotNull IResult<T, E> ofFailed(@NotNull E exception) { return new FailureResult<>(exception); }
    
    static <T, E extends Throwable> @NotNull IResult<T, E> ofFailed(@NotNull String message, @NotNull Function<String, E> function)
        { return new FailureResult<>(function.apply(message)); }
    
    static <T, E extends Throwable> @NotNull IResult<T, E> ofFailed(@NotNull String message, @NotNull Throwable cause, @NotNull BiFunction<String, Throwable, E> function)
        { return new FailureResult<>(function.apply(message, cause)); }
    
    boolean isSucceed();
    @ApiStatus.NonExtendable default boolean isFailure() { return !isSucceed(); }
    
    default void ifSucceed(@NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) Consumer<T> action) {}
    default void ifFailure(@NotNull Consumer<E> action) {}
    @ApiStatus.NonExtendable default void ifSucceedOrElse(@NotNull Consumer<T> action, @NotNull Consumer<E> altAction)
    {
        ifSucceed(action);
        ifFailure(altAction);
    }
    
    default @NotNull IResult<T, E> peekIfSucceed(@NotNull Consumer<T> action)
    {
        ifSucceed(action);
        return this;
    }
    default @NotNull IResult<T, E> peekIfFailure(@NotNull Consumer<E> action)
    {
        ifFailure(action);
        return this;
    }
    
    default @NotNull IResult<T, E> peek(@NotNull Consumer<T> action, @NotNull Consumer<E> altAction)
    {
        ifSucceed(action);
        ifFailure(altAction);
        return this;
    }
    
    <U> @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true, targetIsContainer = true) IResult<U, E> map(@NotNull Function<? super T, ? extends U> mapper);
    
    <X extends Throwable> @NotNull IResult<T, X> mapException(@NotNull Function<? super Throwable, ? extends X> mapper);
    
    <U> @NotNull IResult<U, E> flatMap(@NotNull Function<? super T, ? extends IResult<U, E>> mapper);
    
    @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) T orThrow() throws E;
    
    @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) T orElse(T defaultValue);
    
    T orElseGet(@NotNull Function<? super E, ? extends T> mapper);
    
    <U> U fold(
        @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) Function<? super T, ? extends U> success,
        @NotNull Function<? super E, ? extends U> fail
    );
}

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
