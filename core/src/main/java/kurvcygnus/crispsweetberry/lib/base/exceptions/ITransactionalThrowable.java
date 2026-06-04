//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.exceptions;

import kurvcygnus.crispsweetberry.lib.base.lang.IResult;

import java.util.function.Function;

/**
 * A <b>transitional / recoverable exception</b> that can <b>roll back</b> side effects.<br>
 * This is the <b>most powerful interface</b> in this hierarchy for functional error recovery,
 * because a failure no longer means a dead end — it carries its own <b>compensation action</b>.<hr>
 * <p><b>Role in <u>{@link kurvcygnus.crispsweetberry.lib.base.lang.IResult IResult}</u> pipelines:</b></p>
 * <ul>
 *     <li>This interface is designed to pair directly with
 *         <u>{@link IResult#fold(Function, Function)}</u>.
 *         The <b>canonical usage pattern</b> is shown in <u>{@link IResult}</u>'s Javadoc:
 *         <pre>{@code
 *          return IResult.of(data).
 *              map(Data::transform).
 *              flatMap(this::handle).
 *              fold(
 *                  TransformedData::result,
 *                  ex ->
 *                  {
 *                      LOGGER.error(ex);
 *                      return ex.rollback();  // ITransitionalThrowable in action
 *                  }
 *              );
 *         }</pre></li>
 *     <li>In the failure branch of a pipeline, call <u>{@link #rollback()}</u> to
 *         <b>undo any partial work</b> that was done before the failure occurred,
 *         then return a fallback value — effectively turning a failure into a
 *         <span style="color: 95cc6d">safe, recoverable result</span>.</li>
 *     <li>This makes {@code IResult<T, ITransitionalThrowable<...>>} an <b>exception monad with
 *         built-in compensation</b>, analogous to a {@code try/catch} with a {@code finally}-like
 *         cleanup baked into the exception itself.</li>
 * </ul>
 * <p><b>What it adds to exceptions:</b></p>
 * <ul>
 *     <li><u>{@link #rollback()}</u> — A <b>compensation action</b> that reverts the state changes
 *         associated with this failure and returns a recovery value of type {@code R}.
 *         <i>This elevates exceptions from passive error descriptors to active recovery agents.</i></li>
 *     <li>All features from <u>{@link IDetailedThrowable}</u> (typed cause data) and
 *         <u>{@link IStructuredThrowable}</u> (type tag) are inherited, so a single exception
 *         carries: categorization + detailed context + a rollback strategy.</li>
 * </ul>
 * @param <E> The concrete <u>{@link StructuredException}</u> subtype that implements this interface
 *            (CRTP self-type).
 * @param <T> The type of the detailed cause data (pass-through from <u>{@link IDetailedThrowable}</u>).
 * @param <R> The type of the <b>rollback return value</b> — the fallback result produced when
 *            compensating for this failure.
 *            <i>If you need side effects, and doesn't need a return value, use <u>{@link Void}</u> at here</i>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see StructuredException
 */
public interface ITransactionalThrowable<E extends StructuredException & ITransactionalThrowable<E, T, R>, T, R> extends IDetailedThrowable<E, T>
{
    /**
     * Performs the <b>rollback / compensation action</b> for this exception, undoing any partial
     * side effects that occurred before the failure, and returns a recovery value.
     * @return the fallback result of type {@code R} after compensation
     */
    R rollback();
}
