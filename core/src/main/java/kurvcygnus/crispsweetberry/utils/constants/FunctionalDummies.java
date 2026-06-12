//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.constants;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @since 1.0 Release
 */
public final class FunctionalDummies
{
    private FunctionalDummies() { throw new IllegalAccessError("Class \"FunctionalDummies\" is not meant to be instantized!"); }
    
    private static final Runnable RUN_NOTHING = () -> {};
    private static final Consumer<Object> CONSUME_NOTHING = ignored -> {};
    private static final BiConsumer<Object, Object> BI_CONSUME_NOTHING = (ignored1, ignored2) -> {};
    
    public static Runnable runNothing() { return RUN_NOTHING; }
    @SuppressWarnings("unchecked") public static <T> @NotNull Consumer<T> silentConsume() { return (Consumer<T>) CONSUME_NOTHING; }
    @SuppressWarnings("unchecked") public static <T, U> @NotNull BiConsumer<T, U> silentConsumeBi() { return (BiConsumer<T, U>) BI_CONSUME_NOTHING; }
    public static boolean alwaysTrue() { return true; }
    public static boolean alwaysFalse() { return false; }
}