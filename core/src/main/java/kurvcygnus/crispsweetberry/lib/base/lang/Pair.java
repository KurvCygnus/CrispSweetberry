//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.lang;

import kurvcygnus.crispsweetberry.lib.base.extensions.BaseNestedPrinter;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import kurvcygnus.crispsweetberry.lib.base.functions.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.*;

/**
 * A simple container class, which can hold two non-null values.
 *
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
public final class Pair<L, R> extends BaseNestedPrinter<Pair<L, R>> implements Map.Entry<L, R>
{
    private final @NotNull L left;
    private final @NotNull R right;
    
    private Pair(@NotNull L left, @NotNull R right)
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
        this.left = left;
        this.right = right;
    }
    
    public static <L, R> @NotNull Pair<L, R> of(@NotNull L left, @NotNull R right) { return new Pair<>(left, right); }
    
    @Override public @NotNull L getKey() { return this.left; }
    
    @Override public @NotNull R getValue() { return this.right; }
    
    public <U> @NotNull Pair<U, R> mapLeft(@NotNull Function<? super L, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return Pair.of(mapper.apply(left), right);
    }
    
    public <U> @NotNull U destructLeft(@NotNull Function<? super L, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left);
    }
    
    public byte destructLeftToByte(@NotNull ToByteFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsByte(this.left);
    }
    
    public short destructLeftToShort(@NotNull ToShortFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsShort(this.left);
    }
    
    public int destructLeftToInt(@NotNull ToIntFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsInt(this.left);
    }
    
    public long destructLeftToLong(@NotNull ToLongFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsLong(this.left);
    }
    
    public float destructLeftToFloat(@NotNull ToFloatFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsFloat(this.left);
    }
    
    public double destructLeftToDouble(@NotNull ToDoubleFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsDouble(this.left);
    }
    
    public char destructLeftToChar(@NotNull ToCharFunction<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsChar(this.left);
    }
    
    public boolean destructLeftToBoolean(@NotNull Predicate<? super L> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.test(this.left);
    }
    
    public @NotNull Pair<L, R> withLeft(@NotNull L left) { return new Pair<>(left, this.right); }
    
    public <U> @NotNull Pair<L, U> mapRight(@NotNull Function<? super R, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return new Pair<>(left, mapper.apply(right));
    }
    
    public <U> @NotNull U destructRight(@NotNull Function<? super R, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.right);
    }
    
    public byte destructRightToByte(@NotNull ToByteFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsByte(this.right);
    }
    
    public short destructRightToShort(@NotNull ToShortFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsShort(this.right);
    }
    
    public int destructRightToInt(@NotNull ToIntFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsInt(this.right);
    }
    
    public long destructRightToLong(@NotNull ToLongFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsLong(this.right);
    }
    
    public float destructRightToFloat(@NotNull ToFloatFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsFloat(this.right);
    }
    
    public double destructRightToDouble(@NotNull ToDoubleFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsDouble(this.right);
    }
    
    public char destructRightToChar(@NotNull ToCharFunction<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsChar(this.right);
    }
    
    public boolean destructRightToBoolean(@NotNull Predicate<? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.test(this.right);
    }
    
    public @NotNull Pair<L, R> withRight(@NotNull R right) { return new Pair<>(this.left, right); }
    
    public <LU, RU> @NotNull Pair<LU, RU> dualMap(@NotNull Function<? super L, ? extends LU> leftMapper, @NotNull Function<? super R, ? extends RU> rightMapper)
    {
        Objects.requireNonNull(leftMapper, "Param \"leftMapper\" must not be null!");
        Objects.requireNonNull(rightMapper, "Param \"rightMapper\" must not be null!");
        return new Pair<>(leftMapper.apply(left), rightMapper.apply(right));
    }
    
    public <LU, RU> @NotNull Pair<LU, RU> flatMap(@NotNull BiFunction<? super L, ? super R, ? extends Pair<LU, RU>> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left, this.right);
    }
    
    public <U> U normalize(@NotNull BiFunction<? super L, ? super R, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(this.left, this.right);
    }
    
    public byte normalizeToByte(@NotNull ToByteBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsByte(this.left, this.right);
    }
    
    public short normalizeToShort(@NotNull ToShortBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsShort(this.left, this.right);
    }
    
    public int normalizeToInt(@NotNull ToIntBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsInt(this.left, this.right);
    }
    
    public long normalizeToLong(@NotNull ToLongBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsLong(this.left, this.right);
    }
    
    public float normalizeToFloat(@NotNull ToFloatBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsFloat(this.left, this.right);
    }
    
    public double normalizeToDouble(@NotNull ToDoubleBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsDouble(this.left, this.right);
    }
    
    public char normalizeToChar(@NotNull ToCharBiFunction<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.applyAsChar(this.left, this.right);
    }
    
    public boolean normalizeToBoolean(@NotNull BiPredicate<? super L, ? super R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.test(this.left, this.right);
    }
    
    public @NotNull Pair<R, L> swap() { return new Pair<>(right, left); }
    
    public @NotNull Map.Entry<L, R> asEntry() { return this; }
    
    public @NotNull @Unmodifiable Map<L, R> asMap() { return Collections.singletonMap(this.left, this.right); }
    
    @Contract(value = "_ -> fail", pure = true) @Override public R setValue(@Nullable R value)
        { throw new UnsupportedOperationException("This is an immutable Class. Value mutation is not allowed!"); }
    
    @Override public @NotNull @Unmodifiable Map<String, Function<Pair<L, R>, @Nullable Object>> getFields()
        { return INestedPrintable.buildFieldMap(Pair.of("left", Pair::left), Pair.of("right", Pair::right)); }
    
    public @NotNull L left() { return left; }
    
    public @NotNull R right() { return right; }
    
    @Override public boolean equals(@Nullable Object obj)
    {
        return this == obj ||
            obj instanceof Pair<?, ?> that &&
            Objects.equals(left, that.left) &&
            Objects.equals(right, that.right);
    }
    
    @Override public int hashCode() { return Objects.hash(left, right); }
}
