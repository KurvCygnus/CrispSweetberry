//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.lang;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import kurvcygnus.crispsweetberry.lib.base.extensions.BaseNestedPrinter;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static kurvcygnus.crispsweetberry.lib.base.lang.Privates.*;

/**
 * A state-aware container that provides fine-grained control over value assignment and lifecycle.
 * Unlike a standard <u>{@link java.util.concurrent.atomic.AtomicReference AtomicReference}</u> or <u>{@link java.util.Optional Optional}</u>, an {@code ISealableBox}
 * acts as a finite-state machine to enforce write-once or write-limited semantics.
 * <hr>
 * <h3>State Transitions and Lifecycle:</h3>
 * <ul>
 * <li><b>UNBOUND:</b> Initial empty state. Permits transition to {@code ASSIGNABLE} or {@code BOUND}.</li>
 * <li><b>ASSIGNABLE:</b> The value is present and mutable. Multiple calls to {@link #assign(Object)} are allowed.
 * Can be transitioned to {@code BOUND} or {@code SEALED}.</li>
 * <li><b>BOUND:</b> The value is effectively final. No further assignments are permitted.
 * Can be transitioned to {@code SEALED}.</li>
 * <li><b>SEALED:</b> Terminal state. The internal reference is cleared (nullified), and any attempt
 * to access the value throws an {@link IllegalStateException}.</li>
 * </ul>
 * <hr>
 * <h3>Key Use Cases:</h3>
 * <ul>
 * <li><b>Late Initialization:</b> Holding a dependency that is not available at construction but should
 * be immutable once set (via {@link #bound(Object)}).</li>
 * <li><b>Security/Resource Disposal:</b> Using {@link #seal()} to ensure sensitive data is purged
 * from memory and no longer accessible by subsequent logic.</li>
 * <li><b>Configuration Scoping:</b> Allowing a value to be modified during a "warm-up" phase
 * ({@code ASSIGNABLE}) before locking it down for the remainder of the application lifecycle.</li>
 * </ul>
 *
 * @param <T> The type of value held by this container.
 * @author Kurv Cygnus
 * @see IVault
 * @since 1.0 Release
 */
public sealed interface ISealableBox<T>// extends Supplier<T>
{
    /**
     * Creates a <u>{@link ISealableBox}</u> instance that is <b>already bound</b> to the provided value.
     * @apiNote This is faster, <b>but not thread-safe</b>, <u>{@link #ofAtomic(Object) this method}</u> is safe.
     */
    static <T> @NotNull ISealableBox<T> of(@NotNull T value)
        { return new SealableBox<>(Objects.requireNonNull(value, "Param \"value\" must not be null!"), BOUND); }
    
    /**
     * Creates a thread-safe <u>{@link ISealableBox}</u> instance that is <b>already bound</b> to the provided value.
     */
    static <T> @NotNull ISealableBox<T> ofAtomic(@NotNull T value)
        { return new AtomicSealableBox<>(Objects.requireNonNull(value, "Param \"value\" must not be null!"), BOUND); }
    
    /**
     * Creates a <u>{@link ISealableBox}</u> instance that is <b>assignable</b>, initialized with the provided value.
     * @apiNote This is faster, <b>but not thread-safe</b>, <u>{@link #assignableAtomic(Object) this method}</u> is safe.
     */
    static <T> @NotNull ISealableBox<T> assignable(@NotNull T value)
        { return new SealableBox<>(Objects.requireNonNull(value, "Param \"value\" must not be null!"), ASSIGNABLE); }
    
    /**
     * Creates a thread-safe <u>{@link ISealableBox}</u> instance that is <b>assignable</b>, initialized with the provided value.
     */
    static <T> @NotNull ISealableBox<T> assignableAtomic(@NotNull T value)
        { return new AtomicSealableBox<>(Objects.requireNonNull(value, "Param \"value\" must not be null!"), ASSIGNABLE); }
    
    /**
     * Creates an <b>empty</b> <u>{@link ISealableBox}</u> instance with an <b>unbound</b> state.<br>
     * It can be changed to <u>{@link #assignable(Object) assigned}</u>, <u>{@link #bound(Object) bounded}</u>,
     * but not <u>{@link #seal() sealed}</u>.
     * @apiNote This is faster, <b>but not thread-safe</b>, <u>{@link #createAtomic() this method}</u> is safe.
     */
    static <T> @NotNull ISealableBox<T> create() { return new SealableBox<>(null, UNBOUND); }
    
    /**
     * Creates a thread-safe and <b>empty</b> <u>{@link ISealableBox}</u> instance with an <b>unbound</b> state.<br>
     * It can be changed to <u>{@link #assignable(Object) assigned}</u>, <u>{@link #bound(Object) bounded}</u>,
     * but not <u>{@link #seal() sealed}</u>.
     */
    static <T> @NotNull ISealableBox<T> createAtomic() { return new AtomicSealableBox<>(null, UNBOUND); }
    
    /**
     * Creates a <u>{@link ISealableBox}</u> instance from a nullable value.
     * Returns a <b>bound</b> box if the value is present, or an <b>unbound</b> box if null.
     * @apiNote This is faster, <b>but not thread-safe</b>, <u>{@link #ofNullableAtomic(Object) this method}</u> is safe.
     */
    static <T> @NotNull ISealableBox<T> ofNullable(@Nullable T value) { return value != null ? of(value) : create(); }
    
    /**
     * Creates a thread-safe <u>{@link ISealableBox}</u> instance from a nullable value.
     * Returns a <b>bound</b> box if the value is present, or an <b>unbound</b> box if null.
     */
    static <T> @NotNull ISealableBox<T> ofNullableAtomic(@Nullable T value) { return value != null ? ofAtomic(value) : createAtomic(); }
    
    /**
     * Attempts to assign a new value to the box.
     * Assignment is only allowed if the box is <b>unbound</b> or already <b>assignable</b>.
     * @return <b>{@code true}</b> if the assignment was successful.
     */
    @CanIgnoreReturnValue boolean assign(@NotNull T value);
    
    /**
     * Transitions the box to a <b>bound</b> state with the provided value.
     * Binding is a <b>final</b> assignment; once bound, the value cannot be changed via {@link #assign(Object)}.
     * @return <b>{@code true}</b> if the binding was successful.
     */
    @CanIgnoreReturnValue boolean bound(@NotNull T value);
    
    /**
     * Permanently <b>seals</b> the box.
     * Accessing the value of a sealed box will result in an <u>{@link IllegalStateException}</u>.
     * @return <b>{@code true}</b> if the box was successfully sealed.
     * @throws IllegalStateException If the box is <b>unbound</b> and cannot be sealed.
     * @apiNote This method will also change this container's value to {@code null}, even if it got accessed illegally,
     * that still won't changing anything.
     */
    @CanIgnoreReturnValue boolean seal();
    
    @CheckReturnValue default boolean isPresent() { return isAssignable() || isBound(); }
    
    @CheckReturnValue boolean isUnbound();
    
    @CheckReturnValue boolean isAssignable();
    
    @CheckReturnValue boolean isBound();
    
    @CheckReturnValue boolean isSealed();
    
    int hashCode();
    
    boolean equals(@Nullable Object object);
    
    @NotNull String toString();
    
    @NotNull T orThrow();
    
    //? I think making a [[Supplier]] possible of throwing exception at any time may make debugging hard.
    //? If you do want to use [[ISealableBox]] as [[Supplier]], then just write `box::orThrow` and take the risk you'll bring.
    //? It won't bring any crisis, as long as it has been initialized, and the [[Supplier]] you passed is the type of use-then-discard, instead of persistent.
    //@Override default @NotNull T get() { return orThrow(); }
}

@NotThreadSafe final class SealableBox<T> implements ISealableBox<T>, INestedPrintable<SealableBox<T>>
{
    private @Nullable T value;
    private @MagicConstant(valuesFromClass = Privates.class) byte state;
    
    SealableBox(@Nullable T value, @MagicConstant(valuesFromClass = Privates.class) byte state)
    {
        assert state >= UNBOUND && state <= SEALED : "Param \"state\"'s value is illegal: " + state;
        
        if(value instanceof ISealableBox<?>)
            throw new IllegalArgumentException("Self wrapping is not allowed!");
        
        this.value = value;
        this.state = state;
    }
    
    @Override public boolean assign(@NotNull T value)
    {
        assert value != null : "Param \"value\" must not be null!";
        
        if(state == BOUND || state == SEALED)
            return false;
        
        this.value = value;
        
        if(state == UNBOUND)
            state = ASSIGNABLE;
        
        return true;
    }
    
    @Override public boolean bound(@NotNull T value)
    {
        assert value != null : "Param \"value\" must not be null!";
        
        if(state != UNBOUND && state != ASSIGNABLE)
            return false;
        
        this.value = value;
        this.state = BOUND;
        return true;
    }
    
    @Override public boolean seal()
    {
        if(state == UNBOUND)
            throw new IllegalStateException("This container doesn't have a presentable value, it can't be sealed!");
        else if(state != SEALED)
        {
            state = SEALED;
            value = null;
            return true;
        }
        return false;
    }
    
    @Override public boolean isUnbound() { return state == UNBOUND; }
    
    @Override public boolean isAssignable() { return state == ASSIGNABLE; }
    
    @Override public boolean isBound() { return state == BOUND; }
    
    @Override public boolean isSealed() { return state == SEALED; }
    
    @Override public @NotNull T orThrow()
    {
        if(value == null || state == UNBOUND)
            throw new IllegalStateException("Can't get value, because it is sealed, its value is not present!");
        
        if(state == SEALED)
            throw new IllegalStateException("This container is sealed, value access is not allowed!");
        
        return value;
    }
    
    @Override public boolean equals(@Nullable Object object)
    {
        return this == object || object instanceof SealableBox<?> that &&
            Objects.equals(value, that.value) &&
            state == that.state;
    }
    
    @Override public int hashCode() { return Objects.hashCode(value); }
    
    @Override public @NotNull String toString() { return toNestedString(); }
    
    @Override public @NotNull @Unmodifiable INestedFieldMap<SealableBox<T>> getFields()
    {
        return INestedPrintable.buildFieldMap(
            map ->
            {
                map.put("value", box -> box.value);
                map.put("state", box -> getStateName(box.state));
            },
            2
        );
    }
}

@ThreadSafe final class AtomicSealableBox<T> extends BaseNestedPrinter<AtomicSealableBox<T>> implements ISealableBox<T>
{
    private final AtomicReference<AtomicPair<T>> pair;
    
    AtomicSealableBox(@Nullable T value, @MagicConstant(valuesFromClass = Privates.class) byte state)
    {
        assert state >= UNBOUND && state <= SEALED : "Param \"state\"'s value is illegal: " + state;
        
        if(value instanceof ISealableBox<?>)
            throw new IllegalArgumentException("Self wrapping is not allowed!");
        
        this.pair = new AtomicReference<>(new AtomicPair<>(value, state));
    }
    
    @Override public boolean assign(@NotNull T value)
    {
        assert value != null : "Param \"value\" must not be null!";
        
        while(true)
        {
            final var current = current();
            if(current.state == BOUND || current.state == SEALED)
                return false;
            
            final byte nextState = (current.state == UNBOUND) ? ASSIGNABLE : current.state;
            final var next = new AtomicPair<>(value, nextState);
            
            if(pair.compareAndSet(current, next))
                return true;
        }
    }
    
    @Override public boolean bound(@NotNull T value)
    {
        assert value != null : "Param \"value\" must not be null!";
        
        while(true)
        {
            final var current = current();
            if(current.state != UNBOUND && current.state != ASSIGNABLE)
                return false;
            
            final var next = new AtomicPair<>(value, BOUND);
            if(pair.compareAndSet(current, next))
                return true;
        }
    }
    
    @Override public boolean seal()
    {
        while(true)
        {
            final var current = current();
            
            if(current.state == UNBOUND)
                throw new IllegalStateException("This container doesn't have a presentable value, it can't be sealed!");
            if(current.state == SEALED)
                return false;
            
            final var next = new AtomicPair<T>(null, SEALED);
            if(pair.compareAndSet(current, next))
                return true;
        }
    }
    
    private AtomicPair<T> current() { return pair.get(); }
    
    @Override public boolean isUnbound() { return current().state == UNBOUND; }
    
    @Override public boolean isAssignable() { return current().state == ASSIGNABLE; }
    
    @Override public boolean isBound() { return current().state == BOUND; }
    
    @Override public boolean isSealed() { return current().state == SEALED; }
    
    @Override public @NotNull T orThrow()
    {
        final AtomicPair<T> current = current();
        if(current.value == null || current.state == UNBOUND)
            throw new IllegalStateException("Can't get value, because it is sealed, its value is not present!");
        if(current.state == SEALED)
            throw new IllegalStateException("This container is sealed, value access is not allowed!");
        return current.value;
    }
    
    @Override public boolean equals(@Nullable Object object)
    {
        return this == object || object instanceof AtomicSealableBox<?> that &&
            this.pair.get().equals(that.pair.get());
    }
    
    @Override public int hashCode() { return Objects.hashCode(pair.get().value); }
    
    @Override public @NotNull @Unmodifiable INestedFieldMap<AtomicSealableBox<T>> getFields()
    {
        return INestedPrintable.buildFieldMap(
            map ->
            {
                map.put("value", box -> box.current().value);
                map.put("state", box -> getStateName(box.current().state));
            },
            2
        );
    }
    
    private record AtomicPair<T>(@Nullable T value, byte state) {}
}

final class Privates
{
    static final byte UNBOUND = 0;
    static final byte ASSIGNABLE = 1;
    static final byte BOUND = 2;
    static final byte SEALED = 3;
    
    static @NotNull String getStateName(byte state)
    {
        return switch(state)
        {
            case UNBOUND -> "UNBOUND";
            case ASSIGNABLE -> "ASSIGNABLE";
            case BOUND -> "BOUND";
            case SEALED -> "SEALED";
            default -> throw new IllegalStateException("Unexpected value: " + state);
        };
    }
}
