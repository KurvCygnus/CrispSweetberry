package kurvcygnus.crispsweetberry.utils.base.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;

/**
 * This is an extension interface for all classes that has complex data, prints nested JSON-structured info instead of
 * <u>{@link Record#toString() record's ugly #toString()}</u>, with <u>{@link #getFields()}</u> implemented.
 * @apiNote Due to Java's {@code interface} limitation, you have to overwrite <u>{@link Object#toString()}</u> with calling <u>{@link #toNestedString()}</u> by your own.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see #takeNullFieldAsOptional() Null value handle Config
 * @see #getIndent() Indent Config
 */
public interface INestedPrintable
{
    /**
     * A simple method for building a immutable map, for <u>{@link #getFields()}</u>.
     * @apiNote It's recommend to this when your class has too many fields, or <b>any of them may be nullable,
     * because <u>{@link Map#of()}</u> do NOT accept {@code null} value.</b>
     */
    static @NotNull @Unmodifiable Map<@NotNull String, @Nullable Object> buildFieldMap(@NotNull Consumer<Map<@NotNull String, @Nullable Object>> consumer)
    {
        final var map = new HashMap<@NotNull String, @Nullable Object>();
        consumer.accept(map);
        return Collections.unmodifiableMap(map);
    }
    
    /**
     * Decides whether the {@code null} value should be printed.<hr>
     * If the value is {@code false}, {@code null} value will be replace with {@code "N/A"}.<br>
     * Or else, this entry won't be printed.
     */
    default boolean takeNullFieldAsOptional() { return false; }
    
    /**
     * Gets the indent standard of this class.
     * @implNote Each <u>{@link INestedPrintable}</u>'s implementer's Indent() is independent, they won't have any override logics, so
     * iterable's recommended to keep all data classes whose are in your project with the same indent.
     */
    default @Range(from = 1, to = Integer.MAX_VALUE) int getIndent() { return 2; }
    
    @NotNull @Unmodifiable Map<@NotNull String, @Nullable Object> getFields();
    
    default @NotNull String toNestedString()
    {
        final StringBuilder stringBuilder = new StringBuilder();
        final Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        buildNestedString(stringBuilder, 0, visited);
        return stringBuilder.toString();
    }
    
    private void buildNestedString(@NotNull StringBuilder stringBuilder, int currentIndent, @NotNull Set<Object> visited)
    {
        final String indentStr = " ".repeat(currentIndent);
        
        stringBuilder.append("%s\n%s{\n".formatted(this.getClass().getSimpleName(), indentStr));
        
        final int nextIndent = currentIndent + getIndent();
        final String fieldIndent = " ".repeat(nextIndent);
        
        getFields().forEach((name, object) -> parse(stringBuilder, nextIndent, fieldIndent, visited, name, object));
        
        stringBuilder.append("%s}".formatted(indentStr));
    }
    
    private void parse(
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
                stringBuilder.append(entryTemplate(indent, name, "N/A"));
            return;
        }
        
        final String prefix = name.isEmpty() ? indent : "%s%s: ".formatted(indent, name);
        
        switch(obj)
        {
            case INestedPrintable nested ->
            {
                if(!visited.add(nested))
                    stringBuilder.append(entryTemplate(indent, name, "...(Circular Reference)"));
                else
                {
                    stringBuilder.append(prefix);
                    nested.buildNestedString(stringBuilder, currentIndent, visited);
                    stringBuilder.append("\n");
                }
            }
            case Iterable<?> iterable ->
            {
                if(!visited.add(iterable))
                {
                    stringBuilder.append(entryTemplate(indent, name, "[...(Circular Reference)]"));
                    return;
                }
                stringBuilder.append("%s\n[\n".formatted(prefix));
                final int nextIndent = currentIndent + getIndent();
                final String nextIndentStr = " ".repeat(nextIndent);
                
                for(final Object item: iterable)
                    parse(stringBuilder, nextIndent, nextIndentStr, visited, "", item);
                stringBuilder.append("%s]\n".formatted(indent));
            }
            default -> stringBuilder.append(prefix).append(obj).append("\n");
        }
    }
    
    private static @NotNull String entryTemplate(@NotNull String indent, @NotNull String name, @NotNull String value) { return "%s%s: %s\n".formatted(indent, name, value); }
}