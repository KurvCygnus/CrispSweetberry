//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.trait;

import it.unimi.dsi.fastutil.objects.Object2ByteFunction;
import it.unimi.dsi.fastutil.objects.Object2CharFunction;
import it.unimi.dsi.fastutil.objects.Object2FloatFunction;
import it.unimi.dsi.fastutil.objects.Object2ShortFunction;
import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * This is a trait-styled interface for container classes which can hold null value.<br>
 * Once implemented <u>{@link #value()}</u>, all fluent API, extra value getting methods
 * and other container classes' conversion methods will be all available.<br><br>
 * <i>This is not monad.</i>
 * @apiNote This is <span style="color: f84b4b">NOT</span> thread-safe, when the implementer is mutable and also not thread-safe.
 * Due to the limitation of {@code interface}, it's value access during fluent usage is actually not immutable.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @implNote This interface won't provide {@code #map()} and {@code #flatmap()}, because when container's value is invalid, the behavior of the container itself will be danger:
 * <ol>
 *     <li>
 *         The invalidity of this container shall be passed. And thus, we need constructor.
 *         <b>This requires an extra instance method to carry that, which is completely valueless for container classes' users.</b>
 *     </li>
 *     <li>
 *         <u>{@link #isPresent()}</u>'s existence means the ability of customization.
 *         <b>This also blurs the definition that, "what is the invalid state of this container?"</b>
 *     </li>
 * </ol>
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
    @ApiStatus.NonExtendable default boolean isPresent() { return value() != null; }
    
    private @Nullable T present() { return isPresent() ? value() : null; }
    
    default @NotNull T orThrow()
    {
        final @Nullable T value = present();
        
        if(value == null)
            throw new NoSuchElementException("No value present!");
        return value;
    }
    
    default @NotNull T orElse(@NotNull T defaultValue)
    {
        if(withCheck())
            Objects.requireNonNull(defaultValue, "Param \"defaultValue\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return defaultValue;
        return value;
    }
    
    default @NotNull T orElseGet(@NotNull Supplier<? extends T> supplier)
    {
        if(withCheck())
            Objects.requireNonNull(supplier, "Param \"supplier\" must not be null!");
        
        return Objects.requireNonNullElseGet(present(), supplier);
    }
    
    default void ifPresent(@NotNull Consumer<? super T> action)
    {
        if(withCheck())
            Objects.requireNonNull(action, "Param \"action\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value != null)
            action.accept(value);
    }
    
    default @Nullable T conditionalGet(@NotNull Predicate<? super T> condition)
    {
        if(withCheck())
            Objects.requireNonNull(condition, "Param \"condition\" must not be null!");
        
        final @Nullable T value = present();
        if(value == null || !condition.test(value))
            return null;
        return value;
    }
    
    default @NotNull T assertOrThrow(@NotNull Predicate<? super T> condition)
    {
        if(withCheck())
            Objects.requireNonNull(condition, "Param \"condition\" must not be null!");
        
        final T value = orThrow();
        if(!condition.test(value))
            throw new IllegalArgumentException(TextUtils.format("Condition not met! Value: {}", value));
        return value;
    }
    
    default @NotNull Stream<T> stream()
    {
        final @Nullable T value = present();
        return value != null ? Stream.of(value) : Stream.empty();
    }
    
    default <U> @Nullable U destruct(@NotNull Function<? super T, ? extends U> mapper)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return null;
        return mapper.apply(value);
    }
    
    default byte destructToByte(@NotNull Object2ByteFunction<? super T> mapper, byte defaultValue)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return defaultValue;
        return mapper.apply(value);
    }
    
    default short destructToShort(@NotNull Object2ShortFunction<? super T> mapper, short defaultValue)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return defaultValue;
        return mapper.apply(value);
    }
    
    default int destructToInt(@NotNull ToIntFunction<? super T> mapper, int defaultValue)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return defaultValue;
        return mapper.applyAsInt(value);
    }
    
    default long destructToLong(@NotNull ToLongFunction<? super T> mapper, long defaultValue)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return defaultValue;
        return mapper.applyAsLong(value);
    }
    
    default float destructToFloat(@NotNull Object2FloatFunction<? super T> mapper, float defaultValue)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return defaultValue;
        return mapper.apply(value);
    }
    
    default float destructToFloat(@NotNull Object2FloatFunction<? super T> mapper) { return destructToFloat(mapper, Float.NaN); }
    
    default double destructToDouble(@NotNull ToDoubleFunction<? super T> mapper, double defaultValue)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return defaultValue;
        return mapper.applyAsDouble(value);
    }
    
    default double destructToDouble(@NotNull ToDoubleFunction<? super T> mapper) { return destructToDouble(mapper, Double.NaN); }
    
    default boolean destructToBoolean(@NotNull Predicate<? super T> mapper)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return false;
        return mapper.test(value);
    }
    
    //* ...
    //* To be honest, if you really have such a rare demand like this, I can just say nothing about it.
    default char destructToChar(@NotNull Object2CharFunction<? super T> mapper, char defaultValue)
    {
        if(withCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        final @Nullable T value = present();
        
        if(value == null)
            return defaultValue;
        return mapper.apply(value);
    }
    
    @Override default @Nullable T get() { return value(); }
    
    default boolean withCheck() { return true; }
}
