//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.constants;

import kurvcygnus.crispsweetberry.utils.base.functions.ITriConsumer;

import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/**
 * @since 1.0 Release
 */
public final class DummyFunctionalConstants
{
    private DummyFunctionalConstants() { throw new IllegalAccessError("Class \"DummyFunctionalConstants\" is not meant to be instantized!"); }
    
    public static final Consumer<?> DO_NOTHING_CONSUMER = ignored -> {};
    public static final BiConsumer<?, ?> DO_NOTHING_CONSUMER2 = (var1, var2) -> {};
    public static final ITriConsumer<?, ?, ?> DO_NOTHING_CONSUMER3 = (var1, var2, var3) -> {};
    public static final Runnable DO_NOTHING_RUN = () -> {};
    public static final Callable<Void> VOID_CALL = () -> null;
    public static final BooleanSupplier ALWAYS_TRUE = () -> true;
    public static final BooleanSupplier ALWAYS_FALSE = () -> false;
}