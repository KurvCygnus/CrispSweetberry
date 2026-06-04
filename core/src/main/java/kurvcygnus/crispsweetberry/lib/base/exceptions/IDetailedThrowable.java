//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.exceptions;

import kurvcygnus.crispsweetberry.lib.base.lang.IResult;
import kurvcygnus.crispsweetberry.lib.base.trait.ICRTPCaster;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * An <b>enriched structured exception</b> that carries <b>typed detailed data</b> about the failure cause.<br>
 * It uses <u>{@link kurvcygnus.crispsweetberry.lib.base.trait.ICRTPCaster ICRTPCaster}</u> (CRTP) so that
 * implementations can fluently cast back to their concrete type without manual casting.<hr>
 * <p><b>Role in <u>{@link kurvcygnus.crispsweetberry.lib.base.lang.IResult IResult}</u> pipelines:</b></p>
 * <ul>
 *     <li>When an <u>{@link IResult}</u> carries a failure of this type, the <b>exception itself becomes a
 *         data source</b> — the error branch can call <u>{@link #causeData()}</u> to obtain <b>typed context</b>
 *         about what went wrong, which feeds naturally into
 *         <u>{@link IResult#orElseMapException(Function)}</u> or
 *         <u>{@link IResult#fold(Function, Function)}</u>.</li>
 *     <li>Because this extends <u>{@link IStructuredThrowable}</u>, every pipeline that accepts
 *         {@code IResult<T, ? extends IDetailedThrowable>} gets both the <b>type tag</b> and the
 *         <b>rich cause data</b>, making error recovery more informative than raw message parsing.</li>
 * </ul>
 * <p><b>What it adds to exceptions:</b></p>
 * <ul>
 *     <li><u>{@link #causeData()}</u> — Returns a <b>typed payload</b> ({@code T}) that holds domain-specific
 *         details about the failure (e.g. the invalid input, the failed entity ID).</li>
 *     <li><u>{@link #asException()}</u> — <span style="color: 95cc6d">Safe</span> self-casting via CRTP,
 *         returning this exception as its concrete {@link StructuredException} subtype without a cast.</li>
 *     <li><u>{@link #throwSelf()}</u> — A convenience that <b>throws</b> this exception directly, avoiding
 *         the need to catch-and-rethrow with a cast.</li>
 *     <li><u>{@link #cause()}</u> — {@linkplain Throwable#getCause() Analogous to} {@code Throwable.getCause()},
 *         but <span style="color: 95cc6d">guaranteed non-null</span>, returning the result of
 *         <u>{@link #cause()}</u>.</li>
 * </ul>
 *
 * @param <E> The concrete <u>{@link StructuredException}</u> subtype that implements this interface
 *            (CRTP self-type).
 * @param <T> The type of the <b>detailed cause data</b> carried alongside the exception.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see StructuredException
 * @see ITransactionalThrowable
 */
public interface IDetailedThrowable<E extends StructuredException & IDetailedThrowable<E, T>, T>
extends IStructuredThrowable, ICRTPCaster<IDetailedThrowable<E, T>, E>
{
    /**
     * Returns the <b>typed detailed data</b> associated with this exception's cause.
     * @return the detailed cause data, never null
     */
    @NotNull T causeData();

    /**
     * <span style="color: 95cc6d">Safe</span> self-casting via <u>{@link kurvcygnus.crispsweetberry.lib.base.trait.ICRTPCaster ICRTPCaster}</u>.
     * @return this exception cast to its concrete <u>{@link StructuredException}</u> subtype {@code E}
     */
    default @NotNull E asException() { return getSelf(); }

    /**
     * Throws this exception as its concrete type {@code E}.
     * @throws E always, as this is a failure result
     */
    default void throwSelf() throws E { throw asException(); }

    /**
     * {@inheritDoc}
     * Delegates to <u>{@link #asException()}</u>{@code .getMessage()}.
     * @return the formatted message from the underlying structured exception
     */
    default @NotNull String getMessage() { return asException().getMessage(); }

    /**
     * Returns the <b>wrapped original exception</b> as the <u>{@linkplain Throwable#getCause() cause}</u>.
     * @return the wrapped throwable, never null
     */
    @Override default @NotNull Throwable cause() { return asException().cause(); }
}
