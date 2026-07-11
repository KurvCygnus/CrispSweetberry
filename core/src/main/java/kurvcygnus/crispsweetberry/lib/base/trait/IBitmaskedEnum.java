//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.trait;

import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.*;

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
 * <span style="color: f84b4b">Exceeded count of enum instances will cause unexpected behavior.</span><br>
 * <i>Also, if this has involved persistent stuff, <span style="color: f84b4b">DO NOT CHANGE ENUM's ORDER, this breaks data validation.</span></i>
 * <br>
 * <i>(
 *     To be honestly, if you really want to do persistent stuff,
 *     overriding <u>{@link IBitmaskedEnum#getIndex()}</u>, using <u>{@link Enum#name()}</u> are both considerable solutions.
 * )</i>
 * @param <E> The <u>{@link Enum}</u> it bounds to.
 */
public interface IBitmaskedEnum<E extends Enum<E> & IBitmaskedEnum<E>> extends ICRTPCaster<IBitmaskedEnum<E>, E>
{
    //region Mask Constants
    int MASK    = 0x3;
    int DEFAULT = 0x0;
    int TRUE    = 0x1;
    int FALSE   = 0x2;
    int EXTRA   = 0x3;
    //endregion
    
    //region Bitmask computations
    @ApiStatus.NonExtendable default int shift()        { return getIndex() *  2;       }
    @ApiStatus.NonExtendable default int shiftTrue()    { return TRUE       << shift(); }
    @ApiStatus.NonExtendable default int shiftFalse()   { return FALSE      << shift(); }
    @ApiStatus.NonExtendable default int shiftDefault() { return DEFAULT    << shift(); }
    
    /**
     * @implNote Unused bitmask. If you want to use it, it is recommended to write a new semantical method that redirects to this.
     */
    @ApiStatus.NonExtendable default int shiftExtra()   { return EXTRA      << shift(); }
    
    default @Range(from = 0, to = 15) int getIndex()
    {
        final E enumeration = getSelf();
        
        if(withOverflowCheck() && InterfaceHelper.MEMO.add(getClass()))
        {
            final int length = enumeration.getDeclaringClass().getEnumConstants().length;
            
            if(length == 0 || length > 16)
                throw new IllegalArgumentException("The count of enum instances has exceeded 16, this will lead to unexpected behavior!");
        }
        
        return enumeration.ordinal();
    }
    
    default boolean withOverflowCheck() { return false; }
    //endregion
    
    //region Public APIs
    default int handleExtra() { throw new IllegalStateException("Invalid input: Parse result is 3(EXTRA), which shouldn't be undefined currently. Please review your code."); }
    
    @ApiStatus.NonExtendable default int computeRaw(int flags)
    {
        final int state = flags >> shift() & MASK;
        
        if(state == EXTRA)
            return handleExtra();
        
        return state;
    }
    
    private @Nullable Boolean computeBoolean(int flags) { return InterfaceHelper.BOOLEAN_LOOKUP[computeRaw(flags)]; }
    
    @ApiStatus.NonExtendable default @NotNull Optional<Boolean> computeOptional(int flags) { return Optional.ofNullable(computeBoolean(flags)); }
    
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
    
    @ApiStatus.NonExtendable default @NotNull Optional<TriState> compute(int flags) { return Optional.ofNullable(InterfaceHelper.TRISTATE_LOOKUP[computeRaw(flags)]); }
    
    @ApiStatus.NonExtendable default boolean isTrue(int flags) { return computeRaw(flags) == TRUE; }
    @ApiStatus.NonExtendable default boolean isFalse(int flags) { return computeRaw(flags) == FALSE; }
    @ApiStatus.NonExtendable default boolean isDefault(int flags) { return computeRaw(flags) == DEFAULT; }
    
    /**
     * Unused bitmask. If you want to use it, it is recommended to write a new semantical method that redirects to this.
     */
    @ApiStatus.NonExtendable default boolean isExtra(int flags) { return computeRaw(flags) == EXTRA; }
    //endregion
}

/**
 * Exists for avoiding invalid accesses to these constants.
 * @since 1.0 Release
 */
enum InterfaceHelper
{;//! All static stuff, so enum element is no mandatory.
    //* Faster Comparing.
    static final Set<Class<?>> MEMO = Collections.newSetFromMap(new IdentityHashMap<>());
    
    /**
     * A constant array for result querying, and to prevent CPU's branch prediction penalty.
     */
    static final @Nullable TriState[] TRISTATE_LOOKUP = { TriState.DEFAULT, TriState.TRUE, TriState.FALSE, null };
    
    /**
     * A constant array for result querying, and to prevent CPU's branch prediction penalty.
     */
    static final @Nullable Boolean[] BOOLEAN_LOOKUP = { null, Boolean.TRUE, Boolean.FALSE, null };
}