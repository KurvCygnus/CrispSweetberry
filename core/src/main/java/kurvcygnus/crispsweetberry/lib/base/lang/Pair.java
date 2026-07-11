//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.lang;

import it.unimi.dsi.fastutil.objects.Object2ByteFunction;
import it.unimi.dsi.fastutil.objects.Object2CharFunction;
import it.unimi.dsi.fastutil.objects.Object2FloatFunction;
import it.unimi.dsi.fastutil.objects.Object2ShortFunction;
import kurvcygnus.crispsweetberry.lib.base.extensions.IAutoNestedPrintable;
import kurvcygnus.crispsweetberry.lib.base.functions.ToByteBiFunction;
import kurvcygnus.crispsweetberry.lib.base.functions.ToCharBiFunction;
import kurvcygnus.crispsweetberry.lib.base.functions.ToFloatBiFunction;
import kurvcygnus.crispsweetberry.lib.base.functions.ToShortBiFunction;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;

/**
 * A simple container class, which can hold two non-null values and do monad transformations.
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
public record Pair<L, R>(@NotNull L left, @NotNull R right) implements Map.Entry<@NotNull L, @NotNull R>, IAutoNestedPrintable.OfRecordHandle<Pair<L, R>>
{
    //region Basic Supports
    public Pair
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
    }
    
    public static <L, R> @NotNull Pair<L, R> of(@NotNull L left, @NotNull R right) { return new Pair<>(left, right); }
    
    @Override public @NotNull L getKey() { return this.left; }
    
    @Override public @NotNull R getValue() { return this.right; }
    
    public @NotNull Pair<L, R> withLeft(@NotNull L left) { return new Pair<>(left, this.right); }
    
    public @NotNull Pair<L, R> withRight(@NotNull R right) { return new Pair<>(this.left, right); }
    
    public @NotNull Pair<R, L> swap() { return new Pair<>(right, left); }
    
    public @NotNull @Unmodifiable Map<L, R> asMap() { return Collections.singletonMap(this.left, this.right); }
    
    @Contract(value = "_ -> fail", pure = true) @Override public R setValue(@Nullable R value)
        { throw new UnsupportedOperationException("This is an immutable Class. Value mutation is not allowed!"); }
    
    @Override public @NotNull String toString() { return toNestedString(); }
    //endregion
    
    //region Mapping Functions
    public <U> @NotNull Pair<U, R> mapLeft(@NotNull Function<? super L, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return Pair.of(mapper.apply(left), right);
    }
    
    /**
     * Enhanced <u>{@link #mapLeft(Function)}</u>, which uses {@code defaultValue} when the result of {@code mapper} is null, or it throws exception.
     */
    public <U> @NotNull Pair<U, R> mapLeft(@NotNull Function<? super L, ? extends @Nullable U> mapper, @NotNull U defaultValue)
        { return new Pair<>(safeGet(left, mapper, defaultValue), right); }
    
    public <U> @NotNull Pair<L, U> mapRight(@NotNull Function<? super R, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return new Pair<>(left, mapper.apply(right));
    }
    
    /**
     * Enhanced <u>{@link #mapRight(Function)}</u>, which uses {@code defaultValue} when the result of {@code mapper} is null, or it throws exception.
     */
    public <U> @NotNull Pair<L, U> mapRight(@NotNull Function<? super R, ? extends @Nullable U> mapper, @NotNull U defaultValue)
        { return new Pair<>(left, safeGet(right, mapper, defaultValue)); }
    
    public <LU, RU> @NotNull Pair<LU, RU> biMap(@NotNull Function<? super L, ? extends LU> leftMapper, @NotNull Function<? super R, ? extends RU> rightMapper)
    {
        Objects.requireNonNull(leftMapper, "Param \"leftMapper\" must not be null!");
        Objects.requireNonNull(rightMapper, "Param \"rightMapper\" must not be null!");
        return new Pair<>(leftMapper.apply(left), rightMapper.apply(right));
    }
    
    /**
     * Enhanced <u>{@link #biMap(Function, Function)}</u>, which uses {@code defaultValue} when the result of {@code mapper} is null, or it throws exception.
     */
    public <LU, RU> @NotNull Pair<LU, RU> biMap(
        @NotNull Function<? super L, ? extends @Nullable LU> leftMapper,
        @NotNull Function<? super R, ? extends @Nullable RU> rightMapper,
        @NotNull LU leftDefault,
        @NotNull RU rightDefault
    ) { return new Pair<>(safeGet(left, leftMapper, leftDefault), safeGet(right, rightMapper, rightDefault)); }
    
    public <LU, RU> @NotNull Pair<LU, RU> flatMap(@NotNull BiFunction<? super L, ? super R, ? extends Pair<LU, RU>> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left, this.right);
    }
    
    private static <T, R> @NotNull R safeGet(@NotNull T value, @NotNull Function<? super T, ? extends @Nullable R> mapper, @NotNull R fallback)
    {
        assert value != null;
        
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        Objects.requireNonNull(fallback, "Param \"fallback\" must not be null!");
        
        //!                                       ↓ this may throw exception.
        try { return Objects.requireNonNullElse(mapper.apply(value), fallback); }
        catch(Exception e) { return fallback; }
    }
    //endregion
    
    //region Apply Functions
    public <U> @NotNull U applyLeft(@NotNull Function<? super L, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left);
    }
    
    public byte applyLeftToByte(@NotNull Object2ByteFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left);
    }
    
    public short applyLeftToShort(@NotNull Object2ShortFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left);
    }
    
    public int applyLeftToInt(@NotNull ToIntFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsInt(this.left);
    }
    
    public long applyLeftToLong(@NotNull ToLongFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsLong(this.left);
    }
    
    public float applyLeftToFloat(@NotNull Object2FloatFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left);
    }
    
    public double applyLeftToDouble(@NotNull ToDoubleFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsDouble(this.left);
    }
    
    public char applyLeftToChar(@NotNull Object2CharFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left);
    }
    
    public boolean applyLeftToBoolean(@NotNull Predicate<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.test(this.left);
    }
    
    public <U> @NotNull U applyRight(@NotNull Function<? super R, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.right);
    }
    
    public byte applyRightToByte(@NotNull Object2ByteFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.right);
    }
    
    public short applyRightToShort(@NotNull Object2ShortFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.right);
    }
    
    public int applyRightToInt(@NotNull ToIntFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsInt(this.right);
    }
    
    public long applyRightToLong(@NotNull ToLongFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsLong(this.right);
    }
    
    public float applyRightToFloat(@NotNull Object2FloatFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.right);
    }
    
    public double applyRightToDouble(@NotNull ToDoubleFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsDouble(this.right);
    }
    
    public char applyRightToChar(@NotNull Object2CharFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.right);
    }
    
    public boolean applyRightToBoolean(@NotNull Predicate<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.test(this.right);
    }
    
    public <U> U apply(@NotNull BiFunction<? super L, ? super R, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left, this.right);
    }
    
    public byte applyAsByte(@NotNull ToByteBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsByte(this.left, this.right);
    }
    
    public short applyAsShort(@NotNull ToShortBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsShort(this.left, this.right);
    }
    
    public int applyAsInt(@NotNull ToIntBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsInt(this.left, this.right);
    }
    
    public long applyAsLong(@NotNull ToLongBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsLong(this.left, this.right);
    }
    
    public float applyAsFloat(@NotNull ToFloatBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsFloat(this.left, this.right);
    }
    
    public double applyAsDouble(@NotNull ToDoubleBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsDouble(this.left, this.right);
    }
    
    public char applyAsChar(@NotNull ToCharBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsChar(this.left, this.right);
    }
    
    public boolean applyAsBoolean(@NotNull BiPredicate<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.test(this.left, this.right);
    }
    //endregion
    
    //region Consume Functions
    /**
     * A simple method for simplifying some usages of applying <u>{@link Pair}</u>.<br><br>
     *
     * e.g.
     * <pre>{@code
     *  final var lookup = new HashMap<String, String>();
     *  final var pair = Pair.of("foo", "bar");
     *
     *  pair.accept(lookup::put);
     * }</pre>
     */
    public void accept(@NotNull BiConsumer<? super L, ? super R> consumer)
    {
        Objects.requireNonNull(consumer, "Param \"consumer\" must not be null!");
        consumer.accept(left, right);
    }
    
    public void accept(@NotNull Consumer<? super L> leftConsumer, @NotNull Consumer<? super R> rightConsumer)
    {
        Objects.requireNonNull(leftConsumer, "Param \"leftConsumer\" must not be null!");
        Objects.requireNonNull(rightConsumer, "Param \"rightConsumer\" must not be null!");
        leftConsumer.accept(left);
        rightConsumer.accept(right);
    }
    
    public void acceptLeft(@NotNull Consumer<? super L> consumer)
    {
        Objects.requireNonNull(consumer, "Param \"consumer\" must not be null!");
        consumer.accept(this.left);
    }
    
    public void acceptRight(@NotNull Consumer<? super R> consumer)
    {
        Objects.requireNonNull(consumer, "Param \"consumer\" must not be null!");
        consumer.accept(this.right);
    }
    //endregion
}
