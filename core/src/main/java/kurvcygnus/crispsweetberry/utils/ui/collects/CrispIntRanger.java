//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.ui.collects;

import com.google.common.collect.Range;
import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants.NAN;

/**
 * A simple range class for making range checks more readable.<br>
 * The reason of not using <u>{@link Range Range}</u> is that the function we need is far more few than it offers,
 * besides, API may change with time.<br><br>
 * <h3><b>Please note that this is not a math range class, it's specially designed for Minecraft UI.</b></h3>
 *
 * @author Kurv Cygnus
 * @apiNote It is recommended to <b>use this as a constant</b>, constantly creating instances like this only brings performance penalty.<br>
 * Also, <u>{@link CrispIntRanger}</u> is always immutable, and we recommend reduce the usage of <u>{@link CrispIntRanger}</u>'s {@code forEach}
 * in performance-sensitive cases, as it brings unboxing performance penalty.
 * @since 1.0 Release
 */
@ApiStatus.Internal @SuppressWarnings("unused")
public final class CrispIntRanger implements Iterable<Integer>
{
    public static final CrispIntRanger BACKPACK_SLOTS_RANGE = closed(ExampleSlotConstants.BACKPACK_SLOT_START_INDEX, ExampleSlotConstants.BACKPACK_SLOT_END_INDEX);
    public static final CrispIntRanger HOTBAR_SLOTS_RANGE = closed(ExampleSlotConstants.HOTBAR_SLOT_START_INDEX, ExampleSlotConstants.HOTBAR_SLOT_END_INDEX);
    public static final CrispIntRanger INVENTORY_SLOTS_RANGE = closed(BACKPACK_SLOTS_RANGE.getMin(), HOTBAR_SLOTS_RANGE.getMax());
    
    private final int min;
    private final int max;
    private final boolean minClosed;
    private final boolean maxClosed;
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private CrispIntRanger(int min, int max, boolean minClosed, boolean maxClosed)
    {
        CrispFunctionalUtils.throwIf(min == max && (!minClosed || !maxClosed), () ->
            new IllegalArgumentException("This is an empty, and illegal ranger!"));
        
        if(min < max)
        {
            this.min = minClosed ? min : ++min;
            this.max = maxClosed ? max : --max;
        }
        else
        {
            this.min = maxClosed ? max : ++max;
            this.max = minClosed ? min : --min;
            LOGGER.warn("Swapped the value of min({})/max({}) to avoid abnormal behaviors.", max, min);
        }
        
        this.minClosed = minClosed;
        this.maxClosed = maxClosed;
    }
    
    /**
     * Creates a {@code [min, max]} range.
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min}.
     */
    @Contract("_, _ -> new")
    public static @NotNull CrispIntRanger closed(int min, int max) { return new CrispIntRanger(min, max, true, true); }
    
    /**
     * Creates a {@code (min, max)} range.
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min},
     * and throws <u>{@link IllegalArgumentException}</u> when {@code min} equals {@code max}.
     */
    @Contract("_, _ -> new")
    public static @NotNull CrispIntRanger open(int min, int max) { return new CrispIntRanger(min, max, false, false); }
    
    /**
     * Creates a {@code (min, max]} range.
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min},
     * and throws <u>{@link IllegalArgumentException}</u> when {@code min} equals {@code max}.
     */
    @Contract("_, _ -> new")
    public static @NotNull CrispIntRanger openClosed(int min, int max) { return new CrispIntRanger(min, max, false, true); }
    
    /**
     * Creates a {@code [min, max)} range.
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min},
     * and throws <u>{@link IllegalArgumentException}</u> when {@code min} equals {@code max}.
     */
    @Contract("_, _ -> new")
    public static @NotNull CrispIntRanger closedOpen(int min, int max) { return new CrispIntRanger(min, max, true, false); }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     */
    @CheckReturnValue
    public boolean inRange(int value) { return value >= this.min && value <= this.max; }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.<br>
     * @apiNote <b><i>It is safer than <u>{@link #inRangers(int, List)}</u>, but has unboxing penalty</i></b>.
     */
    @CheckReturnValue
    public static @NotNull Optional<Integer> inRangers(@NotNull Integer value, @NotNull List<CrispIntRanger> rangers)
    {
        requireNonNull(value, "Param \"value\" must not be null!");
        
        for(int index = 0; index < rangers.size(); index++)
        {
            final CrispIntRanger ranger = rangers.get(index);
            
            requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return Optional.of(index);
        }
        
        return Optional.empty();
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
            final CrispIntRanger ranger = rangers.get(index);
            
            requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return index;
        }
        
        return NAN;
    }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     * @apiNote <b><i>It is safer than <u>{@link #inRangers(int, CrispIntRanger...)}</u>, but has unboxing penalty</i></b>.
     */
    @CheckReturnValue
    public static @NotNull Optional<Integer> inRangers(@NotNull Integer value, CrispIntRanger @NotNull ... rangers)
    {
        requireNonNull(value, "Param \"value\" must not be null!");
        
        for(int index = 0; index < rangers.length; index++)
        {
            final CrispIntRanger ranger = rangers[index];
            requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return Optional.of(index);
        }
        
        return Optional.empty();
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
            final CrispIntRanger ranger = rangers[index];
            requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return index;
        }
        
        return NAN;
    }
    
    /**
     * Iterate the ranger, and do things.
     * @apiNote Despite {@code forEachInt()} is more inconvenient than {@code for(int foo: Bar)}, 
     * it has no unboxing penalty.
     */
    public void forEachInt(@NotNull IntConsumer action)
    {
        requireNonNull(action, "Param \"action\" must not be null!");
        
        final IntIterator it = new IntIterator();
        
        while(it.hasNext())
            action.accept(it.nextInt());
    }
    
    /**
     * Iterate the <u>{@link AbstractContainerMenu}</u> with ranger(and boundary check), and do <u>{@link Slot slot}</u> things.
     * @implNote Uses <u>{@link #forEachInt}</u>, no unboxing penalty.
     */
    public <C extends AbstractContainerMenu> void forEachSlot(@NotNull C menu, @NotNull Consumer<Slot> action)
    {
        requireNonNull(menu, "Param \"menu\" must not be null!");
        requireNonNull(action, "Param \"action\" must not be null!");
        
        this.forEachInt((index) -> 
            {
                if(index >= 0 && index < menu.slots.size())
                    action.accept(menu.slots.get(index));
            }
        );
    }
    
    /**
     * Finds and returns the first <u>{@link Slot slot}</u> that suits the <u>{@link Predicate rule}</u>, if no <u>{@link Slot slot}</u> suits, 
     * returns <u>{@link Optional#empty()}</u> as result.
     * @implNote Uses raw {@code for()} to implement, no unboxing penalty.<br>
     * This method also has boundary check.
     */
    public <C extends AbstractContainerMenu> @NotNull Optional<Slot> findFirst(@NotNull C menu, @NotNull Predicate<Slot> rule)
    {
        requireNonNull(menu, "Param \"menu\" must not be null!");
        requireNonNull(rule, "Param \"rule\" must not be null!");
        
        for(int index = this.min; index <= this.max; index++)
        {
            final Slot slot = menu.getSlot(index);
            
            if(index >= 0 && index < menu.slots.size() && rule.test(slot))
                return Optional.of(slot);
        }
        
        return Optional.empty();
    }
    
    public boolean contains(@NotNull CrispIntRanger ranger)
    {
        requireNonNull(ranger, "Param \"ranger\" must not be null!");
        
        return this.min <= ranger.min && ranger.max <= this.max;
    }
    
    public @NotNull Optional<CrispIntRanger> merge(@NotNull CrispIntRanger ranger)
    {
        requireNonNull(ranger, "Param \"ranger\" must not be null!");
        
        if(this.min > ranger.max + 1 || ranger.min > this.max + 1)
        {
            LOGGER.warn("Ranges are separated and not adjacent, cannot be merged. This: {}, Other: {}", this, ranger);
            return Optional.empty();
        }
        
        final int newMin = Math.min(this.min, ranger.min);
        final int newMax = Math.max(this.max, ranger.max);
        
        return Optional.of(closed(newMin, newMax));
    }
    
    public static @NotNull Optional<CrispIntRanger> merge(@NotNull CrispIntRanger first, @NotNull CrispIntRanger second)
    {
        requireNonNull(first, "Param \"first\" must not be null!");
        return first.merge(second);
    }
    
    @Contract("_ -> new")
    public @NotNull CrispIntRanger offset(int offset) { return closed(this.min + offset, this.max + offset); }
    
    public @NotNull IntStream stream() { return IntStream.rangeClosed(this.min, this.max); }
    
    public @NotNull IntStream rawStream() { return IntStream.rangeClosed(this.minClosed ? min : min - 1, this.maxClosed ? max : max + 1); }
    
    @CheckReturnValue public int getMin() { return this.min; }
    
    @CheckReturnValue public int getMax() { return this.max; }
    
    @CheckReturnValue public int size() { return this.max - this.min + 1; }
    
    @Override @Contract(" -> new") 
    public @NotNull Iterator<Integer> iterator() { return new IntegerIterator(); }
    
    @Override @Contract(" -> new")
    public @NotNull Spliterator.OfInt spliterator()
    {
        return Spliterators.spliterator(
            new IntIterator(),
            size(),
            Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL
        );
    }
    
    @ApiStatus.Internal
    private final class IntegerIterator implements Iterator<Integer>
    {
        private int cursor = min;
        
        @Override
        public boolean hasNext() { return cursor <= max; }
        
        @Override
        public Integer next()
        {
            if(!hasNext())
                throw new NoSuchElementException();
            return cursor++;
        }
    }
    
    @ApiStatus.Internal
    private final class IntIterator implements PrimitiveIterator.OfInt
    {
        private int cursor = min;
        @Override
        public int nextInt()
        {
            if(!hasNext())
                throw new NoSuchElementException();
            return cursor++;
        }
        
        @Override
        public boolean hasNext() { return cursor <= max; }
    }
    
    public @NotNull String toString()
    {
        return "%s%d...%d%s".formatted(
            this.minClosed ? "[" : "(",
            this.minClosed ? this.min : this.min - 1,
            this.maxClosed ? this.max : this.max + 1,
            this.maxClosed ? "]" : ")"
        );
    }
}
