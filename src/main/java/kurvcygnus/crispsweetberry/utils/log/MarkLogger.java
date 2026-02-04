package kurvcygnus.crispsweetberry.utils.log;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import static java.util.Objects.requireNonNull;

/**
 * This is a simple wrapper for SLF4J's <u>{@link Logger}</u>. It reduces the verbosity of passing <u>{@link Marker}</u> to log functions.
 * @apiNote We recommend using {@code SCREAMING_SNAKE_CASE} for <u>{@link Marker}</u>, because it is more attractive, and easy to search.<br>
 * <b>This logger uses <u>{@link ThreadLocal}</u>. Do not leak MarkerHandle across async boundaries</b>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@SuppressWarnings("unused")
public final class MarkLogger
{
    private final Logger logger;
    private final @Nullable Marker defaultMarker;
    private final @Nullable Marker errorMarker;
    private final @Nullable Marker warnMarker;
    private final ThreadLocal<Marker> mutableMarker = ThreadLocal.withInitial(() -> null);
    
    private MarkLogger(@NotNull Logger logger, @Nullable Marker marker, @Nullable Marker errorMarker, @Nullable Marker warnMarker)
    {
        this.logger = requireNonNull(logger, "Param \"logger\" must not be null!");
        this.defaultMarker = marker;
        this.errorMarker = errorMarker;
        this.warnMarker = warnMarker;
    }
    
    @Contract("_ -> new")
    public static @NotNull MarkLogger getLogger(@NotNull Logger logger)
    {
        requireNonNull(logger);
        return new MarkLogger(logger, null, null, null);
    }
    
    /**
     * Produces a standard logger, which automatically deals marker.
     * @apiNote Using <u>{@link LogUtils#getLogger()}</u> is recommended rather than 
     * <u>{@link org.slf4j.LoggerFactory#getLogger(String) LoggerFactory#getMarkedLogger()}</u>, 
     * since Mojang did some configuration on former one, it is more compatible.
     * @implSpec <pre>{@code 
     *  private static final MarkLogger LOGGER = MarkLogger.
     *      getMarkedLogger(
     *          LogUtils.getLogger(),
     *          MarkerFactory.getMarker("FOO")
     *      );
     * }</pre>
     */
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger getMarkedLogger(@NotNull Logger logger, @NotNull Marker marker)
    {
        requireNonNull(logger, "Param \"logger\" must not be null!");
        requireNonNull(marker, "Param \"marker\" must not be null!");
        
        return new MarkLogger(logger, marker, marker, marker);
    }
    
    /**
     * Produces a special logger, whose have two extra unique markers with {@code _ERR} and {@code _WARN} suffixes to override 
     * <u>{@link #defaultMarker}</u> in corresponded log level functions.
     * @implSpec <pre>{@code
     *  private static final MarkLogger LOGGER = MarkLogger.
     *      withMarkerSuffixes(
     *          LogUtils.getLogger(),
     *          MarkerFactory.getMarker("BAR")
     *      );
     * }</pre>
     * Produces these markers:<pre>{@code 
     *  //foo -> foo_err(For error log funcs)
     *  //BAR -> BAR_WARN(For warn log funcs)
     * }</pre>
     * <i>Current suffix conversion is quite simple, since we don't think it needs to support all text cases.</i>
     */
    @Contract("_, _ -> new")
    public static @NotNull MarkLogger withMarkerSuffixes(@NotNull Logger logger, @NotNull Marker marker)
    {
        requireNonNull(logger);
        requireNonNull(marker);
        
        final Marker err = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_ERR"));
        final Marker expr = MarkerFactory.getMarker(adaptSuffix(marker.getName(), "_WARN"));
        
        return new MarkLogger(logger, marker, err, expr);
    }
    
    @Contract("_ -> new")
    public @NotNull MarkerHandle pushMarker(@Nullable Marker marker)
    {
        setTempMarker(marker);
        return new MarkerHandle(this);
    }
    
    @Contract("_ -> new")
    public @NotNull MarkerHandle pushMarker(@Nullable String mark)
    {
        setTempMarker(MarkerFactory.getMarker(mark));
        return new MarkerHandle(this);
    }
    
    /**
     * A simple class to implement <u>{@link AutoCloseable}</u> for <u>{@link MarkLogger}</u>, making it usable in {@code Try-With-Resources}.
     *
     * @author Kurv Cygnus
     * @see #pushMarker(Marker) Usage
     * @since 1.0 Release
     */
    public static final class MarkerHandle implements AutoCloseable
    {
        private final MarkLogger logger;
        
        private MarkerHandle(@NotNull MarkLogger logger) { this.logger = logger; }
        
        public void changeMarker(@Nullable Marker marker) { logger.setTempMarker(marker); }
        
        public void changeMarker(@Nullable String mark) { logger.setTempMarker(MarkerFactory.getMarker(mark)); }
        
        @Override
        public void close() { logger.resetMarker(); }
    }
    
    public void trace(@Nullable String msg) { logger.trace(getMarker(), msg); }
    
    public void trace(@Nullable String msg, Object @Nullable ... args) { logger.trace(getMarker(), msg, args); }
    
    public void trace(@Nullable String msg, @Nullable Throwable t) { logger.trace(getMarker(), msg, t); }
    
    public void traceIf(boolean condition, @Nullable String msg) { if(condition) trace(msg); }
    
    public void traceIf(boolean condition, @Nullable String msg, Object @Nullable ... args) { if(condition) trace(msg, args); }
    
    public void debug(@Nullable String msg) { logger.debug(getMarker(), msg); }
    
    public void debug(@Nullable String msg, Object @Nullable ... args) { logger.debug(getMarker(), msg, args); }
    
    public void debug(@Nullable String msg, @Nullable Throwable t) { logger.debug(getMarker(), msg, t); }
    
    public void debugIf(boolean condition, @Nullable String msg) { if(condition) debug(msg); }
    
    public void debugIf(boolean condition, @Nullable String msg, Object @Nullable ... args) { if(condition) debug(msg, args); }
    
    public void info(@Nullable String msg) { logger.info(getMarker(), msg); }
    
    public void info(@Nullable String msg, Object @Nullable ... args) { logger.info(getMarker(), msg, args); }
    
    public void info(@Nullable String msg, @Nullable Throwable t) { logger.info(getMarker(), msg, t); }
    
    public void infoIf(boolean condition, @Nullable String msg) { if(condition) info(msg); }
    
    public void infoIf(boolean condition, @Nullable String msg, Object @Nullable ... args) { if(condition) info(msg, args); }
    
    public void warn(@Nullable String msg) { logger.warn(getWarnMarker(), msg); }
    
    public void warn(@Nullable String msg, Object @Nullable ... args) { logger.warn(getWarnMarker(), msg, args); }
    
    public void warn(@Nullable String msg, @Nullable Throwable t) { logger.warn(getWarnMarker(), msg, t); }
    
    public void warnIf(boolean condition, @Nullable String msg) { if(condition) warn(msg); }
    
    public void warnIf(boolean condition, @Nullable String msg, Object @Nullable ... args) { if(condition) warn(msg, args); }
    
    public void error(@Nullable String msg) { logger.error(getErrorMarker(), msg); }
    
    public void error(@Nullable String msg, Object @Nullable ... args) { logger.error(getErrorMarker(), msg, args); }
    
    public void error(@Nullable String msg, @Nullable Throwable t) { logger.error(getErrorMarker(), msg, t); }
    
    public void errorIf(boolean condition, @Nullable String msg) { if(condition) error(msg); }
    
    public void errorIf(boolean condition, @Nullable String msg, Object @Nullable ... args) { if(condition) error(msg, args); }
    
    /**
     * Sets a temporary marker for this MarkLogger.<br>
     * This temporary marker will override all markers of MarkLogger's in log functions,
     * until <u>{@link #resetMarker()}</u> is used.
     */
    private void setTempMarker(@Nullable Marker marker) { this.mutableMarker.set(marker); }
    
    private void resetMarker() { this.mutableMarker.remove(); }
    
    private @Nullable Marker getMarker()
    {
        final Marker current = this.mutableMarker.get();
        return current != null ? current : this.defaultMarker;
    }
    
    private @Nullable Marker getErrorMarker()
    {
        final Marker current = this.mutableMarker.get();
        return current != null ? current : (this.errorMarker != null ? this.errorMarker : this.defaultMarker);
    }
    
    private @Nullable Marker getWarnMarker()
    {
        final Marker current = this.mutableMarker.get();
        return current != null ? current : (this.warnMarker != null ? this.warnMarker : this.defaultMarker);
    }
    
    private static String adaptSuffix(@NotNull String baseName, @NotNull String suffix)
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
            return suffix.toLowerCase();
        
        return suffix.toUpperCase();
    }
}