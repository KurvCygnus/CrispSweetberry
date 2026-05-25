//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.exceptions;

import kurvcygnus.crispsweetberry.lib.base.lang.IResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.helpers.MessageFormatter;

import java.util.Objects;
import java.util.function.Function;

public class StructuredException extends RuntimeException implements IStructuredThrowable
{
    private final Throwable wrappedException;
    private final String type;
    
    public StructuredException(@NotNull Throwable wrappedException, @NotNull String type)
    {
        super(MessageFormatter.format("<{}> {}", checkEx(wrappedException), checkType(type)).getMessage(), wrappedException);
        
        this.wrappedException = wrappedException;
        this.type = checkType(type);
    }
    
    public static <T> @NotNull IResult<T, StructuredException> failedResult(@NotNull Throwable wrappedException, @NotNull String type)
        { return IResult.ofFailed(new StructuredException(wrappedException, type)); }
    
    public static <T> @NotNull IResult<T, StructuredException> failedResult(@NotNull String message, @NotNull Function<String, Throwable> exceptionFactory, @NotNull String type)
    {
        Objects.requireNonNull(message, "Param \"message\" must not be null!");
        Objects.requireNonNull(exceptionFactory, "Param \"exceptionFactory\" must not be null!");
        return IResult.ofFailed(new StructuredException(exceptionFactory.apply(message), type));
    }
    
    @Override public @NotNull Throwable wrappedException() { return wrappedException; }
    
    @Override public @NotNull String type() { return type; }
    
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
    
    /**
     * @implNote The original <u>{@link Throwable#getMessage() method}</u>'s result is <b>nullable</b>(<i>despite it has no annotation, and usually won't happen</i>),
     * and this exception's message is clearly 100% NotNull, so we rewrite this method with <u>{@link NotNull annotation}</u>, with also a {@code final} attribute
     * to prevent message edit.
     */
    @Override public final @NotNull String getMessage() { return super.getMessage(); }
}
