//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.lang;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class StructuredException extends RuntimeException
{
    private final Throwable wrappedException;
    private final String type;
    
    public StructuredException(@NotNull Throwable wrappedException, @NotNull String type)
    {
        super(
            "<%s> %s".formatted(
                checkEx(wrappedException).getClass().getSimpleName(),
                wrappedException.getMessage()
            )
        );
        
        this.wrappedException = wrappedException;
        this.type = checkType(type);
    }
    
    public @NotNull Throwable wrappedException() { return wrappedException; }
    
    public @NotNull String type() { return type; }
    
    private static @NotNull Throwable checkEx(@NotNull Throwable wrappedException)
    {
        Objects.requireNonNull(wrappedException, "Param \"wrappedException\" must not be null!");
        
        if(wrappedException instanceof StructuredException)
            throw new IllegalArgumentException("This exception cannot wrap itself!");
        
        return wrappedException;
    }
    
    private static @NotNull String checkType(@NotNull String type)
    {
        Objects.requireNonNull(type, "Param \"type\" must not be null!");
        
        if(type.isBlank())
            throw new IllegalArgumentException("The type must not be empty!");
        
        return type;
    }
}
