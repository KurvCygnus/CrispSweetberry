//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.lang;

import kurvcygnus.crispsweetberry.lib.base.exceptions.ITransactionalThrowable;
import kurvcygnus.crispsweetberry.lib.base.exceptions.StructuredException;
import org.intellij.lang.annotations.Flow;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A simple implementation of <u><a href="https://en.wikipedia.org/wiki/Monad_(functional_programming)">Either Monad</a></u> from <b>Functional Programming</b>.<hr>
 * A usage example:
 * <pre>{@code
 *  return IResult.of(data).
 *      map(Data::transform).
 *      flatMap(this::handle).
 *      fold(
 *          TransformedData::result,
 *          ex ->
 *          {
 *              LOGGER.error(ex);
 *              return ex.rollback();
 *          }
 *      );
 *  // [[ITransitionalThrowable]] is used at here.
 * }</pre>
 * @param <T> The type of data this holder holds.
 * @param <E> The type of exception this holder holds.
 * @author Kurv Cygnus
 * @since 1.0 Release
 * @apiNote Due to Java's type system, generic {@code E} can be burden at most situations.<br>
 * In that case, you can try using <u>{@link StructuredException}</u> instead.
 */
public sealed interface IResult<T, E extends Throwable> permits FailureResult, SuccessResult
{
    /**
     * Wraps a non-null value into a successful <u>{@link IResult}</u>.
     * @param value the value to wrap, must not be null
     * @param <T>   the type of the value. <span style="color: f84b4b">Cannot be <u>{@link IResult}</u> or <u>{@link Optional}</u>.</span>
     * @param <E>   the exception type parameter (unused for success)
     * @return a new <u>{@link SuccessResult}</u> containing the given value
     * @throws NullPointerException if {@code value} is null
     */
    static <T, E extends Throwable> @NotNull IResult<T, E> of(@NotNull @Flow(targetIsContainer = true) T value) { return new SuccessResult<>(value); }

    /**
     * Wraps a non-null exception into a failed <u>{@link IResult}</u>.
     * @param exception the exception to wrap, must not be null
     * @param <T>       the value type parameter (unused for failure)
     * @param <E>       the type of the exception
     * @return a new <u>{@link FailureResult}</u> containing the given exception
     * @throws NullPointerException if {@code exception} is null
     * @see #ofFailed(String, Function)
     * @see #ofFailed(String, Throwable, BiFunction)
     */
    static <T, E extends Throwable> @NotNull IResult<T, E> ofFailed(@NotNull E exception) { return new FailureResult<>(exception); }

    /**
     * Creates a failed <u>{@link IResult}</u> by applying the given message to the provided exception factory.
     * @param message  the detail message for the exception
     * @param function a factory that creates an {@code E} from the message string
     * @param <T>      the value type parameter (unused for failure)
     * @param <E>      the type of the exception
     * @return a new <u>{@link FailureResult}</u> containing the constructed exception
     * @throws NullPointerException if any argument is null
     * @see #ofFailed(Throwable)
     * @see #ofFailed(String, Throwable, BiFunction)
     */
    static <T, E extends Throwable> @NotNull IResult<T, E> ofFailed(@NotNull String message, @NotNull Function<String, E> function)
    {
        Objects.requireNonNull(message, "Param \"message\" must not be null!");
        if(message.isBlank())
            throw new IllegalArgumentException("Param \"message\" must not be empty!");
        Objects.requireNonNull(function, "Param \"function\" must not be null!");
        return ofFailed(function.apply(message));
    }

    /**
     * Creates a failed <u>{@link IResult}</u> by applying the given message and cause to the provided exception factory.
     * @param message  the detail message for the exception
     * @param cause    the root cause of the failure
     * @param function a factory that creates an {@code E} from the message and cause
     * @param <T>      the value type parameter (unused for failure)
     * @param <E>      the type of the exception
     * @return a new <u>{@link FailureResult}</u> containing the constructed exception
     * @throws NullPointerException if any argument is null
     * @see #ofFailed(Throwable)
     * @see #ofFailed(String, Function)
     */
    static <T, E extends Throwable> @NotNull IResult<T, E> ofFailed(@NotNull String message, @NotNull Throwable cause, @NotNull BiFunction<String, Throwable, E> function)
    {
        Objects.requireNonNull(message, "Param \"message\" must not be null!");
        if(message.isBlank())
            throw new IllegalArgumentException("Param \"message\" must not be empty!");
        Objects.requireNonNull(cause, "Param \"cause\" must not be null!");
        Objects.requireNonNull(function, "Param \"function\" must not be null!");
        return ofFailed(function.apply(message, cause));
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")//! For conversion.
    static <T, E extends Throwable> @NotNull IResult<T, E> fromOptional(
        @NotNull Optional<T> optional,
        @NotNull E exception
    )
    {
        Objects.requireNonNull(optional, "Param \"optional\" must not be null!");
        Objects.requireNonNull(exception, "Param \"exception\" must not be null!");
        
        return optional.<IResult<T, E>>map(SuccessResult::new).orElseGet(() -> new FailureResult<>(exception));
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")//! For conversion.
    static <T, E extends Throwable> @NotNull IResult<T, E> fromOptional(
        @NotNull Optional<T> optional,
        @NotNull String message,
        @NotNull Function<String, E> function
    )
    {
        Objects.requireNonNull(optional, "Param \"optional\" must not be null!");
        Objects.requireNonNull(message, "Param \"message\" must not be null!");
        if(message.isBlank())
            throw new IllegalArgumentException("Param \"message\" must not be empty!");
        Objects.requireNonNull(function, "Param \"function\" must not be null!");
        
        return optional.<IResult<T, E>>map(SuccessResult::new).orElseGet(() -> new FailureResult<>(function.apply(message)));
    }
    
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")//! For conversion.
    static <T, E extends Throwable> @NotNull IResult<T, E> fromOptional(
        @NotNull Optional<T> optional,
        @NotNull String message,
        @NotNull Throwable cause,
        @NotNull BiFunction<String, Throwable, E> function
    )
    {
        Objects.requireNonNull(optional, "Param \"optional\" must not be null!");
        Objects.requireNonNull(message, "Param \"message\" must not be null!");
        if(message.isBlank())
            throw new IllegalArgumentException("Param \"message\" must not be empty!");
        Objects.requireNonNull(cause, "Param \"cause\" must not be null!");
        Objects.requireNonNull(function, "Param \"function\" must not be null!");
        
        return optional.<IResult<T, E>>map(SuccessResult::new).orElseGet(() -> new FailureResult<>(function.apply(message, cause)));
    }

    /**
     * Returns {@code true} if this result represents a successful outcome.
     */
    @CheckReturnValue boolean isSucceed();

    /**
     * Returns {@code true} if this result represents a failed outcome.
     */
    @CheckReturnValue default boolean isFailure() { return !isSucceed(); }

    /**
     * Performs the given action on the contained value if this result is a success; otherwise does nothing.
     * @param action the action to perform on the success value
     * @throws NullPointerException if {@code action} is null and this result is a success
     * @see #peekIfSucceed(Consumer)
     */
    default void ifSucceed(@NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) Consumer<T> action) {}

    /**
     * Performs the given action on the contained exception if this result is a failure; otherwise does nothing.
     * @param action the action to perform on the failure exception
     * @throws NullPointerException if {@code action} is null and this result is a failure
     * @see #peekIfFailure(Consumer)
     */
    default void ifFailure(@NotNull Consumer<E> action) {}

    /**
     * Performs one of the two actions depending on whether this result is a success or a failure.
     * @param action    the action to perform on the success value
     * @param altAction the action to perform on the failure exception
     * @see #ifSucceed(Consumer)
     * @see #ifFailure(Consumer)
     */
    default void biAction(@NotNull Consumer<T> action, @NotNull Consumer<E> altAction)
    {
        ifSucceed(action);
        ifFailure(altAction);
    }

    /**
     * Performs the given action on the contained value if this result is a success, then returns {@code this} for chaining.
     * @param action the action to perform on the success value
     * @see #ifSucceed(Consumer)
     * @see #peek(Consumer, Consumer)
     */
    default @NotNull IResult<T, E> peekIfSucceed(@NotNull Consumer<T> action)
    {
        ifSucceed(action);
        return this;
    }

    /**
     * Performs the given action on the contained exception if this result is a failure, then returns {@code this} for chaining.
     * @param action the action to perform on the failure exception
     * @see #ifFailure(Consumer)
     * @see #peek(Consumer, Consumer)
     */
    default @NotNull IResult<T, E> peekIfFailure(@NotNull Consumer<E> action)
    {
        ifFailure(action);
        return this;
    }

    /**
     * Performs one of the two actions depending on the outcome, then returns {@code this} for chaining.
     * @param action    the action to perform on the success value
     * @param altAction the action to perform on the failure exception
     * @see #peekIfSucceed(Consumer)
     * @see #peekIfFailure(Consumer)
     */
    default @NotNull IResult<T, E> peek(@NotNull Consumer<T> action, @NotNull Consumer<E> altAction)
    {
        ifSucceed(action);
        ifFailure(altAction);
        return this;
    }

    /**
     * Transforms the success value by applying the given mapper function.
     * If this result is a failure, the exception is propagated unchanged.
     * @param mapper the function to map the success value
     * @param <U>    the type of the mapped value
     * @return a new <u>{@link IResult}</u> with the mapped value on success, or the same failure
     * @throws NullPointerException if {@code mapper} is null and this result is a success
     * @see #flatMap(Function)
     * @see #biMap(Function, Function)
     * @see java.util.Optional#map(Function)
     */
    <U> @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true, targetIsContainer = true) IResult<U, E> map(@NotNull Function<? super T, ? extends U> mapper);

    /**
     * Transforms the failure exception by applying the given mapper function.
     * If this result is a success, the value is propagated unchanged.
     * @param mapper the function to map the exception
     * @param <X>    the new exception type
     * @return a new <u>{@link IResult}</u> with the mapped exception on failure, or the same success
     * @see #biMap(Function, Function)
     * @see #map(Function)
     */
    <X extends Throwable> @NotNull IResult<T, X> mapException(@NotNull Function<? super Throwable, ? extends X> mapper);
    
    /**
     * Transforms the failure exception into <u>{@link StructuredException}</u>, with given {@code tag}.
     * @param tag The error type this exception belongs to. Cannot be null, and empty string
     * @return a new <u>{@link IResult}</u> with transformed <u>{@link StructuredException}</u>, or the same success
     * @see #mapException(Function)
     */
    @NotNull IResult<T, StructuredException> structuredException(@NotNull String tag);
    
    /**
     * Transforms the failure exception into <u>{@link StructuredException}</u>, with given transforming <u>{@link Function}</u>..
     * @param tagGenerator the <u>{@link Function}</u> that maps the exception to a <u>{@link String}</u> that represents <u>{@link StructuredException}</u>'s type
     * @return a new <u>{@link IResult}</u> with transformed <u>{@link StructuredException}</u>, or the same success
     * @see #mapException(Function)
     */
    @NotNull IResult<T, StructuredException> structuredException(@NotNull Function<E, String> tagGenerator);

    /**
     * Transforms both the success value and the failure exception in a single pass.
     * @param mapper          the function to map the success value
     * @param exceptionMapper the function to map the exception
     * @param <U>             the type of the mapped value
     * @param <X>             the new exception type
     * @return a new <u>{@link IResult}</u> with the mapped value (on success) or the mapped exception (on failure)
     * @see #map(Function)
     * @see #mapException(Function)
     */
    <U, X extends Throwable> @NotNull IResult<U, X> biMap(
        @NotNull Function<? super T, ? extends U> mapper,
        @NotNull Function<? super Throwable, ? extends X> exceptionMapper
    );

    /**
     * Applies a function that returns an <u>{@link IResult}</u> to the success value,
     * flattening the nested result. This is the <a href="https://en.wikipedia.org/wiki/Monad_(functional_programming)#Bind">monadic bind</a> operation.
     * If this result is a failure, the exception is propagated unchanged.
     * @param mapper the function to apply to the success value, returning an {@code IResult}
     * @param <U>    the type of the final success value
     * @return the <u>{@link IResult}</u> returned by the mapper on success, or the same failure
     * @throws NullPointerException if {@code mapper} is null and this result is a success
     * @see #map(Function)
     * @see #andThenFlat(Function, Function)
     * @see java.util.Optional#flatMap(Function)
     */
    <U> @NotNull IResult<U, E> flatMap(@NotNull Function<? super T, ? extends IResult<U, E>> mapper);

    /**
     * Chains two <u>{@link IResult}</u>-returning functions sequentially, composing them with <u>{@link #flatMap(Function)}</u>.
     * Equivalent to {@code fore.andThen(r -> r.flatMap(behind))}.
     * @implSpec <pre>{@code
     *  private IResult<String, IllegalArgumentException> precheck(@NotNull String text) {...}
     *
     *  private IResult<String, IllegalArgumentException> process1(@NotNull String text) {...}
     *
     *  private IResult<String, IllegalArgumentException> process2(@NotNull String text) {...}
     *
     *  Function<String, IResult<String, IllegalArgumentException>> checkThenProcess1 =
     *      IResult.andThenFlat(this::precheck, this::process1);
     *
     *  Function<String, IResult<String, IllegalArgumentException>> checkThenProcess2 =
     *      IResult.andThenFlat(this::precheck, this::process2);
     * }</pre>
     * @param fore   the first function to apply
     * @param behind the second function to apply to the result of {@code fore}
     * @param <T>    the input type of the first function
     * @param <U>    the output type of the second function
     * @param <E>    the exception type shared across both functions
     * @return a composed function that pipes the input through {@code fore} then {@code behind}
     * @throws NullPointerException if any argument is null
     * @see #flatMap(Function)
     */
    static <T, U, E extends Throwable> @NotNull Function<T, IResult<U, E>> andThenFlat(
        @NotNull Function<T, IResult<T, E>> fore,
        @NotNull Function<T, IResult<U, E>> behind
    )
    {
        Objects.requireNonNull(fore, "Param \"fore\" must not be null!");
        Objects.requireNonNull(behind, "Param \"behind\" must not be null!");
        return fore.andThen(r -> r.flatMap(behind));
    }

    /**
     * Converts this result into a <u>{@link Optional}</u>.
     * @return an <u>{@link Optional}</u> containing the success value, or empty if this is a failure
     * @deprecated <span style="color: f84b4b">The exception information is silently discarded.</span> Prefer {@link #fold(Function, Function)}
     * or {@link #orElseMapException(Function)} if you need to handle the failure case explicitly. It is recommended to only use this to pass result to method that
     * accepts <u>{@link Optional}</u> only.
     */
    @NotNull @Deprecated Optional<T> asOptional();

    /**
     * Converts this result into a single-element or empty <u>{@link Stream}</u>.<br>
     * A success result yields a stream containing the value;<br>
     * a failure result yields an empty stream.
     * @deprecated It's not recommended to use this at most situations, <b>only use this when other methods to pass accepts <u>{@link Stream}</u> only.</b>
     */
    @NotNull @Deprecated Stream<T> asStream();

    /**
     * Returns the success value if this result is a success, or throws the contained exception if this is a failure.
     * @return the success value
     * @throws E the contained exception if this is a failure
     * @see #orElse(Object)
     * @see #orElseGet(Supplier)
     * @see #orElseMapException(Function)
     */
    @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) T orThrow() throws E;
    
    /**
     * Returns transformed success value if this result is a success, or throws the contained exception if this is a failure.
     * @return the success value
     * @throws E the contained exception if this is a failure
     */
    default @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) <R> R destruct(@NotNull Function<T, R> destructor) throws E
    {
        Objects.requireNonNull(destructor, "Param \"destructor\" must not be null!");
        return destructor.apply(orThrow());
    }
    
    /**
     * Returns an {@code int} transformed by success value if this result is a success, or throws the contained exception if this is a failure.
     * @return the success value
     * @throws E the contained exception if this is a failure
     */
    default @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) int destructToInt(@NotNull ToIntFunction<T> destructor) throws E
    {
        Objects.requireNonNull(destructor, "Param \"destructor\" must not be null!");
        return destructor.applyAsInt(orThrow());
    }
    
    /**
     * Returns a {@code long} transformed by success value if this result is a success, or throws the contained exception if this is a failure.
     * @return the success value
     * @throws E the contained exception if this is a failure
     */
    default @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) long destructToLong(@NotNull ToLongFunction<T> destructor) throws E
    {
        Objects.requireNonNull(destructor, "Param \"destructor\" must not be null!");
        return destructor.applyAsLong(orThrow());
    }
    
    /**
     * Returns a {@code double} transformed by success value if this result is a success, or throws the contained exception if this is a failure.
     * @return the success value
     * @throws E the contained exception if this is a failure
     */
    default @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) double destructToDouble(@NotNull ToDoubleFunction<T> destructor) throws E
    {
        Objects.requireNonNull(destructor, "Param \"destructor\" must not be null!");
        return destructor.applyAsDouble(orThrow());
    }
    
    /**
     * Returns a {@code boolean} transformed by success value if this result is a success, or throws the contained exception if this is a failure.
     * @return the success value
     * @throws E the contained exception if this is a failure
     */
    default @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) boolean destructToBoolean(@NotNull Predicate<T> destructor) throws E
    {
        Objects.requireNonNull(destructor, "Param \"destructor\" must not be null!");
        return destructor.test(orThrow());
    }

    /**
     * Returns the success value if this result is a success, or the given {@code defaultValue} if this is a failure.
     * @param defaultValue the value to return if this is a failure
     * @return the success value or the default value
     * @see #orElseGet(Supplier)
     * @see #orThrow()
     * @see #orElseMapException(Function)
     */
    @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) @NotNull T orElse(@NotNull T defaultValue);

    /**
     * Returns the success value if this result is a success, or the value supplied by {@code defaultValue} if this is a failure.
     * @param defaultValue a supplier for the fallback value
     * @return the success value or the supplied fallback
     * @throws NullPointerException if {@code defaultValue} is null and this result is a failure
     * @see #orElse(Object)
     * @see #orThrow()
     * @see #orElseMapException(Function)
     */
    @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) T orElseGet(@NotNull Supplier<? extends T> defaultValue);

    /**
     * Returns the success value if it satisfies the given condition, or throws {@link NoSuchElementException} otherwise.
     * If this result is a failure, the contained exception is thrown instead.
     * @param condition the predicate to test the success value against
     * @return the success value if it passes the condition
     * @throws NoSuchElementException if the value does not satisfy the condition
     */
    @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true)
    T conditionalGet(@NotNull Predicate<? super T> condition) throws NoSuchElementException, IllegalStateException;

    /**
     * Returns the success value if this result is a success, or applies the given mapper to the
     * exception and returns the result if this is a failure.
     * This is useful for recovery — converting a failure into a fallback value.
     * @param mapper the function to map the exception to a fallback value
     * @return the success value, or the mapped fallback value
     * @see #orElse(Object)
     * @see #orElseGet(Supplier)
     * @see #fold(Function, Function)
     */
    T orElseMapException(@NotNull Function<? super E, ? extends T> mapper);

    /**
     * Evaluates this result as a <u><a href="https://en.wikipedia.org/wiki/Catamorphism">catamorphism</a></u>,
     * applying one of the two functions to produce a single result.
     * Equivalent to pattern matching on success vs failure.
     * @param success the function to apply to the success value
     * @param fail    the function to apply to the failure exception
     * @param <U>     the type of the folded result
     * @return the result of applying {@code success} on success, or {@code fail} on failure
     * @throws NullPointerException if the matched function is null
     * @see #map(Function)
     * @see #orElseMapException(Function)
     * @apiNote See <u>{@link ITransactionalThrowable ITransitionalThrowable}</u>, and <u>{@link StructuredException}</u>.
     * Both of these utils can play an important and powerful role in this method.
     */
    <U> U fold(
        @NotNull @Flow(source = Flow.THIS_SOURCE, sourceIsContainer = true) Function<? super T, ? extends U> success,
        @NotNull Function<? super E, ? extends U> fail
    );

    @NotNull String toString();
}

@ApiStatus.Internal
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
    
    @SuppressWarnings("unchecked")//! Valid casting since value does not exist.
    @Override public @NotNull <U> IResult<U, E> map(@NotNull Function<? super T, ? extends U> mapper) { return (IResult<U, E>) this; }

    @Override public @NotNull <X extends Throwable> IResult<T, X> mapException(@NotNull Function<? super Throwable, ? extends X> mapper)
        { return new FailureResult<>(mapper.apply(exception)); }
    
    @Override public @NotNull IResult<T, StructuredException> structuredException(@NotNull String tag) { return StructuredException.failedResult(exception, tag); }
    
    @Override public @NotNull IResult<T, StructuredException> structuredException(@NotNull Function<E, String> tagGenerator)
        { return StructuredException.failedResult(exception, tagGenerator.apply(exception)); }
    
    @Override public @NotNull <U, X extends Throwable> IResult<U, X> biMap(
        @NotNull Function<? super T, ? extends U> mapper,
        @NotNull Function<? super Throwable, ? extends X> exceptionMapper
    ) { return new FailureResult<>(exceptionMapper.apply(exception)); }
    
    @SuppressWarnings("unchecked")//! Valid casting since value does not exist.
    @Override public @NotNull <U> IResult<U, E> flatMap(@NotNull Function<? super T, ? extends IResult<U, E>> mapper) { return (IResult<U, E>) this; }

    @Override @Deprecated public @NotNull Optional<T> asOptional() { return Optional.empty(); }

    @Override @Deprecated public @NotNull Stream<T> asStream() { return Stream.empty(); }

    @Override public @NotNull T orThrow() throws E { throw exception; }

    @Override public @NotNull T orElse(@NotNull T defaultValue) { return defaultValue; }

    @Override public @NotNull T orElseGet(@NotNull Supplier<? extends T> defaultValue) { return defaultValue.get(); }

    @Override public @NotNull T conditionalGet(@NotNull Predicate<? super T> condition) { throw new NoSuchElementException("No value present!"); }

    @Override public @NotNull T orElseMapException(@NotNull Function<? super E, ? extends T> mapper) { return mapper.apply(exception); }

    @Override public @NotNull <U> U fold(@NotNull Function<? super T, ? extends U> success, @NotNull Function<? super E, ? extends U> fail) { return fail.apply(exception); }

    @Override public boolean equals(@Nullable Object obj) { return this == obj || obj instanceof FailureResult<?, ?> that && Objects.equals(this.exception, that.exception); }

    @Override public int hashCode() { return Objects.hash(exception); }

    @Override public @NotNull String toString() { return "Result -> exception: " + exception; }
}

@ApiStatus.Internal
final class SuccessResult<T, E extends Throwable> implements IResult<T, E>
{
    private final @NotNull T value;

    SuccessResult(@NotNull T value)
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        
        if(value instanceof IResult<?, ?> || value instanceof Optional<?>)
            throw new IllegalArgumentException("Wrapping IResult or Optional is not allowed!");
        
        this.value = value;
    }

    @Override public boolean isSucceed() { return true; }

    @Override public void ifSucceed(@NotNull Consumer<T> action) { action.accept(value); }

    @Override public @NotNull <U> IResult<U, E> map(@NotNull Function<? super T, ? extends U> mapper) { return new SuccessResult<>(mapper.apply(value)); }

    @SuppressWarnings("unchecked")//! Valid casting since exception does not exist.
    @Override public @NotNull <X extends Throwable> IResult<T, X> mapException(@NotNull Function<? super Throwable, ? extends X> mapper) { return (IResult<T, X>) this; }
    
    @SuppressWarnings("unchecked")//! Valid casting since exception does not exist.
    @Override public @NotNull IResult<T, StructuredException> structuredException(@NotNull String tag) { return (IResult<T, StructuredException>) this; }
    
    @SuppressWarnings("unchecked")//! Valid casting since exception does not exist.
    @Override public @NotNull IResult<T, StructuredException> structuredException(@NotNull Function<E, String> tagGenerator)
        { return (IResult<T, StructuredException>) this; }
    
    @Override public @NotNull <U, X extends Throwable> IResult<U, X> biMap(
        @NotNull Function<? super T, ? extends U> mapper,
        @NotNull Function<? super Throwable, ? extends X> exceptionMapper
    ) { return new SuccessResult<>(mapper.apply(value)); }

    @Override public @NotNull <U> IResult<U, E> flatMap(@NotNull Function<? super T, ? extends IResult<U, E>> mapper) { return mapper.apply(value); }

    @Override @Deprecated public @NotNull Optional<T> asOptional() { return Optional.of(value); }

    @Override @Deprecated public @NotNull Stream<T> asStream() { return Stream.of(value); }

    @Override public @NotNull T orThrow() { return value; }

    @Override public @NotNull T orElse(@NotNull T defaultValue) { return value; }

    @Override public @NotNull T orElseGet(@NotNull Supplier<? extends T> defaultValue) { return value; }

    @Override public @NotNull T conditionalGet(@NotNull Predicate<? super T> condition)
    {
        if(condition.test(value))
            return value;
        
        throw new IllegalStateException("The value doesn't meet the condition!");
    }

    @Override public @NotNull T orElseMapException(@NotNull Function<? super E, ? extends T> mapper) { return value; }

    @Override public @NotNull <U> U fold(@NotNull Function<? super T, ? extends U> success, @NotNull Function<? super E, ? extends U> fail) { return success.apply(value); }

    @Override public boolean equals(@Nullable Object obj) { return this == obj || obj instanceof SuccessResult<?, ?> that && Objects.equals(value, that.value); }

    @Override public int hashCode() { return Objects.hash(value); }

    @Override public @NotNull String toString() { return "Result -> value: " + value; }
}