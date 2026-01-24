package kurvcygnus.crispsweetberry.utils.ui.collects;

import com.google.common.collect.Range;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

/**
 * A simple range class for making range checks more readable.<br>
 * The reason of not using <u>{@link Range Range}</u> is that the function we need is far more few than it offers,
 * besides, API may change with time.<br>
 * We do recommend using <u>{@link CrispIntRanger CrispIntRanger}</u> at most cases.
 * @apiNote It is recommended to <b>use this as a constant</b>, constantly creating instances like this only brings performance penalty.<br>
 * Also, {@code CrispRanger} is always immutable.
 * @author Kurv Cygnus
 * @since 1.0 Release
 * @param <N> The type of number that will be hold by Ranger.
 */
public final class CrispRanger<N extends Number & Comparable<N>>
{
    private final N min;
    private final N max;
    private final boolean minClosed;
    private final boolean maxClosed;
    private static final Logger logger = LoggerFactory.getLogger(CrispRanger.class);
    
    @Contract("null, _, _, _ -> fail; _, null, _, _ -> fail")
    private CrispRanger(N min, N max, boolean minClosed, boolean maxClosed)
    {
        if(min == null || max == null)
            throw new IllegalArgumentException("Invalid min/max values!");
        if(min.compareTo(max) == 0 && (!minClosed || !maxClosed))
            throw new IllegalArgumentException("This is an empty, and illegal ranger!");
        
        if(min.compareTo(max) < 0)
        {
            this.min = min;
            this.max = max;
        }
        else
        {
            this.min = max;
            this.max = min;
            logger.warn("Swapped the value of min({})/max({}) to avoid abnormal behaviors.", min, max);
        }
        this.minClosed = minClosed;
        this.maxClosed = maxClosed;
    }
    
    @Contract("null, _ -> fail; _, null -> fail; !null, !null -> new")
    public static <A extends Number & Comparable<A>> @NotNull CrispRanger<A> closed(A min, A max) { return new CrispRanger<>(min, max, true, true); }
    
    @Contract("null, _ -> fail; _, null -> fail; !null, !null -> new")
    public static <A extends Number & Comparable<A>> @NotNull CrispRanger<A> open(A min, A max) { return new CrispRanger<>(min, max, false, false); }
    
    @Contract("null, _ -> fail; _, null -> fail; !null, !null -> new")
    public static <A extends Number & Comparable<A>> @NotNull CrispRanger<A> openClosed(A min, A max) { return new CrispRanger<>(min, max, false, true); }
    
    @Contract("null, _ -> fail; _, null -> fail; !null, !null -> new")
    public static <A extends Number & Comparable<A>> @NotNull CrispRanger<A> closedOpen(A min, A max) { return new CrispRanger<>(min, max, true, false); }
    
    public boolean inRange(@NotNull N value)
    {
        final boolean minCondition = this.minClosed ? value.compareTo(this.min) >= 0 : value.compareTo(this.min) > 0;
        final boolean maxCondition = this.maxClosed ? value.compareTo(this.max) <= 0 : value.compareTo(this.max) < 0;
        
        return minCondition && maxCondition;
    }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     */
    public static <A extends Number & Comparable<A>> int inRangers(@NotNull A value, @NotNull List<CrispRanger<A>> rangers)
    {
        for(int index = 0; index < rangers.size(); index++)
        {
            CrispRanger<A> ranger = rangers.get(index);
            Objects.requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return index;
        }
        
        return -1;
    }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     * @apiNote 
     * Do not use this with a list holds different number types, it causes data corruption or game crash.
     */
    @SafeVarargs
    public static <A extends Number & Comparable<A>> int inRangers(@NotNull A value, @NotNull CrispRanger<A>... rangers)
    {
        for(int index = 0; index < rangers.length; index++)
        {
            CrispRanger<A> ranger = rangers[index];
            Objects.requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return index;
        }
        
        return -1;
    }
    
    @Contract(pure = true)
    public N getMin() { return min; }
    
    @Contract(pure = true)
    public N getMax() { return max; }
    
    /**
     * Returns the value of this ranger's range.
     * @apiNote This didn't take closed/open cases in account.
     */
    public @NotNull Number getRange()
    {
        return switch(min)
        {
            case Integer i -> max.intValue() - i;
            case Long l -> max.longValue() - l;
            case Float f -> max.floatValue() - f;
            case Double d -> max.doubleValue() - d;
            case Byte b -> max.byteValue() - b;
            default -> max.doubleValue() - min.doubleValue();//throw new IllegalStateException("Unexpected number type: " + min.getClass().getSimpleName());
        };
    }
}
