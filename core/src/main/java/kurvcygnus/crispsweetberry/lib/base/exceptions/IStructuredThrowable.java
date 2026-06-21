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

import java.util.function.Function;

/**
 * The <b>foundational interface</b> of this exception hierarchy.<br>
 * It transforms any <u>{@link Throwable}</u> into a <b>structured</b> form by attaching a <u>{@link #tag() type tag}</u>
 * and delegating to a <u>{@link #cause() wrapped exception}</u>.<hr>
 * <p><b>Role in <u>{@link kurvcygnus.crispsweetberry.lib.base.lang.IResult IResult}</u> pipelines:</b></p>
 * <ul>
 *     <li><u>{@link kurvcygnus.crispsweetberry.lib.base.lang.IResult IResult}</u> uses {@code E extends Throwable} as its error type parameter.
 *         <u>{@code IStructuredThrowable}</u> is the <b>base contract</b> that allows the
 *         pipeline to distinguish <i>structured</i> failures from raw ones — the <u>{@link #tag()}</u>
 *         provides a categorical key that can be used in <u>{@link IResult#fold(Function, Function)}</u> or
 *         <u>{@link IResult#orElseMapException(Function)}</u> for <b>type-driven error recovery</b>.</li>
 *     <li>All exception classes in this package extend this interface, so every
 *         {@code IResult<T, ? extends IStructuredThrowable>} can rely on <u>{@link #cause()}</u>
 *         to access the original cause without losing the structural metadata.</li>
 * </ul>
 * <p><b>What it adds to exceptions:</b></p>
 * <ul>
 *     <li><u>{@link #tag()}</u> — A non-null string tag (e.g. {@code "NETWORK"}) that categorizes
 *         the failure, enabling pattern-match-style handling without {@code instanceof} chains.</li>
 *     <li><u>{@link #cause()}</u> — Unwraps the original {@link Throwable}
 *         that this structured exception decorates, preserving the full causal chain.</li>
 * </ul>
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see StructuredException
 * @see IDetailedThrowable
 */
public interface IStructuredThrowable
{
    /**
     * Returns the original, wrapped {@link Throwable} that this structured exception decorates.
     * @return the wrapped throwable, never null
     */
    @NotNull Throwable cause();

    /**
     * Returns the <b>tag</b> that categorizes this exception by <b>contextual semantics</b>.<br>
     * This means, <span style="color: f84b4b">you shouldn't use it to do pattern matching, or rollback externally,
     * it should only be used to represent process phase, or being use by <u>{@link ITransactionalThrowable#rollback()}</u>.</span>
     * <br><br>
     * For rollback/recover ability, see <u>{@link ITransactionalThrowable}</u>.
     * @return a non-blank string identifier (e.g. {@code "NETWORK"}, {@code "VALIDATION"}), never null
     */
    @NotNull String tag();
}
