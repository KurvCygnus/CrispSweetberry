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

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface IResult<T, E extends Throwable> permits FailureResult, SuccessResult
{
    static <T, E extends Throwable> @NotNull IResult<T, E> of(@NotNull @Flow(targetIsContainer = true) T value) { return new SuccessResult<>(value); }
    
    static <T, E extends Throwable> @NotNull IResult<T, E> ofFailed(@NotNull E exception) { return new FailureResult<>(exception); }
    
    static <T, E extends Throwable> @NotNull IResult<T, E> ofFailed(@NotNull Supplier<E> supplier) { return new FailureResult<>(supplier.get()); }
    
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
    
    <U> @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true, targetIsContainer = true) IResult<U, E> map(@NotNull Function<? super T, ? extends U> mapper);
    
    <X extends Throwable> @NotNull IResult<T, X> mapException(@NotNull Function<? super Throwable, ? extends X> mapper);
    
    <U> @NotNull IResult<U, E> flatMap(@NotNull Function<? super T, ? extends IResult<U, E>> mapper);
    
    @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) T orThrow() throws E;
    
    @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) T orElse(@NotNull T defaultValue);
    
    T orElseGet(@NotNull Function<? super E, ? extends T> mapper);
    
    <U> U fold(
        @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) Function<? super T, ? extends U> success,
        @NotNull Function<? super E, ? extends U> fail
    );
}
