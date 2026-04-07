//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * This util is used for generate definitions related stuff faster, like <u>{@link ResourceLocation}</u>.
 * @since 1.0 Release
 */
public final class DefinitionUtils
{
    private DefinitionUtils() { throw new IllegalAccessError("Class \"DefinitionUtils\" is not meant to be instantized!"); }
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * Produces a <u>{@link ResourceLocation}</u>, with <b>{@code crispsweetberry}</b> as its namespace.
     */
    @Contract("_ -> new") public static @NotNull ResourceLocation getModNamespacedLocation(@NotNull String assetLocation) 
    {
        requireNonNull(assetLocation, "Param \"assetLocation\" cannot be null!");
        return ResourceLocation.fromNamespaceAndPath(CrispSweetberry.NAMESPACE, assetLocation);
    }
    
    /**
     * Creates a <b>Identity Name</b> for a persistent custom <u>{@link CompoundTag}</u> data, with <b>{@code crispsweetberry}</b> as its namespace,
     * <b>{@code persistent_tags}</b> as its category scope.
     */
    public static @NotNull String createPersistentTag(@NotNull String tagName) { return "%s.persistent_tags.%s".formatted(CrispSweetberry.NAMESPACE, tagName); }
    
    /**
     * Unwarps a <u>{@link Component}</u>, and returns its text.
     * @throws IllegalArgumentException When it is neither <u>{@link TranslatableContents}</u>,
     * or <u>{@link net.minecraft.network.chat.contents.PlainTextContents.LiteralContents}</u>.
     */
    public static @NotNull String unwrapTextKey(@NotNull Component component)
    {
        return switch(component.getContents())
        {
            case TranslatableContents translatable -> translatable.getKey();
            case PlainTextContents.LiteralContents(String text) -> text;
            default -> throw new IllegalArgumentException("This is an illegal Component, which is neither literal nor translatable!");
        };
    }
    
    /**
     * Creates an immutable, <u>{@link Enum}</u> specified map collection.
     * @see EnumMap
     */
    public static <E extends Enum<E>, V>
    @Unmodifiable @NotNull Map<E, V> createImmutableEnumMap(@NotNull Class<E> enumClass, @NotNull Consumer<EnumMap<E, V>> dataInsertAction)
    {
        requireNonNull(enumClass, "Param \"enumClass\" must not be null!");
        requireNonNull(dataInsertAction, "Param \"dataInsertAction\" must not be null!");
        
        final EnumMap<E, V> enumMap = new EnumMap<>(enumClass);
        dataInsertAction.accept(enumMap);
        
        return Maps.immutableEnumMap(enumMap);
    }
    
    /**
     * Unwarps a <u>{@link Component}</u>, and returns its text.
     * @apiNote Returns the <u>{@link Component}</u>'s <u>{@link Object#toString() toString()}</u> content when it is
     * neither <u>{@link TranslatableContents}</u>, nor <u>{@link net.minecraft.network.chat.contents.PlainTextContents.LiteralContents}</u>.
     */
    public static @NotNull String safeUnwrapTextKey(@NotNull Component component)
    {
        return switch(component.getContents())
        {
            case TranslatableContents translatable -> translatable.getKey();
            case PlainTextContents.LiteralContents(String text) -> text;
            default ->
            {
                LOGGER.warn("Component \"{}\" is an illegal string.", component);
                yield component.toString();
            }
        };
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
     * original class componentExecutionType due to the synthetic subclassing.</li>
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
