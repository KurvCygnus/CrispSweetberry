//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.datastructure;

import kurvcygnus.crispsweetberry.lib.base.trait.IBitmaskedEnum;
import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static kurvcygnus.crispsweetberry.lib.base.datastructure.ProcessOptions.*;

/**
 * A simple range class for making range checks more readable.<br>
 * The reason of not using <u>{@link com.google.common.collect.Range Range}</u> is that the function we need is far more few than it offers,
 * besides, API may change with time.<hr>
 * <h3><b>Please note that this is not a math range class, it's specially designed for Minecraft UI.</b></h3>
 *
 * @author Kurv Cygnus
 * @apiNote It is recommended to <b>use this as a constant</b>, constantly creating instances like this only brings performance penalty.<br>
 * Also, <u>{@link Ranger}</u> is always immutable, and we recommend reduce the usage of <u>{@link Ranger}</u>'s {@code forEach}
 * in performance-sensitive cases, as it brings unboxing performance penalty.
 * @see RangeMap
 * @since 1.0 Release
 */
public final class Ranger implements Iterable<Integer>
{
    //region Constants & Fields
    public static final int BACKPACK_SLOT_START_INDEX = 3;
    public static final int BACKPACK_SLOT_END_INDEX = 29;
    public static final int HOTBAR_SLOT_START_INDEX = 30;
    public static final int HOTBAR_SLOT_END_INDEX = 38;
    
    public static final Ranger BACKPACK_SLOTS_RANGE = closed(BACKPACK_SLOT_START_INDEX, BACKPACK_SLOT_END_INDEX);
    public static final Ranger HOTBAR_SLOTS_RANGE = closed(HOTBAR_SLOT_START_INDEX, HOTBAR_SLOT_END_INDEX);
    public static final Ranger INVENTORY_SLOTS_RANGE = BACKPACK_SLOTS_RANGE.union(HOTBAR_SLOTS_RANGE).orElseThrow();
    
    public static final int START_AT_LEFT = DIRECTION.shiftTrue();
    public static final int START_AT_RIGHT = DIRECTION.shiftFalse();
    public static final int EXCLUSIVE = OPENNESS.shiftTrue();
    public static final int INCLUSIVE = OPENNESS.shiftFalse();
    public static final int ERROR = -1;
    
    public final int min;
    public final int max;
    //endregion
    
    //region Constructors
    private Ranger(int min, int max, boolean minClosed, boolean maxClosed)
    {
        if(min == max && (!minClosed || !maxClosed))
            throw new IllegalArgumentException("This is an empty, and illegal range!");
        
        if(min >= max)
            throw new IllegalArgumentException(TextUtils.format("Min({}) is not smaller than max({}), this is an illegal range!", min, max));
        
        this.min = minClosed ? min : ++min;
        this.max = maxClosed ? max : --max;
    }
    
    /**
     * Creates a {@code [min, max]} range.
     *
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min}.
     */
    @Contract("_, _ -> new") public static @NotNull Ranger closed(int min, int max) { return new Ranger(min, max, true, true); }
    
    /**
     * Creates a {@code (min, max)} range.
     *
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min},
     * and throws <u>{@link IllegalArgumentException}</u> when {@code min} equals {@code max}.
     */
    @Contract("_, _ -> new") public static @NotNull Ranger open(int min, int max) { return new Ranger(min, max, false, false); }
    
    /**
     * Creates a {@code (min, max]} range.
     *
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min},
     * and throws <u>{@link IllegalArgumentException}</u> when {@code min} equals {@code max}.
     */
    @Contract("_, _ -> new") public static @NotNull Ranger openClosed(int min, int max) { return new Ranger(min, max, false, true); }
    
    /**
     * Creates a {@code [min, max)} range.
     *
     * @apiNote Exchanges the value and warn if {@code max} are smaller than {@code min},
     * and throws <u>{@link IllegalArgumentException}</u> when {@code min} equals {@code max}.
     */
    @Contract("_, _ -> new") public static @NotNull Ranger closedOpen(int min, int max) { return new Ranger(min, max, true, false); }
    //endregion
    
    //region Public APIs
    /**
     * Transforms a float percentage into corresponded value in this {@code Ranger}.
     */
    public @CheckReturnValue int lerp(@Range(from = 0, to = 1) float percentage)
    {
        if(percentage < 0 || percentage > 1)
            return ERROR;
        
        return this.min + (int) (percentage * this.size());
    }
    
    /**
     * Transforms a float percentage into corresponded value in this {@code Ranger}.
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
     */
    @CheckReturnValue public static @NotNull OptionalInt inSafeRangers(int value, @NotNull List<Ranger> rangers)
    {
        for(int index = 0; index < rangers.size(); index++)
        {
            final var ranger = rangers.get(index);
            
            requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return OptionalInt.of(index);
        }
        
        return OptionalInt.empty();
    }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     */
    @CheckReturnValue public static int inRangers(int value, @NotNull List<Ranger> rangers)
    {
        for(int index = 0; index < rangers.size(); index++)
        {
            final var ranger = rangers.get(index);
            
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
     */
    @CheckReturnValue public static @NotNull OptionalInt inSafeRangers(int value, @NotNull Ranger @NotNull ... rangers)
    {
        for(int index = 0; index < rangers.length; index++)
        {
            final var ranger = rangers[index];
            requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return OptionalInt.of(index);
        }
        
        return OptionalInt.empty();
    }
    
    /**
     * This method is used for searching which range does {@code index} located in.<br>
     * This could be handy in complex UI methods to make them more readable and simple with {@code switch} statement,
     * like {@link AbstractContainerMenu#quickMoveStack quickMoveStack()}.
     */
    @CheckReturnValue public static int inRangers(int value, @NotNull Ranger @NotNull ... rangers)
    {
        for(int index = 0; index < rangers.length; index++)
        {
            final var ranger = rangers[index];
            requireNonNull(ranger, "Ranger must not be null! Null ranger at index: " + index);
            
            if(ranger.inRange(value))
                return index;
        }
        
        return ERROR;
    }
    
    /**
     * Iterates over every <b>integer value</b> within this range, feeding each to the given <u>{@link IntConsumer}</u>.
     * <br><i>Use this if you want to avoid the boxing overhead of <u>{@link #forEach(Consumer)}</u>.</i>
     *
     * @param action the action to perform on each integer value in this range
     * @throws NullPointerException if {@code action} is null
     * @see #forEach(Consumer)
     * @see PrimitiveIterator
     */
    public void forEachInt(@NotNull IntConsumer action)
    {
        requireNonNull(action, "Param \"action\" must not be null!");
        
        final var it = new PrimitiveIterator();
        
        while(it.hasNext())
            action.accept(it.nextInt());
    }
    
    /**
     * Iterate the <u>{@link AbstractContainerMenu}</u> with ranger(and boundary check), and do <u>{@link Slot slot}</u> things.
     */
    public <C extends AbstractContainerMenu> void forEachSlot(@NotNull C menu, @NotNull Consumer<Slot> action)
    {
        requireNonNull(menu, "Param \"menu\" must not be null!");
        requireNonNull(action, "Param \"action\" must not be null!");
        
        this.forEachInt(index -> { if(index >= 0 && index < menu.slots.size()) action.accept(menu.slots.get(index)); });
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
            final var slot = menu.getSlot(index);
            
            if(index >= 0 && index < menu.slots.size() && rule.test(slot))
                return Optional.of(slot);
        }
        
        return Optional.empty();
    }
    
    /**
     * Checks whether the param {@code that}'s range is included in <b>this {@code Ranger}</b>.
     */
    public @CheckReturnValue boolean overlaps(@Nullable Ranger that)
    {
        if(that == null)
            return false;
        return this.min <= that.min && that.max <= this.max;
    }
    
    /**
     * Merges two rangers(as instantiated method, the first that is {@code this}) together.
     * @return Merge result. Once two rangers have no common range, this method will return <u>{@link Optional#empty()}</u>.
     * @see Ranger#union(Ranger, Ranger) Static method
     */
    public @CheckReturnValue @NotNull Optional<Ranger> union(@NotNull Ranger that)
    {
        requireNonNull(that, "Param \"that\" must not be null!");
        
        if(this.min > that.max + 1 || that.min > this.max + 1)
            return Optional.empty();
        
        final int newMin = Math.min(this.min, that.min);
        final int newMax = Math.max(this.max, that.max);
        
        return Optional.of(closed(newMin, newMax));
    }
    
    /**
     * Merges two rangers together.
     * @return Merge result. Once two rangers have no common range, this method will return <u>{@link Optional#empty()}</u>.
     * @see Ranger#union(Ranger) Instantiated method
     */
    public static @CheckReturnValue @NotNull Optional<Ranger> union(@NotNull Ranger first, @NotNull Ranger second)
    {
        requireNonNull(first, "Param \"first\" must not be null!");
        return first.union(second);
    }
    
    /**
     * Returns the <b>intersection</b> of this <u>{@link Ranger}</u> and the given one.
     *
     * @param that the other <u>{@link Ranger}</u>
     * @return the overlapping range if the two intersect, or <u>{@link Optional#empty()}</u> if they
     *         <span style="color: f84b4b">do not overlap</span>
     * @see #intersect(Ranger, Ranger) Static counterpart
     * @see #overlaps(Ranger)
     */
    public @CheckReturnValue @NotNull Optional<Ranger> intersect(@NotNull Ranger that)
    {
        if(!this.overlaps(that))
            return Optional.empty();
        
        requireNonNull(that, "Param \"that\" must not be null!");
        
        final int newMin = Math.max(this.min, that.min);
        final int newMax = Math.min(this.max, that.max);
        
        return Optional.of(closed(newMin, newMax));
    }
    
    /**
     * Returns the <b>intersection</b> of two <u>{@link Ranger}</u>s.
     *
     * @param first  the first <u>{@link Ranger}</u>
     * @param second the second <u>{@link Ranger}</u>
     * @return the overlapping range if the two intersect, or <u>{@link Optional#empty()}</u> if they
     *         <span style="color: f84b4b">do not overlap</span>
     * @see #intersect(Ranger) Instance counterpart
     */
    public static @CheckReturnValue @NotNull Optional<Ranger> intersect(@NotNull Ranger first, @NotNull Ranger second)
    {
        requireNonNull(first, "Param \"first\" must not be null!");
        requireNonNull(second, "Param \"second\" must not be null!");
        return first.intersect(second);
    }
    
    /**
     * Computes the <b>set difference</b> (<i>this \ that</i>): returns the portion of <b>this</b> {@link Ranger}
     * that does <span style="color: f84b4b">not</span> overlap with {@code that}.
     * <ul>
     *     <li>If the two ranges <b>do not overlap</b>, returns <u>{@link Optional#of(Object) this}</u>.</li>
     *     <li>If they are <b>identical</b>, returns <u>{@link Optional#empty()}</u>.</li>
     *     <li>Otherwise returns the non-overlapping portion of <b>this</b> that lies
     *         to the <b>left</b> of {@code that}.</li>
     * </ul>
     * @apiNote <span style="color: f84b4b">This is not a setter, it is a math range method.</span>
     *
     * @param that the <u>{@link Ranger}</u> to subtract from this
     * @return the non-overlapping portion, or empty if fully consumed
     */
    public @CheckReturnValue @NotNull Optional<Ranger> setDifference(@NotNull Ranger that)
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
     * Slice this {@code Ranger} by param {@code value}, and returns sliced, new {@code Ranger}.
     * @apiNote You should fill param {@code flags} with these flags:
     * <b>
     *     <u>{@link Ranger#START_AT_LEFT}</u>,
     *     <u>{@link Ranger#START_AT_RIGHT}</u>,
     *     <u>{@link Ranger#INCLUSIVE}</u>,
     *     <u>{@link Ranger#EXCLUSIVE}</u>,
     *     Combine them with {@code |}.
     * </b>
     * <hr>
     * <i>
     *     When range closure is not defined, <u>{@link Ranger#INCLUSIVE}</u> will be taken as default,
     *     however, once slice direction is not defined, <span style="color: f84b4b">this method will return <u>{@link Optional#empty()}</u> as result.</span>
     * </i>
     */
    public @CheckReturnValue @NotNull Optional<Ranger> slice(
        int value,
        @MagicConstant(flagsFromClass = Ranger.class) int flags
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
            new Ranger(
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
     * Obtain the remaining range from the boundary of this {@code Ranger} to the integer limit,
     * and returns this "outer" part as a new {@code Ranger}.
     *
     * @apiNote You should fill param {@code flags} with one of these directional flags:
     * <b>
     * <u>{@link Ranger#START_AT_LEFT}</u> (Returns range from {@link Integer#MIN_VALUE} to min),
     * <u>{@link Ranger#START_AT_RIGHT}</u> (Returns range from max to {@link Integer#MAX_VALUE}).
     * </b>
     * <hr>
     * <i>
     * Note that range closure is fixed by implementation (Closed-Open for left, Open-Closed for right).<br>
     * <span style="color: f84b4b">This method will return <u>{@link Optional#empty()}</u> if the specified direction
     * is already at the integer boundary or if the direction flag is missing.</span>
     * </i>
     */
    public @CheckReturnValue @NotNull Optional<Ranger> complement(
        @MagicConstant(flagsFromClass = Ranger.class) int flags
    )
    {
        if(DIRECTION.isDefault(flags) || !OPENNESS.isDefault(flags))
            return Optional.empty();
        
        final boolean direction = DIRECTION.computeBooleanOrThrow(flags);
        
        if(direction)
            return this.min == Integer.MIN_VALUE ?
                Optional.empty() :
                Optional.of(Ranger.closedOpen(Integer.MIN_VALUE, this.min));
        
        return this.max == Integer.MAX_VALUE ?
            Optional.empty() :
            Optional.of(Ranger.openClosed(this.max, Integer.MAX_VALUE));
    }
    
    /**
     * Shifts the entire range of this <u>{@link Ranger}</u> by the given {@code offset}, returning a <b>new</b>
     * independent <u>{@link Ranger}</u>.
     * <ul>
     *     <li><span style="color: 95cc6d">Safe</span> against integer overflow — values are capped to
     *         <u>{@link Integer#MAX_VALUE}</u> or <u>{@link Integer#MIN_VALUE}</u> as needed.</li>
     *     <li><span style="color: f84b4b">If both {@code min} and {@code max} overflow to the same value</span>
     *         (making the result an empty/illegal range), this method returns <u>{@link Optional#empty()}</u>.</li>
     * </ul>
     *
     * @param offset the amount to shift (positive moves right, negative moves left)
     * @return the shifted <u>{@link Ranger}</u>, or empty if the shift collapses the range
     */
    @Contract("_ -> new") public @CheckReturnValue @NotNull Optional<Ranger> offset(int offset)
    {
        final int newMin = evaluate(this.min, offset);
        final int newMax = evaluate(this.max, offset);
        
        if(newMin == newMax)
            return Optional.empty();
        
        return Optional.of(closed(newMin, newMax));
    }
    
    /**
     * Opens a <u>{@link IntStream}</u>, with the value of {@code Ranger}'s {@code min} as start value, {@code max} as end value, to do further
     * manipulation.
     */
    public @CheckReturnValue @NotNull IntStream stream() { return IntStream.rangeClosed(this.min, this.max); }
    
    /**
     * @return the <b>minimum</b> (<i>inclusive</i>) value of this <u>{@link Ranger}</u>
     */
    public @CheckReturnValue int min() { return this.min; }

    /**
     * @return the <b>maximum</b> (<i>inclusive</i>) value of this <u>{@link Ranger}</u>
     */
    public @CheckReturnValue int max() { return this.max; }
    
    /**
      * Returns the size of this {@code Ranger}.
     * <br>
     * <i>You know exclusive value is also a part of Range, right?</i>
     */
    public @CheckReturnValue int size() { return this.max - this.min + 1; }
    
    /**
     * Gets the iterator of this Ranger.
     * @deprecated
     * One most important usage of iterator is removing element while iteration, which can't be done by enhanced for-loop.<br>
     * However, {@code Ranger} doesn't have elements, and to be honest, using iterator to iterate is primitive and bloat.<br>
     * <span style="color: 95cc6d">So, please use enhanced for-loop, <u>{@link Ranger#forEachInt(IntConsumer)}</u>, or <u>{@link Ranger#forEach(Consumer)}</u> instead.</span>
     */
    @Deprecated @Override @Contract(" -> new") public @CheckReturnValue @NotNull java.util.Iterator<Integer> iterator() { return new Iterator(); }
    
    /**
     * {@inheritDoc}
     */
    @Override @Contract(" -> new") public @CheckReturnValue @NotNull Spliterator.OfInt spliterator()
    {
        return Spliterators.spliterator(
            new PrimitiveIterator(),
            size(),
            Spliterator.ORDERED | Spliterator.IMMUTABLE | Spliterator.NONNULL
        );
    }
    
    @Override public boolean equals(@Nullable Object obj) { return this == obj || obj instanceof Ranger that && min == that.min && max == that.max; }
    
    @Override public int hashCode() { return Objects.hash(min, max); }
    
    public @CheckReturnValue @NotNull String toString() { return TextUtils.format("[{}, {}]", this.min, this.max); }
    //endregion
    
    //region Inner Helpers
    @ApiStatus.Internal private final class Iterator implements java.util.Iterator<Integer>
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
    
    @ApiStatus.Internal private final class PrimitiveIterator implements java.util.PrimitiveIterator.OfInt
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

enum ProcessOptions implements IBitmaskedEnum<ProcessOptions> { DIRECTION, OPENNESS }