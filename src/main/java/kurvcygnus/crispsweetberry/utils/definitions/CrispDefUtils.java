//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.definitions;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * This util is used for generate definitions related stuff faster, like <u>{@link ResourceLocation}</u>.
 * @since 1.0 Release
 */
public final class CrispDefUtils
{
    private CrispDefUtils() { throw new IllegalAccessError("Class \"CrispDefUtils\" is not meant to be instantized!"); }
    
    private static final MarkLogger LOGGER = MarkLogger.marklessLogger(LogUtils.getLogger());
    
    @Contract("_ -> new")
    public static @NotNull ResourceLocation getModNamespacedLocation(@NotNull String assetLocation) 
    {
        requireNonNull(assetLocation, "Param \"assetLocation\" cannot be null!");
        return ResourceLocation.fromNamespaceAndPath(CrispSweetberry.NAMESPACE, assetLocation);
    }
    
    public static String unwrapTextKey(@NotNull Component component)
    {
        final ComponentContents contents = component.getContents();
        
        if(contents instanceof TranslatableContents translatable)
            return translatable.getKey();
        else if(contents instanceof PlainTextContents.LiteralContents(String text))
            return text;
        
        throw new IllegalArgumentException("This is an illegal Component, which is neither literal nor translatable!");
    }
    
    public static String safeUnwrapTextKey(@NotNull Component component)
    {
        final ComponentContents contents = component.getContents();
        
        if(contents instanceof TranslatableContents translatable)
            return translatable.getKey();
        else if(contents instanceof PlainTextContents.LiteralContents(String text))
            return text;
        
        LOGGER.warn("Component \"{}\" is an illegal string.", component);
        return component.toString();
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
        requireNonNull(action, "Param \"action\" must not be null!");
        
        final CompoundTag tag = new CompoundTag();
        action.accept(tag);
        
        return tag;
    }
}
