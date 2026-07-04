//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.util;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import kurvcygnus.crispsweetberry.lib.base.extensions.StackDebugger;
import kurvcygnus.crispsweetberry.lib.base.lang.Pair;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.function.*;

import static java.util.Objects.requireNonNull;

/**
 * @since 1.0 Release
 */
public final class AssertUtils
{
    private AssertUtils() { throw new IllegalAccessError("Class \"AssertUtils\" is not meant to be instantized!"); }
    
    private static final boolean IS_DEVELOPMENT_ENVIRONMENT = !FMLEnvironment.production;
    
    public static @NotNull AssertionError impossibleBranch(@NotNull Object @Nullable ... contexts)
    {
        requireNonNull(contexts, "Param \"contexts\" must not be null!");
        
        return new AssertionError(
            TextUtils.format(
                "An impossible branch has been reached by \"{}\"!{}",
                StackDebugger.getFullCallerInfo(),
                contexts.length != 0 ?
                    " Detailed contexts: " + Arrays.toString(contexts) :
                    ""
            )
        );
    }
    
    @SuppressWarnings("DuplicatedCode")//! Stupid IDEA: Dealing the duplicated code have to use functional interfaces and even more complex generics,
    @CanIgnoreReturnValue              //! javac can't handle that, ending up have to specify the generic at call, or get Object as lambda param.
    public static <I extends Iterable<T>, T> @NotNull I nonNullCheckIteration(
        @Nullable I iterable,
        @NotNull Consumer<? super T> action,
        @NotNull String varName
    )
    {
        requireNonNull(iterable, "Param \"iterable\" must not be null!");
        requireNonNull(action, "Param \"action\" must not be null!");
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        int index = 0;
        
        for(final T element: iterable)
        {
            requireNonNull(element, TextUtils.format("Param \"{}\" has null element at index {}", varName, index));
            action.accept(element);
            index++;
        }
        
        return iterable;
    }
    
    @SuppressWarnings({"NullableProblems", "DuplicatedCode"})//! Why IDEA is so stupid...
    @CanIgnoreReturnValue
    public static <T> @NotNull T @NotNull [] nonNullCheckIteration(
        @NotNull String varName,
        @Nullable T @Nullable [] array,
        @NotNull Consumer<? super T> action
    )
    {
        requireNonNull(array, "Param \"array\" must not be null!");
        requireNonNull(action, "Param \"action\" must not be null!");
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        for(int index = 0; index < array.length; index++)
        {
            final T element = requireNonNull(array[index], TextUtils.format("Param \"{}\" has null element at index {}", varName, index));
            action.accept(element);
        }
        
        return array;
    }
    
    public static <M extends Map<K, V>, K, V> @NotNull M nonEmptyMap(@Nullable M map, @NotNull String varName) throws NullPointerException, IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(map == null || map.isEmpty())
            throw new IllegalArgumentException(TextUtils.format("Map \"{}\" must not be null or empty!", varName));
        
        return map;
    }
    
    /**
     * Checks a variable's nullability, and <b>only throws <u>{@link NullPointerException}</u> on development environment.</b>
     * @apiNote <span style="color: f84b4b">Using this method means you should take the responsibility of your assertion.
     * If unsure about a variable's nullability, DO NOT USE.</span>
     */
    @Contract("_, _ -> param1") @SuppressWarnings("DataFlowIssue")//! Using this method means you should take the responsibility of your assertion.
    public static <T> @NotNull T nonNullCheckOnDev(@Nullable T value, @NotNull String varName) throws NullPointerException, IllegalArgumentException
    {
        if(!IS_DEVELOPMENT_ENVIRONMENT)
            return value;
        
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(value == null)
            throw new NullPointerException(TextUtils.format("Param \"{}\" must not be null!", varName));
        
        return value;
    }
    
    /**
     * A method that throws specified exception on development environment, and logs error message in production environment.
     */
    public static <E extends Throwable> void throwOnDevOrLogError(
        @NotNull Function<String, E> function,
        @NotNull Logger logger,
        @NotNull String message,
        @Nullable Object @Nullable ... args
    ) throws E
    {
        requireNonNull(function, "Param \"function\" must not be null!");
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(message, "Param \"message\" must not be null!");

        final var fullMessage = TextUtils.format(message, args);

        if(IS_DEVELOPMENT_ENVIRONMENT)
            throw function.apply(fullMessage);

        logger.error(TextUtils.format("{}\nThis error is caused by {}.", fullMessage, StackDebugger.getFullCallerInfo()));
    }

    public static <E extends Throwable> void throwIf(boolean condition, @NotNull String message, @NotNull Function<String, E> function) throws E
    {
        requireNonNull(message, "Param \"message\" must not be null!");
        if(message.isBlank())
            throw new IllegalArgumentException("Param \"message\" must not be empty!");
        requireNonNull(function, "Param \"function\" must not be null!");

        if(!condition)
            return;

        throw function.apply(message);
    }

    public static <E extends Throwable> void throwIf(boolean condition, @NotNull Supplier<E> supplier) throws E
    {
        requireNonNull(supplier, "Param \"supplier\" must not be null!");

        if(!condition)
            return;

        throw supplier.get();
    }

    public static int unsignedRequired(int variable, @NotNull String varName) throws IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(variable < 0)
            throw new IllegalArgumentException(TextUtils.format("Param \"{}\" should be an unsigned number!", varName));
        
        return variable;
    }

    public static long unsignedRequired(long variable, @NotNull String varName) throws IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(variable < 0)
            throw new IllegalArgumentException(TextUtils.format("Param \"{}\" should be an unsigned number!", varName));
        
        return variable;
    }
    
    public static float unsignedRequired(float variable, @NotNull String varName) throws IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(variable < 0)
            throw new IllegalArgumentException(TextUtils.format("Param \"{}\" should be an unsigned number!", varName));
        
        return variable;
    }
    
    public static double unsignedRequired(double variable, @NotNull String varName) throws IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(variable < 0)
            throw new IllegalArgumentException(TextUtils.format("Param \"{}\" should be an unsigned number!", varName));
        
        return variable;
    }
    
    public static int positiveOnly(int variable, @NotNull String varName) throws IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(variable <= 0)
            throw new IllegalArgumentException(TextUtils.format("Param \"{}\" should be a positive number!", varName));
        
        return variable;
    }
    
    public static long positiveOnly(long variable, @NotNull String varName) throws IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(variable <= 0)
            throw new IllegalArgumentException(TextUtils.format("Param \"{}\" should be a positive number!", varName));
        
        return variable;
    }
    
    public static float positiveOnly(float variable, @NotNull String varName) throws IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(variable <= 0)
            throw new IllegalArgumentException(TextUtils.format("Param \"{}\" should be a positive number!", varName));
        
        return variable;
    }
    
    public static double positiveOnly(double variable, @NotNull String varName) throws IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(variable <= 0)
            throw new IllegalArgumentException(TextUtils.format("Param \"{}\" should be a positive number!", varName));
        
        return variable;
    }
    
    public static <I extends Iterable<E>, E> @NotNull I checkIterableElements(
        @Nullable I iterable,
        @NotNull String varName
    ) throws NullPointerException, IllegalArgumentException
    {
        requireNonNull(varName, "Param \"varName\" must not be null!");
        if(varName.isBlank())
            throw new IllegalArgumentException("Param \"varName\" must not be empty!");
        
        if(iterable == null)
            throw new IllegalArgumentException(TextUtils.format("Iterable \"{}\" must not be null!", varName));
        
        for(final E element : iterable)
            if(element == null)
                throw new IllegalArgumentException(TextUtils.format("Iterable \"{}\"'s elements must not be null!", varName));
        
        return iterable;
    }
    
    public static <E extends Throwable> @NotNull IChecker.OfNoArg<E> produceChecker(
        @NotNull BooleanSupplier supplier,
        @NotNull Supplier<String> messageGetter,
        @NotNull Function<String, E> function,
        boolean reverse
    )
    {
        requireNonNull(supplier, "Param \"supplier\" must not be null!");
        requireNonNull(messageGetter, "Param \"messageGetter\" must not be null!");
        requireNonNull(function, "Param \"function\" must not be null!");
        
        return () ->
        { if(supplier.getAsBoolean() == reverse) throw function.apply(messageGetter.get()); };
    }
    
    public static <E extends Throwable> @NotNull IChecker.OfNoArg<E> produceChecker(
        @NotNull BooleanSupplier supplier,
        @NotNull Supplier<String> messageGetter,
        @NotNull Function<String, E> function
    ) { return produceChecker(supplier, messageGetter, function, false); }
    
    public static <E extends Throwable> @NotNull IChecker.OfNoArg<E> produceChecker(
        @NotNull BooleanSupplier supplier,
        @NotNull String message,
        @NotNull Function<String, E> function,
        boolean reverse
    ) { return produceChecker(supplier, () -> message, function, reverse); }
    
    public static <E extends Throwable> @NotNull IChecker.OfNoArg<E> produceChecker(
        @NotNull BooleanSupplier supplier,
        @NotNull String message,
        @NotNull Function<String, E> function
    ) { return produceChecker(supplier, message, function, false); }
    
    public static <E extends Throwable> @NotNull IChecker.OfNoArg<E> produceChecker(
        @NotNull Supplier<Boolean> supplier,
        @NotNull Supplier<String> messageGetter,
        @NotNull Function<String, E> function,
        boolean reverse
    )
    {
        requireNonNull(supplier, "Param \"supplier\" must not be null!");
        requireNonNull(messageGetter, "Param \"messageGetter\" must not be null!");
        requireNonNull(function, "Param \"function\" must not be null!");
        
        return () ->
        {
            final @Nullable var flag = supplier.get();
            if(flag == null || flag == reverse)
                throw function.apply(messageGetter.get());
        };
    }
    
    public static <E extends Throwable> @NotNull IChecker.OfNoArg<E> produceChecker(
        @NotNull Supplier<Boolean> supplier,
        @NotNull Supplier<String> message,
        @NotNull Function<String, E> function
    ) { return produceChecker(supplier, message, function, false); }
    
    public static <E extends Throwable> @NotNull IChecker.OfNoArg<E> produceChecker(
        @NotNull Supplier<Boolean> supplier,
        @NotNull String message,
        @NotNull Function<String, E> function,
        boolean reverse
    ) { return produceChecker(supplier, () -> message, function, reverse); }
    
    public static <E extends Throwable> @NotNull IChecker.OfNoArg<E> produceChecker(
        @NotNull Supplier<Boolean> supplier,
        @NotNull String message,
        @NotNull Function<String, E> function
    ) { return produceChecker(supplier, message, function, false); }
    
    public static <T, E extends Throwable> @NotNull IChecker.OfOneArg<T, E> produceChecker(
        @NotNull Predicate<T> predicate,
        @NotNull Function<T, String> messageGetter,
        @NotNull Function<String, E> function,
        boolean reverse
    )
    {
        requireNonNull(predicate, "Param \"predicate\" must not be null!");
        requireNonNull(messageGetter, "Param \"messageGetter\" must not be null!");
        requireNonNull(function, "Param \"function\" must not be null!");
        
        return val -> { if(predicate.test(val) == reverse) throw function.apply(messageGetter.apply(val)); };
    }
    
    public static <T, E extends Throwable> @NotNull IChecker.OfOneArg<T, E> produceChecker(
        @NotNull Predicate<T> predicate,
        @NotNull Function<T, String> message,
        @NotNull Function<String, E> function
    ) { return produceChecker(predicate, message, function, false); }
    
    public static <T, E extends Throwable> @NotNull IChecker.OfOneArg<T, E> produceChecker(
        @NotNull Predicate<T> predicate,
        @NotNull String message,
        @NotNull Function<String, E> function,
        boolean reverse
    ) { return produceChecker(predicate, t -> message, function, reverse); }
    
    public static <T, E extends Throwable> @NotNull IChecker.OfOneArg<T, E> produceChecker(
        @NotNull Predicate<T> predicate,
        @NotNull String message,
        @NotNull Function<String, E> function
    ) { return produceChecker(predicate, message, function, false); }
    
    public static <T, E extends Throwable> @NotNull IChecker.OfOneArg<T, E> produceChecker(
        @NotNull Function<T, Pair<Boolean, String>> normalizer,
        @NotNull Function<String, E> function,
        boolean reverse
    )
    {
        requireNonNull(normalizer, "Param \"normalizer\" must not be null!");
        requireNonNull(function, "Param \"function\" must not be null!");
        
        return val -> { final var pair = normalizer.apply(val); if(pair.left() == reverse) throw function.apply(pair.right()); };
    }
    
    public static <T, E extends Throwable> @NotNull IChecker.OfOneArg<T, E> produceChecker(
        @NotNull Function<T, Pair<Boolean, String>> normalizer,
        @NotNull Function<String, E> function
    ) { return produceChecker(normalizer, function, false); }
    
    public interface IChecker
    {
        @FunctionalInterface interface OfNoArg<E extends Throwable> { void check() throws E; }
        @FunctionalInterface interface OfOneArg<T, E extends Throwable> { void check(T value) throws E; }
    }
}