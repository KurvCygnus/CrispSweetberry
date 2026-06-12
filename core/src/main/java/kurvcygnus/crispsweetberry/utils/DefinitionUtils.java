//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

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
     * Generates a formated <u>{@link String}</u>, with {@code {}} as placeholder.
     * @apiNote It is mainly used for <u>{@link Throwable}</u>'s message initialization, and it is obviously faster than <u>{@link String#formatted(Object...)}</u>.
     * @implNote The overload of this method doesn't exist, since <u>{@link MessageFormatter#arrayFormat(String, Object[])}</u>'s overload also creates object array.
     */
    public static @NotNull String quickFormat(@NotNull String format, @Nullable Object @Nullable ... args)
    {
        requireNonNull(format, "Param \"format\" must not be null!");
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }
    
    /**
     * Produces a <u>{@link ResourceLocation}</u>, with <u>{@link CrispSweetberry#NAMESPACE "crispsweetberry"}</u> as its namespace.
     */
    @Contract("_ -> new") public static @NotNull ResourceLocation getModNamespacedLocation(@NotNull String assetLocation)
    {
        requireNonNull(assetLocation, "Param \"assetLocation\" cannot be null!");
        return ResourceLocation.fromNamespaceAndPath(CrispSweetberry.NAMESPACE, assetLocation);
    }
    
    public static @NotNull String namespacedDotId(@NotNull String suffix)
    {
        requireNonNull(suffix, "Param \"suffix\" must not be null!");
        return quickFormat("{}.{}", CrispSweetberry.NAMESPACE, suffix);
    }
    
    public static @NotNull String namespacedDotId(@NotNull String... suffixes)
    {
        requireNonNull(suffixes, "Param \"suffixes\" must not be null!");
        final var stringBuilder = new StringBuilder(CrispSweetberry.NAMESPACE);
        for(final String suffix: suffixes)
            stringBuilder.append('.').append(suffix);
        
        return stringBuilder.toString();
    }
    
    public static @NotNull String namespacedUnderscoreId(@NotNull String suffix)
    {
        requireNonNull(suffix, "Param \"suffix\" must not be null!");
        return quickFormat("{}_{}", CrispSweetberry.NAMESPACE, suffix);
    }
    
    public static @NotNull String namespacedUnderscoreId(@NotNull String... suffixes)
    {
        requireNonNull(suffixes, "Param \"suffixes\" must not be null!");
        final var stringBuilder = new StringBuilder(CrispSweetberry.NAMESPACE);
        for(final String suffix: suffixes)
            stringBuilder.append('_').append(suffix);
        
        return stringBuilder.toString();
    }
    
    /**
     * Creates a <b>Identity Name</b> for a persistent custom <u>{@link CompoundTag}</u> data, with <b>{@code crispsweetberry}</b> as its namespace,
     * <b>{@code persistent_tags}</b> as its category scope.
     */
    public static @NotNull String createPersistentTag(@NotNull String tagName)
    {
        requireNonNull(tagName, "Param \"tagName\" must not be null!");
        return quickFormat("{}.{}", namespacedDotId("persistent_tags"), tagName);
    }
    
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
    public static <E extends Enum<E>, V> @Unmodifiable @NotNull Map<E, V> createImmutableEnumMapWithCheck(
        @NotNull Class<E> enumClass,
        @NotNull Consumer<EnumMap<E, V>> dataInsertAction
    )
    {
        requireNonNull(enumClass, "Param \"enumClass\" must not be null!");
        requireNonNull(dataInsertAction, "Param \"dataInsertAction\" must not be null!");
        
        final var enumMap = new EnumMap<E, V>(enumClass);
        dataInsertAction.accept(enumMap);
        
        for(final E enumConstant: enumClass.getEnumConstants())
            if(!enumMap.containsKey(enumConstant))
                throw new IllegalStateException(quickFormat("Enum constant \"{}\" does not own a corresponded value!", enumConstant));
        
        return Collections.unmodifiableMap(enumMap);
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
     * an implicit reference to the outer class (e.g. your BlockEntity or Screen).</li>
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
        
        final var tag = new CompoundTag();
        action.accept(tag);
        
        return tag;
    }
    
    public static <E extends Throwable> void throwOnDevOrLogError(
        @NotNull Function<String, E> function,
        @NotNull Logger logger,
        @NotNull String message,
        @Nullable Object @Nullable ... args
    ) throws E
    {
        requireNonNull(function, "Param \"function\" must not be null!");
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(message, "Param \"message\" must not be null!");
        
        final var fullMessage = quickFormat(message, args);
        
        if(!FMLEnvironment.production)
            throw function.apply(fullMessage);
        
        logger.error(fullMessage);
    }
}
