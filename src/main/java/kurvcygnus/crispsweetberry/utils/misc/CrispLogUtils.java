package kurvcygnus.crispsweetberry.utils.misc;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This util contains log-related functions.
 * @implNote DO NOT create Classes like {@code DevLogger} or something else, it will cause log source
 * ambiguous, and the solution <u>{@link org.apache.logging.log4j.spi.ExtendedLogger ExtendedLogger}</u> & {@code FQCN}
 * doesn't apply to Minecraft, and using hacky castings like<br>
 * <pre> {@code 
 *  Logger LOGGER = (ExtendedLogger) LogUtils.getLogger();
 * }</pre>
 * will <b>only end up with <u>{@link ClassCastException}</u></b>.
 * @since 1.0 Release
 */
public final class CrispLogUtils
{
    private CrispLogUtils() { throw new IllegalAccessError(); }
    
    public static void logIf(boolean condition, @NotNull Runnable logAction)
    {
        if(!condition)
            return;
        
        Objects.requireNonNull(logAction, "Param \"logAction\" cannot be null!");
        
        logAction.run();
    }
    
    public static void conditionalLog(boolean condition, @NotNull Runnable trueLogAction, @NotNull Runnable falseLogAction)
    {
        Objects.requireNonNull(trueLogAction, "Param \"trueLogAction\" cannot be null!(Param 1)");
        Objects.requireNonNull(falseLogAction, "Param \"falseLogAction\" cannot be null!(Param 2)");
        
        if(condition)
            trueLogAction.run();
        else
            falseLogAction.run();
    }
}
