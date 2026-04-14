//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryPipelineTask;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

final class BrokenCarryPipelineException extends RuntimeException
{
    final Throwable wrappedException;
    final String type;
    final CarryPipelineTask causeData;
    
    private BrokenCarryPipelineException(@NotNull Throwable exception, @NotNull String type, @NotNull CarryPipelineTask causeData)
    {
        super(
            "<%s> %s".formatted(
                Objects.requireNonNull(
                    exception,
                    "Param \"exception\" must not be null!"
                ).getClass().getSimpleName(),
                exception.getMessage()
            )
        );
        
        Objects.requireNonNull(type, "Param \"type\" must not be null!");
        Objects.requireNonNull(causeData, "Param \"causeData\" must not be null!");
        
        this.wrappedException = exception;
        this.type = type;
        this.causeData = causeData;
    }
    
    static @NotNull BrokenCarryPipelineException listener(
        @NotNull CarryPipelineTask causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull TriState state
    )
    {
        Objects.requireNonNull(state, "Param \"state\" must not be null!");
        
        return new BrokenCarryPipelineException(
            exception.apply(message), 
            "LISTENER_%s".
                formatted(
                    switch(state)
                    {
                        case TRUE -> "ADD";
                        case FALSE -> "REMOVE";
                        case DEFAULT -> "SKIP";
                    }
                ),
            causeData
        );
    }
    
    static @NotNull BrokenCarryPipelineException component(
        @NotNull CarryPipelineTask causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull TriState state
    ) 
    {
        Objects.requireNonNull(state, "Param \"state\" must not be null!");
        
        return new BrokenCarryPipelineException(
            exception.apply(message),
            "COMPONENT_%s".
                formatted(
                    switch(state)
                    {
                        case TRUE -> "INSERT";
                        case FALSE -> "REMOVE";
                        case DEFAULT -> "DO_NOTHING";
                    }
                ),
            causeData
        );
    }
    
    static @NotNull BrokenCarryPipelineException target(
        @NotNull CarryPipelineTask causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull CarryType type,
        @NotNull TriState state
    ) 
    {
        Objects.requireNonNull(type, "Param \"type\" must not be null!");
        Objects.requireNonNull(state, "Param \"state\" must not be null!");
        
        return new BrokenCarryPipelineException(
            exception.apply(message),
            "TARGET_%s_%s".
                formatted(
                    type.name().toUpperCase(),
                    switch(state)
                    {
                        case TRUE -> "CAPTURE";
                        case FALSE -> "RELEASE";
                        case DEFAULT -> "KEEP";
                    }
                ),
            causeData
        );
    }
}
