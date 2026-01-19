package kurvcygnus.crispsweetberry.utils.collects;

import com.google.common.collect.Range;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A simple range class for making range checks more readable.<br>
 * The reason of not using <u>{@link Range Range}</u> is that the function we need is far more few than it offers,
 * besides, API may change with time.<br>
 * If you need to use it for other number types, you may try <u>{@link CrispRanger}</u>.
 * @apiNote It is recommended to <b>use this as a constant</b>, constantly creating instances like this only brings performance penalty.<br>
 * Also, {@code CrispIntRanger} is always immutable.
 * @author Kurv
 * @since CSB 1.0 Release
 */
public final class CrispIntRanger
{
    private final int min;
    private final int max;
    private final boolean minClosed;
    private final boolean maxClosed;
    private static final Logger logger = LoggerFactory.getLogger(CrispIntRanger.class);
    
    private CrispIntRanger(int min, int max, boolean minClosed, boolean maxClosed)
    {
        if(min == max && (!minClosed || !maxClosed))
            throw new IllegalArgumentException("This is an empty, and illegal ranger!");
        
        if(min < max)
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
    
    @Contract("_, _ -> new")
    public static CrispIntRanger closed(int min, int max) { return new CrispIntRanger(min, max, true, true); }
    
    @Contract("_, _ -> new")
    public static CrispIntRanger open(int min, int max) { return new CrispIntRanger(min, max, false, false); }
    
    @Contract("_, _ -> new")
    public static CrispIntRanger openClosed(int min, int max) { return new CrispIntRanger(min, max, false, true); }
    
    @Contract("_, _ -> new")
    public static CrispIntRanger closedOpen(int min, int max) { return new CrispIntRanger(min, max, true, false); }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     */
    @CheckReturnValue
    public boolean inRange(int value)
    {
        final boolean minCondition = this.minClosed ? value >= this.min : value > this.min;
        final boolean maxCondition = this.maxClosed ? value <= this.max : value < this.max;
        
        return minCondition && maxCondition;
    }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     */
    @CheckReturnValue
    public static int inRangers(int value, @NotNull List<CrispIntRanger> rangers)
    {
        for(int index = 0; index < rangers.size(); index++)
            if(rangers.get(index).inRange(value))
                return index;
        
        return -1;
    }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     */
    @CheckReturnValue
    public static int inRangers(int value, @NotNull CrispIntRanger... rangers)
    {
        for(int index = 0; index < rangers.length; index++)
            if(rangers[index].inRange(value))
                return index;
        
        return -1;
    }
    
    @CheckReturnValue
    public int getMin() { return min; }
    
    @CheckReturnValue
    public int getMax() { return max; }
    
    /**
     * Returns the value of this ranger's range.
     * @apiNote This didn't take closed/open cases in account.
     */
    @CheckReturnValue
    public int getRange() { return this.max - this.min; }
}
