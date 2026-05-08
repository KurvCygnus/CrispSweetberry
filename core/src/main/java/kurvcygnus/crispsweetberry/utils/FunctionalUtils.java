//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @since 1.0 Release
 */
public final class FunctionalUtils
{
    private FunctionalUtils() { throw new IllegalAccessError("Class \"FunctionalUtils\" is not meant to be instantized!"); }
    
    public static <T> void doIfNonNull(@Nullable T object, @NotNull Consumer<@NotNull T> action)
    {
        requireNonNull(action, "Param \"action\" must not be null!");
        
        if(object != null)
            action.accept(object);
    }
    
    public static void doIf(boolean condition, @NotNull Runnable action)
    {
        requireNonNull(action, "Param \"action\" must not be null!");
        
        if(condition)
            action.run();
    }
    
    public static <E extends Throwable> void throwIf(boolean condition, @NotNull String message, @NotNull Function<String, E> function) throws E
    {
        requireNonNull(message, "Param \"message\" must not be null!");
        requireNonNull(function, "Param \"function\" must not be null!");
        
        if(!condition)
            return;
        
        throw function.apply(message);
    }
    
    public static <E extends Throwable> void throwIf(boolean condition, @NotNull Supplier<E> supplier) throws E
    {
        requireNonNull(supplier, "Param \"supplier\" must not be null!");
        
        if(!condition)
            return;
        
        throw supplier.get();
    }
}
