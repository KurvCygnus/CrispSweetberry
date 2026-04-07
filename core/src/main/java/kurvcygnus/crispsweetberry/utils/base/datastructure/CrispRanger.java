//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.datastructure;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import kurvcygnus.crispsweetberry.utils.base.trait.IBitmaskedEnum;
import kurvcygnus.crispsweetberry.utils.constants.ExampleSlotConstants;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static kurvcygnus.crispsweetberry.utils.base.datastructure.CrispRanger.ProcessOptions.*;
import static kurvcygnus.crispsweetberry.utils.constants.ExampleSlotConstants.ERROR;

/**
 * A simple range class for making range checks more readable.<br>
 * The reason of not using <u>{@link com.google.common.collect.Range Range}</u> is that the function we need is far more few than it offers,
 * besides, API may change with time.<hr>
 * <h3><b>Please note that this is not a math range class, it's specially designed for Minecraft UI.</b></h3>
 *
 * @author Kurv Cygnus
 * @apiNote It is recommended to <b>use this as a constant</b>, constantly creating instances like this only brings performance penalty.<br>
 * Also, <u>{@link CrispRanger}</u> is always immutable, and we recommend reduce the usage of <u>{@link CrispRanger}</u>'s {@code forEach}
 * in performance-sensitive cases, as it brings unboxing performance penalty.
 * @see CrispRangeMap
 * @since 1.0 Release
 */
public final class CrispRanger implements Iterable<Integer>
{
    //region Constants & Fields
    public static final CrispRanger BACKPACK_SLOTS_RANGE = closed(ExampleSlotConstants.BACKPACK_SLOT_START_INDEX, ExampleSlotConstants.BACKPACK_SLOT_END_INDEX);
    public static final CrispRanger HOTBAR_SLOTS_RANGE = closed(ExampleSlotConstants.HOTBAR_SLOT_START_INDEX, ExampleSlotConstants.HOTBAR_SLOT_END_INDEX);
    public static final CrispRanger INVENTORY_SLOTS_RANGE = closed(BACKPACK_SLOTS_RANGE.min(), HOTBAR_SLOTS_RANGE.max());
    
    public static final int START_AT_LEFT = DIRECTION.shiftTrue();
    public static final int START_AT_RIGHT = DIRECTION.shiftFalse();
    public static final int EXCLUSIVE = OPENNESS.shiftTrue();
    public static final int INCLUSIVE = OPENNESS.shiftFalse();
    
    public enum ProcessOptions implements IBitmaskedEnum<ProcessOptions> { DIRECTION, OPENNESS }
    
    private final int min;
    private final int max;
    private static final Logger LOGGER = LogUtils.getLogger();
    //endregion
    
    //region Constructors
    private CrispRanger(int min, int max, boolean minClosed, boolean maxClosed)
    {
        FunctionalUtils.throwIf(
            min == max && (!minClosed || !maxClosed),
            "This is an empty, and illegal ranger!",
            IllegalArgumentException::new
        );
        
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
    }
    
    /**
     * Creates a {@code [min, max]} range.
     *
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min}.
     */
    @Contract("_, _ -> new") public static @NotNull CrispRanger closed(int min, int max) { return new CrispRanger(min, max, true, true); }
    
    /**
     * Creates a {@code (min, max)} range.
     *
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min},
     * and throws <u>{@link IllegalArgumentException}</u> when {@code min} equals {@code max}.
     */
    @Contract("_, _ -> new") public static @NotNull CrispRanger open(int min, int max) { return new CrispRanger(min, max, false, false); }
    
    /**
     * Creates a {@code (min, max]} range.
     *
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min},
     * and throws <u>{@link IllegalArgumentException}</u> when {@code min} equals {@code max}.
     */
    @Contract("_, _ -> new") public static @NotNull CrispRanger openClosed(int min, int max) { return new CrispRanger(min, max, false, true); }
    
    /**
     * Creates a {@code [min, max)} range.
     *
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min},
     * and throws <u>{@link IllegalArgumentException}</u> when {@code min} equals {@code max}.
     */
    @Contract("_, _ -> new") public static @NotNull CrispRanger closedOpen(int min, int max) { return new CrispRanger(min, max, true, false); }
    //endregion
    
    //region Public APIs
    
    /**
     * Transforms a float percentage into corresponded value in this {@code CrispRanger}.
     */
    public @CheckReturnValue int lerp(@Range(from = 0, to = 1) float percentage)
    {
        if(percentage < 0 || percentage > 1)
            return ERROR;
        
        return this.min + (int) (percentage * this.size());
    }
    
    /**
     * Transforms a float percentage into corresponded value in this {@code CrispRanger}.
     */
    public @CheckReturnValue int lerp(@Range(from = 0, to = 1) double percentage)
    {
        if(percentage < 0 || percentage > 1)
            return ERROR;
        
        return this.min + (int) (percentage * this.size());
    }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     */
    @CheckReturnValue public boolean inRange(int value) { return value >= this.min && value <= this.max; }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.<br>
     *
     * @apiNote <b><i>It is safer than <u>{@link #inRangers(int, List)}</u>, but has unboxing penalty</i></b>.
     */
    @CheckReturnValue public static @NotNull Optional<Integer> inRangers(@NotNull Integer value, @NotNull List<CrispRanger> rangers)
    {
        requireNonNull(value, "Param \"value\" must not be null!");
        
        for(int index = 0; index < rangers.size(); index++)
        {
            final CrispRanger ranger = rangers.get(index);
            
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
    @CheckReturnValue public static int inRangers(int value, @NotNull List<CrispRanger> rangers)
    {
        for(int index = 0; index < rangers.size(); index++)
        {
            final CrispRanger ranger = rangers.get(index);
            
            requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return index;
        }
        
        return ERROR;
    }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     *
     * @apiNote <b><i>It is safer than <u>{@link #inRangers(int, CrispRanger...)}</u>, but has unboxing penalty</i></b>.
     */
    @CheckReturnValue public static @NotNull Optional<Integer> inRangers(@NotNull Integer value, CrispRanger @NotNull ... rangers)
    {
        requireNonNull(value, "Param \"value\" must not be null!");
        
        for(int index = 0; index < rangers.length; index++)
        {
            final CrispRanger ranger = rangers[index];
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
    @CheckReturnValue public static int inRangers(int value, CrispRanger @NotNull ... rangers)
    {
        for(int index = 0; index < rangers.length; index++)
        {
            final CrispRanger ranger = rangers[index];
            requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return index;
        }
        
        return ERROR;
    }
    
    /**
     * Iterate the ranger, and do things.
     *
     * @apiNote Despite {@code forEachInt()} is more inconvenient than {@code for(int foo: Bar)},
     * it has no unboxing penalty.
     */
    public void forEachInt(@NotNull IntConsumer action)
    {
        requireNonNull(action, "Param \"action\" must not be null!");
        
        final RangerPrimitiveIterator it = new RangerPrimitiveIterator();
        
        while(it.hasNext())
            action.accept(it.nextInt());
    }
    
    /**
     * Iterate the <u>{@link AbstractContainerMenu}</u> with ranger(and boundary check), and do <u>{@link Slot slot}</u> things.
     *
     * @implNote Uses <u>{@link #forEachInt}</u>, no unboxing penalty.
     */
    public <C extends AbstractContainerMenu> void forEachSlot(@NotNull C menu, @NotNull Consumer<Slot> action)
    {
        requireNonNull(menu, "Param \"menu\" must not be null!");
        requireNonNull(action, "Param \"action\" must not be null!");
        
        this.forEachInt(
            (index) ->
            {
                if(index >= 0 && index < menu.slots.size())
                    action.accept(menu.slots.get(index));
            }
        );
    }
    
    /**
     * Finds and returns the first <u>{@link Slot slot}</u> that suits the <u>{@link Predicate rule}</u>, if no <u>{@link Slot slot}</u> suits,
     * returns <u>{@link Optional#empty()}</u> as result.
     *
     * @implNote Uses raw {@code for()} to implement, no unboxing penalty.<br>
     * This method also has boundary check.
     */
    public <C extends AbstractContainerMenu> @CheckReturnValue @NotNull Optional<Slot> findFirst(@NotNull C menu, @NotNull Predicate<Slot> rule)
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
    
    /**
     * Checks whether the param {@code that}'s range is included in <b>this {@code CrispRanger}</b>.
     */
    public @CheckReturnValue boolean overlaps(@Nullable CrispRanger that)
    {
        if(that == null)
            return false;
        return this.min <= that.min && that.max <= this.max;
    }
    
    /**
     * Merges two rangers(as instantiated method, the first that is {@code this}) together.
     * @return Merge result. Once two rangers have no common range, this method will return <u>{@link Optional#empty()}</u>.
     * @see CrispRanger#union(CrispRanger, CrispRanger) Static method
     */
    public @CheckReturnValue @NotNull Optional<CrispRanger> union(@NotNull CrispRanger that)
    {
        requireNonNull(that, "Param \"that\" must not be null!");
        
        if(this.min > that.max + 1 || that.min > this.max + 1)
        {
            LOGGER.warn("Ranges are separated and not adjacent, cannot be merged. This: {}, Other: {}", this, that);
            return Optional.empty();
        }
        
        final int newMin = Math.min(this.min, that.min);
        final int newMax = Math.max(this.max, that.max);
        
        return Optional.of(closed(newMin, newMax));
    }
    
    /**
     * Merges two rangers together.
     * @return Merge result. Once two rangers have no common range, this method will return <u>{@link Optional#empty()}</u>.
     * @see CrispRanger#union(CrispRanger) Instantiated method
     */
    public static @CheckReturnValue @NotNull Optional<CrispRanger> union(@NotNull CrispRanger first, @NotNull CrispRanger second)
    {
        requireNonNull(first, "Param \"first\" must not be null!");
        return first.union(second);
    }
    
    public @CheckReturnValue @NotNull Optional<CrispRanger> intersect(@NotNull CrispRanger that)
    {
        if(!this.overlaps(that))
            return Optional.empty();
        
        requireNonNull(that, "Param \"that\" must not be null!");
        
        final int newMin = Math.max(this.min, that.min);
        final int newMax = Math.min(this.max, that.max);
        
        return Optional.of(closed(newMin, newMax));
    }
    
    public static @CheckReturnValue @NotNull Optional<CrispRanger> intersect(@NotNull CrispRanger first, @NotNull CrispRanger second)
    {
        requireNonNull(first, "Param \"first\" must not be null!");
        requireNonNull(second, "Param \"second\" must not be null!");
        return first.intersect(second);
    }
    
    /**
     * Note: <span style="color: red">This is not a setter, it is a math range method.</span><br>
     * Chop this {@code CrispRanger}'s intersection between this and another Ranger,
     * returns this {@code CrispRanger}'s uncommon range only.
     */
    public @CheckReturnValue @NotNull Optional<CrispRanger> setDifference(@NotNull CrispRanger that)
    {
        if(!this.overlaps(that))
            return Optional.of(this);
        
        if(this.equals(that))
            return Optional.empty();
        
        final int newMin = Math.min(this.min, that.min);
        final int newMax = Math.min(this.max, that.max);
        return Optional.of(closed(newMin, newMax));
    }
    
    /**
     * Slice this {@code CrispRanger} by param {@code value}, and returns sliced, new {@code CrispRanger}.
     * @apiNote You should fill param {@code flags} with these flags:
     * <b>
     *     <u>{@link CrispRanger#START_AT_LEFT}</u>,
     *     <u>{@link CrispRanger#START_AT_RIGHT}</u>,
     *     <u>{@link CrispRanger#INCLUSIVE}</u>,
     *     <u>{@link CrispRanger#EXCLUSIVE}</u>,
     *     Combine them with {@code |}.
     * </b>
     * <hr>
     * <i>
     *     When range closure is not defined, <u>{@link CrispRanger#INCLUSIVE}</u> will be taken as default,
     *     however, once slice direction is not defined, <span style="color: red">this method will return <u>{@link Optional#empty()}</u> as result.</span>
     * </i>
     */
    public @CheckReturnValue @NotNull Optional<CrispRanger> slice(
        int value,
        @MagicConstant(flagsFromClass = CrispRanger.class) int flags
    )
    {
        if(!this.inRange(value) || DIRECTION.computeRaw(flags) == DEFAULT)
            return Optional.empty();
        
        //* true is left, right is false.
        final boolean direction = DIRECTION.computeBooleanOrThrow(flags);
        
        if(value == (direction ? this.min : this.max))
            return Optional.empty();
        
        final boolean closed = OPENNESS.computeBooleanOrDefault(flags, false);
        
        return Optional.of(
            new CrispRanger(
                direction ?
                    value :
                    this.min,
                !direction ?
                    value :
                    this.max,
                direction && closed,
                !direction && closed
            )
        );
    }
    
    /**
     * Obtain the remaining range from the boundary of this {@code CrispRanger} to the integer limit,
     * and returns this "outer" part as a new {@code CrispRanger}.
     *
     * @apiNote You should fill param {@code flags} with one of these directional flags:
     * <b>
     * <u>{@link CrispRanger#START_AT_LEFT}</u> (Returns range from {@link Integer#MIN_VALUE} to min),
     * <u>{@link CrispRanger#START_AT_RIGHT}</u> (Returns range from max to {@link Integer#MAX_VALUE}).
     * </b>
     * <hr>
     * <i>
     * Note that range closure is fixed by implementation (Closed-Open for left, Open-Closed for right).<br>
     * <span style="color: red">This method will return <u>{@link Optional#empty()}</u> if the specified direction
     * is already at the integer boundary or if the direction flag is missing.</span>
     * </i>
     */
    public @CheckReturnValue @NotNull Optional<CrispRanger> complement(
        @MagicConstant(flagsFromClass = CrispRanger.class) int flags
    )
    {
        if(DIRECTION.isDefault(flags) || !OPENNESS.isDefault(flags))
            return Optional.empty();
        
        final boolean direction = DIRECTION.computeBooleanOrThrow(flags);
        
        if(direction)
            return this.min == Integer.MIN_VALUE ?
                Optional.empty() :
                Optional.of(CrispRanger.closedOpen(Integer.MIN_VALUE, this.min));
        
        return this.max == Integer.MAX_VALUE ?
            Optional.empty() :
            Optional.of(CrispRanger.openClosed(this.max, Integer.MAX_VALUE));
    }
    
    /**
     * Move the entire range of this ranger by param {@code offset}, and returns an independent {@code CrispRanger}.
     * @apiNote If both {@code min} and {@code max}'s value are overflowed, this method will return <u>{@link Optional#empty()}</u>
     * as the result, since in that case, the new ranger will be meaningless, and useless.
     */
    @Contract("_ -> new") public @CheckReturnValue @NotNull Optional<CrispRanger> offset(int offset)
    {
        final int newMin = evaluate(this.min, offset);
        final int newMax = evaluate(this.max, offset);
        
        if(newMin == newMax)
            return Optional.empty();
        
        return Optional.of(closed(newMin, newMax));
    }
    
    /**
     * Opens a <u>{@link IntStream}</u>, with the value of {@code CrispRanger}'s {@code min} as start value, {@code max} as end value, to do further
     * manipulation.
     */
    public @CheckReturnValue @NotNull IntStream stream() { return IntStream.rangeClosed(this.min, this.max); }
    
    public @CheckReturnValue int min() { return this.min; }
    
    public @CheckReturnValue int max() { return this.max; }
    
    /**
      * Returns the size of this {@code CrispRanger}.
     * <br>
     * <i>You know exclusive value is also a part of Range, right?</i>
     */
    public @CheckReturnValue int size() { return this.max - this.min + 1; }
    
    /**
     * Gets the iterator of this Ranger.
     * @deprecated
     * One most important usage of iterator is removing element while iteration, which can't be done by enhanced for-loop.<br>
     * However, {@code CrispRanger} doesn't have elements, and to be honest, using iterator to iterate is primitive and bloat.<br>
     * <span style="color: 95cc6d">So, please use enhanced for-loop, <u>{@link CrispRanger#forEachInt(IntConsumer)}</u>, or <u>{@link CrispRanger#forEach(Consumer)}</u> instead.</span>
     */
    @Deprecated @Override @Contract(" -> new") public @CheckReturnValue @NotNull Iterator<Integer> iterator() { return new RangerIterator(); }
    
    /**
     * {@inheritDoc}
     */
    @Override @Contract(" -> new") public @CheckReturnValue @NotNull Spliterator.OfInt spliterator()
    {
        return Spliterators.spliterator(
            new RangerPrimitiveIterator(),
            size(),
            Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL
        );
    }
    
    @Override public boolean equals(@Nullable Object obj) { return obj instanceof CrispRanger that && min == that.min && max == that.max; }
    
    @Override public int hashCode() { return Objects.hash(min, max); }
    
    public @CheckReturnValue @NotNull String toString() { return "[%d, %d]".formatted(this.min, this.max); }
    //endregion
    
    //region Inner Helpers
    @ApiStatus.Internal private final class RangerIterator implements Iterator<Integer>
    {
        private int cursor = min;
        
        @Override public boolean hasNext() { return cursor <= max; }
        
        @Override public Integer next()
        {
            if(!hasNext())
                throw new NoSuchElementException();
            return cursor++;
        }
    }
    
    @ApiStatus.Internal private final class RangerPrimitiveIterator implements PrimitiveIterator.OfInt
    {
        private int cursor = min;
        
        @Override public int nextInt()
        {
            if(!hasNext())
                throw new NoSuchElementException();
            return cursor++;
        }
        
        @Override public boolean hasNext() { return cursor <= max; }
    }
    
    private static int evaluate(int value, int sumValue)
    {
        final long sum = sumValue + value;
        
        if(sum >= Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        else if(sum <= Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        
        return (int) sum;
    }
    //endregion
}
