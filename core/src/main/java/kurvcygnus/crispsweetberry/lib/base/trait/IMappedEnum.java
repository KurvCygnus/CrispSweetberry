//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.trait;

import kurvcygnus.crispsweetberry.lib.base.lang.Pair;
import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This is a <u><a href="https://doc.rust-lang.org/rust-by-example/trait.html">trait-styled</a></u> interface for <u>{@link Enum}</u>,
 * which solves one thing: <b>Making <u>{@link Enum}</u> matching faster, and better</b>.<hr>
 * It requires to implement <u>{@link #getKey()}</u>, and provides <u>{@link #constructLookup(Enum)}</u>, <u>{@link #constructLookup(Class)}</u> to
 * generate an <u>{@link Enum}</u> lookup.<br><br>
 * <i>This will be especially handy when you need to parse something to an <u>{@link Enum}</u></i>.
 * @param <T> The type of the construct map's key.
 * @param <E> The type of the construct map's value, which is the implementer itself.
 * @implNote <u>{@link java.util.Map.Entry Entry}</u> actually contains <u>{@link #equals(Object)}</u> and <u>{@link #hashCode()}</u>, whose should be implemented,
 * however, both two has been already implemented in <u>{@link Enum}</u>, so there's no need to worry about them.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see #getKey()
 */
public interface IMappedEnum<T, E extends Enum<E> & IMappedEnum<T, E>> extends Map.Entry<T, E>, ICRTPCaster<IMappedEnum<T, E>, E>
{
    /**
     * @apiNote It is <span style="color: f84b4b">NOT</span> recommend to bound the key with a non-{@code final} field.<hr>
     * <ul>
     *     <li>First, the map which this interface shall construct is {@code static}, meant to be a constant.</li>
     *     <li>
     *         Second, except {@code forzen} flag for singleton registry <u>{@link Enum}</u>, most mutable fields are <span style="color: f84b4b">bad smells</span>,
     *         they may cause some really terrible problem.
     *     </li>
     * </ul>
     */
    @Override T getKey();
    
    static <T, E extends Enum<E> & IMappedEnum<T, E>> @Unmodifiable @NotNull Map<T, E> constructLookup(@NotNull E self)
    {
        Objects.requireNonNull(self, "Param \"self\" must not be null!");
        return constructLookup(self.getDeclaringClass());
    }
    
    static <T, E extends Enum<E> & IMappedEnum<T, E>> @Unmodifiable @NotNull Map<T, E> constructLookup(@NotNull Class<E> self)
    {
        Objects.requireNonNull(self, "Param \"self\" must not be null!");
        final E[] values = self.getEnumConstants();
        
        //! values.length % 2 makes sure that capacity will never be an odd number, which is not preferred by [[HashMap]].
        final int capacity = values.length + values.length % 2;
        
        final var map = new HashMap<T, E>(capacity, 1F);
        for(final E e: values)
        {
            if(map.containsKey(e.getKey()))
                throw new IllegalArgumentException(
                    TextUtils.format(
                        "This interface doesn't work on duplicated cases! Key \"{}\" has these enums conflicted: {}, {}.",
                        e.getKey(),
                        e,
                        map.get(e.getKey())
                    )
                );
            
            map.put(e.getKey(), e);
        }
        return Collections.unmodifiableMap(map);
    }
    
    /**
     * An alternative method that builds a lookup, without requiring an <u>{@link Enum}</u> that has implemented <u>{@link IMappedEnum}</u>.
     * @implNote Since there always exists demand that <u>{@link Enum}</u> is highly acquired by matching key, but the <u>{@link Enum}</u> itself is exposed as
     * API, or just the lookup is not universal
     * (However, in this case, if the key is good to be in <u>{@link Enum}</u>, you should use <u>{@link #constructLookup(Enum)}</u>), and should be in the class
     * where it get invoked, this method is designed for these cases, with strict key array check.
     */
    @SafeVarargs static <T, E extends Enum<E>> @Unmodifiable @NotNull Map<T, E> constructLookup(@NotNull Class<E> target, @NotNull T @NotNull ... keys)
    {
        Objects.requireNonNull(target, "Param \"target\" must not be null!");
        Objects.requireNonNull(keys, "Param \"keys\" must not be null!");
        final E[] values = target.getEnumConstants();
        final int targetLength = values.length;
        
        if(targetLength != keys.length)
            throw new IllegalArgumentException(
                TextUtils.format(
                    "The element count({}) of enum \"{}\" does not match values' length({})!",
                    values.length,
                    values.getClass().getSimpleName(),
                    keys.length
                )
            );
        
        //! values.length % 2 makes sure that capacity will never be an odd number, which is not preferred by [[HashMap]].
        final int capacity = targetLength + targetLength % 2;
        
        final var map = new HashMap<T, E>(capacity, 1F);
        for(int index = 0, targetValuesLength = values.length; index < targetValuesLength; index++)
        {
            final T key = Objects.requireNonNull(keys[index], "Null element found on index " + index);
            final E value = values[index];
            
            if(map.containsKey(key))
                throw new IllegalArgumentException(
                    TextUtils.format(
                        "This interface doesn't work on duplicated cases! Key \"{}\" has these enums conflicted: {}, {}.",
                        key,
                        value,
                        map.get(key)
                    )
                );
            
            map.put(key, value);
        }
        
        return Collections.unmodifiableMap(map);
    }
    
    @ApiStatus.NonExtendable default @Nullable Pair<T, E> asPair()
    {
        if(getKey() == null)
            return null;//! Pair doesn't accept null values, so once the key is null, we have to return [[Pair]] as null.
        return Pair.of(getKey(), getValue());
    }
    
    default @NotNull Map<T, E> singletonMap() { return Collections.singletonMap(getKey(), getValue()); }
    
    @ApiStatus.NonExtendable @Override default E getValue() { return getSelf(); }
    
    @ApiStatus.NonExtendable @Override default E setValue(E value) { throw new UnsupportedOperationException(); }
}
