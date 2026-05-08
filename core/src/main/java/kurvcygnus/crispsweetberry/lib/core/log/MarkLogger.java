//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.core.log;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.lib.base.functions.ITriConsumer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.event.Level;
import org.slf4j.spi.LoggingEventBuilder;

import java.util.ArrayDeque;
import java.util.function.*;

import static java.util.Objects.requireNonNull;

/**
 * This is a simple wrapper for SLF4J's <u>{@link Logger}</u>. It reduces the verbosity of passing <u>{@link Marker}</u> to log functions.
 *
 * @author Kurv Cygnus
 * @apiNote We recommend using {@code SCREAMING_SNAKE_CASE} for <u>{@link Marker}</u>, because it is more attractive, and easy to search.<br>
 * <b>This logger uses <u>{@link ThreadLocal}</u>. Do not leak <u>{@link MarkerHandle MarkerHandle}</u> across async boundaries</b>.
 * @since 1.0 Release
 */
public final class MarkLogger implements Logger
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
    //endregion
    
    //  region Constructor & Static Factories
    private MarkLogger(
        @NotNull Logger logger,
        @Nullable Marker defaultMarker,
        @Nullable Marker errorMarker,
        @Nullable Marker warnMarker,
        @NotNull Predicate<Level> condition
    )
    {
        if(logger instanceof MarkLogger)
            throw new IllegalArgumentException("The logger to be wrapped is MarkLogger, this will cause nested wrapping, which is not allowed!");
        
        this.logger = requireNonNull(logger, "Param \"logger\" must not be null!");
        this.defaultMarker = defaultMarker;
        this.errorMarker = errorMarker;
        this.warnMarker = warnMarker;
        this.condition = condition;
    }
    
    /**
     * Produces a standard logger with no default marker.
     *
     * @throws NullPointerException When {@code logger} is {@code null}
     * @apiNote Using <u>{@link LogUtils#getLogger()}</u> is recommended rather than
     * <u>{@link org.slf4j.LoggerFactory#getLogger(String) LoggerFactory#markedLogger()}</u>,
     * since Mojang did some configuration on former one, it is more compatible.
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.marklessLogger(...);
     * }</pre>
     */
    @Contract("_ -> new")
    public static @NotNull MarkLogger marklessLogger(@NotNull Logger logger)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        return new MarkLogger(logger, null, null, null, TRUE);
    }
    
    /**
     * Produces a standard logger, which automatically deals marker.
     *
     * @throws NullPointerException When {@code logger} or {@code marker} is {@code null}
     * @apiNote Using <u>{@link LogUtils#getLogger()}</u> is recommended rather than
     * <u>{@link org.slf4j.LoggerFactory#getLogger(String) LoggerFactory#markedLogger()}</u>,
     * since Mojang did some configuration on former one, it is more compatible.
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.markedLogger(
     *      LogUtils.getLogger(),
     *      MarkerFactory.getMarker("Foo")
     *  );
     * }</pre>
     */
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger markedLogger(@NotNull Logger logger, @NotNull Marker marker)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(marker, "Param \"marker\" must not be null!");
        
        return new MarkLogger(logger, marker, marker, marker, TRUE);
    }
    
    /**
     * Produces a standard logger, which automatically deals marker.
     *
     * @throws NullPointerException When {@code logger} or {@code mark} is {@code null}
     * @apiNote Using <u>{@link LogUtils#getLogger()}</u> is recommended rather than
     * <u>{@link org.slf4j.LoggerFactory#getLogger(String) LoggerFactory#markedLogger()}</u>,
     * since Mojang did some configuration on former one, it is more compatible.
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.markedLogger(
     *      LogUtils.getLogger(),
     *      "Foo"
     *  );
     * }</pre>
     */
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger markedLogger(@NotNull Logger logger, @NotNull String mark)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(mark, "Param \"mark\" must not be null!");
        
        final Marker marker = MarkerFactory.getMarker(mark);
        
        return new MarkLogger(logger, marker, marker, marker, TRUE);
    }
    
    /**
     * Produces a special logger, whose have two extra unique markers with {@code _ERR} and {@code _WARN} suffixes to override
     * <u>{@link #defaultMarker}</u> in corresponded log level functions.
     *
     * @throws NullPointerException When {@code logger} or {@code marker} is {@code null}
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.withMarkerSuffixes(
     *      LogUtils.getLogger(),
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
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger withMarkerSuffixes(@NotNull Logger logger, @NotNull Marker marker)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(marker, "Param \"marker\" must not be null!");
        
        final Marker err = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_ERR"));
        final Marker warn = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_WARN"));
        
        return new MarkLogger(logger, marker, err, warn, TRUE);
    }
    
    /**
     * Produces a special logger, whose have two extra unique markers with {@code _ERR} and {@code _WARN} suffixes to override
     * <u>{@link #defaultMarker}</u> in corresponded log level functions.
     *
     * @throws NullPointerException When {@code logger} or {@code mark} is {@code null}
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.withMarkerSuffixes(
     *      LogUtils.getLogger(),
     *      "Bar"
     *  );
     * }</pre><hr>
     * Produces these markers:
     * <pre>{@code
     *  // foo -> foo_err(For error log funcs)
     *  // BAR -> BAR_WARN(For warn log funcs)
     * }</pre>
     * <i>Current suffix conversion is quite simple, since we don't think it needs to support all text cases.</i>
     */
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger withMarkerSuffixes(@NotNull Logger logger, @NotNull String mark)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(mark, "Param \"mark\" must not be null!");
        
        return withMarkerSuffixes(logger, MarkerFactory.getMarker(mark));
    }
    
    /**
     * Produces a highly configurable logger, which supports adaptive markers suffix<i>(with {@code adaptive} arg's value equaling {@code true})</i>,
     * and the ability to log message, depending on arg {@code condition}.
     */
    @Contract("_, null, true, _ -> fail")
    private static @NotNull MarkLogger configuredLogger(@NotNull Logger logger, @Nullable Marker marker, boolean adaptive, @NotNull Predicate<Level> condition)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(condition, "Param \"condition\" must not be null!");
        
        if(marker == null && adaptive)
            throw new IllegalArgumentException("Creating a MarkLogger instance with null marker and adaptive markers is not allowed!");
        
        if(adaptive)
        {
            requireNonNull(marker, "Param \"marker\" must not be null!");
            
            final Marker err = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_ERR"));
            final Marker warn = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_WARN"));
            
            return new MarkLogger(logger, marker, err, warn, condition);
        }
        
        return new MarkLogger(logger, marker, marker, marker, condition);
    }
    
    /**
     * Produces a highly configurable logger, which supports adaptive markers suffix<i>(with {@code adaptive} arg's value equaling {@code true})</i>,
     * and the ability to log message, depending on arg {@code condition}.
     */
    public static @NotNull MarkLogger configuredLogger(@NotNull Logger logger, @NotNull String mark, @NotNull Predicate<Level> condition, boolean adaptive)
        { return configuredLogger(logger, MarkerFactory.getMarker(mark), adaptive, condition); }
    
    /**
     * Produces a highly configurable logger, which supports the ability to log message, depending on arg {@code condition}.
     */
    public static @NotNull MarkLogger configuredLogger(@NotNull Logger logger, @NotNull Predicate<Level> condition)
        { return configuredLogger(logger, null, false, condition); }
    
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
    public static @NotNull Predicate<Level> allowWhen(@NotNull Level level, @NotNull ConditionSituation situation, BooleanSupplier extra)
        { return leveledCondition(level, situation, extra, false); }
    
    /**
     * Creates a condition that allows logging only when the log level satisfies
     * the specified comparison against the provided reference level.
     *
     * @param level     The reference log level to compare against.
     * @param situation The comparison logic (e.g. <u>{@link ConditionSituation#EQUAL EQUAL}</u>, <u>{@link ConditionSituation#HIGHER HIGHER}</u>,
     *                  <u>{@link ConditionSituation#LOWER LOWER}</u>).
     * @param extra     An additional boolean flag to force-enable the log (OR logic).
     * @return A predicate that returns {@code true} if the log should be performed.
     * @apiNote <span style="color: f84b4b">The value of {@code extra} is <b>static</b></span>, it will be immutable, and won't be reassigned anymore.
     */
    public static @NotNull Predicate<Level> allowWhen(@NotNull Level level, @NotNull ConditionSituation situation, boolean extra)
        { return leveledCondition(level, situation, () -> extra, false); }
    
    /**
     * Creates a condition that rejects logging when the log level satisfies
     * the specified comparison against the provided reference level.
     *
     * @param level     The reference log level to compare against.
     * @param situation The comparison logic (e.g. <u>{@link ConditionSituation#EQUAL EQUAL}</u>, <u>{@link ConditionSituation#HIGHER HIGHER}</u>,
     *                  <u>{@link ConditionSituation#LOWER LOWER}</u>).
     * @param extra     An additional boolean flag to force-enable the log (OR logic).
     * @return A predicate that returns {@code true} if the log should be performed.
     * @apiNote <span style="color: f84b4b">The value of {@code extra} is <b>static</b></span>, it will be immutable, and won't be reassigned anymore.
     */
    public static @NotNull Predicate<Level> denyWhen(@NotNull Level level, @NotNull ConditionSituation situation, boolean extra)
        { return leveledCondition(level, situation, () -> extra, true); }
    
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
    public static @NotNull Predicate<Level> denyWhen(@NotNull Level level, @NotNull ConditionSituation situation, BooleanSupplier extra)
        { return leveledCondition(level, situation, extra, true); }
    //endregion
    
    //  region Scoped Marker Logics
    /**
     * Push a <u>{@link #mutableMarker temporary marker}</u> to <u>{@link MarkLogger}</u>,
     * and will always be used until current key is ended.<br><br>
     * <b>Thus, this will only work correctly and normally with
     * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a></b>.
     *
     * @throws NullPointerException When {@code marker} is {@code null}
     * @implSpec <pre>{@code
     *  // ↓ "ignored" disables unused warnings.
     *  try(var ignored = LOGGER.pushMarker(MarkerFactory.getMarker("Foo")))
     *  {
     *      // All markers of log inside
     *      // will be overridden with "Foo".
     *  }
     *  // Out of key, override no longer exists.
     * }</pre>
     */
    @Contract("_ -> new")
    public @NotNull MarkerHandle pushMarker(@NotNull Marker marker)
    {
        requireNonNull(marker, "Param \"marker\" must not be null!");
        pushTempMarker(marker);
        return new MarkerHandle(this);
    }
    
    /**
     * Push a <u>{@link #mutableMarker temporary marker}</u> to <u>{@link MarkLogger}</u>,
     * and will always be used until <u>{@link MarkLogger}</u>'s lifecycle is ended, which is obviously impossible.<br><br>
     * <b>Thus, this will only work correctly and normally with
     * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a></b>.
     *
     * @throws NullPointerException When {@code mark} is {@code null}
     * @implSpec <pre>{@code
     *  // ↓ "ignored" disables unused warnings.
     *  try(var ignored = LOGGER.pushMarker("Foo"))
     *  {
     *      // All markers of log inside
     *      // will be overridden with "Foo".
     *  }
     *  // Out of key, override no longer exists.
     * }</pre>
     */
    @Contract("_ -> new")
    public @NotNull MarkerHandle pushMarker(@NotNull String mark)
    {
        requireNonNull(mark, "Param \"mark\" must not be null!");
        return this.pushMarker(MarkerFactory.getMarker(mark));
    }
    
    /**
     * A simple class to implement <u>{@link AutoCloseable}</u> for <u>{@link MarkLogger}</u>, making it usable in
     * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a>.
     *
     * @author Kurv Cygnus
     * @see #pushMarker(Marker) Usage
     * @since 1.0 Release
     */
    public static final class MarkerHandle implements AutoCloseable
    {
        private final MarkLogger logger;
        private boolean closed = false;
        
        private MarkerHandle(@NotNull MarkLogger logger) { this.logger = logger; }
        
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
         */
        public void changeMarker(@NotNull Marker marker)
        {
            requireNonNull(marker, "Param \"marker\" must not be null!");
            logger.setTempMarker(marker);
        }
        
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
         */
        public void changeMarker(@NotNull String mark)
        {
            requireNonNull(mark, "Param \"mark\" must not be null!");
            logger.setTempMarker(MarkerFactory.getMarker(mark));
        }
        
        /**
         * Closes this handle and removes the associated <u>{@link Marker}</u> from the <u>{@link ThreadLocal}</u> stack.
         *
         * @apiNote This method is idempotent. Calling it multiple times will not pop more than one marker.
         * <b>Failure to close this handle (especially in async or pooled thread environments) will lead to
         * <i>Marker Pollution</i></b>.
         */
        @Override public void close()
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
    /**
     * A fluent style API for conditional log.<br>
     * It makes the log method called after it displayed only when this method's param, {@code condition} is true.
     *
     * @implNote Param {@code condition} will be wrapped in a <u>{@link Supplier}</u> for optimization.
     * @implSpec <pre>{@code
     *  LOGGER.when(foo).info("Bar");
     *  // "Bar" will only be printed when foo is true.
     * }</pre>
     */
    @Contract("_ -> new")
    public @NotNull ConditionalLogCommons when(boolean condition) { return new ConditionalLogCommons(this, condition); }
    
    //*:== Trace
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void trace(@Nullable String message) { this.print(logger::trace, Level.TRACE, getMarker(), message); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void trace(@Nullable String message, Object @Nullable ... args) { this.print(logger::trace, Level.TRACE, getMarker(), message, args); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void trace(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::trace, Level.TRACE, getMarker(), message, throwable); }
    
    //*:== Debug
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void debug(@Nullable String message) { this.print(logger::debug, Level.DEBUG, getMarker(), message); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void debug(@Nullable String message, Object @Nullable ... args) { this.print(logger::debug, Level.DEBUG, getMarker(), message, args); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void debug(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::debug, Level.DEBUG, getMarker(), message, throwable); }
    
    //*:== Info
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void info(@Nullable String message) { this.print(logger::info, Level.INFO, getMarker(), message); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void info(@Nullable String message, Object @Nullable ... args) { this.print(logger::info, Level.INFO, getMarker(), message, args); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void info(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::info, Level.INFO, getMarker(), message, throwable); }
    
    //*:== Warn
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void warn(@Nullable String message) { this.print(logger::warn, Level.WARN, getMarker(), message); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void warn(@Nullable String message, Object @Nullable ... args) { this.print(logger::warn, Level.WARN, getWarnMarker(), message, args); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void warn(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::warn, Level.WARN, getWarnMarker(), message, throwable); }
    
    //*:== Error
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void error(@Nullable String message) { this.print(logger::error, Level.ERROR, getMarker(), message); }
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void error(@Nullable String message, Object @Nullable ... args) { this.print(logger::error, Level.ERROR, getErrorMarker(), message, args); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void error(@Nullable String message, @Nullable Throwable throwable) { this.print(logger::error, Level.ERROR, getErrorMarker(), message, throwable); }
    //endregion
    
    //  region Log Common Extras
    public static final class ConditionalLogCommons
    {
        private final Logger logger;
        private final @Nullable Marker defaultMarker;
        private final @Nullable Marker errorMarker;
        private final @Nullable Marker warnMarker;
        private final @Nullable Marker currentMutableMarker;
        private final Predicate<Level> condition;
        
        private ConditionalLogCommons(@NotNull MarkLogger logger, boolean condition)
        {
            requireNonNull(logger, "Param \"logger\" must not be null!");
            this.logger = logger.logger;
            this.defaultMarker = logger.defaultMarker;
            this.errorMarker = logger.errorMarker;
            this.warnMarker = logger.warnMarker;
            this.currentMutableMarker = logger.mutableMarker.get() != null ? logger.mutableMarker.get().peek() : null;
            this.condition = l -> logger.condition.test(l) && condition;
        }
        
        //  region
        //*:== Trace
        public void trace(@Nullable String message) { this.print(logger::trace, Level.TRACE, getMarker(), message); }
        
        public void trace(@Nullable String message, Object @Nullable ... args) { this.print(logger::trace, Level.TRACE, getMarker(), message, args); }
        
        public void trace(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            this.print(logger::trace, Level.TRACE, getMarker(), messageSupplier.get());
        }
        
        public void trace(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::trace, Level.TRACE, getMarker(), message, argsSupplier.get());
        }
        
        public void trace(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::trace, Level.TRACE, getMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Debug
        public void debug(@Nullable String message) { this.print(logger::debug, Level.DEBUG, getMarker(), message); }
        
        public void debug(@Nullable String message, Object @Nullable ... args) { this.print(logger::debug, Level.DEBUG, getMarker(), message, args); }
        
        public void debug(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            this.print(logger::debug, Level.DEBUG, getMarker(), messageSupplier.get());
        }
        
        public void debug(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::debug, Level.DEBUG, getMarker(), message, argsSupplier.get());
        }
        
        public void debug(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::debug, Level.DEBUG, getMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Info
        public void info(@Nullable String message) { this.print(logger::info, Level.INFO, getMarker(), message); }
        
        public void info(@Nullable String message, Object @Nullable ... args) { this.print(logger::info, Level.INFO, getMarker(), message, args); }
        
        public void info(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            this.print(logger::info, Level.INFO, getMarker(), messageSupplier.get());
        }
        
        public void info(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::info, Level.INFO, getMarker(), message, argsSupplier.get());
        }
        
        public void info(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::info, Level.INFO, getMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Warn
        public void warn(@Nullable String message) { this.print(logger::warn, Level.WARN, getWarnMarker(), message); }
        
        public void warn(@Nullable String message, Object @Nullable ... args) { this.print(logger::warn, Level.WARN, getWarnMarker(), message, args); }
        
        public void warn(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            this.print(logger::warn, Level.WARN, getWarnMarker(), messageSupplier.get());
        }
        
        public void warn(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::warn, Level.WARN, getWarnMarker(), message, argsSupplier.get());
        }
        
        public void warn(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::warn, Level.WARN, getWarnMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Error
        public void error(@Nullable String message) { this.print(logger::error, Level.ERROR, getErrorMarker(), message); }
        
        public void error(@Nullable String message, Object @Nullable ... args) { this.print(logger::error, Level.ERROR, getErrorMarker(), message, args); }
        
        public void error(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            this.print(logger::error, Level.ERROR, getErrorMarker(), messageSupplier.get());
        }
        
        public void error(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::error, Level.ERROR, getErrorMarker(), message, argsSupplier.get());
        }
        
        public void error(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            this.print(logger::error, Level.ERROR, getErrorMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Private Helpers
        private @Nullable Marker getMarkerBase(@Nullable Marker content) { return currentMutableMarker != null ? currentMutableMarker : content; }
        
        private @Nullable Marker getMarker() { return getMarkerBase(defaultMarker); }
        
        private @Nullable Marker getErrorMarker() { return getMarkerBase(errorMarker); }
        
        private @Nullable Marker getWarnMarker() { return getMarkerBase(warnMarker); }
        
        private void print(
            @NotNull ITriConsumer<Marker, String, Object[]> consumer,
            @NotNull Level level,
            @Nullable Marker marker,
            @Nullable String message,
            Object @Nullable ... args
        ) { MarkLogger.print(consumer, condition, level, marker, message, args); }
        
        private void print(
            @NotNull BiConsumer<Marker, String> consumer,
            @NotNull Level level,
            @Nullable Marker marker,
            @Nullable String message
        ) { MarkLogger.print(consumer, condition, level, marker, message); }
        
        private void print(
            @NotNull ITriConsumer<Marker, String, Throwable> consumer,
            @NotNull Level level,
            @Nullable Marker marker,
            @Nullable String message,
            @Nullable Throwable throwable
        ) { MarkLogger.print(consumer, condition, level, marker, message, throwable); }
        //endregion
    }
    //endregion
    
    //  region Private helpers
    private static void print(
        @NotNull ITriConsumer<Marker, String, Object[]> consumer,
        @NotNull Predicate<Level> predicate,
        @NotNull Level level,
        @Nullable Marker marker,
        @Nullable String message,
        Object @Nullable ... args
    )
    {
        if(predicate.test(level))
            consumer.accept(marker, message, args);
    }
    
    private static void print(
        @NotNull BiConsumer<Marker, String> consumer,
        @NotNull Predicate<Level> predicate,
        @NotNull Level level,
        @Nullable Marker marker,
        @Nullable String message
    )
    {
        if(predicate.test(level))
            consumer.accept(marker, message);
    }
    
    private static void print(
        @NotNull ITriConsumer<Marker, String, Throwable> consumer,
        @NotNull Predicate<Level> predicate,
        @NotNull Level level,
        @Nullable Marker marker,
        @Nullable String message,
        @Nullable Throwable throwable
    )
    {
        if(predicate.test(level))
            consumer.accept(marker, message, throwable);
    }
    
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
    
    public enum ConditionSituation
    {
        EQUAL((fieldLevel, argLevel) -> fieldLevel == argLevel),
        HIGHER((fieldLevel, argLevel) -> fieldLevel.toInt() >= argLevel.toInt()),
        LOWER((fieldLevel, argLevel) -> fieldLevel.toInt() <= argLevel.toInt());
        
        private final BiPredicate<Level, Level> condition;
        
        ConditionSituation(@NotNull BiPredicate<Level, Level> condition) { this.condition = condition; }
    }
    
    @Override public @NotNull String toString()
    {
        return """
            MarkLogger
            {
                Logger Name: %s
                Default Marker Name: %s
                Warn Marker Name: %s
                Error Marker Name: %s
                Marker Stacks: %s
                Current Condition's value for each level:
                {
                    Trace: %s
                    Debug: %s
                    Info: %s
                    Warn: %s
                    Error: %s
                }
            }
            """.
            formatted(
                logger.getName(),
                getNameSafely(getMarker()),
                getNameSafely(getWarnMarker()),
                getNameSafely(getErrorMarker()),
                mutableMarker.get() == null ? "N/A" : mutableMarker.get().toString(),
                condition.test(Level.TRACE),
                condition.test(Level.DEBUG),
                condition.test(Level.INFO),
                condition.test(Level.WARN),
                condition.test(Level.ERROR)
            );
    }
    
    private @NotNull String getNameSafely(@Nullable Marker marker) { return marker == null ? "N/A" : marker.getName(); }
    //endregion
    
    //region SLF4J Integrations
    /**
     * {@inheritDoc}
     */
    @Override public @NotNull String getName() { return logger.getName(); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isTraceEnabled() { return logger.isTraceEnabled(); }
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void trace(String format, Object arg) { this.print(logger::trace, Level.TRACE, getMarker(), format, arg); }
    
    /**
     * {@inheritDoc}
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void trace(String format, Object arg1, Object arg2) { this.print(logger::trace, Level.TRACE, getMarker(), format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Deprecated @Override public boolean isTraceEnabled(Marker marker) { return logger.isTraceEnabled(marker); }
    
    /**
     * {@inheritDoc}
     */
    @Deprecated @Override public void trace(Marker marker, String msg) { this.print(logger::trace, Level.TRACE, marker, msg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void trace(Marker marker, String format, Object arg) { this.print(logger::trace, Level.TRACE, marker, format, arg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void trace(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::trace, Level.TRACE, marker, format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void trace(Marker marker, String format, Object... argArray) { this.print(logger::trace, Level.TRACE, marker, format, argArray); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void trace(Marker marker, String msg, Throwable t) { this.print(logger::trace, Level.TRACE, marker, msg, t); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isDebugEnabled() { return logger.isDebugEnabled(); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void debug(String format, Object arg) { this.print(logger::debug, Level.DEBUG, getMarker(), format, arg); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void debug(String format, Object arg1, Object arg2) { this.print(logger::debug, Level.DEBUG, getMarker(), format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isDebugEnabled(Marker marker) { return logger.isDebugEnabled(marker); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void debug(Marker marker, String msg) { this.print(logger::debug, Level.DEBUG, marker, msg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void debug(Marker marker, String format, Object arg) { this.print(logger::debug, Level.DEBUG, marker, format, arg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void debug(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::debug, Level.DEBUG, marker, format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void debug(Marker marker, String format, Object... arguments) { this.print(logger::debug, Level.DEBUG, marker, format, arguments); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void debug(Marker marker, String msg, Throwable t) { this.print(logger::debug, Level.DEBUG, marker, msg, t); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isInfoEnabled() { return logger.isInfoEnabled(); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void info(String format, Object arg) { this.print(logger::info, Level.INFO, getMarker(), format, arg); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void info(String format, Object arg1, Object arg2) { this.print(logger::info, Level.INFO, getMarker(), format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isInfoEnabled(Marker marker) { return logger.isInfoEnabled(marker); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void info(Marker marker, String msg) { this.print(logger::info, Level.INFO, marker, msg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void info(Marker marker, String format, Object arg) { this.print(logger::info, Level.INFO, marker, format, arg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void info(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::info, Level.INFO, marker, format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void info(Marker marker, String format, Object... arguments) { this.print(logger::info, Level.INFO, marker, format, arguments); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void info(Marker marker, String msg, Throwable t) { this.print(logger::info, Level.INFO, marker, msg, t); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isWarnEnabled() { return logger.isWarnEnabled(); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void warn(String format, Object arg) { this.print(logger::warn, Level.WARN, getMarker(), format, arg); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void warn(String format, Object arg1, Object arg2) { this.print(logger::warn, Level.WARN, getMarker(), format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isWarnEnabled(Marker marker) { return logger.isWarnEnabled(marker); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void warn(Marker marker, String msg) { this.print(logger::warn, Level.WARN, marker, msg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void warn(Marker marker, String format, Object arg) { this.print(logger::warn, Level.WARN, marker, format, arg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void warn(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::warn, Level.WARN, marker, format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void warn(Marker marker, String format, Object... arguments) { this.print(logger::warn, Level.WARN, marker, format, arguments); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void warn(Marker marker, String msg, Throwable t) { this.print(logger::warn, Level.WARN, marker, msg, t); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isErrorEnabled() { return logger.isErrorEnabled(); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void error(String format, Object arg) { this.print(logger::error, Level.ERROR, getMarker(), format, arg); }
    
    /**
     * {@inheritDoc}
     *
     * @apiNote <span style="color: 95cc6d">This log method will take <u>{@link MarkLogger}</u>'s <u>{@link Marker}</u> usage rule.</span>
     */
    @Override public void error(String format, Object arg1, Object arg2) { this.print(logger::error, Level.ERROR, getMarker(), format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isErrorEnabled(Marker marker) { return logger.isInfoEnabled(marker); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void error(Marker marker, String msg) { this.print(logger::error, Level.ERROR, marker, msg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void error(Marker marker, String format, Object arg) { this.print(logger::error, Level.ERROR, marker, format, arg); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void error(Marker marker, String format, Object arg1, Object arg2) { this.print(logger::error, Level.ERROR, marker, format, arg1, arg2); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void error(Marker marker, String format, Object... arguments) { this.print(logger::error, Level.ERROR, marker, format, arguments); }
    
    /**
     * {@inheritDoc}
     */
    @Override public void error(Marker marker, String msg, Throwable t) { this.print(logger::error, Level.ERROR, marker, msg, t); }
    
    /**
     * {@inheritDoc}
     */
    @Override public LoggingEventBuilder makeLoggingEventBuilder(Level level) { return Logger.super.makeLoggingEventBuilder(level); }
    
    /**
     * {@inheritDoc}
     */
    @Override public LoggingEventBuilder atLevel(Level level) { return Logger.super.atLevel(level); }
    
    /**
     * {@inheritDoc}
     */
    @Override public boolean isEnabledForLevel(Level level) { return Logger.super.isEnabledForLevel(level); }
    
    /**
     * {@inheritDoc}
     */
    @Override public LoggingEventBuilder atTrace() { return Logger.super.atTrace(); }
    
    /**
     * {@inheritDoc}
     */
    @Override public LoggingEventBuilder atDebug() { return Logger.super.atDebug(); }
    
    /**
     * {@inheritDoc}
     */
    @Override public LoggingEventBuilder atInfo() { return Logger.super.atInfo(); }
    
    /**
     * {@inheritDoc}
     */
    @Override public LoggingEventBuilder atWarn() { return Logger.super.atWarn(); }
    
    /**
     * {@inheritDoc}
     */
    @Override public LoggingEventBuilder atError() { return Logger.super.atError(); }
    //endregion
}