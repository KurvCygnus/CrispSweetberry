//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.extensions;

import kurvcygnus.crispsweetberry.lib.base.lang.Pair;
import org.jetbrains.annotations.*;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This is an extension interface for all classes that has complex data, prints nested JSON-structured info instead of
 * <u>{@link Record#toString() record's ugly #toString()}</u>, with <u>{@link #getFields()}</u> implemented.
 * @author Kurv Cygnus
 * @apiNote Due to Java's {@code interface} limitation, you have to overwrite <u>{@link Object#toString()}</u> with calling <u>{@link #toNestedString()}</u> by your own.
 * @see IAutoNestedPrintable
 * @see #buildFieldMap <span style="color: f84b4b">MUST SEE:<br> </span> <u>{@link #buildFieldMap(Pair[])}</u>, <u>{@link #buildFieldMap(Consumer)}</u>
 * @since 1.0 Release
 */
public interface INestedPrintable extends Serializable
{
    /**
     * A simple method for building a immutable map, for <u>{@link #getFields()}</u>.
     * @apiNote Using this is necessary, because <u>{@link Map#of()}</u>'s result is <span style="color: f84b4b">UNORDERED</span>.
     */
    static @NotNull @Unmodifiable Map<String, Supplier<@Nullable Object>> buildFieldMap(@NotNull Consumer<Map<String, Supplier<@Nullable Object>>> consumer)
    {
        Objects.requireNonNull(consumer, "Param \"consumer\" must not be null!");
        final var map = new LinkedHashMap<String, Supplier<@Nullable Object>>();
        consumer.accept(map);
        return Collections.unmodifiableMap(map);
    }
    
    /**
     * A simple method for building a immutable map, for <u>{@link #getFields()}</u>.
     *
     * @apiNote Using this is necessary, because <u>{@link Map#of()}</u>'s result is <span style="color: f84b4b">UNORDERED</span>.
     */
    @SafeVarargs static @NotNull @Unmodifiable Map<String, Supplier<@Nullable Object>> buildFieldMap(@NotNull Pair<String, Supplier<@Nullable Object>> @NotNull ... pairs)
    {
        Objects.requireNonNull(pairs, "Param \"pairs\" must not be null!");
        final var map = new LinkedHashMap<String, Supplier<@Nullable Object>>();
        
        for(final var pair: pairs)
        {
            Objects.requireNonNull(pair, "Element \"%s\"'s getter must not be null!".formatted(pair.left()));
            map.put(pair.getKey(), pair.getValue());
        }
        
        return Collections.unmodifiableMap(map);
    }
    
    /**
     * Decides whether the {@code null} value should be printed.<hr>
     * If the value is {@code false}, {@code null} value will be replace with {@code "N/A"}.<br>
     * Or else, this entry won't be printed.<br><br>
     * <i>Also, notes that this method only tweaks nullable field, <b><u>{@link Optional}</u> won't be affected.</b></i>
     */
    default boolean takeNullFieldAsOptional() { return false; }
    
    /**
     * Gets the indent standard of this class.
     * @implNote Each <u>{@link INestedPrintable}</u>'s implementer's indent is independent, they won't have any override logics, so
     * iterable's recommended to keep all data classes whose are in your project with the same indent.
     */
    default @Range(from = 1, to = Integer.MAX_VALUE) int getIndent() { return 2; }
    
    /**
     * Decides whether the <u>{@link #toNestedString() #toString()}</u> result is started at a new line.
     *
     * @implNote This exists as we usually integrate some Logger API in our project, whose print info has date, time, level, FQCN as prefix,
     * in that case, printing without starting at a new line could be ugly.<hr>
     * <b>This config's default value is {@code true}, if you don't like this, or want to keep maximum compatibility, just overwrite and set this to {@code false}</b>.
     */
    default boolean startsAtNewLine() { return true; }
    
    default @Range(from = 10, to = 100) int getDefaultCapacityForEachField() { return 20; }
    
    /**
     * Gets this class's field map.
     * @apiNote The return result should <b>NOT</b> be dynamic. Only the first get result will count, the laters will be ignored.
     * @see Cacher The reason
     */
    @NotNull @Unmodifiable Map<String, Supplier<@Nullable Object>> getFields();
    
    private @NotNull @Unmodifiable Map<String, Supplier<@Nullable Object>> getFields(@NotNull Class<?> clazz)
    {
        final @Nullable var fields = Objects.requireNonNullElseGet(Cacher.CACHE.get(clazz), this::getFields);
        if(!Cacher.CACHE.containsKey(clazz))
            Cacher.CACHE.put(clazz, fields);
        return fields;
    }
    
    @ApiStatus.NonExtendable default @NotNull String toNestedString() { return toNestedString(0); }
    
    @ApiStatus.NonExtendable default @NotNull String toNestedString(@Range(from = 0, to = Integer.MAX_VALUE) int indent)
    {
        if(indent < 0)
            throw new IllegalArgumentException("Indent must be non-negative!");
        
        final StringBuilder stringBuilder = new StringBuilder(getFields(this.getClass()).size() * getDefaultCapacityForEachField());
        final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        buildNestedString(stringBuilder.append(startsAtNewLine() ? "\n" : ""), indent, visited);
        return stringBuilder.toString();
    }
    
    private void buildNestedString(@NotNull StringBuilder stringBuilder, int currentIndent, @NotNull Set<Object> visited)
    {
        final String indentString = " ".repeat(currentIndent);
        
        stringBuilder.append(this.getClass().getSimpleName()).append(indentString);
        
        final int nextIndent = currentIndent + getIndent();
        final String fieldIndent = " ".repeat(nextIndent);
        
        for(final var entry: getFields(this.getClass()).entrySet())
        {
            final String name = entry.getKey();
            final Object object = entry.getValue().get();
            analyseAndAppend(stringBuilder, nextIndent, fieldIndent, visited, name, object);
        }
        
        stringBuilder.append(indentString).append('}');
    }
    
    private void analyseAndAppend(
        @NotNull StringBuilder stringBuilder,
        int currentIndent,
        @NotNull String indent,
        @NotNull Set<Object> visited,
        @NotNull String name,
        @Nullable Object obj
    )
    {
        if(obj == null)
        {
            if(!takeNullFieldAsOptional())
                appendEntryTemplate(stringBuilder, indent, name, "N/A");
            return;
        }
        
        final String prefix = name.isEmpty() ? indent : "%s%s: ".formatted(indent, name);
        
        switch(obj)
        {
            case INestedPrintable nested ->
            {
                if(!visited.add(nested))
                {
                    appendEntryTemplate(stringBuilder, indent, name, "...(Circular Reference on %s)".formatted(nested.getClass().getSimpleName()));
                    return;
                }
                
                stringBuilder.append(prefix);
                nested.buildNestedString(stringBuilder, currentIndent, visited);
                stringBuilder.append("\n");
            }
            case Iterable<?> iterable ->
            {
                if(!visited.add(iterable))
                {
                    appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference on %s)]".formatted(iterable.getClass().getSimpleName()));
                    return;
                }
                //noinspection DuplicatedCode
                stringBuilder.append(prefix).append("\n[\n");
                final int nextIndent = currentIndent + getIndent();
                final String nextIndentStr = " ".repeat(nextIndent);
                
                for(final Object item: iterable)
                    analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
                stringBuilder.append(indent).append("]\n");
            }
            case Map<?, ?> map ->
            {
                if(!visited.add(map))
                {
                    appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference %s)]".formatted(map.getClass().getSimpleName()));
                    return;
                }
                stringBuilder.append(prefix).append("\n[\n");
                final int nextIndent = currentIndent + getIndent();
                final String nextIndentStr = " ".repeat(nextIndent);
                
                for(final Map.Entry<?, ?> entry: map.entrySet())
                {
                    stringBuilder.append(indent).append("{");
                    analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", entry.getKey());
                    analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", entry.getValue());
                    stringBuilder.append(indent).append("}\n");
                }
                stringBuilder.append(indent).append("]\n");
            }
            case Optional<?> box -> stringBuilder.append(prefix).append(box.map(Object::toString).orElse("null")).append('\n');
            //* That's why we ALWAYS like C#.
            case Object[] array -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, array, prefix);
            case int[] intArray -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, intArray, prefix);
            case long[] longArray -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, longArray, prefix);
            case byte[] byteArray -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, byteArray, prefix);
            case float[] floatArray ->
                arrayAppend(stringBuilder, currentIndent, indent, visited, name, floatArray, prefix);
            case double[] doubleArray -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, doubleArray, prefix);
            case char[] charArray -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, charArray, prefix);
            case boolean[] boolArray ->
                arrayAppend(stringBuilder, currentIndent, indent, visited, name, boolArray, prefix);
            default -> stringBuilder.append(prefix).append(obj).append('\n');
        }
    }
    
    //region Primitive Arrays' Boilerplate Codes
    /**
     * @implNote Yes, that's sad. Despite both array and <u>{@link Iterable}</u> can use enhanced for-each,
     * they have completely no common, we can not do any more abstract stuff on this.<br>
     * OMG, I DO HATE primitive types.
     */
    @SuppressWarnings("DuplicatedCode")
    private void arrayAppend(
        @NotNull StringBuilder stringBuilder,
        int currentIndent,
        @NotNull String indent,
        @NotNull Set<Object> visited,
        @NotNull String name,
        Object[] array,
        String prefix
    )
    {
        if(!visited.add(array))
        {
            appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference on array)]");
            return;
        }
        stringBuilder.append(prefix).append("\n[\n");
        final int nextIndent = currentIndent + getIndent();
        final String nextIndentStr = " ".repeat(nextIndent);
        
        for(final Object item: array)
            analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
        stringBuilder.append(indent).append("]\n");
    }
    
    /**
     * @implNote Yes, that's sad. Despite both array and <u>{@link Iterable}</u> can use enhanced for-each,
     * they have completely no common, we can not do any more abstract stuff on this.<br>
     * OMG, I DO HATE primitive types.
     */
    @SuppressWarnings("DuplicatedCode")
    private void arrayAppend(
        @NotNull StringBuilder stringBuilder,
        int currentIndent,
        @NotNull String indent,
        @NotNull Set<Object> visited,
        @NotNull String name,
        int[] array,
        String prefix
    )
    {
        if(!visited.add(array))
        {
            appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference on array)]");
            return;
        }
        stringBuilder.append(prefix).append("\n[\n");
        final int nextIndent = currentIndent + getIndent();
        final String nextIndentStr = " ".repeat(nextIndent);
        
        for(final int item: array)
            analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
        stringBuilder.append(indent).append("]\n");
    }
    
    /**
     * @implNote Yes, that's sad. Despite both array and <u>{@link Iterable}</u> can use enhanced for-each,
     * they have completely no common, we can not do any more abstract stuff on this.<br>
     * OMG, I DO HATE primitive types.
     */
    @SuppressWarnings("DuplicatedCode")
    private void arrayAppend(
        @NotNull StringBuilder stringBuilder,
        int currentIndent,
        @NotNull String indent,
        @NotNull Set<Object> visited,
        @NotNull String name,
        long[] array,
        String prefix
    )
    {
        if(!visited.add(array))
        {
            appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference on array)]");
            return;
        }
        stringBuilder.append(prefix).append("\n[\n");
        final int nextIndent = currentIndent + getIndent();
        final String nextIndentStr = " ".repeat(nextIndent);
        
        for(final long item: array)
            analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
        stringBuilder.append(indent).append("]\n");
    }
    
    /**
     * @implNote Yes, that's sad. Despite both array and <u>{@link Iterable}</u> can use enhanced for-each,
     * they have completely no common, we can not do any more abstract stuff on this.<br>
     * OMG, I DO HATE primitive types.
     */
    @SuppressWarnings("DuplicatedCode")
    private void arrayAppend(
        @NotNull StringBuilder stringBuilder,
        int currentIndent,
        @NotNull String indent,
        @NotNull Set<Object> visited,
        @NotNull String name,
        byte[] array,
        String prefix
    )
    {
        if(!visited.add(array))
        {
            appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference on array)]");
            return;
        }
        stringBuilder.append(prefix).append("\n[\n");
        final int nextIndent = currentIndent + getIndent();
        final String nextIndentStr = " ".repeat(nextIndent);
        
        for(final byte item: array)
            analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
        stringBuilder.append(indent).append("]\n");
    }
    
    /**
     * @implNote Yes, that's sad. Despite both array and <u>{@link Iterable}</u> can use enhanced for-each,
     * they have completely no common, we can not do any more abstract stuff on this.<br>
     * OMG, I DO HATE primitive types.
     */
    @SuppressWarnings("DuplicatedCode")
    private void arrayAppend(
        @NotNull StringBuilder stringBuilder,
        int currentIndent,
        @NotNull String indent,
        @NotNull Set<Object> visited,
        @NotNull String name,
        float[] array,
        String prefix
    )
    {
        if(!visited.add(array))
        {
            appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference on array)]");
            return;
        }
        stringBuilder.append(prefix).append("\n[\n");
        final int nextIndent = currentIndent + getIndent();
        final String nextIndentStr = " ".repeat(nextIndent);
        
        for(final float item: array)
            analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
        stringBuilder.append(indent).append("]\n");
    }
    
    /**
     * @implNote Yes, that's sad. Despite both array and <u>{@link Iterable}</u> can use enhanced for-each,
     * they have completely no common, we can not do any more abstract stuff on this.<br>
     * OMG, I DO HATE primitive types.
     */
    @SuppressWarnings("DuplicatedCode")
    private void arrayAppend(
        @NotNull StringBuilder stringBuilder,
        int currentIndent,
        @NotNull String indent,
        @NotNull Set<Object> visited,
        @NotNull String name,
        double[] array,
        String prefix
    )
    {
        if(!visited.add(array))
        {
            appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference on array)]");
            return;
        }
        stringBuilder.append(prefix).append("\n[\n");
        final int nextIndent = currentIndent + getIndent();
        final String nextIndentStr = " ".repeat(nextIndent);
        
        for(final double item: array)
            analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
        stringBuilder.append(indent).append("]\n");
    }
    
    /**
     * @implNote Yes, that's sad. Despite both array and <u>{@link Iterable}</u> can use enhanced for-each,
     * they have completely no common, we can not do any more abstract stuff on this.<br>
     * OMG, I DO HATE primitive types.
     */
    @SuppressWarnings("DuplicatedCode")
    private void arrayAppend(
        @NotNull StringBuilder stringBuilder,
        int currentIndent,
        @NotNull String indent,
        @NotNull Set<Object> visited,
        @NotNull String name,
        boolean[] array,
        String prefix
    )
    {
        if(!visited.add(array))
        {
            appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference on array)]");
            return;
        }
        stringBuilder.append(prefix).append("\n[\n");
        final int nextIndent = currentIndent + getIndent();
        final String nextIndentStr = " ".repeat(nextIndent);
        
        for(final boolean item: array)
            analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
        stringBuilder.append(indent).append("]\n");
    }
    
    /**
     * @implNote Yes, that's sad. Despite both array and <u>{@link Iterable}</u> can use enhanced for-each,
     * they have completely no common, we can not do any more abstract stuff on this.<br>
     * OMG, I DO HATE primitive types.
     */
    @SuppressWarnings("DuplicatedCode")
    private void arrayAppend(
        @NotNull StringBuilder stringBuilder,
        int currentIndent,
        @NotNull String indent,
        @NotNull Set<Object> visited,
        @NotNull String name,
        char[] array,
        String prefix
    )
    {
        if(!visited.add(array))
        {
            appendEntryTemplate(stringBuilder, indent, name, "[...(Circular Reference on array)]");
            return;
        }
        stringBuilder.append(prefix).append("\n[\n");
        final int nextIndent = currentIndent + getIndent();
        final String nextIndentStr = " ".repeat(nextIndent);
        
        for(final char item: array)
            analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
        stringBuilder.append(indent).append("]\n");
    }
    //endregion
    
    private static void appendEntryTemplate(
        @NotNull StringBuilder stringBuilder,
        @NotNull String indent,
        @NotNull String name,
        @NotNull String value
    ) { stringBuilder.append(indent).append(name).append(": ").append(value).append("\n"); }
}

/**
 * A holder that holds the cache of <u>{@link INestedPrintable}</u>'s all implementers' field map.
 *
 * @implNote Constructing <u>{@link Map}</u> at every call of <u>{@link INestedPrintable#toNestedString()}</u> is <b>expensive</b>.<br>
 * So we need such a cache map to enhance performance. However, cache map is obviously a mutable map, and sadly, {@code interface} can only have
 * {@code public} constants, so we use {@code package-private} <u>{@link Enum}</u> to store cache instead,
 * which can deny unexpected access(both directly, and on reflect aspect).
 */
enum Cacher { INST; static final Map<Class<?>, Map<String, Supplier<@Nullable Object>>> CACHE = new ConcurrentHashMap<>(); }