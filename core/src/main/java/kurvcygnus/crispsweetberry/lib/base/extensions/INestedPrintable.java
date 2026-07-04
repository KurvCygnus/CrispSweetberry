//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.extensions;

import kurvcygnus.crispsweetberry.lib.base.lang.Pair;
import kurvcygnus.crispsweetberry.lib.base.trait.ICRTPCaster;
import kurvcygnus.crispsweetberry.lib.base.util.AssertUtils;
import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import org.jetbrains.annotations.*;

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
 *               // Also, this param is optional.
 *          );
 *
 *          // Using this is also valid:
 *          // return INestedPrintable.buildFieldMap(new Pair<>("left", Pair::left), new Pair<>("right", Pair::right));
 *      }
 *  }
 * }</pre>
 * <i>Tip: this is actually verbose, using <u>{@link IAutoNestedPrintable.OfRecordHandle}</u></i> is more recommended.
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
public interface INestedPrintable<T extends INestedPrintable<T>> extends ICRTPCaster<INestedPrintable<T>, T>
{
    //region Field Map Definitions & Utils
    /**
     * A simple specified readonly <u>{@link Map}</u> that <u>{@link INestedPrintable}</u> requires,
     * which makes writing the type of constant map quicker and easier and understand.
     * @implNote Using {@code ?}(<i>wildcard</i>) instead of <u>{@link Object}</u> is for special cases, which you put constants <u>{@link Function}</u>
     * (Serving multi functional libraries's definitions at most cases),
     * like {@code private static final Function<Foo, String> ID_GETTER = f -> f.id;}, such <u>{@link Function}</u> are illegal in helper methods,
     * since comparing to {@code ?}, <u>{@link Object}</u> requires the generic type must be exactly same. For non-constants case, the type is deduced,
     * so it works.<hr>
     * Also, you may ask about implementing this with <b>{@code type alias}</b> for simplicity
     * (directly {@code extends} <u>{@link SequencedMap}</u>{@code <String, Function<T, ?>>}, do casting at use instead of implementing the interface itself).
     * However, <span style="color: f84b4b">that'll 100% explode on Runtime with <u>{@link ClassCastException}</u>,</span>
     * <span style="color: 95cc6d">because Java is a Nominal Type Language, not a Structural Type Language.</span>
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @see INestedPrintable
     * @see NestedFieldMap Implementation
     * @param <T> A class that has implemented <u>{@link INestedPrintable}</u>, restriction is set in {@code static} utils, since <u>{@link Cacher}</u> needs to store
     *           all field maps.
     */
    sealed interface INestedFieldMap<T>
    {
        int size();
        
        //* Note: `T` is [[NotNull]], while `?` is [[Nullable]](and wildcard itself doesn't support being annotated).
        @NotNull @Unmodifiable Set<Map.Entry<String, Function<@NotNull T, ? extends @Nullable Object>>> iterateSet();
    }
    
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
        final var map = new LinkedHashMap<String, Function<@NotNull T, ? extends @Nullable Object>>();
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
        final var map = new LinkedHashMap<String, Function<@NotNull T, ? extends @Nullable Object>>(allocSize, 1F);
        consumer.accept(map);
        
        if(map.size() != allocSize)
            System.err.println(
                TextUtils.format(
                    "The actual size of field map is {}, not {}(allocSize).\n This flaw happens at {}.",
                    map.size(),
                    allocSize,
                    StackDebugger.getCallerClass().getSimpleName()
                )
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
        
        final var map = new LinkedHashMap<String, Function<@NotNull T, ? extends @Nullable Object>>(length + length % 2, 1F);
        
        AssertUtils.nonNullCheckIteration(
            "pairs",
            pairs,
            pair -> map.put(pair.getKey(), pair.getValue())
        );
        
        return new NestedFieldMap<>(map);
    }
    //endregion
    
    //region Implementer Configurations
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
    //endregion
    
    //region Core Logics
    /**
     * Gets this class's field map.
     * @apiNote The return result should <b>NOT</b> be dynamic. Only the first get result will count, the laters will be ignored.
     * @see Cacher The reason
     */
    @NotNull @Unmodifiable INestedFieldMap<T> getFields();
    
    @SuppressWarnings("unchecked")//! Safe casting. It relies on [[Class]].
    private @NotNull @Unmodifiable INestedFieldMap<T> getFields(@NotNull Class<?> clazz)
    {
        final var fields = Objects.requireNonNullElseGet(Cacher.CACHE.get(this.getClass()), this::getFields);
        if(!Cacher.CACHE.containsKey(clazz))
            Cacher.CACHE.put(clazz, (INestedFieldMap<Object>) fields);
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
        
        for(final var entry: getFields(this.getClass()).iterateSet())
        {
            final var name = entry.getKey();
            final Object object = entry.getValue().apply(getSelf());
            
            if(name.isBlank())
                throw new IllegalArgumentException(
                    TextUtils.format(
                        "The field of Class \"{}\" has a unpresentable name! Value: {}",
                        this.getClass().getSimpleName(),
                        object
                    )
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
        
        final var prefix = name.isEmpty() ? indent : TextUtils.format("{}{}: ", indent, name);
        
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
                stringBuilder.append('\n');
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
                final var nextIndentStr = " ".repeat(nextIndent);
                
                for(final var item: iterable)
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
                final var nextIndentStr = " ".repeat(nextIndent);
                
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
    //endregion
    
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
    
    //region Private String Templates
    private static @NotNull String circularReferenceTemplate(@NotNull Class<?> clazz, boolean isDataStructure)
    {
        return TextUtils.format(
            "{}...(Circular Reference {}){}",
            isDataStructure ? '[' : "", clazz.getSimpleName(), isDataStructure ? ']' : ""
        );
    }
    
    private static void appendEntryTemplate(
        @NotNull StringBuilder stringBuilder,
        @NotNull String indent,
        @NotNull String name,
        @NotNull String value
    ) { stringBuilder.append(indent).append(name).append(": ").append(value).append('\n'); }
    //endregion
}

/**
 * A holder that holds the cache of <u>{@link INestedPrintable}</u>'s all implementers' field map.
 *
 * @implNote Constructing <u>{@link Map}</u> at every call of <u>{@link INestedPrintable#toNestedString()}</u> is <b>expensive</b>.<br>
 * So we need such a cache map to enhance performance. However, cache map is obviously a mutable map, and sadly, {@code interface} can only have
 * {@code public} constants, so we use {@code package-private} <u>{@link Enum}</u> to store cache instead,
 * which can deny unexpected access(both directly, and on reflect aspect).
 */
@ApiStatus.Internal enum Cacher {; static final Map<Class<?>, INestedPrintable.INestedFieldMap<Object>> CACHE = new ConcurrentHashMap<>(); }

final class NestedFieldMap<T> implements INestedPrintable.INestedFieldMap<T>
{
    private final SequencedMap<String, Function<@NotNull T, ? extends @Nullable Object>> map;
    
    NestedFieldMap(@NotNull SequencedMap<String, Function<@NotNull T, ? extends @Nullable Object>> map)
    {
        Objects.requireNonNull(map, "Param \"map\" must not be null!");
        
        if(map.isEmpty())
            throw new IllegalArgumentException("Param \"map\" doesn't insert any element into the map, this is invalid!");
        
        this.map = map;
    }
    
    @Override public int size() { return map.size(); }
    
    @Override public @NotNull @Unmodifiable Set<Map.Entry<String, Function<T, ? extends @Nullable Object>>> iterateSet()
        { return Collections.unmodifiableSet(map.entrySet()); }
}