//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.extensions;

import com.google.errorprone.annotations.DoNotCall;
import kurvcygnus.crispsweetberry.lib.base.lang.Pair;
import kurvcygnus.crispsweetberry.lib.base.trait.ICRTPCaster;
import org.jetbrains.annotations.*;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is an extension interface for all classes that has complex data, prints nested JSON-structured info instead of
 * <u>{@link Record#toString() record's ugly #toString()}</u>, with <u>{@link #getFields()}</u> implemented.
 * @implSpec <pre>{@code
 *  public record Pair<L, R>(L left, R right) implements INestedPrintable<Pair<L, R>>
 *  {
 *      @Override public @NotNull String toString() { return toNestedString(); }
 *
 *      @Override public @NotNull @Unmodifiable INestedFieldMap<Pair<L, R>> getFields()
 *      {
 *          return INestedPrintable.buildFieldMap(
 *              map ->
 *              {
 *                  map.put("left", Pair::left);
 *                  map.put("right", Pair::right);
 *              },
 *              2// The quantity of this class's printable fields, optional param.
 *               // Specifying with correct quantity will make initialization slightly faster.
 *          );
 *
 *          // Using this is also valid:
 *          // return INestedPrintable.buildFieldMap(new Pair<>("left", Pair::left), new Pair<>("right", Pair::right));
 *      }
 *  }
 * }</pre>
 * @author Kurv Cygnus
 * @apiNote Due to Java's {@code interface} limitation, you have to overwrite <u>{@link Object#toString()}</u> with calling <u>{@link #toNestedString()}</u> by your own.
 * <b>Or, if your class is a regular class and haven't inherit something, you can {@code extends} <u>{@link BaseNestedPrinter}</u>.</b>
 * @see IAutoNestedPrintable
 * @see BaseNestedPrinter
 * @see #toNestedString(int) 
 * @see #buildFieldMap
 * <span style="color: f84b4b">MUST SEE:<br> </span><u>{@link #buildFieldMap(Consumer)}</u>, <u>{@link #buildFieldMap(Consumer, int)}</u>, <u>{@link #buildFieldMap(Pair[])}</u>
 * @since 1.0 Release
 */
public interface INestedPrintable<T extends INestedPrintable<T>> extends Serializable, ICRTPCaster<INestedPrintable<T>, T>
{
    /**
     * A simple specified readonly <u>{@link Map}</u> that <u>{@link INestedPrintable}</u> requires,
     * which makes writing the type of constant map quicker and easier and understand.
     * <br><br>
     * <i>Original verbose type: <u>{@link Map}</u><b>{@code <String, Function<T, ?>>}</b></i>, and <b>{@code wildcard} type is <u>{@link Nullable}</u></b>.
     * @implNote Using {@code ?}(<i>wildcard</i>) instead of <u>{@link Object}</u> is for special cases, which you put constants <u>{@link Function}</u>
     * (Serving multi functional libraries's definitions at most cases),
     * like {@code private static final Function<Foo, String> ID_GETTER = f -> f.id;}, such <u>{@link Function}</u> are illegal in helper methods,
     * since comparing to {@code ?}, <u>{@link Object}</u> requires the generic type must be exactly same. For non-constants case, the type is deduced,
     * so it works.<hr>
     * Also, you may ask about implementing this with <b>{@code type alias}</b> for simplicity.
     * However, <span style="color: f84b4b">that'll 100% explode on Runtime,</span>
     * <span style="color: 95cc6d">because Java is a Nominal Type Language, not a Structural Type Language.</span>
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @see INestedPrintable
     * @see NestedFieldMap Implementation
     * @param <T> A class that has implemented <u>{@link INestedPrintable}</u>.
     */
    sealed interface INestedFieldMap<T extends INestedPrintable<T>> extends SequencedMap<String, Function<T, ?>> {}
    
    /**
     * A simple method for building a immutable map, for <u>{@link #getFields()}</u>.
     * @apiNote Using this is necessary, because <u>{@link Map#of()}</u>'s result is <span style="color: f84b4b">UNORDERED</span>.<br>
     * Also, <b>it is recommended to make the return value of this method as a constant. Since <u>{@link #getFields()}</u> is NOT a {@code static method},
     * <span style="color: f84b4b">it is dangerous and easy to leak the instance state in the map, polluting the <u>{@link #toNestedString() toString()}</u> result.</span></b>
     * <br><br>
     * Example: <pre>{@code
     *  @Override public @NotNull @Unmodifiable INestedFieldMap<Foo<T>> getFields()
     *  {
     *      return INestedPrintable.buildFieldMap(
     *          map ->
     *          {
     *              map.put("value", that -> that.current().value);
     *              map.put("state", that -> that.getStateName(state));
     *              // Oh no! `this.state` get leaked!           ↑
     *              // Now state has been bound to a unique instance,
     *              // making it unable to display the field correctly,
     *              // and hard to get recycled.
     *          }
     *      );
     *  }
     * }</pre>
     * <i>This won't happen in a constant.</i>
     */
    static <T extends INestedPrintable<T>> @NotNull @Unmodifiable INestedFieldMap<T> buildFieldMap(@NotNull Consumer<Map<String, Function<T, ?>>> consumer)
    {
        Objects.requireNonNull(consumer, "Param \"consumer\" must not be null!");
        final var map = new LinkedHashMap<String, Function<T, ?>>();
        consumer.accept(map);
        
        return new NestedFieldMap<>(map);
    }
    
    /**
     * A simple method for building a immutable map, for <u>{@link #getFields()}</u>.
     * @apiNote Using this is necessary, because <u>{@link Map#of()}</u>'s result is <span style="color: f84b4b">UNORDERED</span>.<br>
     * Also, <b>it is recommended to make the return value of this method as a constant. Since <u>{@link #getFields()}</u> is NOT a {@code static method},
     * <span style="color: f84b4b">it is dangerous and easy to leak the instance state in the map, polluting the <u>{@link #toNestedString() toString()}</u> result.</span></b>
     * <br>
     * Example: <pre>{@code
     *  @Override public @NotNull @Unmodifiable INestedFieldMap<Foo<T>> getFields()
     *  {
     *      return INestedPrintable.buildFieldMap(
     *          map ->
     *          {
     *              map.put("value", that -> that.current().value);
     *              map.put("state", that -> that.getStateName(state));
     *              // Oh no! `this.state` get leaked!           ↑
     *              // Now state has been bound to a unique instance,
     *              // making it unable to display the field correctly,
     *              // and hard to get recycled.
     *          },
     *          2
     *      );
     *  }
     * }</pre>
     * <i>This won't happen in a constant.</i>
     */
    static <T extends INestedPrintable<T>> @NotNull @Unmodifiable INestedFieldMap<T> buildFieldMap(
        @NotNull Consumer<Map<String, Function<T, ?>>> consumer,
        @Range(from = 1, to = Integer.MAX_VALUE) int allocSize
    )
    {
        Objects.requireNonNull(consumer, "Param \"consumer\" must not be null!");
        final var map = new LinkedHashMap<String, Function<T, ?>>(allocSize, 1F);
        consumer.accept(map);
        
        if(map.size() != allocSize)
            System.err.println(
                MessageFormatter.format(
                    "The actual size of field map is {}, not {}(allocSize).\n This flaw happens at {}.",
                    new Object[] {
                        map.size(),
                        allocSize,
                        Cacher.STACK_WALKER.getCallerClass().getSimpleName()
                    }
                ).getMessage()
            );
        
        return new NestedFieldMap<>(map);
    }
    
    /**
     * A simple method for building a immutable map, for <u>{@link #getFields()}</u>.
     * @apiNote Using this is necessary, because <u>{@link Map#of()}</u>'s result is <span style="color: f84b4b">UNORDERED</span>.<br>
     * Also, this method uses <u>{@link Pair}</u>, it makes writing boilerplate faster,
     * but reminds that <b>Java's generic deduction often fails at such a usage. You have to specify the type of pair at some situations,
     * however, method reference will always work fine in this method, which lambda CAN'T.</b>
     * <hr>
     * Also, <b>it is recommended to make the return value of this method as a constant. Since <u>{@link #getFields()}</u> is NOT a {@code static method},
     * <span style="color: f84b4b">it is dangerous and easy to leak the instance state in the map, polluting the <u>{@link #toNestedString() toString()}</u> result.</span></b>
     */
    @SafeVarargs static <T extends INestedPrintable<T>> @NotNull @Unmodifiable INestedFieldMap<T> buildFieldMap(
        @NotNull Pair<String, Function<T, ?>> @NotNull ... pairs
    )
    {
        Objects.requireNonNull(pairs, "Param \"pairs\" must not be null!");
        
        final int length = pairs.length;
        
        final var map = new LinkedHashMap<String, Function<T, ?>>(length + length % 2, 1F);
        
        for(final var pair: pairs)
        {
            Objects.requireNonNull(pair, "Param \"pair\" must not be null!");
            map.put(pair.getKey(), pair.getValue());
        }
        
        return new NestedFieldMap<>(map);
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
    @NotNull @Unmodifiable INestedFieldMap<T> getFields();
    
    @SuppressWarnings("unchecked")//! Safe casting. It relies on [[Class]].
    private @NotNull @Unmodifiable INestedFieldMap<T> getFields(@NotNull Class<?> clazz)
    {
        final var fields = Objects.requireNonNullElseGet(Cacher.CACHE.get(clazz), this::getFields);
        if(!Cacher.CACHE.containsKey(clazz))
            Cacher.CACHE.put(clazz, (SequencedMap<String, Function<Object, Object>>) fields);
        return (INestedFieldMap<T>) fields;
    }
    
    @ApiStatus.NonExtendable default @NotNull String toNestedString() { return toNestedString(0); }
    
    /**
     * Alt method for printing. It is used for such a case:
     * <pre>{@code
     *  LOGGER.debug(
     *      """
     *      The statsis of this procession:
     *          Id: {}
     *          Stat: {}
     *          Data: {}
     *      """,
     *      id,
     *      stat,
     *      data.toNestedString(6)
     *  );
     * }</pre>
     * Here, {@code data} is <u>{@link INestedPrintable}</u>'s implementer, its position is not started at the head of line, which will mess up the print result.<br>
     * In that case, you can use this method.
     */
    @ApiStatus.NonExtendable default @NotNull String toNestedString(@Range(from = 0, to = Integer.MAX_VALUE) int indent)
    {
        if(indent < 0)
            throw new IllegalArgumentException("Indent must be non-negative!");
        
        final var stringBuilder = new StringBuilder(getFields(this.getClass()).size() * getDefaultCapacityForEachField());
        buildNestedString(stringBuilder.append(startsAtNewLine() ? "\n" : ""), indent, Collections.newSetFromMap(new IdentityHashMap<>()));
        return stringBuilder.toString();
    }
    
    private void buildNestedString(@NotNull StringBuilder stringBuilder, int currentIndent, @NotNull Set<Object> visited)
    {
        final var indentString = " ".repeat(currentIndent);
        
        stringBuilder.append(this.getClass().getSimpleName()).append('\n').append(indentString).append("{\n");
        
        final int nextIndent = currentIndent + getIndent();
        final var fieldIndent = " ".repeat(nextIndent);
        
        for(final var entry: getFields(this.getClass()).entrySet())
        {
            final var name = entry.getKey();
            final Object object = entry.getValue().apply(getSelf());
            
            if(name.isBlank())
                throw new IllegalArgumentException(
                    MessageFormatter.format(
                        "The field of Class \"{}\" has a unpresentable name! Value: {}",
                        this.getClass().getSimpleName(),
                        object
                    ).getMessage()
                );
            
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
        
        final var prefix = name.isEmpty() ? indent : MessageFormatter.format("{}{}: ", indent, name).getMessage();
        
        switch(obj)
        {
            case INestedPrintable<?> nested ->
            {
                if(!visited.add(nested))
                {
                    appendEntryTemplate(stringBuilder, indent, name, circularReferenceTemplate(nested.getClass(), false));
                    return;
                }
                
                stringBuilder.append(prefix);
                nested.buildNestedString(stringBuilder, currentIndent, visited);
                stringBuilder.append("\n");
            }
            case Iterable<?> iterable ->
            {
                //noinspection DuplicatedCode
                if(!visited.add(iterable))
                {
                    appendEntryTemplate(stringBuilder, indent, name, circularReferenceTemplate(iterable.getClass(), true));
                    return;
                }
                stringBuilder.append(prefix).append("\n[\n");
                final int nextIndent = currentIndent + getIndent();
                final String nextIndentStr = " ".repeat(nextIndent);
                
                for(final Object item: iterable)
                    analyseAndAppend(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
                stringBuilder.append(indent).append("]\n");
            }
            case Map<?, ?> map ->
            {
                //noinspection DuplicatedCode
                if(!visited.add(map))
                {
                    appendEntryTemplate(stringBuilder, indent, name, circularReferenceTemplate(map.getClass(), true));
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
            case Object  [] array       -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, array,       prefix);
            case int     [] intArray    -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, intArray,    prefix);
            case long    [] longArray   -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, longArray,   prefix);
            case byte    [] byteArray   -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, byteArray,   prefix);
            case float   [] floatArray  -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, floatArray,  prefix);
            case double  [] doubleArray -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, doubleArray, prefix);
            case char    [] charArray   -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, charArray,   prefix);
            case boolean [] boolArray   -> arrayAppend(stringBuilder, currentIndent, indent, visited, name, boolArray,   prefix);
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
    
    private static @NotNull String circularReferenceTemplate(@NotNull Class<?> clazz, boolean isDataStructure)
    {
        return MessageFormatter.format(
            "{}...(Circular Reference {}){}",
            new Object[] { isDataStructure ? '[' : "", clazz.getSimpleName(), isDataStructure ? ']' : "" }
        ).getMessage();
    }
    
    private static void appendEntryTemplate(
        @NotNull StringBuilder stringBuilder,
        @NotNull String indent,
        @NotNull String name,
        @NotNull String value
    ) { stringBuilder.append(indent).append(name).append(": ").append(value).append('\n'); }
}

/**
 * A holder that holds the cache of <u>{@link INestedPrintable}</u>'s all implementers' field map.
 *
 * @implNote Constructing <u>{@link Map}</u> at every call of <u>{@link INestedPrintable#toNestedString()}</u> is <b>expensive</b>.<br>
 * So we need such a cache map to enhance performance. However, cache map is obviously a mutable map, and sadly, {@code interface} can only have
 * {@code public} constants, so we use {@code package-private} <u>{@link Enum}</u> to store cache instead,
 * which can deny unexpected access(both directly, and on reflect aspect).
 */
@ApiStatus.Internal enum Cacher
{;
    static final Map<Class<?>, SequencedMap<String, Function<Object, @Nullable Object>>> CACHE = new ConcurrentHashMap<>();
    static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
}

final class NestedFieldMap<T extends INestedPrintable<T>> implements INestedPrintable.INestedFieldMap<T>
{
    private final SequencedMap<String, Function<T, ?>> map;
    
    NestedFieldMap(@NotNull SequencedMap<String, Function<T, ?>> map)
    {
        Objects.requireNonNull(map, "Param \"map\" must not be null!");
        
        if(map.isEmpty())
            throw new IllegalArgumentException("Param \"map\" doesn't insert any element into the map, this is invalid!");
        
        this.map = map;
    }
    
    @Override public int size() { return map.size(); }
    
    @Override public boolean isEmpty() { return map.isEmpty(); }
    
    @Override public boolean containsKey(Object key) { return map.containsKey(key); }
    
    @Override public boolean containsValue(Object value) { return map.containsValue(value); }
    
    @Override public Function<T, ?> get(Object key) { return map.get(key); }
    
    @Override @DoNotCall public @Nullable Function<T, ?> put(String key, Function<T, ?> value) { throw new UnsupportedOperationException("awa"); }
    
    @Override @DoNotCall public Function<T, ?> remove(Object key) { throw new UnsupportedOperationException("uwu"); }
    
    @Override @DoNotCall public void putAll(@NotNull Map<? extends String, ? extends Function<T, ?>> m) { throw new UnsupportedOperationException("xwx"); }
    
    @Override @DoNotCall public void clear() { throw new UnsupportedOperationException("fuk u"); }
    
    @Override public @NotNull @Unmodifiable Set<String> keySet() { return Collections.unmodifiableSet(map.keySet()); }
    
    @Override public @NotNull @Unmodifiable Collection<Function<T, ?>> values() { return Collections.unmodifiableCollection(map.values()); }
    
    @Override public @NotNull @Unmodifiable Set<Entry<String, Function<T, ?>>> entrySet() { return Collections.unmodifiableSet(map.entrySet()); }
    
    @Override public @NotNull @Unmodifiable SequencedMap<String, Function<T, ?>> reversed() { return new NestedFieldMap<>(map.reversed()); }
}