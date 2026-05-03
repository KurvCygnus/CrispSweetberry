//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core.exceptions;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContext;
import kurvcygnus.crispsweetberry.utils.base.exceptions.ITransitionalThrowable;
import kurvcygnus.crispsweetberry.utils.base.exceptions.StructuredException;
import kurvcygnus.crispsweetberry.utils.base.lang.IResult;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public final class CarryInteractHandleException extends StructuredException
implements ITransitionalThrowable<CarryInteractHandleException, CarryInteractContext, InteractionResult>
{
    private static final BinaryOperator<String> TYPE_TEMPLATE = "%s_%s"::formatted;
    
    private final CarryInteractContext causeData;
    
    private CarryInteractHandleException(@NotNull Throwable wrappedException, @NotNull String type, @NotNull CarryInteractContext causeData)
    {
        super(wrappedException, type);
        Objects.requireNonNull(causeData, "Param \"causeData\" must not be null!");
        this.causeData = causeData;
    }
    
    public static <T> @NotNull IResult<T, CarryInteractHandleException> miscFailed(
        @NotNull CarryInteractContext causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exceptionFactory,
        @NotNull String type
    )
    {
        Objects.requireNonNull(message, "Param \"message\" must not be null!");
        Objects.requireNonNull(exceptionFactory, "Param \"exceptionFactory\" must not be null!");
        return IResult.ofFailed(new CarryInteractHandleException(exceptionFactory.apply(message), type, causeData));
    }
    
    public static <T> @NotNull IResult<T, CarryInteractHandleException> boxInFailed(
        @NotNull CarryInteractContext causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exceptionFactory,
        @NotNull CarryType type
    )
    {
        return IResult.ofFailed(
            new CarryInteractHandleException(
                exceptionFactory.apply(message),
                TYPE_TEMPLATE.apply(type.name(), "BOX_IN"),
                causeData
            )
        );
    }
    
    public static <T> @NotNull IResult<T, CarryInteractHandleException> unboxFailed(
        @NotNull CarryInteractContext causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exceptionFactory,
        @NotNull CarryType type
    )
    {
        return IResult.ofFailed(
            new CarryInteractHandleException(
                exceptionFactory.apply(message),
                TYPE_TEMPLATE.apply(type.name(), "UNBOX"),
                causeData
            )
        );
    }
    
    public static <T> @NotNull IResult<T, CarryInteractHandleException> listener(
        @NotNull CarryInteractContext causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull TriState state
    )
    {
        assert state != null : "Param \"state\" must not be null!";
        
        return IResult.ofFailed(
            new CarryInteractHandleException(
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
    
    public static <T> @NotNull IResult<T, CarryInteractHandleException> component(
        @NotNull CarryInteractContext causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull TriState state
    )
    {
        assert state != null : "Param \"state\" must not be null!";
        
        return IResult.ofFailed(
            new CarryInteractHandleException(
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
    
    public static <T> @NotNull IResult<T, CarryInteractHandleException> target(
        @NotNull CarryInteractContext causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull CarryType type,
        @NotNull TriState state
    )
    {
        assert type != null : "Param \"type\" must not be null!";
        assert state != null : "Param \"state\" must not be null!";
        
        return IResult.ofFailed(
            new CarryInteractHandleException(
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
    
    @Override public @NotNull CarryInteractContext causeData() { return causeData; }
    
    @Override public InteractionResult rollback() { return causeData.result(); }
}
