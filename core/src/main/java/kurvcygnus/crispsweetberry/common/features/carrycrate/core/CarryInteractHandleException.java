//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContext;
import kurvcygnus.crispsweetberry.lib.base.exceptions.ITransactionalThrowable;
import kurvcygnus.crispsweetberry.lib.base.exceptions.StructuredException;
import kurvcygnus.crispsweetberry.lib.base.lang.IResult;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import net.minecraft.world.InteractionResult;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static kurvcygnus.crispsweetberry.utils.AssertUtils.nonNullCheckOnDev;

/**
 * A simple exception for <u>{@link CarryEngine CarryEngine}</u>'s core logic error handling,
 * which wraps an exception to make Functional Pipelines fluent, with self-descriptive error type tags, abnormal data snapshot and rollback ability provided.
 *
 * @author Kurv Cygnus
 * @see StructuredException
 * @see ITransactionalThrowable
 * @since 1.0 Release
 */
@ApiStatus.Internal final class CarryInteractHandleException extends StructuredException
implements ITransactionalThrowable<CarryInteractHandleException, CarryInteractContext, InteractionResult>
{
    private static final BinaryOperator<String> TYPE_TEMPLATE = (s1, s2) -> DefinitionUtils.quickFormat("{}_{}", s1, s2);
    
    private final CarryInteractContext causeData;
    
    private CarryInteractHandleException(@NotNull Throwable wrappedException, @NotNull String type, @NotNull CarryInteractContext causeData)
    {
        super(wrappedException, type);
        Objects.requireNonNull(causeData, "Param \"causeData\" must not be null!");
        this.causeData = causeData;
    }
    
    static <T> @NotNull IResult<T, CarryInteractHandleException> miscFailed(
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
    
    static <T> @NotNull IResult<T, CarryInteractHandleException> boxInFailed(
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
    
    @SuppressWarnings("SameParameterValue") static <T> @NotNull IResult<T, CarryInteractHandleException> unboxFailed(
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
    
    static <T> @NotNull IResult<T, CarryInteractHandleException> listener(
        @NotNull CarryInteractContext causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull TriState state
    )
    {
        nonNullCheckOnDev(state, "state");
        
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
    
    static <T> @NotNull IResult<T, CarryInteractHandleException> target(
        @NotNull CarryInteractContext causeData,
        @NotNull String message,
        @NotNull Function<String, Throwable> exception,
        @NotNull CarryType type,
        @NotNull TriState state
    )
    {
        nonNullCheckOnDev(type, "type");
        nonNullCheckOnDev(state, "state");
        
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
