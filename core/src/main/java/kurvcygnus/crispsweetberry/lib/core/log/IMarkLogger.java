//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.core.log;

import kurvcygnus.crispsweetberry.lib.base.extensions.BaseNestedPrinter;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import kurvcygnus.crispsweetberry.lib.base.functions.ITriConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.function.*;

import static java.util.Objects.requireNonNull;
import static org.slf4j.event.Level.*;

/**
 * This is a simple wrapper for SLF4J's <u>{@link Logger}</u>. It reduces the verbosity of passing <u>{@link Marker}</u> to log functions.
 * @author Kurv Cygnus
 * @apiNote We recommend using {@code SCREAMING_SNAKE_CASE} for <u>{@link Marker}</u>, because it is more attractive, and easy to search.<br>
 * <b>This logger uses <u>{@link ThreadLocal}</u>. Do not leak <u>{@link IMarkerHandle IMarkerHandle}</u> across async boundaries</b>.<br>
 * <i>(However, in async, you can solve this with passing <u>{@link Marker}</u> into logging methods manually to override {@code IMarkLogger}'s mechanic,
 * which solves basic problems, you can see <u>{@link #info(Marker, String)}</u> as an example)</i>
 * @since 1.0 Release
 */
public sealed interface IMarkLogger extends Logger
{
    /**
     * Produces a standard logger with no default marker.
     * @throws NullPointerException When {@code logger} is {@code null}
     * @implSpec <pre>{@code
     *  private static final IMarkLogger LOGGER = IMarkLogger.marklessLogger();
     * }</pre>
     */
    static @NotNull IMarkLogger marklessLogger() { return MarkLogger.marklessLogger(MarkLogger.STACK_WALKER.getCallerClass()); }
    
    /**
     * Produces a standard logger, which automatically deals marker.
     * @throws NullPointerException When {@code logger} or {@code marker} is {@code null}
     * @implSpec <pre>{@code
     *  private static final IMarkLogger LOGGER = IMarkLogger.markedLogger(
     *      MarkerFactory.getMarker("Foo")
     *  );
     * }</pre>
     */
    static @NotNull IMarkLogger markedLogger(@NotNull Marker marker) { return MarkLogger.markedLogger(MarkLogger.STACK_WALKER.getCallerClass(), marker); }
    
    /**
     * Produces a standard logger, which automatically deals marker.
     * @throws NullPointerException When {@code logger} or {@code mark} is {@code null}
     * @implSpec <pre>{@code
     *  private static final IMarkLogger LOGGER = IMarkLogger.markedLogger("Foo");
     * }</pre>
     */
    static @NotNull IMarkLogger markedLogger(@NotNull String mark) { return MarkLogger.markedLogger(MarkLogger.STACK_WALKER.getCallerClass(), mark); }
    
    /**
     * Produces a special logger, whose have two extra unique markers with {@code _ERR} and {@code _WARN} suffixes to override
     * {@code defaultMarker} in corresponded log level functions.
     *
     * @throws NullPointerException When {@code logger} or {@code marker} is {@code null}
     * @implSpec <pre>{@code
     *  private static final IMarkLogger LOGGER = IMarkLogger.withMarkerSuffixes(
     *      MarkerFactory.getMarker("Bar")
     *  );
     * }</pre>
     * <hr>
     * Produces these markers:
     * <pre>{@code
     *  // foo -> foo_err(For error log funcs)
     *  // BAR -> BAR_WARN(For warn log funcs)
     * }</pre>
     * <i>Current suffix conversion is quite simple, since we don't think it needs to support all text cases.</i>
     */
    static @NotNull IMarkLogger withMarkerSuffixes(@NotNull Marker marker) { return MarkLogger.withMarkerSuffixes(MarkLogger.STACK_WALKER.getCallerClass(), marker); }
    
    /**
     * Produces a special logger, whose have two extra unique markers with {@code _ERR} and {@code _WARN} suffixes to override
     * {@code defaultMarker} in corresponded log level functions.
     *
     * @throws NullPointerException When {@code logger} or {@code mark} is {@code null}
     * @implSpec <pre>{@code
     *  private static final IMarkLogger LOGGER = IMarkLogger.withMarkerSuffixes("Bar");
     * }</pre><hr>
     * Produces these markers:
     * <pre>{@code
     *  // foo -> foo_err(For error log funcs)
     *  // BAR -> BAR_WARN(For warn log funcs)
     * }</pre>
     * <i>Current suffix conversion is quite simple, since we don't think it needs to support all text cases.</i>
     */
    static @NotNull IMarkLogger withMarkerSuffixes(@NotNull String mark) { return MarkLogger.withMarkerSuffixes(MarkLogger.STACK_WALKER.getCallerClass(), mark); }
    
    /**
     * Produces a highly configurable logger, which supports adaptive markers suffix<i>(with {@code adaptive} arg's value equaling {@code true})</i>,
     * and the ability to log message, depending on arg {@code condition}.
     */
    static @NotNull IMarkLogger configuredLogger(@NotNull String mark, @NotNull Predicate<Level> condition, boolean adaptive)
        { return MarkLogger.configuredLogger(MarkLogger.STACK_WALKER.getCallerClass(), mark, condition, adaptive); }
    
    /**
     * Produces a highly configurable logger, which supports the ability to log message, depending on arg {@code condition}.
     */
    static @NotNull IMarkLogger configuredLogger(@NotNull Predicate<Level> condition) { return MarkLogger.configuredLogger(MarkLogger.STACK_WALKER.getCallerClass(), condition); }
    
    /**
     * Creates a condition that allows logging only when the <u>{@link Level log level}</u> satisfies
     * the specified comparison against the provided reference level.
     *
     * @param level     The reference log level to compare against.
     * @param situation The comparison logic (e.g. <u>{@link ConditionSituation#EQUAL EQUAL}</u>, <u>{@link ConditionSituation#HIGHER HIGHER}</u>,
     *                  <u>{@link ConditionSituation#LOWER LOWER}</u>).
     * @param extra     An additional boolean flag to force-enable the log (OR logic).
     * @return A predicate that returns {@code true} if the log should be performed.
     * @apiNote <span style="color: 95cc6d">The value of {@code extra} is <b>dynamic</b></span>, it will changed with the formula of the <u>{@link Predicate}</u>.
     */
    static @NotNull Predicate<Level> allowWhen(@NotNull Level level, @NotNull ConditionSituation situation, BooleanSupplier extra)
        { return MarkLogger.allowWhen(level, situation, extra); }
    
    /**
     * Creates a condition that rejects logging when the log level satisfies
     * the specified comparison against the provided reference level.
     *
     * @param level     The reference log level to compare against.
     * @param situation The comparison logic (e.g. <u>{@link ConditionSituation#EQUAL EQUAL}</u>, <u>{@link ConditionSituation#HIGHER HIGHER}</u>,
     *                  <u>{@link ConditionSituation#LOWER LOWER}</u>).
     * @param extra     An additional boolean flag to force-enable the log (OR logic).
     * @return A predicate that returns {@code true} if the log should be performed.
     * @apiNote <span style="color: 95cc6d">The value of {@code extra} is <b>dynamic</b></span>, it will changed with the formula of the <u>{@link Predicate}</u>.
     */
    static @NotNull Predicate<Level> denyWhen(@NotNull Level level, @NotNull ConditionSituation situation, BooleanSupplier extra) { return MarkLogger.denyWhen(level, situation, extra); }
    
    /**
     * Push a temporary marker to <u>{@link MarkLogger}</u>,
     * and will always be used until current key is ended.<br><br>
     * <b>Thus, this will only work correctly and normally with
     * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a></b>.
     * @throws NullPointerException When {@code marker} is {@code null}
     * @implSpec <pre>{@code
     *  // ↓ "ignored" disables unused warnings.
     *  try(var ignored = LOGGER.pushMarker(MarkerFactory.getMarker("Foo")))
     *  {
     *      // All markers of log inside
     *      // will be overridden with "Foo".
     *  }
     *  // Out of scope, override no longer exists.
     * }</pre>
     */
    @NotNull IMarkerHandle pushMarker(@NotNull Marker marker);
    
    /**
     * Push a temporary marker to <u>{@link MarkLogger}</u>,
     * and will always be used until <u>{@link MarkLogger}</u>'s lifecycle is ended, which is obviously impossible.<br><br>
     * <b>Thus, this will only work correctly and normally with
     * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a></b>.
     * @throws NullPointerException When {@code mark} is {@code null}
     * @implSpec <pre>{@code
     *  // ↓ "ignored" disables unused warnings.
     *  try(var ignored = LOGGER.pushMarker("Foo"))
     *  {
     *      // All markers of log inside
     *      // will be overridden with "Foo".
     *  }
     *  // Out of scope, override no longer exists.
     * }</pre>
     */
    @NotNull IMarkerHandle pushMarker(@NotNull String mark);
    
    /**
     * A simple interface to implement <u>{@link AutoCloseable}</u> for <u>{@link MarkLogger}</u>, making it usable in
     * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a>.
     * @author Kurv Cygnus
     * @see #pushMarker(Marker) Usage
     * @since 1.0 Release
     */
    sealed interface IMarkerHandle extends AutoCloseable
    {
        /**
         * Changes the temporary marker that key the default one.
         *
         * @throws NullPointerException When {@code marker} is {@code null}
         * @apiNote This should be used in the key of
         * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a>,
         * which is started by <u>{@link #pushMarker(Marker)}</u>, or <u>{@link #pushMarker(String)}</u>.
         * @implSpec <pre>{@code
         *  try(var handle = LOGGER.pushMarker("Foo"))
         *  {
         *      // ↓ With marker "Foo".
         *      LOGGER.info("Bar");
         *      handle.changeMarker(MarkerFactory.getMarker("Baz"));
         *
         *      // ↓ With marker "Baz".
         *      LOGGER.info("42");
         *  }
         * }</pre>
         * @see #pushMarker(Marker)
         */
        void changeMarker(@NotNull Marker marker);
        
        /**
         * Changes the temporary marker that key the default one.
         *
         * @throws NullPointerException When {@code mark} is {@code null}
         * @apiNote This should be used in the key of
         * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a>,
         * which is started by <u>{@link #pushMarker(Marker)}</u>, or <u>{@link #pushMarker(String)}</u>.
         * @implSpec <pre>{@code
         *  try(var handle = LOGGER.pushMarker("Foo"))
         *  {
         *      // ↓ With marker "Foo".
         *      LOGGER.info("Bar");
         *      handle.changeMarker("Baz");
         *
         *      // ↓ With marker "Baz".
         *      LOGGER.info("42");
         *  }
         * }</pre>
         * @see #pushMarker(String)
         */
        void changeMarker(@NotNull String mark);
        
        /**
         * Closes this handle and removes the associated <u>{@link Marker}</u> from the <u>{@link ThreadLocal}</u> stack.
         * @apiNote This method is idempotent. Calling it multiple times will not pop more than one marker.
         * <b>Failure to close this handle (especially in async or pooled thread environments) will lead to
         * <i>Marker Pollution</i></b>.
         * @deprecated It is no longer recommended to {@code #close} the resource manually. Use
         * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a>
         * instead.
         */
        @Override @Deprecated @ApiStatus.Obsolete void close();
    }
    
    /**
     * Prints a <u>{@link Object}</u> at TRACE level.
     * @deprecated This is not deprecated, but it is marked because this is only used for quick debugging, the standard log info should not be such short, and indescriptive.
     */
    @Deprecated default void trace(Object object) { this.trace("{}", object); }
    
    /**
     * {@inheritDoc}
     */
    @Override String getName();
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isTraceEnabled();
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isTraceEnabled(Marker marker);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(String message);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(String format, Object... arguments);
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(String message, Throwable throwable);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(Marker marker, String message);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(Marker marker, String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(Marker marker, String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(Marker marker, String format, Object... argArray);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void trace(Marker marker, String message, Throwable throwable);
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isDebugEnabled();
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isDebugEnabled(Marker marker);
    
    /**
     * Prints a <u>{@link Object}</u> at TRACE level.
     * @deprecated This is not deprecated, but it is marked because this is only used for quick debugging, the standard log info should not be such short, and indescriptive.
     */
    @Deprecated default void debug(Object object) { this.debug("{}", object); }
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(String message);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(String format, Object... arguments);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(String message, Throwable throwable);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(Marker marker, String message);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(Marker marker, String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(Marker marker, String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(Marker marker, String format, Object... arguments);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void debug(Marker marker, String message, Throwable throwable);
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isInfoEnabled();
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isInfoEnabled(Marker marker);
    
    /**
     * Prints a <u>{@link Object}</u> at TRACE level.
     * @deprecated This is not deprecated, but it is marked because this is only used for quick debugging, the standard log info should not be such short, and indescriptive.
     */
    @Deprecated default void info(Object object) { this.info("{}", object); }
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(String message);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(String format, Object... arguments);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(String message, Throwable throwable);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(Marker marker, String message);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(Marker marker, String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(Marker marker, String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(Marker marker, String format, Object... arguments);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void info(Marker marker, String message, Throwable throwable);
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isWarnEnabled();
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isWarnEnabled(Marker marker);
    
    /**
     * Prints a <u>{@link Object}</u> at TRACE level.
     * @deprecated This is not deprecated, but it is marked because this is only used for quick debugging, the standard log info should not be such short, and indescriptive.
     */
    @Deprecated default void warn(Object object) { this.warn("{}", object); }
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(String message);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(String format, Object... arguments);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(String message, Throwable throwable);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(Marker marker, String msg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(Marker marker, String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(Marker marker, String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(Marker marker, String format, Object... arguments);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void warn(Marker marker, String message, Throwable throwable);
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isErrorEnabled();
    
    /**
     * {@inheritDoc}
     * @apiNote <b>If the condition from <u>{@link #configuredLogger(Predicate)}</u> is not met, this will always return {@code false}.</b>
     */
    @Override boolean isErrorEnabled(Marker marker);
    
    /**
     * Prints a <u>{@link Object}</u> at TRACE level.
     * @deprecated This is not deprecated, but it is marked because this is only used for quick debugging, the standard log info should not be such short, and indescriptive.
     */
    @Deprecated default void error(Object object) { this.error("{}", object); }
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(String message);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(String format, Object... arguments);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(String message, Throwable throwable);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(Marker marker, String message);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(Marker marker, String format, Object arg);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(Marker marker, String format, Object arg1, Object arg2);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(Marker marker, String format, Object... arguments);
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: f84b4b">This log method won't be overridden by
     * <u>{@link IMarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override void error(Marker marker, String message, Throwable throwable);
    
    @NotNull String toString();
    
    enum ConditionSituation
    {
        EQUAL((fieldLevel, argLevel) -> fieldLevel == argLevel),
        HIGHER((fieldLevel, argLevel) -> fieldLevel.toInt() >= argLevel.toInt()),
        LOWER((fieldLevel, argLevel) -> fieldLevel.toInt() <= argLevel.toInt());
        
        final BiPredicate<Level, Level> condition;
        
        ConditionSituation(@NotNull BiPredicate<Level, Level> condition) { this.condition = condition; }
    }
}

final class MarkLogger extends BaseNestedPrinter<MarkLogger> implements IMarkLogger
{
    //  region Fields & Constants
    /**
     * The core of this wrapper. All log functions are actually executed by this.
     */
    private final @NotNull Logger logger;
    
    /**
     * The <u>{@link Marker}</u> used for logging. At non-error and non-warn cases, and with no key, it
     * is the marker that will be used for display.
     */
    private final @Nullable Marker defaultMarker;
    
    /**
     * The <u>{@link Marker}</u> exclusively used for error level logging. With no key, it
     * is the marker that will be used for display.
     */
    private final @Nullable Marker errorMarker;
    
    /**
     * The <u>{@link Marker}</u> exclusively used for warn level logging. With no key, it
     * is the marker that will be used for display.
     */
    private final @Nullable Marker warnMarker;
    
    /**
     * The marker collections based on <u>{@link java.util.Stack Stack}</u>(or more precisely, <u>{@link ArrayDeque}</u>),
     * and <u>{@link ThreadLocal}</u>(Out of prevent context pollution, and implement inexplicit context passing).<br>
     * As long as this deque has a single <u>{@link Marker}</u>, both three markers above
     * (<u>{@link #defaultMarker}</u>, <u>{@link #errorMarker}</u> and <u>{@link #warnMarker}</u>) will all be overridden.<br>
     *
     * @apiNote Usually, {@code mutableMarker} will only exists in a limited key with <u>{@link #pushMarker(String)}</u> or <u>{@link #pushMarker(Marker)}</u>,
     * which is recommended to be used with
     * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a></b>,
     * unexpected situations will only happen in neither directly use <u>{@link #pushMarker(String)}</u> or <u>{@link #pushMarker(Marker)}</u>,
     * nor using {@code Reflection}, <b>both two situations are not recommended, or supposed to be happened</b>.
     * <br>
     * As mentioned above, <span style="color: f84b4b">do not leak <u>{@link MarkerHandle MarkerHandle}</u> across async boundaries</b>.</span>
     */
    private final @NotNull ThreadLocal<ArrayDeque<Marker>> mutableMarker = new ThreadLocal<>();
    
    /**
     * The <u>{@link Level}</u> based condition. It decides whether the message will be printed for specified cases.
     */
    private final @NotNull Predicate<Level> condition;
    
    private static final @NotNull Predicate<Level> TRUE = ignored -> true;
    static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    
    private static final Map<String, Function<MarkLogger, @Nullable Object>> FIELD_MAP = INestedPrintable.buildFieldMap(
        map ->
        {
            map.put("name", ml -> ml.logger.getName());
            map.put("defaultMarkerName", ml -> ml.getNameSafely(ml.getMarker()));
            map.put("warnMarkerName", ml -> ml.getNameSafely(ml.getWarnMarker()));
            map.put("errorMarkerName", ml -> ml.getNameSafely(ml.getErrorMarker()));
            map.put("markerStacks", ml -> ml.mutableMarker.get() != null ? ml.mutableMarker.get().toString() : "N/A");
            map.put("traceAccess", ml -> ml.condition.test(TRACE));
            map.put("debugAccess", ml -> ml.condition.test(DEBUG));
            map.put("infoAccess", ml -> ml.condition.test(INFO));
            map.put("warnAccess", ml -> ml.condition.test(WARN));
            map.put("errorAccess", ml -> ml.condition.test(ERROR));
        },
        10
    );
    //endregion
    
    //  region Constructor & Static Factories
    private MarkLogger(
        @NotNull Class<?> clazz,
        @Nullable Marker defaultMarker,
        @Nullable Marker errorMarker,
        @Nullable Marker warnMarker,
        @NotNull Predicate<Level> condition
    )
    {
        requireNonNull(clazz, "Param \"clazz\" must not be null!");
        this.logger = LoggerFactory.getLogger(clazz);
        this.defaultMarker = defaultMarker;
        this.errorMarker = errorMarker;
        this.warnMarker = warnMarker;
        this.condition = condition;
    }
    
    static @NotNull MarkLogger marklessLogger(@NotNull Class<?> clazz)
    {
        requireNonNull(clazz, "Param \"clazz\" must not be null!");
        return new MarkLogger(clazz, null, null, null, TRUE);
    }
    
    static @NotNull MarkLogger markedLogger(@NotNull Class<?> clazz, @NotNull Marker marker)
    {
        requireNonNull(clazz, "Param \"clazz\" must not be null!");
        requireNonNull(marker, "Param \"marker\" must not be null!");
        
        return new MarkLogger(clazz, marker, marker, marker, TRUE);
    }
    
    static @NotNull MarkLogger markedLogger(@NotNull Class<?> clazz, @NotNull String mark)
    {
        requireNonNull(clazz, "Param \"clazz\" must not be null!");
        requireNonNull(mark, "Param \"mark\" must not be null!");
        if(mark.isBlank())
            throw new IllegalArgumentException("Param \"mark\" must not be empty!");
        
        final Marker marker = MarkerFactory.getMarker(mark);
        
        return new MarkLogger(clazz, marker, marker, marker, TRUE);
    }
    
    static @NotNull MarkLogger withMarkerSuffixes(@NotNull Class<?> clazz, @NotNull Marker marker)
    {
        requireNonNull(clazz, "Param \"clazz\" must not be null!");
        requireNonNull(marker, "Param \"marker\" must not be null!");
        
        final Marker err = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_ERR"));
        final Marker warn = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_WARN"));
        
        return new MarkLogger(clazz, marker, err, warn, TRUE);
    }
    
    static @NotNull MarkLogger withMarkerSuffixes(@NotNull Class<?> clazz, @NotNull String mark)
    {
        requireNonNull(clazz, "Param \"clazz\" must not be null!");
        requireNonNull(mark, "Param \"mark\" must not be null!");
        if(mark.isBlank())
            throw new IllegalArgumentException("Param \"mark\" must not be empty!");
        
        return withMarkerSuffixes(clazz, MarkerFactory.getMarker(mark));
    }
    
    /**
     * Produces a highly configurable logger, which supports adaptive markers suffix<i>(with {@code adaptive} arg's value equaling {@code true})</i>,
     * and the ability to log message, depending on arg {@code condition}.
     */
    private static @NotNull MarkLogger configuredLogger(@NotNull Class<?> clazz, @Nullable Marker marker, boolean adaptive, @NotNull Predicate<Level> condition)
    {
        requireNonNull(clazz, "Param \"clazz\" must not be null!");
        requireNonNull(condition, "Param \"condition\" must not be null!");
        
        if(marker == null && adaptive)
            throw new IllegalArgumentException("Creating a MarkLogger instance with null marker and adaptive markers is not allowed!");
        
        if(adaptive)
        {
            requireNonNull(marker, "Param \"marker\" must not be null!");
            
            final Marker err = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_ERR"));
            final Marker warn = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_WARN"));
            
            return new MarkLogger(clazz, marker, err, warn, condition);
        }
        
        return new MarkLogger(clazz, marker, marker, marker, condition);
    }
    
    static @NotNull MarkLogger configuredLogger(@NotNull Class<?> clazz, @NotNull String mark, @NotNull Predicate<Level> condition, boolean adaptive)
    {
        requireNonNull(mark, "Param \"mark\" must not be null!");
        if(mark.isBlank())
            throw new IllegalArgumentException("Param \"mark\" must not be empty!");
        
        return configuredLogger(clazz, MarkerFactory.getMarker(mark), adaptive, condition);
    }
    
    static @NotNull MarkLogger configuredLogger(@NotNull Class<?> clazz, @NotNull Predicate<Level> condition)
        { return configuredLogger(clazz, null, false, condition); }
    
    static @NotNull Predicate<Level> allowWhen(@NotNull Level level, @NotNull ConditionSituation situation, BooleanSupplier extra)
        { return leveledCondition(level, situation, extra, false); }
    
    static @NotNull Predicate<Level> denyWhen(@NotNull Level level, @NotNull ConditionSituation situation, BooleanSupplier extra)
        { return leveledCondition(level, situation, extra, true); }
    //endregion
    
    //  region Scoped Marker Logics
    /**
     * {@inheritDoc}
     */
    @Override public @NotNull IMarkerHandle pushMarker(@NotNull Marker marker)
    {
        requireNonNull(marker, "Param \"marker\" must not be null!");
        pushTempMarker(marker);
        return new MarkerHandle(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public @NotNull IMarkerHandle pushMarker(@NotNull String mark)
    {
        requireNonNull(mark, "Param \"mark\" must not be null!");
        return this.pushMarker(MarkerFactory.getMarker(mark));
    }
    
    static final class MarkerHandle implements IMarkerHandle
    {
        private final MarkLogger logger;
        private boolean closed = false;
        
        private MarkerHandle(@NotNull MarkLogger logger) { this.logger = logger; }
        
        /**
         * {@inheritDoc}
         */
        @Override public void changeMarker(@NotNull Marker marker)
        {
            requireNonNull(marker, "Param \"marker\" must not be null!");
            logger.setTempMarker(marker);
        }
        
        /**
         * {@inheritDoc}
         */
        @Override public void changeMarker(@NotNull String mark)
        {
            requireNonNull(mark, "Param \"mark\" must not be null!");
            logger.setTempMarker(MarkerFactory.getMarker(mark));
        }
        
        /**
         * {@inheritDoc}
         * @deprecated See <u>{@link IMarkerHandle#close()}</u>.
         */
        @SuppressWarnings("deprecated") @Deprecated @Override public void close()
        {
            if(closed)
                return;
            
            final ArrayDeque<Marker> stack = logger.mutableMarker.get();
            
            if(stack != null)
            {
                if(!stack.isEmpty())
                    stack.pop();
                else
                    logger.mutableMarker.remove();
            }
            
            closed = true;
        }
    }
    //endregion
    
    //  region Log Print Commons
    //*:== Trace
    @Override public void trace(@Nullable String message) { this.print(logger::trace, TRACE, getMarker(), message); }
    
    @Override public void trace(@Nullable String message, Object @Nullable ... args) { this.print(logger::trace, TRACE, getMarker(), message, args); }
    
    @Override public void trace(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::trace, TRACE, getMarker(), message, throwable); }
    
    //*:== Debug
    @Override public void debug(@Nullable String message) { this.print(logger::debug, DEBUG, getMarker(), message); }
    
    @Override public void debug(@Nullable String message, Object @Nullable ... args) { this.print(logger::debug, DEBUG, getMarker(), message, args); }
    
    @Override public void debug(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::debug, DEBUG, getMarker(), message, throwable); }
    
    //*:== Info
    @Override public void info(@Nullable String message) { this.print(logger::info, INFO, getMarker(), message); }
    
    @Override public void info(@Nullable String message, Object @Nullable ... args) { this.print(logger::info, INFO, getMarker(), message, args); }
    
    @Override public void info(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::info, INFO, getMarker(), message, throwable); }
    
    //*:== Warn
    @Override public void warn(@Nullable String message) { this.print(logger::warn, WARN, getMarker(), message); }
    
    @Override public void warn(@Nullable String message, Object @Nullable ... args) { this.print(logger::warn, WARN, getWarnMarker(), message, args); }
    
    @Override public void warn(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::warn, WARN, getWarnMarker(), message, throwable); }
    
    //*:== Error
    @Override public void error(@Nullable String message) { this.print(logger::error, ERROR, getErrorMarker(), message); }
    
    @Override public void error(@Nullable String message, Object @Nullable ... args) { this.print(logger::error, ERROR, getErrorMarker(), message, args); }
    
    @Override public void error(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::error, ERROR, getErrorMarker(), message, throwable); }
    //endregion
    
    //  region Private helpers
    private static void print(
        @NotNull ITriConsumer<Marker, String, Object[]> consumer,
        @NotNull Predicate<Level> predicate,
        @NotNull Level level,
        @Nullable Marker marker,
        @Nullable String message,
        Object @Nullable ... args
    ) { if(predicate.test(level)) consumer.accept(marker, message, args); }
    
    private static void print(
        @NotNull BiConsumer<Marker, String> consumer,
        @NotNull Predicate<Level> predicate,
        @NotNull Level level,
        @Nullable Marker marker,
        @Nullable String message
    ) { if(predicate.test(level)) consumer.accept(marker, message); }
    
    private static void print(
        @NotNull ITriConsumer<Marker, String, Throwable> consumer,
        @NotNull Predicate<Level> predicate,
        @NotNull Level level,
        @Nullable Marker marker,
        @Nullable String message,
        @Nullable Throwable throwable
    ) { if(predicate.test(level)) consumer.accept(marker, message, throwable); }
    
    private void print(
        @NotNull ITriConsumer<Marker, String, Object[]> consumer,
        @NotNull Level level,
        @Nullable Marker marker,
        @Nullable String message,
        Object @Nullable ... args
    ) { print(consumer, condition, level, marker, message, args); }
    
    private void print(
        @NotNull BiConsumer<Marker, String> consumer,
        @NotNull Level level,
        @Nullable Marker marker,
        @Nullable String message
    ) { print(consumer, condition, level, marker, message); }
    
    private void print(
        @NotNull ITriConsumer<Marker, String, Throwable> consumer,
        @NotNull Level level,
        @Nullable Marker marker,
        @Nullable String message,
        @Nullable Throwable throwable
    ) { print(consumer, condition, level, marker, message, throwable); }
    
    private void pushTempMarker(@NotNull Marker marker)
    {
        requireNonNull(marker, "Param \"marker\" must not be null!");
        @Nullable ArrayDeque<Marker> stack = mutableMarker.get();
        
        if(stack == null)
        {
            stack = new ArrayDeque<>();
            mutableMarker.set(stack);
        }
        
        stack.push(marker);
    }
    
    private void setTempMarker(@NotNull Marker marker)
    {
        requireNonNull(marker, "Param \"marker\" must not be null!");
        @Nullable ArrayDeque<Marker> stack = mutableMarker.get();
        
        if(stack == null)
        {
            stack = new ArrayDeque<>();
            mutableMarker.set(stack);
        }
        
        stack.removeFirst();
        stack.addFirst(marker);
    }
    
    private @Nullable Marker getMarkerBase(@NotNull Supplier<Marker> sequence)
    {
        final @Nullable ArrayDeque<Marker> stack = this.mutableMarker.get();
        final Marker current = (stack != null) ? stack.peek() : null;
        return current != null ? current : sequence.get();
    }
    
    private @Nullable Marker getMarker() { return getMarkerBase(() -> this.defaultMarker); }
    
    private @Nullable Marker getErrorMarker() { return getMarkerBase(() -> this.errorMarker != null ? this.errorMarker : this.defaultMarker); }
    
    private @Nullable Marker getWarnMarker() { return getMarkerBase(() -> this.warnMarker != null ? this.warnMarker : this.defaultMarker); }
    
    private static @NotNull String adaptSuffix(@NotNull String baseName, @NotNull String suffix)
    {
        requireNonNull(baseName, "Param \"baseName\" must not be null!");
        requireNonNull(suffix, "Param \"suffix\" must not be null!");
        
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        
        for(final char c: baseName.toCharArray())
        {
            if(Character.isUpperCase(c))
                hasUppercase = true;
            if(Character.isLowerCase(c))
                hasLowercase = true;
        }
        
        if(hasLowercase && !hasUppercase)
            return baseName + suffix.toLowerCase();
        
        return baseName + suffix.toUpperCase();
    }
    
    private static @NotNull Predicate<Level> leveledCondition(
        @NotNull Level level,
        @NotNull ConditionSituation situation,
        @NotNull BooleanSupplier extra,
        boolean reverse
    )
    {
        requireNonNull(level, "Param \"level\" must not be null!");
        requireNonNull(situation, "Param \"situation\" must not be null!");
        
        return l -> reverse == situation.condition.test(l, level) || extra.getAsBoolean();
    }
    
    @Override public @NotNull @Unmodifiable Map<String, Function<MarkLogger, @Nullable Object>> getFields() { return FIELD_MAP; }
    
    private @NotNull String getNameSafely(@Nullable Marker marker) { return marker == null ? "N/A" : marker.getName(); }
    //endregion
    
    //region SLF4J Integrations
    @Override public @NotNull String getName() { return logger.getName(); }
    
    @Override public boolean isTraceEnabled() { return condition.test(TRACE) && logger.isTraceEnabled(); }
    
    @Override public void trace(String format, Object arg) { this.print(logger::trace, TRACE, getMarker(), format, arg); }
    
    @Override public void trace(String format, Object arg1, Object arg2) { this.print(logger::trace, TRACE, getMarker(), format, arg1, arg2); }
    
    @Override public boolean isTraceEnabled(Marker marker) { return condition.test(TRACE) && logger.isTraceEnabled(marker); }
    
    @Override public void trace(Marker marker, String message) { this.print(logger::trace, TRACE, marker, message); }
    
    @Override public void trace(Marker marker, String format, Object arg) { this.print(logger::trace, TRACE, marker, format, arg); }
    
    @Override public void trace(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::trace, TRACE, marker, format, arg1, arg2); }
    
    @Override public void trace(Marker marker, String format, Object... argArray) { this.print(logger::trace, TRACE, marker, format, argArray); }
    
    @Override public void trace(Marker marker, String message, Throwable throwable) { this.print(logger::trace, TRACE, marker, message, throwable); }
    
    @Override public boolean isDebugEnabled() { return condition.test(DEBUG) && logger.isDebugEnabled(); }
    
    @Override public void debug(String format, Object arg) { this.print(logger::debug, DEBUG, getMarker(), format, arg); }
    
    @Override public void debug(String format, Object arg1, Object arg2) { this.print(logger::debug, DEBUG, getMarker(), format, arg1, arg2); }
    
    @Override public boolean isDebugEnabled(Marker marker) { return condition.test(DEBUG) && logger.isDebugEnabled(marker); }
    
    @Override public void debug(Marker marker, String message) { this.print(logger::debug, DEBUG, marker, message); }
    
    @Override public void debug(Marker marker, String format, Object arg) { this.print(logger::debug, DEBUG, marker, format, arg); }
    
    @Override public void debug(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::debug, DEBUG, marker, format, arg1, arg2); }
    
    @Override public void debug(Marker marker, String format, Object... arguments) { this.print(logger::debug, DEBUG, marker, format, arguments); }
    
    @Override public void debug(Marker marker, String message, Throwable throwable) { this.print(logger::debug, DEBUG, marker, message, throwable); }
    
    @Override public boolean isInfoEnabled() { return condition.test(INFO) && logger.isInfoEnabled(); }
    
    @Override public void info(String format, Object arg) { this.print(logger::info, INFO, getMarker(), format, arg); }
    
    @Override public void info(String format, Object arg1, Object arg2) { this.print(logger::info, INFO, getMarker(), format, arg1, arg2); }
    
    @Override public boolean isInfoEnabled(Marker marker) { return condition.test(INFO) && logger.isInfoEnabled(marker); }
    
    @Override public void info(Marker marker, String message) { this.print(logger::info, INFO, marker, message); }
    
    @Override public void info(Marker marker, String format, Object arg) { this.print(logger::info, INFO, marker, format, arg); }
    
    @Override public void info(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::info, INFO, marker, format, arg1, arg2); }
    
    @Override public void info(Marker marker, String format, Object... arguments) { this.print(logger::info, INFO, marker, format, arguments); }
    
    @Override public void info(Marker marker, String message, Throwable throwable) { this.print(logger::info, INFO, marker, message, throwable); }
    
    @Override public boolean isWarnEnabled() { return condition.test(WARN) && logger.isWarnEnabled(); }
    
    @Override public void warn(String format, Object arg) { this.print(logger::warn, WARN, getWarnMarker(), format, arg); }
    
    @Override public void warn(String format, Object arg1, Object arg2) { this.print(logger::warn, WARN, getWarnMarker(), format, arg1, arg2); }
    
    @Override public boolean isWarnEnabled(Marker marker) { return condition.test(WARN) && logger.isWarnEnabled(marker); }
    
    @Override public void warn(Marker marker, String msg) { this.print(logger::warn, WARN, marker, msg); }
    
    @Override public void warn(Marker marker, String format, Object arg) { this.print(logger::warn, WARN, marker, format, arg); }
    
    @Override public void warn(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::warn, WARN, marker, format, arg1, arg2); }
    
    @Override public void warn(Marker marker, String format, Object... arguments) { this.print(logger::warn, WARN, marker, format, arguments); }
    
    @Override public void warn(Marker marker, String message, Throwable throwable) { this.print(logger::warn, WARN, marker, message, throwable); }
    
    @Override public boolean isErrorEnabled() { return condition.test(ERROR) && logger.isErrorEnabled(); }
    
    @Override public void error(String format, Object arg) { this.print(logger::error, ERROR, getErrorMarker(), format, arg); }
    
    @Override public void error(String format, Object arg1, Object arg2) { this.print(logger::error, ERROR, getErrorMarker(), format, arg1, arg2); }
    
    @Override public boolean isErrorEnabled(Marker marker) { return condition.test(ERROR) && logger.isErrorEnabled(marker); }
    
    @Override public void error(Marker marker, String message) { this.print(logger::error, ERROR, marker, message); }
    
    @Override public void error(Marker marker, String format, Object arg) { this.print(logger::error, ERROR, marker, format, arg); }
    
    @Override public void error(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::error, ERROR, marker, format, arg1, arg2); }
    
    @Override public void error(Marker marker, String format, Object... arguments) { this.print(logger::error, ERROR, marker, format, arguments); }
    
    @Override public void error(Marker marker, String message, Throwable throwable) { this.print(logger::error, ERROR, marker, message, throwable); }
    //endregion
}
