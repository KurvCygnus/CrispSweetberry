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
import kurvcygnus.crispsweetberry.utils.base.lang.IResult;
import kurvcygnus.crispsweetberry.utils.base.lang.StructuredException;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

final class BrokenCarryPipelineException extends StructuredException
{
    private static final BinaryOperator<String> TYPE_TEMPLATE = "%s_%s"::formatted;
    
    final CarryPipelineTask causeData;
    
    private BrokenCarryPipelineException(@NotNull Throwable exception, @NotNull String type, @NotNull CarryPipelineTask causeData)
    {
        super(exception, type);
        Objects.requireNonNull(causeData, "Param \"causeData\" must not be null!");
        this.causeData = causeData;
    }
    
    static <T> @NotNull IResult<T, BrokenCarryPipelineException> listener(
        @NotNull CarryPipelineTask causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull TriState state
    )
    {
        Objects.requireNonNull(state, "Param \"state\" must not be null!");
        
        return IResult.ofFailed(
            new BrokenCarryPipelineException(
                exception.apply(message),
                TYPE_TEMPLATE.apply(
                    "LISTENER",
                    switch(state)
                    {
                        case TRUE -> "ADD";
                        case FALSE -> "REMOVE";
                        case DEFAULT -> "SKIP";
                    }
                ),
                causeData
            )
        );
    }
    
    static <T> @NotNull IResult<T, BrokenCarryPipelineException> component(
        @NotNull CarryPipelineTask causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull TriState state
    ) 
    {
        Objects.requireNonNull(state, "Param \"state\" must not be null!");
        
        return IResult.ofFailed(
            new BrokenCarryPipelineException(
                exception.apply(message),
                TYPE_TEMPLATE.apply(
                    "COMPONENT",
                    switch(state)
                    {
                        case TRUE -> "INSERT";
                        case FALSE -> "REMOVE";
                        case DEFAULT -> "DO_NOTHING";
                    }
                ),
                causeData
            )
        );
    }
    
    static <T> @NotNull IResult<T, BrokenCarryPipelineException> target(
        @NotNull CarryPipelineTask causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull CarryType type,
        @NotNull TriState state
    ) 
    {
        Objects.requireNonNull(type, "Param \"type\" must not be null!");
        Objects.requireNonNull(state, "Param \"state\" must not be null!");
        
        return IResult.ofFailed(
            new BrokenCarryPipelineException(
                exception.apply(message),
                TYPE_TEMPLATE.apply(
                    TYPE_TEMPLATE.apply(
                        "TARGET",
                        type.name().toUpperCase()
                    ),
                    switch(state)
                    {
                        case TRUE -> "CAPTURE";
                        case FALSE -> "RELEASE";
                        case DEFAULT -> "KEEP";
                    }
                ),
                causeData
            )
        );
    }
}
