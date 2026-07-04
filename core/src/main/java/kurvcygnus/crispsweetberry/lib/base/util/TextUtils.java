//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

import java.util.Objects;

/**
 * @since 1.0 Release
 */
public final class TextUtils
{
    private TextUtils() { throw new IllegalAccessError("Class \"TextUtils\" is not meant to be instantized!"); }
    
    /**
     * Generates a formated <u>{@link String}</u>, with {@code {}} as placeholder.
     * @apiNote It is mainly used for <u>{@link Throwable}</u>'s message initialization, and it is obviously faster than <u>{@link String#formatted(Object...)}</u>.
     * @implNote The overload of this method doesn't exist, since <u>{@link MessageFormatter#arrayFormat(String, Object[])}</u>'s overload also creates object array.
     */
    public static @NotNull String format(@NotNull String format, @Nullable Object @Nullable ... args)
    {
        Objects.requireNonNull(format, "Param \"format\" must not be null!");
        return MessageFormatter.arrayFormat(format, args).getMessage();
    }
    
    public static @NotNull String nonBlank(@Nullable String string) throws NullPointerException, IllegalArgumentException
    {
        Objects.requireNonNull(string, "Param \"string\" must not be null!");
        
        if(string.isBlank())
            throw new IllegalArgumentException("Param \"string\" is empty.");
        
        return string;
    }
}