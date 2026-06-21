//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.datastructure;

import kurvcygnus.crispsweetberry.lib.base.exceptions.NotImplementedYetException;
import kurvcygnus.crispsweetberry.lib.base.lang.Pair;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * This is a specialized <u>{@link Map}</u> implementation for non-discrete data structures like <u>{@link Ranger}</u>,
 * focusing on range value mapping.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @param <V> The corresponded value type for this {@code CrispRangeMap}.
 * @see Ranger Ranger
 * @implNote Since this is a lite implementation, it uses <u>{@link LinkedHashMap}</u> as internal indexing map.
 */
public final class RangeMap<V> extends AbstractMap<Ranger, V>
{
    //region Pre-defined Conflict Handlers
    private static final BiPredicate<Ranger, Ranger> CONTAINS = (first, second) ->
        first.inRange(second.min()) || first.inRange(second.max());
    
    private static final BiPredicate<Ranger, Ranger> CONFLICTED = CONTAINS.or((first, second) -> second.overlaps(first));
    
    /**
     * One of the pre-defined conflict <u>{@link Ranger}</u>'s handle logic.<br>
     * Once there exists confliction, <b><u>{@link IllegalStateException}</u> will be thrown</b>.
     */
    public static final ConflictHandler<Object> THROW = (map, range, value) ->
    {
        for(Ranger existing: map.keySet())
            if(CONFLICTED.test(range, existing))
                throw new IllegalStateException(MessageFormatter.format("Range conflict detected: {} overlaps with {}", range, existing).getMessage());
        
        return ConflictHandleResult.singleRanger(range, value);
    };
    
    /**
     * One of the pre-defined conflict <u>{@link Ranger}</u>'s handle logic.<br>
     * Once there exists confliction, <b>the original one will be replaced</b>.
     */
    public static final ConflictHandler<Object> OVERWRITE = (map, range, value) ->
    {
        map.keySet().removeIf(existing -> CONFLICTED.test(range, existing));
        return ConflictHandleResult.singleRanger(range, value);
    };
    
    /**
     * One of the pre-defined conflict <u>{@link Ranger}</u>'s handle logic.<br>
     * Once there exists confliction, <b>the new one will be ignored</b>.
     */
    public static final ConflictHandler<Object> IGNORE = (map, range, value) ->
    {
        for(Ranger existing: map.keySet())
            if(CONTAINS.test(range, existing))
                return ConflictHandleResult.noReplace();
        
        return ConflictHandleResult.singleRanger(range, value);
    };
    
    /**
     * One of the pre-defined conflict <u>{@link Ranger}</u>'s handle logic.<br>
     * Once there exists confliction, <b>the two will be merged into one <u>{@link Ranger}</u></b>.
     */
    public static final ConflictHandler<Object> UNION = (map, range, value) ->
    {
        final Iterator<Map.Entry<Ranger, Object>> it = map.entrySet().iterator();
        Ranger finalRange = range;
        
        while(it.hasNext())
        {
            final Map.Entry<Ranger, Object> entry = it.next();
            if(Objects.equals(entry.getValue(), value))
            {
                final Optional<Ranger> merged = entry.getKey().union(finalRange);
                
                if(merged.isPresent())
                {
                    finalRange = merged.get();
                    it.remove();
                }
            }
        }
        
        return ConflictHandleResult.singleRanger(finalRange, value);
    };
    
    /**
     * One of the pre-defined conflict <u>{@link Ranger}</u>'s handle logic.<br>
     * Once there exists confliction, <b>the two will be splits into two independent {@code CrispRanger}, and the result is depended on param {@code flags} you provided</b>.
     */
    public static final IntFunction<ConflictHandler<Object>> DIFFERENCE =
        flags -> (map, range, value) -> difference(map, range, value, flags);
    
    private static @NotNull ConflictHandleResult<Object> difference(
        @NotNull Map<Ranger, Object> existingMap,
        @NotNull Ranger newRange,
        @NotNull Object newValue,
        @MagicConstant int flags
    )
    {
        //? TODO: This is going to be a hard time...
        throw new NotImplementedYetException();
    }
    //endregion
    
    //region Fields & Constructors
    private final Map<Ranger, V> internalMap;
    
    private RangeMap() { this.internalMap = new LinkedHashMap<>(); }
    
    public static <V> @NotNull RangeMap<V> create(@NotNull Consumer<Map<Ranger, V>> insertAction, @NotNull ConflictHandler<? super V> conflictHandler)
    {
        Objects.requireNonNull(insertAction, "Param \"insertAction\" must not be null!");
        Objects.requireNonNull(conflictHandler, "Param \"conflictHandler\" must not be null!");
        
        final RangeMap<V> resultMap = new RangeMap<>();
        
        final Map<Ranger, V> inputData = new LinkedHashMap<>();
        insertAction.accept(inputData);
        
        for(final Entry<Ranger, V> entry: inputData.entrySet())
        {
            final Ranger range = entry.getKey();
            final V value = entry.getValue();
            
            @SuppressWarnings("unchecked")//! Safe generic manipulation.
            final ConflictHandleResult<V> result = ((ConflictHandler<V>) conflictHandler).handle(resultMap.internalMap, range, value);
            
            final var rangerToReturn = result.rangerToReturn();
            final var newRanger = result.newRanger();
            
            final Consumer<Entry<Ranger, V>> put = e -> resultMap.internalMap.put(e.getKey(), e.getValue());
            
            rangerToReturn.ifPresent(put);
            newRanger.ifPresent(put);
        }
        
        return resultMap;
    }
    //endregion
    
    //region public APIs
    public @Nullable V getValue(int value)
    {
        //? TODO: Opti this.
        for(final Map.Entry<Ranger, V> entry: this.internalMap.entrySet())
            if(entry.getKey().inRange(value))
                return entry.getValue();
        
        return null;
    }
    
    public @NotNull V getValueOrThrow(int value)
    {
        @Nullable final V result = getValue(value);
        
        if(result == null)
            throw new NoSuchElementException("No value present. Key: " + value);
        
        return result;
    }
    
    public @NotNull V getValueOrDefault(int value, @NotNull V defaultValue) { return Objects.requireNonNullElse(getValue(value), defaultValue); }
    
    public @NotNull Optional<V> getValueSafe(int value) { return Optional.ofNullable(getValue(value)); }
    
    @Override public int size() { return this.internalMap.size(); }
    
    @Override public boolean isEmpty() { return this.internalMap.isEmpty(); }
    
    @Override public boolean containsValue(Object value) { return this.internalMap.containsValue(value); }
    
    @Override public boolean containsKey(Object key) { return this.internalMap.containsKey(key); }
    
    @Override public V get(Object key) { return this.internalMap.get(key); }
    
    @Override public @NotNull Set<Ranger> keySet() { return this.internalMap.keySet(); }
    
    @Override public @NotNull Collection<V> values() { return this.internalMap.values(); }
    
    @Override public @NotNull Set<Entry<Ranger, V>> entrySet() { return this.internalMap.entrySet(); }
    //endregion
    
    //region Conflict Handler Definition
    @FunctionalInterface public interface ConflictHandler<V>
    {
        @NotNull ConflictHandleResult<V> handle(
            @NotNull Map<Ranger, V> existingMap,
            @NotNull Ranger newRange,
            @NotNull V newValue
        );
    }
    
    public static final class ConflictHandleResult<V>
    {
        private static final ConflictHandleResult<Object> NO_REPLACE = new ConflictHandleResult<>(null, null);
        
        private final @Nullable Entry<Ranger, V> rangerToReturn;
        private final @Nullable Entry<Ranger, V> newRanger;
        
        private ConflictHandleResult(
            @Nullable Entry<Ranger, V> rangerToReturn,
            @Nullable Entry<Ranger, V> newRanger
        )
        {
            this.rangerToReturn = rangerToReturn;
            this.newRanger = newRanger;
        }
        
        public static @NotNull <V> ConflictHandleResult<V> singleRanger(@NotNull Ranger ranger, @NotNull V value)
        {
            Objects.requireNonNull(ranger, "Param \"ranger\" must not be null!");
            Objects.requireNonNull(value, "Param \"value\" must not be null!");
            
            return new ConflictHandleResult<>(Pair.of(ranger, value), null);
        }
        
        @SuppressWarnings("unchecked")//! Since [[ConflictHandleResult#NO_REPLACE]] has no value inside, of course we can cast it!
        public static @NotNull <V> ConflictHandleResult<V> noReplace() { return (ConflictHandleResult<V>) NO_REPLACE; }
        
        public static @NotNull <V> ConflictHandleResult<V> withExtraRanger(
            @NotNull Ranger ranger,
            @NotNull V value,
            @NotNull Ranger newRanger,
            @NotNull V newValue
        )
        {
            Objects.requireNonNull(ranger, "Param \"ranger\" must not be null!");
            Objects.requireNonNull(value, "Param \"value\" must not be null!");
            Objects.requireNonNull(newRanger, "Param \"newRanger\" must not be null!");
            Objects.requireNonNull(newValue, "Param \"newValue\" must not be null!");
            
            return new ConflictHandleResult<>(Pair.of(ranger, value), Pair.of(newRanger, newValue));
        }
        
        public @NotNull Optional<@NotNull Entry<Ranger, V>> rangerToReturn() { return Optional.ofNullable(rangerToReturn); }
        
        public @NotNull Optional<@NotNull Entry<Ranger, V>> newRanger() { return Optional.ofNullable(newRanger); }
        
        @Override public boolean equals(@Nullable Object obj)
        {
            if(obj == this)
                return true;
            if(obj == null || obj.getClass() != this.getClass())
                return false;
            final ConflictHandleResult<?> that = (ConflictHandleResult<?>) obj;
            return Objects.equals(this.rangerToReturn, that.rangerToReturn) &&
                Objects.equals(this.newRanger, that.newRanger);
        }
        
        @Override public int hashCode() { return Objects.hash(rangerToReturn, newRanger); }
        
        @Override public @NotNull String toString() { return "ConflictHandleResult{ rangerToReturn: %s, newRanger: %s }".formatted(rangerToReturn, newRanger); }
    }
    //endregion
}
