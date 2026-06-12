//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.Unmodifiable;

/**
 * A simple implementation of <u>{@link INestedPrintable}</u>, which overrides the <u>{@link Object#toString()}</u>, making this class's implementer only needs to
 * implement <u>{@link #getFields()}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see INestedPrintable
 * @see OfAuto
 */
public abstract class BaseNestedPrinter<T extends BaseNestedPrinter<T>> implements INestedPrintable<T>
{
    @Override public final @NotNull String toString() { return toNestedString(); }
    
    @Override public final @NotNull String toNestedString(@Range(from = 0, to = Integer.MAX_VALUE) int indent) { return INestedPrintable.super.toNestedString(indent); }
    
    @Override public final @NotNull String toNestedString() { return toNestedString(0); }
    
    /**
     * A convenient specific implementation pf <u>{@link IAutoNestedPrintable}</u>. It uses <u>{@link java.lang.reflect Reflection}</u> to process fields map.<br>
     * Thus, the implementer doesn't need to implement any extra methods.
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @see IAutoNestedPrintable
     * @see #getBlacklistedFields()
     * @see #gonnaBeCruel()
     */
    public static abstract class OfAuto<T extends OfAuto<T>> extends BaseNestedPrinter<T> implements IAutoNestedPrintable<T>
    {
        @Override public final @NotNull @Unmodifiable INestedFieldMap<T> getFields() { return IAutoNestedPrintable.super.getFields(); }
    }
}
