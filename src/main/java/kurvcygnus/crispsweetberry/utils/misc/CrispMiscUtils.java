package kurvcygnus.crispsweetberry.utils.misc;

import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class CrispMiscUtils
{
    private CrispMiscUtils() { throw new IllegalAccessError(); }
    
    public static <T> void doIfNonNull(T object, Consumer<T> action)
    {
        if(object == null)
            return;
        
        action.accept(object);
    }
    
    /**
     * A functional helper to replace the <b>Anti-Pattern: Double Brace Initialization</b>.<br>
     * This method ensures a cleaner instance construction without the hidden side effects of anonymous inner classes.
     * @implNote
     * The vanilla {@link CompoundTag} API lacks a <b>Fluent Interface</b> (method chaining),
     * which often leads developers to use {@code new CompoundTag() {{ put... }}}.
     * <b>This is fundamentally flawed because:</b>
     * <ul>
     * <li><b>Memory Leak Risk:</b> Every usage creates a hidden anonymous subclass that holds
     * an implicit reference to the outer class (e.g., your BlockEntity or Screen).</li>
     * <li><b>Classloader Bloat:</b> It generates a unique {@code .class} file for every single call site,
     * increasing the JVM's Metaspace pressure.</li>
     * <li><b>Serialization Issues:</b> Some reflection-based systems may fail to identify the
     * original class type due to the synthetic subclassing.</li>
     * </ul>
     * @see CompoundTag
     */
    public static @NotNull CompoundTag createTag(@NotNull Consumer<CompoundTag> action)
    {
        CompoundTag tag = new CompoundTag();
        action.accept(tag);
        
        return tag;
    }
    
    public static <E extends Throwable> void throwIf(boolean condition, @NotNull Supplier<E> supplier) throws E
    {
        if(!condition)
            return;
        
        Objects.requireNonNull(supplier, "Param \"supplier\" cannot be null!(Param 2, Bro, serious?)");
        
        throw supplier.get();
    }
}
