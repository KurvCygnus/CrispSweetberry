package kurvcygnus.crispsweetberry.utils.ui.collects;

import com.google.common.collect.Range;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A simple range class for making range checks more readable.<br>
 * The reason of not using <u>{@link Range Range}</u> is that the function we need is far more few than it offers,
 * besides, API may change with time.<br>
 * If you need to use it for other number types, you may try <u>{@link CrispRanger}</u>.
 *
 * @author Kurv Cygnus
 * @apiNote It is recommended to <b>use this as a constant</b>, constantly creating instances like this only brings performance penalty.<br>
 * Also, {@code CrispIntRanger} is always immutable.
 * @since 1.0 Release
 */
@ApiStatus.Internal
public final class CrispIntRanger implements Iterable<Integer>
{
    private final int min;
    private final int max;
    private final boolean minClosed;
    private final boolean maxClosed;
    private static final Logger LOGGER = LoggerFactory.getLogger(CrispIntRanger.class);
    
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
            LOGGER.warn("Swapped the value of min({})/max({}) to avoid abnormal behaviors.", max, min);
        }
        this.minClosed = minClosed;
        this.maxClosed = maxClosed;
    }
    
    @Contract("_, _ -> new")
    public static @NotNull CrispIntRanger closed(int min, int max) { return new CrispIntRanger(min, max, true, true); }
    
    @Contract("_, _ -> new")
    public static @NotNull CrispIntRanger open(int min, int max) { return new CrispIntRanger(min, max, false, false); }
    
    @Contract("_, _ -> new")
    public static @NotNull CrispIntRanger openClosed(int min, int max) { return new CrispIntRanger(min, max, false, true); }
    
    @Contract("_, _ -> new")
    public static @NotNull CrispIntRanger closedOpen(int min, int max) { return new CrispIntRanger(min, max, true, false); }
    
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
        {
            CrispIntRanger ranger = rangers.get(index);
            
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
     */
    @CheckReturnValue
    public static int inRangers(int value, CrispIntRanger @NotNull ... rangers)
    {
        for(int index = 0; index < rangers.length; index++)
        {
            CrispIntRanger ranger = rangers[index];
            Objects.requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return index;
        }
        
        return -1;
    }
    
    public <C extends AbstractContainerMenu> void forEachSlot(@NotNull C menu, @NotNull Consumer<Slot> action)
    {
        Objects.requireNonNull(menu, "Param \"menu\" must not be null!");
        Objects.requireNonNull(action, "Param \"action\" must not be null!");
        
        for(int index: this)
            if(index >= 0 && index < menu.slots.size())
                action.accept(menu.getSlot(index));
    }
    
    @CheckReturnValue
    public int getMin() { return min; }
    
    @CheckReturnValue
    public int getMax() { return max; }
    
    public int size() { return (maxClosed ? max : max - 1) - (minClosed ? min : min + 1) + 1; }
    
    @Override @Contract(" -> new")
    public @NotNull Iterator<Integer> iterator() { return new CrispIntIterator(); }
    
    private class CrispIntIterator implements Iterator<Integer>
    {
        private int cursor = minClosed ? min : min + 1;
        private final int last = maxClosed ? max : max - 1;
        
        @Override
        public boolean hasNext() { return cursor <= last; }
        
        @Override
        public Integer next()
        {
            if(!hasNext())
                throw new NoSuchElementException();
            return cursor++;
        }
    }
}
