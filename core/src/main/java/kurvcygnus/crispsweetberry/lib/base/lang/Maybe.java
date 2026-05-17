//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.lang;

import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A extended <u><a href="https://en.wikipedia.org/wiki/Monad_(functional_programming)">Either Monad</a></u>, focusing on
 * storing at least one value that could be one of the 3 specified types.<br><br>
 * If you are looking for doing <u><a href="https://fsharpforfunandprofit.com/rop/"><b>Railway Oriented Programming</b></a></u> stuff, see <u>{@link IResult}</u>,
 * this is not for that thing.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public record Maybe<L, M, R>(@Nullable L left, @Nullable M middle, @Nullable R right) implements INestedPrintable<Maybe<L, M, R>>
{
    private static final Supplier<NoSuchElementException> ERROR = () -> new NoSuchElementException("Value is not present!");
    
    public Maybe
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
    }
    
    public static <L, M, R> @NotNull Maybe<L, M, R> ofLeft(@NotNull L left)
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        return new Maybe<>(left, null, null);
    }
    
    public static <L, M, R> @NotNull Maybe<L, M, R> ofMiddle(@NotNull M middle)
    {
        Objects.requireNonNull(middle, "Param \"middle\" must not be null!");
        return new Maybe<>(null, middle, null);
    }
    
    public static <L, M, R> @NotNull Maybe<L, M, R> ofRight(@NotNull R right)
    {
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
        return new Maybe<>(null, null, right);
    }
    
    public static <L, M, R> @NotNull Maybe<L, M, R> ofLM(@NotNull L left, @NotNull M middle)
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(middle, "Param \"middle\" must not be null!");
        return new Maybe<>(left, middle, null);
    }
    
    public static <L, M, R> @NotNull Maybe<L, M, R> ofLR(@NotNull L left, @NotNull R right)
    {
        Objects.requireNonNull(left, "Param \"left\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
        return new Maybe<>(left, null, right);
    }
    
    public static <L, M, R> @NotNull Maybe<L, M, R> ofMR(@NotNull M middle, @NotNull R right)
    {
        Objects.requireNonNull(middle, "Param \"middle\" must not be null!");
        Objects.requireNonNull(right, "Param \"right\" must not be null!");
        return new Maybe<>(null, middle, right);
    }
    
    @Contract("null, null, null -> fail") public static <L, M, R> @NotNull Maybe<L, M, R> ofNullable(@Nullable L left, @Nullable M middle, @Nullable R right)
        { return new Maybe<>(left, middle, right); }
    
    @Override public @NotNull L left()
    {
        if(left == null)
            throw ERROR.get();
        return left;
    }
    
    public <U> @NotNull U mapLeftOrThrow(Function<? super L, ? extends U> mapper)
    {
        if(left == null)
            throw ERROR.get();
        return mapper.apply(left);
    }
    
    public @NotNull L leftOrDefault(@NotNull L defaultValue)
    {
        if(left == null)
            return defaultValue;
        return left;
    }
    
    @Override public @NotNull M middle()
    {
        if(middle == null)
            throw ERROR.get();
        return middle;
    }
    
    public <U> @NotNull U mapMiddleOrThrow(@NotNull Function<? super M, ? extends U> mapper)
    {
        if(middle == null)
            throw ERROR.get();
        return mapper.apply(middle);
    }
    
    public @NotNull M middleOrDefault(@NotNull M defaultValue)
    {
        if(middle == null)
            return defaultValue;
        return middle;
    }
    
    @Override public @NotNull R right()
    {
        if(right == null)
            throw ERROR.get();
        return right;
    }
    
    public <U> @NotNull U mapRightOrThrow(@NotNull Function<? super R, ? extends U> mapper)
    {
        if(right == null)
            throw ERROR.get();
        return mapper.apply(right);
    }
    
    public @NotNull R rightOrDefault(@NotNull R defaultValue)
    {
        if(right == null)
            return defaultValue;
        return right;
    }
    
    public boolean isLeftPresent() { return left != null; }
    
    public boolean isMiddlePresent() { return middle != null; }
    
    public boolean isRightPresent() { return right != null; }
    
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
    
    public @NotNull Maybe<R, M, L> reverse() { return new Maybe<>(right, middle, left); }
    
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
    
    public <U> @NotNull SequencedSet<U> compress(
        @NotNull Function<? super L, ? extends U> leftMapper,
        @NotNull Function<? super M, ? extends U> middleMapper,
        @NotNull Function<? super R, ? extends U> rightMapper
    ) { return compress(leftMapper, middleMapper, rightMapper, LinkedHashSet::new); }
    
    @Override public @NotNull String toString() { return toNestedString(); }
    
    @Override public @NotNull @Unmodifiable Map<String, Function<Maybe<L, M, R>, @Nullable Object>> getFields()
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
}
