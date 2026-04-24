//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.annotation.processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

final class LazyVariable<T> implements Supplier<T>
{
    private T value;
    private boolean bound;
    
    private LazyVariable(@Nullable T value, boolean bound)
    {
        this.value = value;
        this.bound = bound;
    }
    
    static @NotNull <T> LazyVariable<T> create() { return new LazyVariable<>(null, false); }
    
    static @NotNull <T> LazyVariable<T> of(@NotNull T value)
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        return new LazyVariable<>(value, true);
    }
    
    void bound(@NotNull T value)
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        
        if(bound)
            throw new IllegalStateException();
        
        bound = true;
        this.value = value;
    }
    
    public @NotNull T get()
    {
        if(!bound)
            throw new IllegalStateException();
        
        return value;
    }
    
    @Override public String toString() { return this.value.toString(); }
}
