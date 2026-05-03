//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.lang;

import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import kurvcygnus.crispsweetberry.utils.base.trait.INullableContainer;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Objects;
import java.util.function.Function;

@NotThreadSafe
public final class LockableBox<T> implements INullableContainer<T>
{
    private static final byte UNBOUND = 0;
    private static final byte ASSIGNABLE = 1;
    private static final byte BOUND = 2;
    private static final byte LOCKED = 3;
    
    private @Nullable T value;
    private @MagicConstant(intValues = {UNBOUND, ASSIGNABLE, BOUND, LOCKED}) byte state;
    
    private LockableBox(@Nullable T value, @MagicConstant(intValues = {UNBOUND, ASSIGNABLE, BOUND, LOCKED}) byte state)
    {
        assert state < UNBOUND || state > LOCKED : "Param \"state\"'s value is illegal: %d".formatted(state);
        
        this.value = value;
        this.state = state;
    }
    
    public static <T> @NotNull LockableBox<T> of(@NotNull T value)
    {
        assert value != null : "Param \"value\" must not be null!";
        return new LockableBox<>(value, BOUND);
    }
    
    public static <T> @NotNull LockableBox<T> create() { return new LockableBox<>(null, UNBOUND); }
    
    public static <T> @NotNull LockableBox<T> ofNullable(@Nullable T value) { return value != null ? of(value) : create(); }
    
    @Override public boolean isPresent()
    {
        if(state == LOCKED)
            return false;
        return INullableContainer.super.isPresent();
    }
    
    public boolean assign(@NotNull T value)
    {
        assert value != null : "Param \"value\" must not be null!";
        
        if(state == BOUND || state == LOCKED)
            return false;
        
        this.value = value;
        
        if(state == UNBOUND)
            state = ASSIGNABLE;
        
        return true;
    }
    
    public boolean bound(@NotNull T value)
    {
        assert value != null : "Param \"value\" must not be null!";
        
        if(state != UNBOUND && state != ASSIGNABLE)
            return false;
        
        this.value = value;
        this.state = BOUND;
        return true;
    }
    
    public boolean lock()
    {
        if(state == UNBOUND)
            throw new IllegalStateException("This container doesn't have a presentable value, it can't be locked!");
        else if(state != LOCKED)
        {
            state = LOCKED;
            return true;
        }
        
        return false;
    }
    
    public boolean isUnbound() { return state == UNBOUND; }
    
    public boolean isAssignable() { return state == ASSIGNABLE || isUnbound(); }
    
    public boolean isBound() { return state == BOUND; }
    
    public boolean isLocked() { return state == LOCKED; }
    
    /**
     * {@inheritDoc}
     */
    @Override public @Nullable T value()
    {
        if(state == LOCKED)
            throw new IllegalStateException("This container is locked, value access is not allowed!");
        
        return value;
    }
    
    @Override public @NotNull Function<@Nullable T, @NotNull INullableContainer<T>> createInstance() { return LockableBox::ofNullable; }
    
    @Override public @NotNull T orThrow()
    {
        FunctionalUtils.throwIf(
            value == null || state == UNBOUND,
            "Can't get value, because it is unbound, its value is not present!",
            IllegalStateException::new
        );
        
        if(state == LOCKED)
            throw new IllegalStateException("This container is locked, value access is not allowed!");
        
        return value;
    }
    
    @Override public boolean equals(@Nullable Object obj)
    {
        return this == obj || obj instanceof LockableBox<?> that &&
            Objects.equals(value, that.value) &&
            state == that.state;
    }
    
    @Override public int hashCode() { return Objects.hash(value, state); }
    
    @Override public @NotNull String toString()
    {
        return "LockableBox: value: %s, state: %s".
            formatted(
                value,
                switch(state)
                {
                    case UNBOUND -> "UNBOUND";
                    case ASSIGNABLE -> "ASSIGNABLE";
                    case BOUND -> "BOUND";
                    case LOCKED -> "LOCKED";
                    default -> throw new IllegalStateException("Unexpected value: %s".formatted(state));
                }
            );
    }
    
    @Override public boolean withCheck() { return false; }
}
