//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.datastructure;

import kurvcygnus.crispsweetberry.lib.base.extensions.BaseNestedPrinter;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import kurvcygnus.crispsweetberry.lib.base.lang.Pair;
import kurvcygnus.crispsweetberry.lib.base.trait.IBitmaskedEnum;
import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntFunction;

/**
 * A <b>read-only</b> <u>{@link Map}</u> implementation that maps <u>{@link Ranger}</u> (integer range) keys to values.
 * Entries are populated exclusively through <u>{@link #create(Consumer, ConflictHandler)}</u> with a
 * <u>{@link ConflictHandler}</u> that resolves overlaps between ranges.<br>
 * <i>Direct mutation methods (<u>{@link #put(Ranger, Object) put}</u>, <u>{@link #remove(Object) remove}</u>,
 * <u>{@link #putAll(Map) putAll}</u>, <u>{@link #clear() clear}</u>) throw <u>{@link UnsupportedOperationException}</u>.</i>
 *
 * @param <V> the value type associated with each <u>{@link Ranger}</u> key
 * @see Ranger
 * @see ConflictHandler
 * @implNote Since this is a lite implementation, it uses <u>{@link LinkedHashMap}</u> as the internal index.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class RangeMap<V> extends AbstractMap<Ranger, V>
{
    //region Pre-defined Conflict Handlers
    private static final BiPredicate<Ranger, Ranger> CONTAINS = (first, second) ->
        first.inRange(second.min) || first.inRange(second.max);
    
    private static final BiPredicate<Ranger, Ranger> CONFLICTED = CONTAINS.or((first, second) -> second.overlaps(first));
    
    /**
     * One of the pre-defined conflict <u>{@link Ranger}</u>'s handle logic.<br>
     * Once there exists confliction, <b><u>{@link IllegalStateException}</u> will be thrown</b>.
     */
    public static final ConflictHandler<Object> THROW =
        (map, range, value) ->
        {
            for(final var existing: map.keySet())
                if(CONFLICTED.test(range, existing))
                    throw new IllegalStateException(TextUtils.format("Range conflict detected: {} overlaps with {}", range, existing));
            
            return ConflictHandleResult.singleRanger(range, value);
        };
    
    /**
     * One of the pre-defined conflict <u>{@link Ranger}</u>'s handle logic.<br>
     * Once there exists confliction, <b>the original one will be replaced</b>.
     */
    public static final ConflictHandler<Object> OVERWRITE =
        (map, range, value) ->
        {
            map.keySet().removeIf(existing -> CONFLICTED.test(range, existing));
            return ConflictHandleResult.singleRanger(range, value);
        };
    
    /**
     * One of the pre-defined conflict <u>{@link Ranger}</u>'s handle logic.<br>
     * Once there exists confliction, <b>the new one will be ignored</b>.
     */
    public static final ConflictHandler<Object> IGNORE =
        (map, range, value) ->
        {
            for(final var existing: map.keySet())
                if(CONTAINS.test(range, existing))
                    return ConflictHandleResult.noReplace();
            
            return ConflictHandleResult.singleRanger(range, value);
        };
    
    /**
     * One of the pre-defined conflict <u>{@link Ranger}</u>'s handle logic.<br>
     * Once there exists confliction, <b>the two will be merged into one <u>{@link Ranger}</u></b>.
     */
    public static final ConflictHandler<Object> UNION =
        (map, range, value) ->
        {
            final var it = map.entrySet().iterator();
            Ranger finalRange = range;
            
            while(it.hasNext())
            {
                final var entry = it.next();
                if(Objects.equals(entry.getValue(), value))
                {
                    final var merged = entry.getKey().union(finalRange);
                    
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
     * Once there exists confliction, <b>the two will be split into independent ranges</b>:
     * the non-overlapping portion(s) of the existing entry are kept with their original value,
     * and the new range is inserted. The {@code flags} parameter (using the same
     * <u>{@link kurvcygnus.crispsweetberry.lib.base.trait.IBitmaskedEnum}</u> pattern as
     * <u>{@link Ranger#slice(int, int)}</u>) controls which side(s) survive:
     * <ul>
     *     <li><u>{@link Ranger#START_AT_LEFT}</u> — keep only the left (before) portion</li>
     *     <li><u>{@link Ranger#START_AT_RIGHT}</u> — keep only the right (after) portion</li>
     *     <li>Neither — keep both portions</li>
     * </ul>
     */
    public static final IntFunction<ConflictHandler<Object>> DIFFERENCE =
        flags -> (existingMap, newRange, newValue) ->
        {
            final var it = existingMap.entrySet().iterator();
            
            while(it.hasNext())
            {
                final var entry = it.next();
                final var existingRange = entry.getKey();
                final var existingValue = entry.getValue();
                
                if(!CONFLICTED.test(existingRange, newRange))
                    continue;
                
                it.remove();
                
                final int direction = ProcessOptions.DIRECTION.computeRaw(flags);
                
                //* Left portion — the part of the existing range that falls BEFORE newRange.
                if(direction != IBitmaskedEnum.FALSE && existingRange.min < newRange.min)
                    existingMap.put(Ranger.closedOpen(existingRange.min, newRange.min), existingValue);
                
                //* Right portion — the part of the existing range that falls AFTER newRange.
                if(direction != IBitmaskedEnum.TRUE && existingRange.max > newRange.max)
                    existingMap.put(Ranger.openClosed(newRange.max, existingRange.max), existingValue);
            }
            
            return ConflictHandleResult.singleRanger(newRange, newValue);
        };
    //endregion
    
    //region Fields & Constructors
    private final Map<Ranger, V> internalMap;
    
    private RangeMap() { this.internalMap = new LinkedHashMap<>(); }
    
    /**
     * Creates a new <u>{@link RangeMap}</u> by inserting entries provided by {@code insertAction} through the given
     * {@code conflictHandler}.
     * <br><i>The handler is invoked for each entry and may <b>reject</b>, <b>replace</b>, <b>merge</b>, or
     * <b>split</b> overlapping ranges as defined by its implementation.</i>
     *
     * @param insertAction    populates a temporary map with the desired <u>{@link Ranger}</u>-to-<b>V</b> mappings
     * @param conflictHandler resolves overlaps between the new entry and already-inserted entries
     * @param <V>             the value type
     * @return a new <u>{@link RangeMap}</u> containing the processed entries
     * @throws NullPointerException if either parameter is null
     * @see #THROW
     * @see #OVERWRITE
     * @see #IGNORE
     * @see #UNION
     * @see #DIFFERENCE
     */
    public static <V> @NotNull RangeMap<V> create(@NotNull Consumer<Map<Ranger, V>> insertAction, @NotNull ConflictHandler<? super V> conflictHandler)
    {
        Objects.requireNonNull(insertAction, "Param \"insertAction\" must not be null!");
        Objects.requireNonNull(conflictHandler, "Param \"conflictHandler\" must not be null!");
        
        final var resultMap = new RangeMap<V>();
        
        final var inputData = new LinkedHashMap<Ranger, V>();
        insertAction.accept(inputData);
        
        for(final var entry: inputData.entrySet())
        {
            @SuppressWarnings("unchecked")//! Safe generic manipulation.
            final var result = ((ConflictHandler<V>) conflictHandler).handle(resultMap.internalMap, entry.getKey(), entry.getValue());
            
            final Consumer<Entry<Ranger, V>> put = e -> resultMap.internalMap.put(e.getKey(), e.getValue());
            
            result.rangerToReturn().ifPresent(put);
            result.newRanger().ifPresent(put);
        }
        
        return resultMap;
    }
    //endregion
    
    //region public APIs
    /**
     * Looks up the value associated with the <u>{@link Ranger}</u> that contains the given integer {@code value}.
     *
     * @param value the integer to locate
     * @return the mapped value, or {@code null} if no range contains this value
     * @see #getValueOrThrow(int)
     * @see #getValueOr(int, Object)
     * @see #getValueAsOptional(int)
     * @implNote Iterates entries in insertion order with an early break: once {@code value < range.min()},
     *           no subsequent range can contain the value (ranges are non-overlapping and
     *           inserted in ascending order).
     */
    public @Nullable V getValue(int value)
    {
        for(final var entry: this.internalMap.entrySet())
        {
            final var range = entry.getKey();
            if(range.min > value)
                break;
            if(range.inRange(value))
                return entry.getValue();
        }

        return null;
    }
    
    /**
     * Looks up the value associated with the <u>{@link Ranger}</u> that contains the given integer {@code value},
     * or throws if no match is found.
     *
     * @param value the integer to locate
     * @return the mapped value (never null)
     * @throws NoSuchElementException if no range contains this value
     */
    public @NotNull V getValueOrThrow(int value)
    {
        final @Nullable V result = getValue(value);

        if(result == null)
            throw new NoSuchElementException("No value present. Key: " + value);

        return result;
    }

    /**
     * Looks up the value associated with the <u>{@link Ranger}</u> that contains the given integer {@code value},
     * returning {@code defaultValue} if no match is found.
     *
     * @param value        the integer to locate
     * @param defaultValue the fallback value
     * @return the mapped value, or {@code defaultValue} if absent
     */
    public @NotNull V getValueOr(int value, @NotNull V defaultValue) { return Objects.requireNonNullElse(getValue(value), defaultValue); }

    /**
     * Looks up the value associated with the <u>{@link Ranger}</u> that contains the given integer {@code value},
     * wrapping the result in an <u>{@link Optional}</u>.
     *
     * @param value the integer to locate
     * @return an <u>{@link Optional}</u> containing the mapped value, or <u>{@link Optional#empty()}</u> if absent
     */
    public @NotNull Optional<V> getValueAsOptional(int value) { return Optional.ofNullable(getValue(value)); }
    
    @Override public int size() { return this.internalMap.size(); }
    
    @Override public boolean isEmpty() { return this.internalMap.isEmpty(); }
    
    @Override public boolean containsValue(Object value) { return this.internalMap.containsValue(value); }
    
    @Override public boolean containsKey(Object key) { return this.internalMap.containsKey(key); }
    
    @Override public V get(Object key) { return this.internalMap.get(key); }
    
    @Override public @NotNull Set<Ranger> keySet() { return this.internalMap.keySet(); }
    
    @Override public @NotNull Collection<V> values() { return this.internalMap.values(); }
    
    @Override public @NotNull Set<Entry<Ranger, V>> entrySet() { return this.internalMap.entrySet(); }
    
    @Override public V put(Ranger key, V value) { throw new UnsupportedOperationException(); }
    
    @Override public V remove(Object key) { throw new UnsupportedOperationException(); }
    
    @Override public void putAll(Map<? extends Ranger, ? extends V> m) { throw new UnsupportedOperationException(); }
    
    @Override public void clear() { throw new UnsupportedOperationException(); }
    
    //endregion
    
    //region Conflict Handler Definition
    /**
     * A conflict resolution strategy invoked by <u>{@link RangeMap#create(Consumer, ConflictHandler)}</u>
     * whenever a new entry's range may overlap with existing entries in the map.
     * <br><i>Implementations may <b>mutate</b> the {@code existingMap} directly (e.g. remove conflicting entries)
     * and return a <u>{@link ConflictHandleResult}</u> that controls what is inserted into the final map.</i>
     *
     * @param <V> the value type
     * @see RangeMap#create(Consumer, ConflictHandler)
     * @see ConflictHandleResult
     */
    @FunctionalInterface public interface ConflictHandler<V>
    {
        @NotNull ConflictHandleResult<V> handle(
            @NotNull Map<Ranger, V> existingMap,
            @NotNull Ranger newRange,
            @NotNull V newValue
        );
    }
    
    /**
     * The result of a <u>{@link ConflictHandler}</u> invocation, carrying up to <b>two</b>
     * <u>{@link java.util.Map.Entry Ranger-V pairs}</u> to be inserted into the final map:
     * <ul>
     *     <li><u>{@link #rangerToReturn()}</u> — the primary entry (always applied first)</li>
     *     <li><u>{@link #newRanger()}</u> — an optional extra entry for split/merge scenarios</li>
     * </ul>
     * Use <u>{@link #singleRanger(Ranger, Object)}</u> for a single entry,
     * <u>{@link #noReplace()}</u> to skip insertion, or
     * <u>{@link #withExtraRanger(Ranger, Object, Ranger, Object)}</u> for two entries.
     *
     * @param <V> the value type
     */
    public static final class ConflictHandleResult<V> extends BaseNestedPrinter<ConflictHandleResult<V>>
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
        
        /**
         * Creates a result that inserts a <b>single</b> <u>{@link Ranger}</u>-value pair.
         *
         * @param ranger the range to insert
         * @param value  the associated value
         * @param <V>    the value type
         * @return a new <u>{@link ConflictHandleResult}</u> with one entry
         */
        public static @NotNull <V> ConflictHandleResult<V> singleRanger(@NotNull Ranger ranger, @NotNull V value)
        {
            Objects.requireNonNull(ranger, "Param \"ranger\" must not be null!");
            Objects.requireNonNull(value, "Param \"value\" must not be null!");
            
            return new ConflictHandleResult<>(Pair.of(ranger, value), null);
        }
        
        /**
         * Creates a result that <b>skips</b> insertion entirely (the new entry is discarded).
         *
         * @param <V> the value type
         * @return a no-replace result
         */
        @SuppressWarnings("unchecked")//! Since [[ConflictHandleResult#NO_REPLACE]] has no value inside, of course we can cast it!
        public static @NotNull <V> ConflictHandleResult<V> noReplace() { return (ConflictHandleResult<V>) NO_REPLACE; }
        
        /**
         * Creates a result that inserts <b>two</b> <u>{@link Ranger}</u>-value pairs.
         * <i>Useful for conflict handlers that split a range and need both pieces.</i>
         *
         * @param ranger    the primary range
         * @param value     the primary value
         * @param newRanger the extra range
         * @param newValue  the extra value
         * @param <V>       the value type
         * @return a new <u>{@link ConflictHandleResult}</u> with two entries
         */
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
            
            return new ConflictHandleResult<>(new Pair<>(ranger, value), new Pair<>(newRanger, newValue));
        }
        
        /**
         * Returns the primary entry to be inserted into the final map.
         *
         * @return an <u>{@link Optional}</u> containing the primary entry, or empty if no insertion should occur
         */
        public @NotNull Optional<@NotNull Entry<Ranger, V>> rangerToReturn() { return Optional.ofNullable(rangerToReturn); }

        /**
         * Returns the optional extra entry for split/merge scenarios.
         *
         * @return an <u>{@link Optional}</u> containing the extra entry, or empty if none
         */
        public @NotNull Optional<@NotNull Entry<Ranger, V>> newRanger() { return Optional.ofNullable(newRanger); }
        
        @Override public boolean equals(@Nullable Object obj)
        {
            return this == obj || obj instanceof RangeMap.ConflictHandleResult<?> that &&
                Objects.equals(this.newRanger, that.newRanger) &&
                Objects.equals(this.rangerToReturn, that.rangerToReturn);
        }
        
        @Override public int hashCode() { return Objects.hash(rangerToReturn, newRanger); }
        
        @Override public @NotNull @Unmodifiable INestedFieldMap<ConflictHandleResult<V>> getFields()
        {
            return INestedPrintable.buildFieldMap(
                new Pair<>("rangerToReturn", ConflictHandleResult::rangerToReturn),
                new Pair<>("newRanger", ConflictHandleResult::newRanger)
            );
        }
    }
    //endregion
}
