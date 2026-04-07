//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.lang;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface IResult<TValue, TException extends Throwable> permits FailureResult, SuccessResult
{
    static <TValue, TException extends Throwable> @NotNull IResult<TValue, TException> of(@NotNull TValue value) { return new SuccessResult<>(value); }
    static <TValue, TException extends Throwable> @NotNull IResult<TValue, TException> ofFailed(@NotNull TException exception) { return new FailureResult<>(exception); }
    static <TValue, TException extends Throwable> @NotNull IResult<TValue, TException> ofFailed(@NotNull Supplier<TException> supplier)
        { return new FailureResult<>(supplier.get()); }
    static <TValue, TException extends Throwable> @NotNull IResult<TValue, TException> ofFailed(@NotNull String message, @NotNull Function<String, TException> function)
        { return new FailureResult<>(function.apply(message)); }
    static <TValue, TException extends Throwable> @NotNull IResult<TValue, TException> ofFailed(
        @NotNull String message,
        @NotNull Throwable cause,
        @NotNull BiFunction<String, Throwable, TException> function
    ) { return new FailureResult<>(function.apply(message, cause)); }
    
    boolean isSucceed();
    @ApiStatus.NonExtendable default boolean isFailure() { return !isSucceed(); }
    
    default void ifSucceed(@NotNull Consumer<TValue> action) {}
    default void ifFailure(@NotNull Consumer<TException> action) {}
    @ApiStatus.NonExtendable default void ifSucceedOrElse(@NotNull Consumer<TValue> action, @NotNull Consumer<TException> altAction)
    {
        ifSucceed(action);
        ifFailure(altAction);
    }
    
    <TNewValue> @NotNull IResult<TNewValue, TException> map(@NotNull Function<? super TValue, ? extends TNewValue> mapper);
    <TNewException extends Throwable> @NotNull IResult<TValue, TNewException> mapException(@NotNull Function<? super Throwable, ? extends TNewException> mapper);
    <TNewValue> @NotNull IResult<TNewValue, TException> flatMap(@NotNull Function<? super TValue, ? extends IResult<TNewValue, TException>> mapper);
    
    @NotNull TValue orThrow() throws TException;
    @NotNull TValue orElse(@NotNull TValue defaultValue);
    @NotNull TValue orElseGet(@NotNull Function<? super TException, ? extends TValue> mapper);
    <TNewValue> @NotNull TNewValue fold(@NotNull Function<? super TValue, ? extends TNewValue> success, @NotNull Function<? super TException, ? extends TNewValue> fail);
}
