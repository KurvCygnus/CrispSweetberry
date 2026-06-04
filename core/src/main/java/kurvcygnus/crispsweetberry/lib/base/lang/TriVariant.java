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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A container class which focuses on storing at least one value that could be one of the 3 specified types.<br><br>
 * If you are looking for doing <u><a href="https://fsharpforfunandprofit.com/rop/"><b>Railway Oriented Programming</b></a></u> stuff, see <u>{@link IResult}</u>,
 * this is for dataflow normalization, compression, and ADT-like logic procession.
 *
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
public final class TriVariant<L, M, R> extends BaseNestedPrinter<TriVariant<L, M, R>>
{
    private final @Nullable L left;
    private final @Nullable M middle;
    private final @Nullable R right;
    
    private TriVariant(@Nullable L left, @Nullable M middle, @Nullable R right)
    {
        int count = 0;
        
        if(left == null)
            count++;
        if(middle == null)
            count++;
        if(right == null)
            count++;
        
        if(count == 3)
            throw new IllegalStateException("All values are null, this container requires at least 1 variable to be non-null.");
        this.left = left;
        this.middle = middle;
        this.right = right;
    }
    
    public static <L, M, R> @NotNull TriVariant<L, M, R> ofLeft(@NotNull L left)
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        return new TriVariant<>(left, null, null);
    }
    
    public static <L, M, R> @NotNull TriVariant<L, M, R> ofMiddle(@NotNull M middle)
    {
        Objects.requireNonNull(middle, "Param \"middle\" must not be null!");
        return new TriVariant<>(null, middle, null);
    }
    
    public static <L, M, R> @NotNull TriVariant<L, M, R> ofRight(@NotNull R right)
    {
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
        return new TriVariant<>(null, null, right);
    }
    
    public static <L, M, R> @NotNull TriVariant<L, M, R> ofRightExcluded(@NotNull L left, @NotNull M middle)
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(middle, "Param \"middle\" must not be null!");
        return new TriVariant<>(left, middle, null);
    }
    
    public static <L, M, R> @NotNull TriVariant<L, M, R> ofMiddleExcluded(@NotNull L left, @NotNull R right)
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
        return new TriVariant<>(left, null, right);
    }
    
    public static <L, M, R> @NotNull TriVariant<L, M, R> ofLeftExcluded(@NotNull M middle, @NotNull R right)
    {
        Objects.requireNonNull(middle, "Param \"middle\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
        return new TriVariant<>(null, middle, right);
    }
    
    public static <L, M, R> @NotNull TriVariant<L, M, R> of(@NotNull L left, @NotNull M middle, @NotNull R right)
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(middle, "Param \"middle\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
        return new TriVariant<>(left, middle, right);
    }
    
    @Contract("null, null, null -> fail")
    public static <L, M, R> @NotNull TriVariant<L, M, R> ofNullable(@Nullable L left, @Nullable M middle, @Nullable R right)
        { return new TriVariant<>(left, middle, right); }
    
    public @NotNull L left()
    {
        if(left == null)
            throw new NoSuchElementException("Value is not present!");
        return left;
    }
    
    @SuppressWarnings("unchecked") public <U> @NotNull TriVariant<U, M, R> mapLeft(@NotNull Function<? super L, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        if(left == null)
            return (TriVariant<U, M, R>) this;
        
        return ofNullable(mapper.apply(left), middle, right);
    }
    
    public <U> @NotNull U destructLeftOrThrow(@NotNull Function<? super L, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        if(left == null)
            throw new NoSuchElementException("Value is not present!");
        return mapper.apply(left);
    }
    
    public @NotNull L leftOrDefault(@NotNull L defaultValue)
    {
        Objects.requireNonNull(defaultValue, "Param \"defaultValue\" must not be null!");
        if(left == null)
            return defaultValue;
        return left;
    }
    
    public @NotNull TriVariant<L, M, R> withLeft(@NotNull L left)
    {
        if(this.left != null)
            return this;
        
        return replaceLeft(left);
    }
    
    public @NotNull TriVariant<L, M, R> replaceLeft(@NotNull L left)
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        return ofNullable(left, middle, right);
    }
    
    public @NotNull M middle()
    {
        if(middle == null)
            throw new NoSuchElementException("Value is not present!");
        return middle;
    }
    
    @SuppressWarnings("unchecked") public <U> @NotNull TriVariant<L, U, R> mapMiddle(@NotNull Function<? super M, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        if(middle == null)
            return (TriVariant<L, U, R>) this;
        
        return ofNullable(left, mapper.apply(middle), right);
    }
    
    public <U> @NotNull U destructMiddleOrThrow(@NotNull Function<? super M, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        if(middle == null)
            throw new NoSuchElementException("Value is not present!");
        return mapper.apply(middle);
    }
    
    public @NotNull M middleOrDefault(@NotNull M defaultValue)
    {
        Objects.requireNonNull(defaultValue, "Param \"defaultValue\" must not be null!");
        if(middle == null)
            return defaultValue;
        return middle;
    }
    
    public @NotNull TriVariant<L, M, R> withMiddle(@NotNull M middle)
    {
        if(this.middle != null)
            return this;
        return replaceMiddle(middle);
    }
    
    public @NotNull TriVariant<L, M, R> replaceMiddle(@NotNull M middle)
    {
        Objects.requireNonNull(middle, "Param \"middle\" must not be null!");
        return ofNullable(left, middle, right);
    }
    
    public @NotNull R right()
    {
        if(right == null)
            throw new NoSuchElementException("Value is not present!");
        return right;
    }
    
    @SuppressWarnings("unchecked") public <U> @NotNull TriVariant<L, M, U> mapRight(@NotNull Function<? super R, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        
        if(right == null)
            return (TriVariant<L, M, U>) this;
        
        return ofNullable(left, middle, mapper.apply(right));
    }
    
    public <U> @NotNull U destructRightOrThrow(@NotNull Function<? super R, ? extends U> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        if(right == null)
            throw new NoSuchElementException("Value is not present!");
        return mapper.apply(right);
    }
    
    public @NotNull R rightOrDefault(@NotNull R defaultValue)
    {
        Objects.requireNonNull(defaultValue, "Param \"defaultValue\" must not be null!");
        if(right == null)
            return defaultValue;
        return right;
    }
    
    public @NotNull TriVariant<L, M, R> withRight(@NotNull R right)
    {
        if(this.right != null)
            return this;
        return replaceRight(right);
    }
    
    public @NotNull TriVariant<L, M, R> replaceRight(@NotNull R right)
    {
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
        return ofNullable(left, middle, right);
    }
    
    public boolean isLeftPresent() { return left != null; }
    
    public boolean isMiddlePresent() { return middle != null; }
    
    public boolean isRightPresent() { return right != null; }
    
    public <U> @NotNull U normalize(
        @NotNull Function<? super L, ? extends U> leftMapper,
        @NotNull Function<? super M, ? extends U> middleMapper,
        @NotNull Function<? super R, ? extends U> rightMapper,
        @NotNull BinaryOperator<U> combiner
    )
    {
        Objects.requireNonNull(leftMapper, "Param \"leftMapper\" must not be null!");
        Objects.requireNonNull(middleMapper, "Param \"middleMapper\" must not be null!");
        Objects.requireNonNull(rightMapper, "Param \"rightMapper\" must not be null!");
        Objects.requireNonNull(combiner, "Param \"combiner\" must not be null!");
        
        final U u1 = left != null ? leftMapper.apply(left) : null;
        final U u2 = middle != null ? middleMapper.apply(middle) : null;
        final U u3 = right != null ? rightMapper.apply(right) : null;
        
        if(u1 != null)
        {
            if(u2 != null)
            {
                final U u12 = combiner.apply(u1, u2);
                return u3 != null ? combiner.apply(u12, u3) : u12;
            }
            
            return u1;
        }
        
        if(u2 != null)
            return u3 != null ? combiner.apply(u2, u3) : u2;
        
        assert u3 != null;
        return u3;
    }
    
    public <U> U fold(
        @NotNull Function<? super L, ? extends U> leftMapper,
        @NotNull Function<? super M, ? extends U> middleMapper,
        @NotNull Function<? super R, ? extends U> rightMapper
    ) { return this.fold(leftMapper, middleMapper, rightMapper, false); }
    
    public <U> U fold(
        @NotNull Function<? super L, ? extends U> leftMapper,
        @NotNull Function<? super M, ? extends U> middleMapper,
        @NotNull Function<? super R, ? extends U> rightMapper,
        boolean reverseOrder
    )
    {
        Objects.requireNonNull(leftMapper, "Param \"leftMapper\" must not be null!");
        Objects.requireNonNull(middleMapper, "Param \"middleMapper\" must not be null!");
        Objects.requireNonNull(rightMapper, "Param \"rightMapper\" must not be null!");
        
        if(!reverseOrder)
            return left != null ? leftMapper.apply(left) :
                middle != null ? middleMapper.apply(middle) : rightMapper.apply(right);
        return right != null ? rightMapper.apply(right) :
            middle != null ? middleMapper.apply(middle) : leftMapper.apply(left);
    }
    
    public @NotNull TriVariant<R, M, L> reverse() { return new TriVariant<>(right, middle, left); }
    
    public <C extends Collection<U>, U> @NotNull C compress(
        @NotNull Function<? super L, ? extends U> leftMapper,
        @NotNull Function<? super M, ? extends U> middleMapper,
        @NotNull Function<? super R, ? extends U> rightMapper,
        @NotNull Supplier<C> container
    )
    {
        Objects.requireNonNull(leftMapper, "Param \"leftMapper\" must not be null!");
        Objects.requireNonNull(middleMapper, "Param \"middleMapper\" must not be null!");
        Objects.requireNonNull(rightMapper, "Param \"rightMapper\" must not be null!");
        Objects.requireNonNull(container, "Param \"container\" must not be null!");
        
        final var box = container.get();
        
        if(left != null)
            box.add(leftMapper.apply(left));
        if(middle != null)
            box.add(middleMapper.apply(middle));
        if(right != null)
            box.add(rightMapper.apply(right));
        return box;
    }
    
    public <C extends Collection<U>, U> @NotNull C compress(
        @NotNull IntFunction<C> container,
        @NotNull Function<? super L, ? extends U> leftMapper,
        @NotNull Function<? super M, ? extends U> middleMapper,
        @NotNull Function<? super R, ? extends U> rightMapper
    )
    {
        Objects.requireNonNull(leftMapper, "Param \"leftMapper\" must not be null!");
        Objects.requireNonNull(middleMapper, "Param \"middleMapper\" must not be null!");
        Objects.requireNonNull(rightMapper, "Param \"rightMapper\" must not be null!");
        Objects.requireNonNull(container, "Param \"container\" must not be null!");
        
        final var box = container.apply(3);
        
        if(left != null)
            box.add(leftMapper.apply(left));
        if(middle != null)
            box.add(middleMapper.apply(middle));
        if(right != null)
            box.add(rightMapper.apply(right));
        return box;
    }
    
    public <U> @NotNull @Unmodifiable List<U> compressReadonly(
        @NotNull Function<? super L, ? extends U> leftMapper,
        @NotNull Function<? super M, ? extends U> middleMapper,
        @NotNull Function<? super R, ? extends U> rightMapper
    )
    {
        final var list = compress(ArrayList::new, leftMapper, middleMapper, rightMapper);
        return Collections.unmodifiableList(list);
    }
    
    public <U> @NotNull Stream<Collection<U>> compressToStream(
        @NotNull Function<? super L, ? extends U> leftMapper,
        @NotNull Function<? super M, ? extends U> middleMapper,
        @NotNull Function<? super R, ? extends U> rightMapper
    ) { return Stream.of(compress(ArrayList::new, leftMapper, middleMapper, rightMapper)); }
    
    @Override public @NotNull @Unmodifiable Map<String, Function<TriVariant<L, M, R>, @Nullable Object>> getFields()
    {
        return INestedPrintable.buildFieldMap(
            map ->
            {
                map.put("left", m -> m.left);
                map.put("middle", m -> m.middle);
                map.put("right", m -> m.right);
            },
            3
        );
    }
    
    @Override public boolean takeNullFieldAsOptional() { return true; }
    
    @Override public boolean equals(@Nullable Object obj)
    {
        return this == obj ||
            obj instanceof TriVariant<?, ?, ?> that &&
            Objects.equals(this.left, that.left) &&
            Objects.equals(this.middle, that.middle) &&
            Objects.equals(this.right, that.right);
    }
    
    @Override public int hashCode() { return Objects.hash(left, middle, right); }
}
