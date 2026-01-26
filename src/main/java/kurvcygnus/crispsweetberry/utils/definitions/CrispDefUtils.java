package kurvcygnus.crispsweetberry.utils.definitions;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * This util is used for generate definitions faster, like <u>{@link ResourceLocation}</u>.
 * @since 1.0 Release
 */
public final class CrispDefUtils
{
    private CrispDefUtils() { throw new IllegalAccessError(); }
    
    @Contract("_ -> new")
    public static @NotNull ResourceLocation getModNamespacedLocation(@NotNull String assetLocation) 
    {
        Objects.requireNonNull(assetLocation, "Param \"assetLocation\" cannot be null!");
        return ResourceLocation.fromNamespaceAndPath(CrispSweetberry.MOD_ID, assetLocation);
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
}
