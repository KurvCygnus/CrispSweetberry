package kurvcygnus.crispsweetberry.utils.log;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.ArrayDeque;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * This is a simple wrapper for SLF4J's <u>{@link Logger}</u>. It reduces the verbosity of passing <u>{@link Marker}</u> to log functions.
 * @apiNote We recommend using {@code SCREAMING_SNAKE_CASE} for <u>{@link Marker}</u>, because it is more attractive, and easy to search.<br>
 * <b>This logger uses <u>{@link ThreadLocal}</u>. Do not leak <u>{@link MarkerHandle MarkerHandle}</u> across async boundaries</b>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@SuppressWarnings("unused")
public final class MarkLogger
{
    //  region
    //*:=== Fields
    /**
     * The core of this wrapper. All log functions are actually executed by this. 
     */
    private final @NotNull Logger logger;
    
    /**
     * The <u>{@link Marker}</u> used for logging. At non-error and non-warn cases, and with no overrides, it 
     * is the marker that will be used for display.
     */
    private final @Nullable Marker defaultMarker;
    
    /**
     * The <u>{@link Marker}</u> exclusively used for error level logging. With no overrides, it 
     * is the marker that will be used for display.
     */
    private final @Nullable Marker errorMarker;
    
    /**
     * The <u>{@link Marker}</u> exclusively used for warn level logging. With no overrides, it
     * is the marker that will be used for display.
     */
    private final @Nullable Marker warnMarker;
    
    /**
     * The marker collections based on <u>{@link java.util.Stack Stack}</u>(or more precisely, <u>{@link ArrayDeque}</u>), 
     * and <u>{@link ThreadLocal}</u>(Out of prevent context pollution, and implement inexplicit context passing).<br>
     * As long as this deque has any <u>{@link Marker}</u>s, both three markers above
     * (<u>{@link #defaultMarker}</u>, <u>{@link #errorMarker}</u> and <u>{@link #warnMarker}</u>) will all be overridden.<br>
     * 
     * @apiNote Usually, {@code mutableMarker} will only exists in a limited scope with <u>{@link #pushMarker(String)}</u> or <u>{@link #pushMarker(Marker)}</u>, 
     * which is recommended to be used with 
     * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a></b>, 
     * unexpected situations will only happen in neither directly use <u>{@link #pushMarker(String)}</u> or <u>{@link #pushMarker(Marker)}</u>, 
     * nor using {@code Reflection}, <b>both two situations are not recommended, or supposed to be happened</b>.
     * <br>
     * <br>
     * As mentioned above, do not leak <u>{@link MarkerHandle MarkerHandle}</u> across async boundaries</b>.
     */
    private final @NotNull ThreadLocal<ArrayDeque<Marker>> mutableMarker = new ThreadLocal<>();
    //endregion
    
    //  region
    //*:=== Constructor & Static Factories
    private MarkLogger(@NotNull Logger logger, @Nullable Marker defaultMarker, @Nullable Marker errorMarker, @Nullable Marker warnMarker)
    {
        this.logger = requireNonNull(logger, "Param \"logger\" must not be null!");
        this.defaultMarker = defaultMarker;
        this.errorMarker = errorMarker;
        this.warnMarker = warnMarker;
    }
    
    /**
     * Produces a standard logger with no default marker.
     *
     * @apiNote Using <u>{@link LogUtils#getLogger()}</u> is recommended rather than
     * <u>{@link org.slf4j.LoggerFactory#getLogger(String) LoggerFactory#markedLogger()}</u>,
     * since Mojang did some configuration on former one, it is more compatible.
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.marklessLogger(...);
     * }</pre>
     * @throws NullPointerException When {@code logger} is {@code null}
     */
    @Contract("_ -> new")
    public static @NotNull MarkLogger marklessLogger(@NotNull Logger logger)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        return new MarkLogger(logger, null, null, null);
    }
    
    /**
     * Produces a standard logger, which automatically deals marker.
     * @apiNote Using <u>{@link LogUtils#getLogger()}</u> is recommended rather than 
     * <u>{@link org.slf4j.LoggerFactory#getLogger(String) LoggerFactory#markedLogger()}</u>, 
     * since Mojang did some configuration on former one, it is more compatible.
     * @implSpec <pre>{@code 
     *  private static final MarkLogger LOGGER = MarkLogger.markedLogger(
     *      LogUtils.getLogger(),
     *      MarkerFactory.getMarker("Foo")
     *  );
     * }</pre>
     * @throws NullPointerException When {@code logger} or {@code marker} is {@code null}
     */
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger markedLogger(@NotNull Logger logger, @NotNull Marker marker)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(marker, "Param \"marker\" must not be null!");
        
        return new MarkLogger(logger, marker, marker, marker);
    }
    
    /**
     * Produces a standard logger, which automatically deals marker.
     *
     * @apiNote Using <u>{@link LogUtils#getLogger()}</u> is recommended rather than
     * <u>{@link org.slf4j.LoggerFactory#getLogger(String) LoggerFactory#markedLogger()}</u>,
     * since Mojang did some configuration on former one, it is more compatible.
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.markedLogger(
     *      LogUtils.getLogger(),
     *      "Foo"
     *  );
     * }</pre>
     * @throws NullPointerException When {@code logger} or {@code mark} is {@code null}
     */
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger markedLogger(@NotNull Logger logger, @NotNull String mark)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(mark, "Param \"mark\" must not be null!");
        
        final Marker marker = MarkerFactory.getMarker(mark);
        
        return new MarkLogger(logger, marker, marker, marker);
    }
    
    /**
     * Produces a special logger, whose have two extra unique markers with {@code _ERR} and {@code _WARN} suffixes to override 
     * <u>{@link #defaultMarker}</u> in corresponded log level functions.
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.withMarkerSuffixes(
     *      LogUtils.getLogger(),
     *      MarkerFactory.getMarker("Bar")
     *  );
     * }</pre>
     * Produces these markers:
     * <pre>{@code 
     *  //foo -> foo_err(For error log funcs)
     *  //BAR -> BAR_WARN(For warn log funcs)
     * }</pre>
     * <i>Current suffix conversion is quite simple, since we don't think it needs to support all text cases.</i>
     * @throws NullPointerException When {@code logger} or {@code marker} is {@code null}
     */
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger withMarkerSuffixes(@NotNull Logger logger, @NotNull Marker marker)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(marker, "Param \"marker\" must not be null!");
        
        final Marker err = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_ERR"));
        final Marker expr = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_WARN"));
        
        return new MarkLogger(logger, marker, err, expr);
    }
    
    /**
     * Produces a special logger, whose have two extra unique markers with {@code _ERR} and {@code _WARN} suffixes to override
     * <u>{@link #defaultMarker}</u> in corresponded log level functions.
     *
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.withMarkerSuffixes(
     *      LogUtils.getLogger(),
     *      "Bar"
     *  );
     * }</pre>
     * Produces these markers:
     * <pre>{@code
     *  //foo -> foo_err(For error log funcs)
     *  //BAR -> BAR_WARN(For warn log funcs)
     * }</pre>
     * <i>Current suffix conversion is quite simple, since we don't think it needs to support all text cases.</i>
     * @throws NullPointerException When {@code logger} or {@code mark} is {@code null}
     */
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger withMarkerSuffixes(@NotNull Logger logger, @NotNull String mark)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(mark, "Param \"mark\" must not be null!");
        
        final Marker marker = MarkerFactory.getMarker(mark);
        final Marker err = MarkerFactory.getMarker(adaptSuffix(mark, "_ERR"));
        final Marker expr = MarkerFactory.getMarker(adaptSuffix(mark, "_WARN"));
        
        return new MarkLogger(logger, marker, err, expr);
    }
    //endregion
    
    //  region
    //*:=== Scoped Marker Logics
    /**
     * Push a <u>{@link #mutableMarker temporary marker}</u> to <u>{@link MarkLogger}</u>, 
     * and will always be used until current scope is ended.<br><br>
     * <b>Thus, this will only work correctly and normally with 
     * <a href="https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html"><u>Try-with-resources</u></a></b>.
     * @implSpec <pre>{@code 
     *  // ↓ "ignored" disables unused warnings.
     *  try(var ignored = LOGGER.pushMarker(MarkerFactory.getMarker("Foo")))
     *  {
     *      // All markers of log inside
     *      // will be overridden with "Foo".
     *  }
     *  // Out of scope, override no longer exists.
     * }</pre>
     * @throws NullPointerException When {@code marker} is {@code null}
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
     * @implSpec <pre>{@code
     *  // ↓ "ignored" disables unused warnings.
     *  try(var ignored = LOGGER.pushMarker("Foo"))
     *  {
     *      // All markers of log inside
     *      // will be overridden with "Foo".
     *  }
     *  // Out of scope, override no longer exists.
     * }</pre>
     * @throws NullPointerException When {@code mark} is {@code null}
     */
    @Contract("_ -> new")
    public @NotNull MarkerHandle pushMarker(@NotNull String mark)
    {
        requireNonNull(mark, "Param \"mark\" must not be null!");
        pushTempMarker(MarkerFactory.getMarker(mark));
        return new MarkerHandle(this);
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
         * Changes the temporary marker that overrides the default one.
         * @apiNote This should be used in the scope of 
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
         * @throws NullPointerException When {@code marker} is {@code null}
         */
        public void changeMarker(@NotNull Marker marker)
        {
            requireNonNull(marker, "Param \"marker\" must not be null!");
            logger.setTempMarker(marker);
        }
        
        /**
         * Changes the temporary marker that overrides the default one.
         *
         * @apiNote This should be used in the scope of
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
         * @throws NullPointerException When {@code mark} is {@code null}
         */
        public void changeMarker(@NotNull String mark) 
        {
            requireNonNull(mark, "Param \"mark\" must not be null!");
            logger.setTempMarker(MarkerFactory.getMarker(mark));
        }
        
        /**
         * Closes this handle and removes the associated <u>{@link Marker}</u> from the <u>{@link ThreadLocal}</u> stack.
         * @apiNote This method is idempotent. Calling it multiple times will not pop more than one marker.
         * <b>Failure to close this handle (especially in async or pooled thread environments) will lead to
         * <i>Marker Pollution</i></b>.
         */
        @Override
        public void close()
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
    
    //  region
    //*:=== Log Print Commons
    /**
     * A fluent style API for conditional log.<br>
     * It makes the log method called after it displayed only when this method's param, {@code condition} is true.
     * @implNote Param {@code condition} will be wrapped in a <u>{@link Supplier}</u> for optimization.
     * @implSpec <pre>{@code 
     *  LOGGER.when(foo).info("Bar");
     *  // "Bar" will only be printed when foo is true.
     * }</pre>
     */
    @Contract("_ -> new")
    public @NotNull MarkLogger.ConditionalLogCommons when(boolean condition) { return new ConditionalLogCommons(this, condition); }
    
    //*:== Trace
    public void trace(@Nullable String message) { logger.trace(getMarker(), message); }
    
    public void trace(@Nullable String message, Object @Nullable ... args) { logger.trace(getMarker(), message, args); }
    
    public void trace(@Nullable String message, @Nullable Throwable throwable) { logger.trace(getMarker(), message, throwable); }
    
    //*:== Debug
    public void debug(@Nullable String message) { logger.debug(getMarker(), message); }
    
    public void debug(@Nullable String message, Object @Nullable ... args) { logger.debug(getMarker(), message, args); }
    
    public void debug(@Nullable String message, @Nullable Throwable throwable) { logger.debug(getMarker(), message, throwable); }
    
    //*:== Info
    public void info(@Nullable String message) { logger.info(getMarker(), message); }
    
    public void info(@Nullable String message, Object @Nullable ... args) { logger.info(getMarker(), message, args); }
    
    public void info(@Nullable String message, @Nullable Throwable throwable) { logger.info(getMarker(), message, throwable); }
    
    //*:== Warn
    public void warn(@Nullable String message) { logger.warn(getWarnMarker(), message); }
    
    public void warn(@Nullable String message, Object @Nullable ... args) { logger.warn(getWarnMarker(), message, args); }
    
    public void warn(@Nullable String message, @Nullable Throwable throwable) { logger.warn(getWarnMarker(), message, throwable); }

    //*:== Error
    public void error(@Nullable String message) { logger.error(getErrorMarker(), message); }
    
    public void error(@Nullable String message, Object @Nullable ... args) { logger.error(getErrorMarker(), message, args); }
    
    public void error(@Nullable String message, @Nullable Throwable throwable) { logger.error(getErrorMarker(), message, throwable); }
    //endregion
    
    //  region
    //*:=== Log Common Extras
    public static final class ConditionalLogCommons
    {
        private final Logger logger;
        private final @Nullable Marker defaultMarker;
        private final @Nullable Marker errorMarker;
        private final @Nullable Marker warnMarker;
        private final @Nullable Marker currentMutableMarker;
        private final Supplier<Boolean> condition;
        
        private ConditionalLogCommons(@NotNull MarkLogger logger, boolean condition)
        {
            requireNonNull(logger, "Param \"logger\" must not be null!");
            this.logger = logger.logger;
            this.defaultMarker = logger.defaultMarker;
            this.errorMarker = logger.errorMarker;
            this.warnMarker = logger.warnMarker;
            this.currentMutableMarker = logger.mutableMarker.get() != null ? logger.mutableMarker.get().peek() : null;
            this.condition = () -> condition;
        }
        
        //  region
        //*:== Trace
        public void trace(@Nullable String message)
        {
            if(condition.get())
                logger.trace(getMarker(), message);
        }
        
        public void trace(@Nullable String message, Object @Nullable ... args)
        {
            if(condition.get())
                logger.trace(getMarker(), message, args);
        }
        
        public void trace(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            if(condition.get())
                logger.trace(getMarker(), messageSupplier.get());
        }
        
        public void trace(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.trace(getMarker(), message, argsSupplier.get());
        }
        
        public void trace(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.trace(getMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Debug
        public void debug(@Nullable String message)
        {
            if(condition.get())
                logger.debug(getMarker(), message);
        }
        
        public void debug(@Nullable String message, Object @Nullable ... args)
        {
            if(condition.get())
                logger.debug(getMarker(), message, args);
        }
        
        public void debug(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            if(condition.get())
                logger.debug(getMarker(), messageSupplier.get());
        }
        
        public void debug(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.debug(getMarker(), message, argsSupplier.get());
        }
        
        public void debug(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.debug(getMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Info
        public void info(@Nullable String message)
        {
            if(condition.get())
                logger.info(getMarker(), message);
        }
        
        public void info(@Nullable String message, Object @Nullable ... args)
        {
            if(condition.get())
                logger.info(getMarker(), message, args);
        }
        
        public void info(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            if(condition.get())
                logger.info(getMarker(), messageSupplier.get());
        }
        
        public void info(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.info(getMarker(), message, argsSupplier.get());
        }
        
        public void info(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.info(getMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Warn
        public void warn(@Nullable String message)
        {
            if(condition.get())
                logger.warn(getWarnMarker(), message);
        }
        
        public void warn(@Nullable String message, Object @Nullable ... args)
        {
            if(condition.get())
                logger.warn(getWarnMarker(), message, args);
        }
        
        public void warn(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            if(condition.get())
                logger.warn(getWarnMarker(), messageSupplier.get());
        }
        
        public void warn(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.warn(getWarnMarker(), message, argsSupplier.get());
        }
        
        public void warn(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.warn(getWarnMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Error
        public void error(@Nullable String message)
        {
            if(condition.get())
                logger.error(getErrorMarker(), message);
        }
        
        public void error(@Nullable String message, Object @Nullable ... args)
        {
            if(condition.get())
                logger.error(getErrorMarker(), message, args);
        }
        
        public void error(@NotNull Supplier<String> messageSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            if(condition.get())
                logger.error(getErrorMarker(), messageSupplier.get());
        }
        
        public void error(@Nullable String message, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.error(getErrorMarker(), message, argsSupplier.get());
        }
        
        public void error(@NotNull Supplier<String> messageSupplier, @NotNull Supplier<Object[]> argsSupplier)
        {
            requireNonNull(messageSupplier, "Param \"messageSupplier\" must not be null!");
            requireNonNull(argsSupplier, "Param \"argsSupplier\" must not be null!");
            
            if(condition.get())
                logger.error(getErrorMarker(), messageSupplier.get(), argsSupplier.get());
        }
        //endregion
        
        //  region
        //*:== Private Helpers
        private @Nullable Marker getMarker() { return currentMutableMarker != null ? currentMutableMarker : defaultMarker; }
        
        private @Nullable Marker getErrorMarker() { return currentMutableMarker != null ? currentMutableMarker : errorMarker; }
        
        private @Nullable Marker getWarnMarker() { return currentMutableMarker != null ? currentMutableMarker : warnMarker; }
        //endregion
    }
    //endregion
    
    //  region
    //*:=== Private helpers
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
        stack.removeLast();
        stack.addLast(marker);
    }
    
    private @Nullable Marker getMarker()
    {
        final @Nullable ArrayDeque<Marker> stack = this.mutableMarker.get();
        final Marker current = (stack != null) ? stack.peek() : null;
        return current != null ? current : this.defaultMarker;
    }
    
    private @Nullable Marker getErrorMarker()
    {
        final @Nullable ArrayDeque<Marker> stack = this.mutableMarker.get();
        final Marker current = (stack != null) ? stack.peek() : null;
        return current != null ? current : (this.errorMarker != null ? this.errorMarker : this.defaultMarker);
    }
    
    private @Nullable Marker getWarnMarker()
    {
        final @Nullable ArrayDeque<Marker> stack = this.mutableMarker.get();
        final Marker current = (stack != null) ? stack.peek() : null;
        return current != null ? current : (this.warnMarker != null ? this.warnMarker : this.defaultMarker);
    }
    
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
    //endregion
}