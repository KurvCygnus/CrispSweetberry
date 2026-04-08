//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.trait;

import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import kurvcygnus.crispsweetberry.utils.constants.DummyFunctionalConstants;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * This is a simple <u><a href="https://doc.rust-lang.org/rust-by-example/trait.html">trait-styled</a></u> interface for
 * <u>{@link Enum}</u>, making it supports bitmasks constant definition, and computation.
 * <hr>
 * <i>For more details about Bitmask, you can see it at <u><a href="https://en.wikipedia.org/wiki/Mask_(computing)">here</a></u></i>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @apiNote This interface uses {@code int} as flags, which has 32 bits, so it accepts 16(
 * <i>{@code 32 / 2}, we have 3 states:
 * <u>{@link IBitmaskedEnum#TRUE}</u>, <u>{@link IBitmaskedEnum#DEFAULT}</u> and <u>{@link IBitmaskedEnum#FALSE}</u>, and 1 bits = 2 cases,
 * <span style="color: 95cc6d">so 3 case roughly equals to 2 bits</span>
 * </i>
 * )
 * enums as maximum.<br>
 * <span style="color: red">Exceeded count of enum instances will cause unexpected behavior.</span><br>
 * <i>Also, if this has involved persistent stuff, <span style="color: red">DO NOT CHANGE ENUM's ORDER, this breaks data validation.</span></i>
 * <br>
 * <i>(
 *     To be honestly, if you really want to do persistent stuff,
 *     overriding <u>{@link IBitmaskedEnum#getIndex()}</u>, using <u>{@link Enum#name()}</u> are both considerable solutions.
 * )</i>
 * @param <E> The <u>{@link Enum}</u> it bounds to.
 */
public interface IBitmaskedEnum<E extends Enum<E> & IBitmaskedEnum<E>>
{
    //region Mask Constants & Lookups
    int MASK    = 0x3;
    int DEFAULT = 0x0;
    int TRUE    = 0x1;
    int FALSE   = 0x2;
    int EXTRA   = 0x3;
    
    /**
     * A constant array for result querying, and to prevent CPU's branch prediction penalty.
     */
    TriState[] TRISTATE_LOOKUP = { TriState.DEFAULT, TriState.TRUE, TriState.FALSE, TriState.DEFAULT };
    
    /**
     * A constant array for result querying, and to prevent CPU's branch prediction penalty.
     */
    @Nullable Boolean[] BOOLEAN_LOOKUP = { null, Boolean.TRUE, Boolean.FALSE, null };
    //endregion
    
    //region Bitmask computations
    @ApiStatus.NonExtendable default int shift()        { return getIndex() *  2;       }
    @ApiStatus.NonExtendable default int shiftTrue()    { return TRUE       << shift(); }
    @ApiStatus.NonExtendable default int shiftFalse()   { return FALSE      << shift(); }
    @ApiStatus.NonExtendable default int shiftDefault() { return DEFAULT    << shift(); }
    @ApiStatus.NonExtendable default int shiftExtra()   { return EXTRA      << shift(); }
    
    @SuppressWarnings("unchecked")//! This interface can only be implemented by [[Enum]], so we can do such a violate casting.
    default @Range(from = 0, to = 15) int getIndex()
    {
        final E enumeration = (E) this;
        
        if(withOverflowCheck().getAsBoolean())
        {
            final int length = enumeration.getDeclaringClass().getEnumConstants().length;
            FunctionalUtils.throwIf(
                length == 0 || length > 16,
                "The count of enum instances has exceeded 16, this will lead to unexpected behavior!",
                IllegalArgumentException::new
            );
            setCheckFlag(false);
        }
        
        return enumeration.ordinal();
    }
    
    default BooleanSupplier withOverflowCheck() { return DummyFunctionalConstants.ALWAYS_FALSE; }
    
    @ApiStatus.OverrideOnly default void setCheckFlag(boolean flag) {}
    //endregion
    
    //region Public APIs
    default int handleExtra() { throw new IllegalStateException("Invalid input: Parse result is 3(EXTRA), which shouldn't undefined currently. Please review your code."); }
    
    @ApiStatus.NonExtendable default int computeRaw(int flags)
    {
        final int state = flags >> shift() & MASK;
        
        if(state == EXTRA)
            return handleExtra();
        
        return state;
    }
    
    @ApiStatus.NonExtendable default @Nullable Boolean computeBoolean(int flags) { return BOOLEAN_LOOKUP[computeRaw(flags)]; }
    
    @ApiStatus.NonExtendable default boolean computeBooleanOrThrow(int flags)
    {
        final @Nullable Boolean value = computeBoolean(flags);
        
        if(value == null)
            throw new IllegalStateException("Param \"flags\"'s unmask state is 0(DEFAULT)!");
        
        return value;
    }
    
    @ApiStatus.NonExtendable default boolean computeBooleanOrDefault(int flags, boolean defaultFallback)
    {
        final @Nullable Boolean value = computeBoolean(flags);
        return Objects.requireNonNullElse(value, defaultFallback);
    }
    
    @ApiStatus.NonExtendable default @NotNull TriState compute(int flags) { return TRISTATE_LOOKUP[computeRaw(flags)]; }
    
    @ApiStatus.NonExtendable default boolean isTrue(int flags) { return computeRaw(flags) == TRUE; }
    @ApiStatus.NonExtendable default boolean isFalse(int flags) { return computeRaw(flags) == FALSE; }
    @ApiStatus.NonExtendable default boolean isDefault(int flags) { return computeRaw(flags) == DEFAULT; }
    @ApiStatus.NonExtendable default boolean isExtra(int flags) { return computeRaw(flags) == EXTRA; }
    //endregion
}
