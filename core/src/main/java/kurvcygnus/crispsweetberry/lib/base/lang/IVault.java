//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A data holder which is capable of restricting unexpected, or illegal accesses.
 * @apiNote Vault can be either <b>immutable</b> or <b>mutable</b> with different static factories,
 * notes that <b>both immutable and mutable are thread-safe.</b><br>
 * Also, notes that <b>this is not designed to restrict reflection accesses
 * (<i>In NeoForge environment, reflection will be restricted by NeoForge, which grantees the safety</i>).</b>
 * In that case, you should try <u><a href="https://en.wikipedia.org/wiki/Java_Platform_Module_System">JPMS</a></u>.
 * Defending against reflection will take a lot of efforts and performance, this is not worthy at most cases.
 * @param <TValue> The type of this container's value.
 * @param <TToken> The access token's type.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public sealed interface IVault<TValue, TToken> extends Function<TToken, Optional<TValue>>
{
    /**
     * Creates a <u>{@link IVault vault}</u> instance, whose value is <b>immutable</b>, and the token's match condition is
     * <u>{@link Object#equals(Object)}</u>.
     * @see #ofMutable(Object, Object) Mutable Version
     */
    static <TValue, TToken> @NotNull IVault<TValue, TToken> of(@NotNull TValue value, @NotNull TToken token)
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        Objects.requireNonNull(token, "Param \"token\" must not be null!");
        
        if(value instanceof IVault<?, ?>)
            throw new IllegalArgumentException("Self wrapping is not allowed!");
        
        return new ImmutableVault<>(value, token, t -> t.equals(token));
    }
    
    /**
     * Creates a <u>{@link IVault vault}</u> instance, whose value is <b>immutable</b>, and the token's match condition is
     * the token itself is <b>not null</b>.
     * @see #ofMutableTypeMatchOnly(Object) Mutable Version
     */
    static <TValue, TToken> @NotNull IVault<TValue, TToken> ofTypeMatchOnly(@NotNull TValue value)
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        
        if(value instanceof IVault<?, ?>)
            throw new IllegalArgumentException("Self wrapping is not allowed!");
        
        return new ImmutableVault<>(value, null, null);
    }
    
    /**
     * Creates a <u>{@link IVault vault}</u> instance, whose value is <b>immutable</b>, and the token's match condition is
     * customizable.
     * @apiNote The former param of <u>{@link BiPredicate}</u> is container's token, the latter one is the external one.
     * @see #ofMutableCustomMatch(Object, Object, BiPredicate) Mutable Version
     */
    static <TValue, TToken> @NotNull IVault<TValue, TToken> ofCustomMatch(
        @NotNull TValue value,
        @NotNull TToken token,
        @NotNull BiPredicate<? super TToken, ? super TToken> predicate
    )
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        Objects.requireNonNull(token, "Param \"token\" must not be null!");
        Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
        
        if(value instanceof IVault<?, ?>)
            throw new IllegalArgumentException("Self wrapping is not allowed!");
        
        return new ImmutableVault<>(value, token, t -> predicate.test(token, t));
    }
    
    /**
     * Creates a <u>{@link IVault vault}</u> instance, whose value is <b>mutable</b>, and the token's match condition is
     * <u>{@link Object#equals(Object)}</u>.
     * @see #of(Object, Object) Immutable Version
     */
    static <TValue, TToken> @NotNull IVault<TValue, TToken> ofMutable(@NotNull TValue value, @NotNull TToken token)
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        Objects.requireNonNull(token, "Param \"token\" must not be null!");
        
        if(value instanceof IVault<?, ?>)
            throw new IllegalArgumentException("Self wrapping is not allowed!");
        
        return new MutableVault<>(value, token, t -> t.equals(token));
    }
    
    /**
     * Creates a <u>{@link IVault vault}</u> instance, whose value is <b>mutable</b>, and the token's match condition is
     * the token itself is <b>not null</b>.
     * @see #ofTypeMatchOnly(Object) Immutable Version
     */
    static <TValue, TToken> @NotNull IVault<TValue, TToken> ofMutableTypeMatchOnly(@NotNull TValue value)
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        
        if(value instanceof IVault<?, ?>)
            throw new IllegalArgumentException("Self wrapping is not allowed!");
        
        return new MutableVault<>(value, null, null);
    }
    
    /**
     * Creates a <u>{@link IVault vault}</u> instance, whose value is <b>mutable</b>, and the token's match condition is
     * customizable.
     * @apiNote The former param of <u>{@link BiPredicate}</u> is container's token, the latter one is the external one.
     * @see #ofCustomMatch(Object, Object, BiPredicate) Immutable Version
     */
    static <TValue, TToken> @NotNull IVault<TValue, TToken> ofMutableCustomMatch(
        @NotNull TValue value,
        @NotNull TToken token,
        @NotNull BiPredicate<? super TToken, ? super TToken> predicate
    )
    {
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        Objects.requireNonNull(token, "Param \"token\" must not be null!");
        Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
        
        if(value instanceof IVault<?, ?>)
            throw new IllegalArgumentException("Self wrapping is not allowed!");
        
        return new MutableVault<>(value, token, t -> predicate.test(token, t));
    }
    
    /**
     * Gets the value of this vault holds, as long as the token matches the requirement.
     * @throws IllegalArgumentException When token doesn't matches the requirement.
     */
    @NotNull TValue tryGet(@NotNull TToken token) throws IllegalArgumentException;
    
    /**
     * Gets the value of this vault holds, as long as the token matches the requirement.
     */
    @NotNull Optional<TValue> trySafeGet(@NotNull TToken token);
    
    /**
     * Tweaks the value of this vault, as long as the vault itself is mutable, with token matches the requirement.
     * @return The old value in this vault. If the vault is immutable, the return value will always be {@code null}.
     */
    @Nullable TValue trySet(@NotNull TValue value, @NotNull TToken token);
    
    boolean isMutable();
    
    default @Override @NotNull Optional<TValue> apply(@NotNull TToken token) { return trySafeGet(token); }
}

abstract sealed class BaseVault<TValue, TToken> implements IVault<TValue, TToken>
{
    protected final @Nullable TToken token;
    protected final @NotNull Predicate<TToken> matcher;
    
    BaseVault(@Nullable TToken token, @Nullable Predicate<TToken> matcher)
    {
        this.token = token;
        this.matcher = Objects.requireNonNullElse(matcher, Objects::nonNull);
    }
    
    @Override public final @NotNull TValue tryGet(@NotNull TToken token) throws IllegalArgumentException
    {
        if(matcher.test(token))
            return value();
        
        throw new IllegalArgumentException("Invalid token: " + token);
    }
    
    @Override public @NotNull Optional<TValue> trySafeGet(@NotNull TToken token)
    {
        if(matcher.test(token))
            return Optional.of(value());
        return Optional.empty();
    }
    
    @Override public final @Nullable TValue trySet(@NotNull TValue value, @NotNull TToken token)
    {
        if(!matcher.test(token))
            return null;
        
        return trySetSequence(value);
    }
    
    protected abstract @NotNull TValue value();
    
    protected abstract @Nullable TValue trySetSequence(@NotNull TValue value);
}

final class MutableVault<TValue, TToken> extends BaseVault<TValue, TToken>
{
    private final AtomicReference<TValue> value;
    
    MutableVault(@NotNull TValue value, @Nullable TToken token, @Nullable Predicate<TToken> matcher)
    {
        super(token, matcher);
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        this.value = new AtomicReference<>(value);
    }
    
    @Override protected @NotNull TValue value() { return value.get(); }
    
    @Override protected @Nullable TValue trySetSequence(@NotNull TValue value)
    {
        final var previous = this.value.get();
        this.value.set(value);
        return previous;
    }
    
    @Override public boolean isMutable() { return true; }
}

final class ImmutableVault<TValue, TToken> extends BaseVault<TValue, TToken>
{
    private final TValue value;
    
    ImmutableVault(@NotNull TValue value, @Nullable TToken token, @Nullable Predicate<TToken> matcher)
    {
        super(token, matcher);
        Objects.requireNonNull(value, "Param \"value\" must not be null!");
        this.value = value;
    }
    
    @Override protected @NotNull TValue value() { return value; }
    
    @Override protected @Nullable TValue trySetSequence(@NotNull TValue value) { return null; }
    
    @Override public boolean isMutable() { return false; }
}
