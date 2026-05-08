//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.trait;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This is a trait-styled interface for container classes which can hold null value.<br>
 * Once implemented <u>{@link #value()}</u>, all fluent API, extra value getting methods
 * and other container classes' conversion methods will be all available.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public interface INullableContainer<T> extends Supplier<T>
{
    /**
     * The only method that needs to be implemented.<br>
     * <b>It's recommended to use <u>{@link #orThrow()}</u> instead of this.</b>
     */
    @Nullable T value();
    
    default @NotNull Optional<T> asOptional() { return Optional.ofNullable(value()); }
    
    /**
     * @apiNote Most extra methods' result relies on this method.<br>
     * So, <span style="color: f84b4b">It is recommended to add conditions, but not to completely rewrite this.</span>
     */
    default boolean isPresent() { return value() != null; }
    
    default @NotNull T orThrow()
    {
        if(!isPresent())
            throw new NoSuchElementException("No value present!");
        //noinspection DataFlowIssue
        return value();//! Safe.
    }
    
    default @NotNull T orElse(@NotNull T defaultValue)
    {
        if(withCheck())
            Objects.requireNonNull(defaultValue, "Param \"defaultValue\" must not be null!");
        
        if(!isPresent())
            return defaultValue;
        //noinspection DataFlowIssue
        return value();//! Safe.
    }
    
    default @NotNull T orElseGet(@NotNull Supplier<? extends T> supplier)
    {
        if(withCheck())
            Objects.requireNonNull(supplier, "Param \"supplier\" must not be null!");
        
        //noinspection DataFlowIssue
        return isPresent() ? value() : supplier.get();//! Safe.
    }
    
    default void ifPresent(@NotNull Consumer<? super T> action)
    {
        if(withCheck())
            Objects.requireNonNull(action, "Param \"action\" must not be null!");
        
        if(isPresent())
        {
            assert value() != null;
            action.accept(value());
        }
    }
    
    default @Nullable T conditionalGet(@NotNull Predicate<? super T> condition)
    {
        if(withCheck())
            Objects.requireNonNull(condition, "Param \"condition\" must not be null!");
        
        final @Nullable T value = value();
        if(!isPresent() || !condition.test(value))
            return null;
        assert value != null;
        return value;
    }
    
    default @NotNull T assertOrThrow(@NotNull Predicate<? super T> condition)
    {
        if(withCheck())
            Objects.requireNonNull(condition, "Param \"condition\" must not be null!");
        
        final T value = orThrow();
        if(!condition.test(value))
            throw new IllegalArgumentException("Condition not met! Value: %s".formatted(value));
        return value;
    }
    
    default @NotNull Stream<T> stream() { return isPresent() ? Stream.of(value()) : Stream.empty(); }
    
    default <U> @Nullable U map(@NotNull Function<? super T, ? extends U> mapper)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        if(!isPresent())
            return null;
        assert value() != null;
        return mapper.apply(value());
    }
    
    @Override default @Nullable T get() { return value(); }
    
    default boolean withCheck() { return true; }
}
