//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.stream;

import it.unimi.dsi.fastutil.longs.LongLongImmutablePair;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.*;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * A lightweight, <b>single-pass push-based, pure functional data stream</b>, semantically equivalent to <u>{@link Consumer Consumer&lt;Consumer&lt;T&gt;&gt;}</u>.
 * <p>
 * Unlike <u>{@link java.util.stream.Stream Stream&lt;T&gt;}</u>, which is designed for complex, potentially parallel pipelines over
 * large datasets with rich terminal operations, {@code Invoker} deliberately focuses on:
 * </p>
 * <ul>
 *     <li><b>Lightweight data flow</b> — no <u>{@link Spliterator}</u>, no intermediate state accumulation,
 *         no framework overhead beyond a lambda per stage.</li>
 *     <li><b>Single-threaded, short call chains</b> — the common case for in-process event dispatch,
 *         callback fan-out, and small-collection processing.</li>
 *     <li><b>Stateless intermediate operations</b> — stateful operations such as {@code findFirst},
 *         {@code distinct}, or {@code sorted} are <span style="color: f84b4b">deliberately omitted</span>.
 *         They conflict with the single-pass, push-based model and would reintroduce the very
 *         complexity this interface avoids.</li>
 * </ul>
 * <p>
 * Under these conditions — <span style="color: 95cc6d">single-threaded, short pipeline, small to moderate
 * data size</span> — {@code Invoker} <b>outperforms</b> {@link java.util.stream.Stream Stream}:
 * </p>
 * <ul>
 *     <li>No <u>{@link Spliterator}</u> allocation or traversal protocol is involved.</li>
 *     <li>Each element is pushed through the <b>entire chain in a single loop iteration</b>,
 *         without building an intermediate <u>{@link Runnable}</u> / <u>{@link java.util.function.Supplier Supplier}</u> ladder.</li>
 *     <li>No boxing overhead for primitive specializations (see the primitive
 *         <u>{@link #unit(int...) unit}</u> factories).</li>
 *     <li>Easy being inlined by Jit. Since {@code Invoker} is obviously used for data trnasformation,
 *     most usage won't be escaped out of a scope, with the attribute of <u>{@link FunctionalInterface}</u>, <b>Jit can do
 *     ratherly aggressive inlining on it.</b></li>
 * </ul>
 * <p>
 * <i>The trade-off is intentional: if you need short-circuiting, non-sequential access, or
 * multi-stage collection, use <u>{@link java.util.stream.Stream Stream}</u> instead. {@code Invoker} owns
 * the "fire-and-forget fan-out" niche — <u>{@link java.util.stream.Stream Stream}</u> owns everything else.</i>
 * </p>
 * @implNote No inheritance of <u>{@link Consumer Consumer&lt;Consumer&lt;T&gt;&gt;}</u>(<i>or, <u>{@link Consumer Consumer&lt;Consumer&lt;? super T&gt;&gt;}</u> LMAO</i>):
 * since down-casting {@code Invoker} and passing it to
 * others is confusing at most cases, and <u>{@link Consumer}</u>'s main usage is completely different from {@code Invoker}.<br>
 * <i>Also, inheriting means the redirecting <u>{@link Consumer#accept(Object)}</u> to <u>{@link #invoke(Consumer)}</u>, which is also stupid,
 * no to mention that the meaning of <u>{@link Consumer#andThen(Consumer)}</u> will be a pure disaster(also, PECS will make you pain on overriding it).</i>
 * @param <T> the type of elements in this invoker
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
@FunctionalInterface public non-sealed interface Invoker<T> extends IBaseInvoker<T, Invoker<T>>
{
    //region Pure Factories
    static @NotNull <T> Invoker<T> unit(T value) { return action -> action.accept(value); }
    
    static @NotNull Invoker.OfInt unit(int value) { return action -> action.accept(value); }

    static @NotNull Invoker.OfLong unit(long value) { return action -> action.accept(value); }

    static @NotNull Invoker.OfDouble unit(double value) { return action -> action.accept(value); }
    
    @SuppressWarnings("unchecked") static @NotNull <T> Invoker<T> empty() { return (Invoker<T>) Invok3r.EMPTY; }
    
    @SafeVarargs static <T> @NotNull Invoker<T> unit(T @NotNull ... values)
    {
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> { for(final T value: values) action.accept(value); };
    }
    
    static @NotNull OfInt unit(int @NotNull ... values)
    {
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> { for(final var val: values) action.accept(val); };
    }
    
    static @NotNull Invoker<Float> unit(float @NotNull ... values)
    {
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> { for(final var val: values) action.accept(val); };
    }
    
    static @NotNull OfDouble unit(double @NotNull ... values)
    {
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> { for(final var val: values) action.accept(val); };
    }
    
    static @NotNull OfLong unit(long @NotNull ... values)
    {
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> { for(final var val: values) action.accept(val); };
    }
    
    static @NotNull Invoker<Boolean> unit(boolean @NotNull ... values)
    {
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> { for(final var val: values) action.accept(val); };
    }
    
    static <T> @NotNull Invoker<T> unit(@NotNull Iterable<? extends T> iterable)
    {
        Objects.requireNonNull(iterable, "Param \"iterable\" must not be null!");
        return action -> { for(final T val: iterable) action.accept(val); };
    }
    //endregion
    
    //region Muti-Thread Pure Factories
    static <T> @NotNull Invoker<T> asyncEmpty(@NotNull ExecutorService executor)
        { return action -> executor.submit(() -> {}); }
    
    static <T> @NotNull Invoker<T> asyncUnit(@NotNull ExecutorService executor, @NotNull T value)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        return action -> executor.submit(() -> action.accept(value));
    }
    
    static @NotNull Invoker.OfInt asyncUnit(@NotNull ExecutorService executor, int value)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        return action -> executor.submit(() -> action.accept(value));
    }

    static @NotNull Invoker.OfLong asyncUnit(@NotNull ExecutorService executor, long value)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        return action -> executor.submit(() -> action.accept(value));
    }

    static @NotNull Invoker.OfDouble asyncUnit(@NotNull ExecutorService executor, double value)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        return action -> executor.submit(() -> action.accept(value));
    }
    
    static <T> @NotNull Invoker<T> asyncUnit(@NotNull ExecutorService executor, @NotNull Iterable<? extends T> iterable)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        Objects.requireNonNull(iterable, "Param \"iterable\" must not be null!");
        return action -> executor.submit(() -> { for(final T val: iterable) action.accept(val); });
    }
    
    @SafeVarargs static <T> @NotNull Invoker<T> asyncUnit(@NotNull ExecutorService executor, T @NotNull ... values)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> executor.submit(() -> { for(final T val: values) action.accept(val); });
    }
    
    static @NotNull OfInt asyncUnit(@NotNull ExecutorService executor, int @NotNull ... values)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> executor.submit(() -> { for(final var val: values) action.accept(val); });
    }
    
    static @NotNull Invoker<Float> asyncUnit(@NotNull ExecutorService executor, float @NotNull ... values)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> executor.submit(() -> { for(final var val: values) action.accept(val); });
    }
    
    static @NotNull OfDouble asyncUnit(@NotNull ExecutorService executor, double @NotNull ... values)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> executor.submit(() -> { for(final var val: values) action.accept(val); });
    }
    
    static @NotNull OfLong asyncUnit(@NotNull ExecutorService executor, long @NotNull ... values)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> executor.submit(() -> { for(final var val: values) action.accept(val); });
    }
    
    static @NotNull Invoker<Boolean> asyncUnit(@NotNull ExecutorService executor, boolean @NotNull ... values)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        Objects.requireNonNull(values, "Param \"values\" must not be null!");
        return action -> executor.submit(() -> { for(final var val: values) action.accept(val); });
    }
    //endregion
    
    //region Intermediate Functions
    /**
     * {@inheritDoc}
     */
    @Override default @NotNull Invoker<T> async(@NotNull ExecutorService executor)
    {
        Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
        return nextAction -> this.invoke(value -> executor.submit(() -> nextAction.accept(value)));
    }
    
    default <R> @NotNull Invoker<R> map(@NotNull Function<? super T, ? extends R> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> nextAction.accept(mapper.apply(value)));
    }
    
    default @NotNull Invoker.OfInt mapToInt(@NotNull ToIntFunction<? super T> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> nextAction.accept(mapper.applyAsInt(value)));
    }
    
    default @NotNull Invoker.OfLong mapToLong(@NotNull ToLongFunction<? super T> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> nextAction.accept(mapper.applyAsLong(value)));
    }
    
    default @NotNull Invoker.OfDouble mapToDouble(@NotNull ToDoubleFunction<? super T> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> nextAction.accept(mapper.applyAsDouble(value)));
    }
    
    default @NotNull Invoker<T> peek(@NotNull Consumer<T> action)
    {
        Objects.requireNonNull(action, "Param \"action\" must not be null!");
        return nextAction -> this.invoke(
            value ->
            {
                action.accept(value);
                nextAction.accept(value);
            }
        );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override default <R> @NotNull Invoker<T> peekMap(@NotNull Function<? super T, ? extends R> mapper, @NotNull BiConsumer<? super T, ? super R> action)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        Objects.requireNonNull(action, "Param \"action\" must not be null!");
        
        return nextAction -> this.invoke(
            value ->
            {
                action.accept(value, mapper.apply(value));
                nextAction.accept(value);
            }
        );
    }
    
    @Override default <R> @NotNull Invoker<T> peekArrayMap(@NotNull Function<? super T, ? extends R[]> mapper, @NotNull BiConsumer<? super T, ? super R> action)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        Objects.requireNonNull(action, "Param \"action\" must not be null!");
        
        return nextAction -> this.invoke(
            value ->
            {
                for(final var element: mapper.apply(value))
                    action.accept(value, element);
                nextAction.accept(value);
            }
        );
    }
    
    @Override default <R> @NotNull Invoker<T> peekIterableMap(@NotNull Function<? super T, ? extends Iterable<R>> mapper, @NotNull BiConsumer<? super T, ? super R> action)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        Objects.requireNonNull(action, "Param \"action\" must not be null!");
        
        return nextAction -> this.invoke(
            value ->
            {
                for(final var element: mapper.apply(value))
                    action.accept(value, element);
                nextAction.accept(value);
            }
        );
    }
    
    /**
     * A specialized intermediate function of <u>{@link #map(Function)}</u>.<br>
     * It accepts any function that produces array as result, then destructs it into flat <u>{@link Invoker}</u> automatically, with a loop, of course.
     * (<i>actually {@code (in T) -> out R[] -> out R}</i>)<br><br>
     * Examples:
     * <pre>{@code
     *  Invoker.unit(creatures).
     *      destructArrayMap(Creature::getAttributes).// Using `#map` gets `Attribute[]`, and using this gets `Attribute`.
     *      map(attr -> ...).
     *      ...
     * }</pre>
     */
    default <R> @NotNull Invoker<R> destructArrayMap(@NotNull Function<? super T, ? extends R[]> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> { for(final var element: mapper.apply(value)) nextAction.accept(element); });
    }
    
    default @NotNull Invoker.OfInt destructIntArrayMap(@NotNull Function<? super T, int[]> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> { for(final var element: mapper.apply(value)) nextAction.accept(element); });
    }
    
    default @NotNull Invoker.OfLong destructLongArrayMap(@NotNull Function<? super T, long[]> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> { for(final var element: mapper.apply(value)) nextAction.accept(element); });
    }
    
    default @NotNull Invoker.OfDouble destructDoubleArrayMap(@NotNull Function<? super T, double[]> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> { for(final var element: mapper.apply(value)) nextAction.accept(element); });
    }
    
    /**
     * A specialized intermediate function of <u>{@link #map(Function)}</u>.<br>
     * It accepts any function that produces <u>{@link Iterable Iterable&lt;T&gt;}</u> as result, then destructs it into flat <u>{@link Invoker}</u> automatically, with a loop, of course.
     * (<i>actually {@code (in T) -> Iterable<out R> -> out R}</i>)<br><br>
     * Examples:
     * <pre>{@code
     *  Invoker.unit(users).
     *      destructArrayMap(User::getOrders).// Using `#map` gets `List<Order>`, and using this gets `Order`.
     *      peek(order -> ...).
     *      ...
     * }</pre>
     */
    default <R> @NotNull Invoker<R> destructIterableMap(@NotNull Function<? super T, ? extends Iterable<R>> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> { for(final var element: mapper.apply(value)) nextAction.accept(element); });
    }
    
    default @NotNull Invoker.OfInt destructIntIterableMap(@NotNull Function<? super T, ? extends Iterable<Integer>> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(
            value ->
            {
                final var iterable = mapper.apply(value);
                
                if(iterable.iterator() instanceof PrimitiveIterator.OfInt intIt)
                    while(intIt.hasNext())
                        nextAction.accept(intIt.nextInt());
                else for(final var element: iterable)
                    nextAction.accept(element);
            }
        );
    }
    
    default @NotNull Invoker.OfLong destructLongIterableMap(@NotNull Function<? super T, ? extends Iterable<Long>> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(
            value ->
            {
                final var iterable = mapper.apply(value);
                
                if(iterable.iterator() instanceof PrimitiveIterator.OfLong longIt)
                    while(longIt.hasNext())
                        nextAction.accept(longIt.nextLong());
                else for(final var element: iterable)
                    nextAction.accept(element);
            }
        );
    }
    
    default @NotNull Invoker.OfDouble destructDoubleIterableMap(@NotNull Function<? super T, ? extends Iterable<Double>> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(
            value ->
            {
                final var iterable = mapper.apply(value);
                
                if(iterable.iterator() instanceof PrimitiveIterator.OfDouble doubleIt)
                    while(doubleIt.hasNext())
                        nextAction.accept(doubleIt.nextDouble());
                else for(final var element: iterable)
                    nextAction.accept(element);
            }
        );
    }
    
    /**
     * @apiNote <u>{@link #destructArrayMap(Function)}</u> and <u>{@link #destructIterableMap(Function)}</u> is more recommended.
     */
    default <R> @NotNull Invoker<R> flatMap(@NotNull Function<? super T, ? extends Invoker<R>> mapper)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return nextAction -> this.invoke(value -> mapper.apply(value).invoke(nextAction));
    }
    
    @Override default @NotNull Invoker<T> filter(@NotNull Predicate<? super T> predicate)
    {
        Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
        return nextAction -> this.invoke(value -> { if(predicate.test(value)) nextAction.accept(value); });
    }

    @Override default <R> @NotNull Invoker<T> peekFilter(@NotNull Function<? super T, ? extends R> mapper, @NotNull BiPredicate<? super T, ? super R> predicate)
    {
        Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
        
        return nextAction -> this.invoke(value -> { if(predicate.test(value, mapper.apply(value))) nextAction.accept(value); });
    }
    //endregion
    
    //region Primitives
    /**
     * Primitive {@code int} specialization of {@link Invoker}. <span style="color: 95cc6d">Intermediate
     * operations work on primitive {@code int} values directly, avoiding boxing.</span>
     *
     * @see Invoker
     * @author Kurv Cygnus
     * @since 1.0 Release
     */
    @FunctionalInterface non-sealed interface OfInt extends IBaseInvoker<Integer, OfInt>
    {
        //region Boxing Bridges
        /**
         * <b>Boxing bridge.</b> If {@code action} is an {@link IntConsumer}, delegates to
         * {@link #invokeAsInt(IntConsumer)} directly without boxing each element. Otherwise
         * falls back to a lambda that boxes via {@link Integer#intValue()}.
         */
        @Override default void invoke(@NotNull Consumer<? super Integer> action)
        {
            if(action instanceof IntConsumer intAction)
                invokeAsInt(intAction);
            else
            {
                Invok3r.LOGGER.debug("Downgrading {} as boxed Integer handle in #invoke.", this.getClass().getSimpleName());
                invokeAsInt(action::accept);
            }
        }
        
        @Override default @NotNull OfInt filter(@NotNull Predicate<? super Integer> predicate)
        {
            if(predicate instanceof IntPredicate intPredicate)
                return filterInt(intPredicate);
            
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
            return nextAction -> this.invokeAsInt(
                value ->
                {
                    Invok3r.LOGGER.debug("Downgrading {} as boxed Integer handle in #filter.", this.getClass().getSimpleName());
                    
                    if(predicate.test(value))
                        nextAction.accept(value);
                }
            );
        }
        //endregion

        //region Intermediate Functions
        /**
         * {@inheritDoc}
         */
        @Override default @NotNull OfInt async(@NotNull ExecutorService executor)
        {
            Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
            return nextAction -> this.invokeAsInt(value -> executor.submit(() -> nextAction.accept(value)));
        }
        
        default @NotNull OfInt peek(@NotNull IntConsumer action)
        {
            Objects.requireNonNull(action, "Param \"action\" must not be null!");
            return nextAction -> this.invokeAsInt(value -> { action.accept(value); nextAction.accept(value); });
        }
        
        /**
         * {@inheritDoc}
         */
        @Override default @NotNull <R> OfInt peekMap(@NotNull Function<? super Integer, ? extends R> mapper, @NotNull BiConsumer<? super Integer, ? super R> action)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(action, "Param \"action\" must not be null!");

            return nextAction -> this.invokeAsInt(value -> { action.accept(value, mapper.apply(value)); nextAction.accept(value); });
        }

        @Override default @NotNull <R> OfInt peekArrayMap(
            @NotNull Function<? super Integer, ? extends R[]> mapper,
            @NotNull BiConsumer<? super Integer, ? super R> action
        )
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(action, "Param \"action\" must not be null!");

            return nextAction -> this.invokeAsInt(
                value ->
                {
                    for(final var element: mapper.apply(value))
                        action.accept(value, element);
                    nextAction.accept(value);
                }
            );
        }

        @Override default @NotNull <R> OfInt peekIterableMap(
            @NotNull Function<? super Integer, ? extends Iterable<R>> mapper,
            @NotNull BiConsumer<? super Integer, ? super R> action
        )
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(action, "Param \"action\" must not be null!");

            return nextAction -> this.invokeAsInt(
                value ->
                {
                    for(final var element: mapper.apply(value))
                        action.accept(value, element);
                    nextAction.accept(value);
                }
            );
        }

        default @NotNull OfInt filterInt(@NotNull IntPredicate predicate)
        {
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
            return nextAction -> this.invokeAsInt(value -> { if(predicate.test(value)) nextAction.accept(value); });
        }

        @Override default <R> @NotNull OfInt peekFilter(
            @NotNull Function<? super Integer, ? extends R> mapper,
            @NotNull BiPredicate<? super Integer, ? super R> predicate
        )
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");

            return nextAction -> this.invokeAsInt(value -> { if(predicate.test(value, mapper.apply(value))) nextAction.accept(value); });
        }

        default @NotNull OfInt peekFilter(@NotNull IntUnaryOperator mapper, @NotNull IntPredicate predicate)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
            return nextAction -> this.invokeAsInt(value -> { if(predicate.test(mapper.applyAsInt(value))) nextAction.accept(value); });
        }

        default @NotNull OfInt map(@NotNull IntUnaryOperator mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsInt(value -> nextAction.accept(mapper.applyAsInt(value)));
        }

        default @NotNull OfLong mapToLong(@NotNull IntToLongFunction mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsInt(value -> nextAction.accept(mapper.applyAsLong(value)));
        }

        default @NotNull OfDouble mapToDouble(@NotNull IntToDoubleFunction mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsInt(value -> nextAction.accept(mapper.applyAsDouble(value)));
        }
        
        default <R> @NotNull Invoker<R> mapToObject(@NotNull IntFunction<? extends R> mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsInt(value -> nextAction.accept(mapper.apply(value)));
        }
        //endregion

        //region Transformation
        default @NotNull Invoker<Integer> boxed() { return this::invoke; }
        //endregion

        //region Terminal / Abstract
        void invokeAsInt(@NotNull IntConsumer action);
        
        default int sum(boolean isMultiThread)
        {
            if(!isMultiThread)
                return sum();
            
            final var adder = new LongAdder();
            invokeAsInt(adder::add);
            return adder.intValue();
        }
        
        default int sum()
        {
            final int[] ref = {0};
            invokeAsInt(value -> ref[0] += value);
            return ref[0];
        }
        
        default int multiplied(boolean isMultiThread)
        {
            if(!isMultiThread)
                return multiplied();
            
            final var ref = new AtomicInteger(1);
            invokeAsInt(
                value ->
                {
                    while(true)
                    {
                        final var current = ref.get();
                        final var next = current * value;
                        
                        if(ref.compareAndSet(current, next))
                            return;
                    }
                }
            );
            
            return ref.get();
        }
        
        default int multiplied()
        {
            final int[] ref = {1};
            invokeAsInt(value -> ref[0] *= value);
            return ref[0];
        }
        
        default int averaged(boolean isMultiThread)
        {
            if(!isMultiThread)
                return averaged();
            
            final var sum = new LongAdder();
            final var counter = new LongAdder();
            
            invokeAsInt(
                value ->
                {
                    counter.increment();
                    sum.add(value);
                }
            );
            
            return sum.intValue() / counter.intValue();
        }
        
        default int averaged()
        {
            final int[] ref = {0, 0};
            invokeAsInt(value -> { ref[0] += value; ref[1]++; });
            return ref[0] / ref[1];
        }
        //endregion
    }

    /**
     * Primitive {@code long} specialization of {@link Invoker}. <span style="color: 95cc6d">Intermediate
     * operations work on primitive {@code long} values directly, avoiding boxing.</span>
     *
     * @see Invoker
     * @author Kurv Cygnus
     * @since 1.0 Release
     */
    @FunctionalInterface non-sealed interface OfLong extends IBaseInvoker<Long, OfLong>
    {
        //region Boxing Bridges
        /**
         * <b>Boxing bridge.</b> If {@code action} is a {@link LongConsumer}, delegates to
         * {@link #invokeAsLong(LongConsumer)} directly without boxing each element. Otherwise
         * falls back to a lambda that boxes via {@link Long#longValue()}.
         */
        @Override default void invoke(@NotNull Consumer<? super Long> action)
        {
            if(action instanceof LongConsumer longAction)
                invokeAsLong(longAction);
            else
            {
                Invok3r.LOGGER.debug("Downgrading {} as boxed Long handle in #invoke.", this.getClass().getSimpleName());
                invokeAsLong(action::accept);
            }
        }
        
        @Override default @NotNull OfLong filter(@NotNull Predicate<? super Long> predicate)
        {
            if(predicate instanceof LongPredicate longPredicate)
                return filterLong(longPredicate);
            
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
            return nextAction -> this.invokeAsLong(
                value ->
                {
                    Invok3r.LOGGER.debug("Downgrading {} as boxed Long handle in #filter.", this.getClass().getSimpleName());
                    if(predicate.test(value))
                        nextAction.accept(value);
                }
            );
        }
        //endregion

        //region Intermediate Functions
        /**
         * {@inheritDoc}
         */
        @Override default @NotNull OfLong async(@NotNull ExecutorService executor)
        {
            Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
            return nextAction -> this.invokeAsLong(value -> executor.submit(() -> nextAction.accept(value)));
        }
        
        default @NotNull OfLong peek(@NotNull LongConsumer action)
        {
            Objects.requireNonNull(action, "Param \"action\" must not be null!");
            return nextAction -> this.invokeAsLong(
                value ->
                {
                    action.accept(value);
                    nextAction.accept(value);
                }
            );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override default @NotNull <R> OfLong peekMap(@NotNull Function<? super Long, ? extends R> mapper, @NotNull BiConsumer<? super Long, ? super R> action)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(action, "Param \"action\" must not be null!");

            return nextAction -> this.invokeAsLong(
                value ->
                {
                    action.accept(value, mapper.apply(value));
                    nextAction.accept(value);
                }
            );
        }

        @Override @NotNull default <R> OfLong peekArrayMap(
            @NotNull Function<? super Long, ? extends R[]> mapper,
            @NotNull BiConsumer<? super Long, ? super R> action
        )
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(action, "Param \"action\" must not be null!");

            return nextAction -> this.invokeAsLong(
                value ->
                {
                    for(final var element: mapper.apply(value))
                        action.accept(value, element);
                    nextAction.accept(value);
                }
            );
        }

        @Override @NotNull default <R> OfLong peekIterableMap(
            @NotNull Function<? super Long, ? extends Iterable<R>> mapper,
            @NotNull BiConsumer<? super Long, ? super R> action
        )
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(action, "Param \"action\" must not be null!");

            return nextAction -> this.invokeAsLong(
                value ->
                {
                    for(final var element: mapper.apply(value))
                        action.accept(value, element);
                    nextAction.accept(value);
                }
            );
        }

        default @NotNull OfLong filterLong(@NotNull LongPredicate predicate)
        {
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
            return nextAction -> this.invokeAsLong(value -> { if(predicate.test(value)) nextAction.accept(value); });
        }

        @Override default <R> @NotNull OfLong peekFilter(
            @NotNull Function<? super Long, ? extends R> mapper,
            @NotNull BiPredicate<? super Long, ? super R> predicate
        )
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");

            return nextAction -> this.invokeAsLong(value -> { if(predicate.test(value, mapper.apply(value))) nextAction.accept(value); });
        }

        default @NotNull OfLong peekFilter(@NotNull LongUnaryOperator mapper, @NotNull LongPredicate predicate)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
            return nextAction -> this.invokeAsLong(value -> { if(predicate.test(mapper.applyAsLong(value))) nextAction.accept(value); });
        }

        default @NotNull OfLong map(@NotNull LongUnaryOperator mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsLong(value -> nextAction.accept(mapper.applyAsLong(value)));
        }

        default @NotNull OfInt mapToInt(@NotNull LongToIntFunction mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsLong(value -> nextAction.accept(mapper.applyAsInt(value)));
        }

        default @NotNull OfDouble mapToDouble(@NotNull LongToDoubleFunction mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsLong(value -> nextAction.accept(mapper.applyAsDouble(value)));
        }
        
        default <R> @NotNull Invoker<R> mapToObject(@NotNull LongFunction<? extends R> mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsLong(value -> nextAction.accept(mapper.apply(value)));
        }
        //endregion

        //region Transformation
        default @NotNull Invoker<Long> boxed() { return this::invoke; }
        //endregion

        //region Terminal / Abstract
        void invokeAsLong(@NotNull LongConsumer action);
        
        default long sum(boolean isMultiThread)
        {
            if(!isMultiThread)
                return sum();
            
            final var adder = new LongAdder();
            invokeAsLong(adder::add);
            return adder.sum();
        }
        
        default long sum()
        {
            final long[] ref = {0};
            invokeAsLong(value -> ref[0] += value);
            return ref[0];
        }
        
        default long multiplied(boolean isMultiThread)
        {
            if(!isMultiThread)
                return multiplied();
            
            final var ref = new AtomicLong(1);
            invokeAsLong(
                value ->
                {
                    while(true)
                    {
                        final var current = ref.get();
                        final var next = current * value;
                        
                        if(ref.compareAndSet(current, next))
                            return;
                    }
                }
            );
            
            return ref.get();
        }
        
        default long multiplied()
        {
            final long[] ref = {1};
            invokeAsLong(value -> ref[0] *= value);
            return ref[0];
        }
        
        default long averaged(boolean isMultiThread)
        {
            if(!isMultiThread)
                return averaged();
            
            final var ref = new AtomicReference<>(new LongLongImmutablePair(0, 0));
            
            final var sum = new LongAdder();
            final var counter = new LongAdder();
            
            invokeAsLong(
                value ->
                {
                    counter.increment();
                    sum.add(value);
                }
            );
            
            return sum.sum() / counter.sum();
        }
        
        default long averaged()
        {
            final long[] ref = {0, 0};
            invokeAsLong(value -> { ref[0] += value; ref[1]++; });
            return ref[0] / ref[1];
        }
        //endregion
    }
    
    /**
     * Primitive {@code double} specialization of {@link Invoker}. <span style="color: 95cc6d">Intermediate
     * operations work on primitive {@code double} values directly, avoiding boxing.</span>
     *
     * @see Invoker
     * @author Kurv Cygnus
     * @since 1.0 Release
     */
    @FunctionalInterface non-sealed interface OfDouble extends IBaseInvoker<Double, OfDouble>
    {
        //region Boxing Bridges
        /**
         * <b>Boxing bridge.</b> If {@code action} is a {@link DoubleConsumer}, delegates to
         * {@link #invokeAsDouble(DoubleConsumer)} directly without boxing each element. Otherwise
         * falls back to a lambda that boxes via {@link Double#doubleValue()}.
         */
        @Override default void invoke(@NotNull Consumer<? super Double> action)
        {
            if(action instanceof DoubleConsumer doubleAction)
                invokeAsDouble(doubleAction);
            else
            {
                Invok3r.LOGGER.debug("Downgrading {} as boxed Double handle in #invoke.", this.getClass().getSimpleName());
                invokeAsDouble(action::accept);
            }
        }
        
        @Override default @NotNull OfDouble filter(@NotNull Predicate<? super Double> predicate)
        {
            if(predicate instanceof DoublePredicate doublePredicate)
                return filterDouble(doublePredicate);
            
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
            return nextAction -> this.invokeAsDouble(
                value ->
                {
                    Invok3r.LOGGER.debug("Downgrading {} as boxed Double handle in #filter.", this.getClass().getSimpleName());
                    if(predicate.test(value))
                        nextAction.accept(value);
                }
            );
        }
        //endregion

        //region Intermediate Functions
        /**
         * {@inheritDoc}
         */
        @Override default @NotNull OfDouble async(@NotNull ExecutorService executor)
        {
            Objects.requireNonNull(executor, "Param \"executor\" must not be null!");
            return nextAction -> this.invokeAsDouble(value -> executor.submit(() -> nextAction.accept(value)));
        }
        
        default @NotNull OfDouble peek(@NotNull DoubleConsumer action)
        {
            Objects.requireNonNull(action, "Param \"action\" must not be null!");
            return nextAction -> this.invokeAsDouble(
                value ->
                {
                    action.accept(value);
                    nextAction.accept(value);
                }
            );
        }
        
        /**
         * {@inheritDoc}
         */
        @Override default @NotNull <R> OfDouble peekMap(@NotNull Function<? super Double, ? extends R> mapper, @NotNull BiConsumer<? super Double, ? super R> action)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(action, "Param \"action\" must not be null!");

            return nextAction -> this.invokeAsDouble(
                value ->
                {
                    action.accept(value, mapper.apply(value));
                    nextAction.accept(value);
                }
            );
        }

        @Override @NotNull default <R> OfDouble peekArrayMap(
            @NotNull Function<? super Double, ? extends R[]> mapper,
            @NotNull BiConsumer<? super Double, ? super R> action
        )
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(action, "Param \"action\" must not be null!");

            return nextAction -> this.invokeAsDouble(
                value ->
                {
                    for(final var element: mapper.apply(value))
                        action.accept(value, element);
                    nextAction.accept(value);
                }
            );
        }

        @Override @NotNull default <R> OfDouble peekIterableMap(
            @NotNull Function<? super Double, ? extends Iterable<R>> mapper,
            @NotNull BiConsumer<? super Double, ? super R> action
        )
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(action, "Param \"action\" must not be null!");

            return nextAction -> this.invokeAsDouble(
                value ->
                {
                    for(final var element: mapper.apply(value))
                        action.accept(value, element);
                    nextAction.accept(value);
                }
            );
        }

        default @NotNull OfDouble filterDouble(@NotNull DoublePredicate predicate)
        {
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
            return nextAction -> this.invokeAsDouble(value -> { if(predicate.test(value)) nextAction.accept(value); });
        }

        @Override default <R> @NotNull OfDouble peekFilter(
            @NotNull Function<? super Double, ? extends R> mapper,
            @NotNull BiPredicate<? super Double, ? super R> predicate
        )
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");

            return nextAction -> this.invokeAsDouble(
                value ->
                {
                    if(predicate.test(value, mapper.apply(value)))
                        nextAction.accept(value);
                }
            );
        }

        default @NotNull OfDouble peekFilter(@NotNull DoubleUnaryOperator mapper, @NotNull DoublePredicate predicate)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
            return nextAction -> this.invokeAsDouble(
                value ->
                {
                    final double mapped = mapper.applyAsDouble(value);
                    if(predicate.test(mapped))
                        nextAction.accept(value);
                }
            );
        }

        default @NotNull OfDouble map(@NotNull DoubleUnaryOperator mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsDouble(value -> nextAction.accept(mapper.applyAsDouble(value)));
        }

        default @NotNull OfInt mapToInt(@NotNull DoubleToIntFunction mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsDouble(value -> nextAction.accept(mapper.applyAsInt(value)));
        }

        default @NotNull OfLong mapToLong(@NotNull DoubleToLongFunction mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsDouble(value -> nextAction.accept(mapper.applyAsLong(value)));
        }
        
        default <R> @NotNull Invoker<R> mapToObject(@NotNull DoubleFunction<? extends R> mapper)
        {
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
            return nextAction -> this.invokeAsDouble(value -> nextAction.accept(mapper.apply(value)));
        }
        //endregion

        //region Transformation
        default @NotNull Invoker<Double> boxed() { return this::invoke; }
        //endregion

        //region Terminal / Abstract
        void invokeAsDouble(@NotNull DoubleConsumer action);
        
        default double sum(boolean isMultiThread)
        {
            if(!isMultiThread)
                return sum();
            
            final var adder = new DoubleAdder();
            invokeAsDouble(adder::add);
            return adder.sum();
        }
        
        default double sum()
        {
            final double[] ref = {0.};
            invokeAsDouble(value -> ref[0] += value);
            return ref[0];
        }
        
        default double multiplied(boolean isMultiThread)
        {
            if(!isMultiThread)
                return multiplied();
            
            final var ref = new AtomicLong(1);
            invokeAsDouble(
                value ->
                {
                    while(true)
                    {
                        final var current = ref.get();
                        final var next = current * Double.doubleToLongBits(value);
                        
                        if(ref.compareAndSet(current, next))
                            return;
                    }
                }
            );
            
            return Double.longBitsToDouble(ref.get());
        }
        
        default double multiplied()
        {
            final double[] ref = {1.};
            invokeAsDouble(value -> ref[0] *= value);
            return ref[0];
        }
        
        default double averaged(boolean isMultiThread)
        {
            if(!isMultiThread)
                return averaged();
            
            final var sum = new DoubleAdder();
            final var counter = new LongAdder();
            
            invokeAsDouble(
                value ->
                {
                    counter.increment();
                    sum.add(value);
                }
            );
            
            return sum.sum() / counter.sum();
        }
        
        default double averaged()
        {
            final double[] ref = {0., 0.};
            invokeAsDouble(value -> { ref[0] += value; ref[1]++; });
            return ref[0] / ref[1];
        }
        //endregion
    }
    //endregion
}

/**
 * The common base interface for the <u>{@link Invoker}</u> family, parameterized by a self-type {@code TInvoker}
 * to enable return-type preservation in subtypes.
 * <p>
 * This interface mirrors the role of <u>{@link java.util.Spliterator.OfPrimitive}</u>
 * and <u>{@link java.util.stream.BaseStream BaseStream}</u> in the JDK — it captures the single
 * fundamental operation shared by all invokers without committing to element boxing or
 * intermediate-operations.
 * </p>
 *
 * @param <TType> the element type
 * @param <TInvoker> the self type of the implementing invoker (e.g. <u>{@link Invoker Invoker&lt;TType&gt;}</u>,
 *            <u>{@link Invoker.OfInt OfInt}</u>, <u>{@link Invoker.OfLong OfLong}</u>,
 *            <u>{@link Invoker.OfDouble OfDouble}</u>)
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
sealed interface IBaseInvoker<TType, TInvoker extends IBaseInvoker<TType, TInvoker>>
{
    //region Intermediate Functions
    /**
     * Makes the operations after this function's invoke asynchronous by submit the following operations to the <u>{@link ExecutorService}</u>
     * you specified.
     */
    @NotNull TInvoker async(@NotNull ExecutorService executor);
    
    /**
     * Do some operations as intermediately, but with another temporary variable which is produced from {@code mapper},
     * making some cases that needs both target data and its sub data simpler to write.<br><br>
     * Example:
     * <pre>{@code
     *  private static final Map<User, Foo> MAYBE_SOME_CACHE_MAP = new HashMap<>();
     *
     *  // ...
     *
     *  Invoker.unit(users).
     *      peekMap(User::getFoo, MAYBE_SOME_CACHE_MAP::put).
     *      filter(User::isLegal).
     *      reduce(...);
     * }</pre>
     */
    <R> @NotNull TInvoker peekMap(@NotNull Function<? super TType, ? extends R> mapper, @NotNull BiConsumer<? super TType, ? super R> action);
    
    <R> @NotNull TInvoker peekArrayMap(@NotNull Function<? super TType, ? extends R[]> mapper, @NotNull BiConsumer<? super TType, ? super R> action);
    
    <R> @NotNull TInvoker peekIterableMap(@NotNull Function<? super TType, ? extends Iterable<R>> mapper, @NotNull BiConsumer<? super TType, ? super R> action);
    
    @NotNull TInvoker filter(@NotNull Predicate<? super TType> predicate);
    
    <R> @NotNull TInvoker peekFilter(@NotNull Function<? super TType, ? extends R> mapper, @NotNull BiPredicate<? super TType, ? super R> predicate);
    //endregion
    
    //region Terminal Operations
    /**
     * <b>Terminal operation.</b> Pushes every element managed by this invoker to the given {@code action},
     * one by one, in the calling thread.
     * <p>
     * This is the single sink of the pipeline; after this method returns, the invoker is consumed and
     * should not be reused (unless it was created from a repeatable source such as
     * <u>{@link Invoker#unit(Object) unit(TType)}</u>).
     * </p>
     *
     * @param action the element consumer — will be invoked once per element
     */
    void invoke(@NotNull Consumer<? super TType> action);
    
    default @NotNull @Unmodifiable List<TType> toList(boolean isMultiThread)
    {
        final List<TType> result = isMultiThread ? new CopyOnWriteArrayList<>() : new ArrayList<>();
        this.invoke(result::add);
        return Collections.unmodifiableList(result);
    }
    
    default @NotNull @Unmodifiable List<TType> toList() { return toList(false); }
    
    default @NotNull @Unmodifiable Set<TType> toSet(boolean isMultiThread)
    {
        final Set<TType> result = isMultiThread ? ConcurrentHashMap.newKeySet() : new HashSet<>();
        this.invoke(result::add);
        return Collections.unmodifiableSet(result);
    }
    
    default @NotNull @Unmodifiable Set<TType> toSet() { return toSet(false); }
    
    //? Currently not thread safe.
    default <A, R> @NotNull R collect(@NotNull Collector<? super TType, A, R> collector)
    {
        Objects.requireNonNull(collector, "Param \"collector\" must not be null!");
        final A container = collector.supplier().get();
        invoke(value -> collector.accumulator().accept(container, value));
        return collector.finisher().apply(container);
    }
    
    default TType reduce(@NotNull TType identity, @NotNull BinaryOperator<TType> reducer)
    {
        Objects.requireNonNull(identity, "Param \"identity\" must not be null!");
        Objects.requireNonNull(reducer, "Param \"reducer\" must not be null!");
        final var ref = new Ref<>(identity);
        invoke(value -> ref.reduceMutate(value, reducer));
        
        return ref.unwrap();
    }
    
    default TType reduce(@NotNull TType identity, @NotNull BinaryOperator<TType> reducer, boolean isMultiThread)
    {
        if(!isMultiThread)
            return reduce(identity, reducer);
        
        Objects.requireNonNull(identity, "Param \"identity\" must not be null!");
        Objects.requireNonNull(reducer, "Param \"reducer\" must not be null!");
        
        final var ref = new AtomicReference<>(identity);
        invoke(
            value ->
            {
                while(true)
                {
                    final var current = ref.get();
                    final var next = reducer.apply(current, value);
                    
                    if(ref.compareAndSet(current, next))
                        return;
                }
            }
        );
        
        return ref.get();
    }
    
    default @NotNull Optional<TType> reduce(@NotNull BinaryOperator<TType> reducer)
    {
        Objects.requireNonNull(reducer, "Param \"reducer\" must not be null!");
        
        final var ref = new Ref<TType>(null);
        invoke(
            value ->
            {
                if(ref.unwrap() == null)
                {
                    ref.mutate(value);
                    return;
                }
                
                ref.reduceMutate(value, reducer);
            }
        );
        
        return Optional.ofNullable(ref.unwrap());
    }
    
    default @NotNull Optional<TType> reduce(@NotNull BinaryOperator<TType> reducer, boolean isMultiThread)
    {
        if(!isMultiThread)
            return reduce(reducer);
        
        Objects.requireNonNull(reducer, "Param \"reducer\" must not be null!");
        final var ref = new AtomicReference<TType>(null);
        invoke(
            value ->
            {
                while(true)
                {
                    final var current = ref.get();
                    final var next = current != null ? reducer.apply(current, value) : value;
                    
                    if(ref.compareAndSet(current, next))
                        return;
                }
            }
        );
        
        return Optional.ofNullable(ref.get());
    }
    //endregion
}

/**
 * Package-private holder of the singleton empty <u>{@link Invoker}</u>.<br>
 * Not meant to be instantiated or referenced externally.
 * @since 1.0 Release
 */
final class Invok3r
{
    private Invok3r() { throw new IllegalAccessError("Class \"Invok3r\" is not meant to be instantized!"); }
    static final Invoker<Object> EMPTY = __ -> {};
    static final IMarkLogger LOGGER = IMarkLogger.marklessLogger();
}

/**
 * For <u>{@link IBaseInvoker#reduce}</u> methods on single-threading case.<br>
 * No <u>{@link AtomicReference}</u>'s Muti-Thread check, no {@code T[]}'s varargs static method creation trick, {@code length} field and access boundary check.
 * @since 1.0 Release
 */
final class Ref<T>
{
    private @Nullable T val;
    
    Ref(@Nullable T val) { this.val = val; }
    
    @Nullable T unwrap() { return val; }
    
    void mutate(@NotNull T val) { this.val = val; }
    
    void reduceMutate(@NotNull T val, @NotNull BinaryOperator<T> reducer) { this.val = reducer.apply(this.val, val); }
}
