//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.exceptions;

import kurvcygnus.crispsweetberry.lib.base.lang.IResult;
import kurvcygnus.crispsweetberry.lib.base.util.AssertUtils;
import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;

/**
 * A <b>concrete, structured runtime exception</b> that wraps any <u>{@link Throwable}</u> with a
 * {@linkplain #tag() type tag} for categorical error handling.<br>
 * This is the <b>default implementation</b> of <u>{@link IStructuredThrowable}</u>, and the
 * recommended base class for all custom exceptions in this project's ROP usage.<hr>
 * <p><b>Role in <u>{@link kurvcygnus.crispsweetberry.lib.base.lang.IResult IResult}</u> pipelines:</b></p>
 * <ul>
 *     <li>This class provides <b>factory methods</b> (<u>{@link #failedResult(Throwable, String)}</u>,
 *         <u>{@link #failedResult(String, Function, String)}</u>) that directly create
 *         {@code IResult<T, StructuredException>} instances — the most convenient way to
 *         <b>enter the failure pipeline</b> from a caught or constructed exception.</li>
 *     <li>Because <u>{@link IResult}</u> uses {@code E extends Throwable}, and
 *         {@code StructuredException extends RuntimeException}, it fits naturally as the type
 *         argument {@code E} with zero extra declaration burden.
 *         <i>See <u>{@link IResult}</u>'s &#64;apiNote about Java's generic type inference.</i></li>
 *     <li>Subtypes that implement <u>{@link IDetailedThrowable}</u> or <u>{@link ITransactionalThrowable}</u>
 *         can extend this class to inherit the formatting, validation, and type-tag semantics
 *         while adding richer error-recovery capabilities.</li>
 * </ul>
 * <p><b>What it adds to exceptions:</b></p>
 * <ul>
 *     <li>A <b>formatted message</b> in the pattern {@code <SimpleName:Tag> message},
 *         making logs and stack traces immediately self-descriptive.</li>
 *     <li><span style="color: f84b4b">Validation guards</span> against wrapping another
 *         {@link IStructuredThrowable} (prevents double-wrapping) and against blank type tags.</li>
 *     <li>A <b>non-null contract</b> on <u>{@link #getMessage()}</u> — overridden as {@code final}
 *         to guarantee the message is never null, unlike the base <u>{@link Throwable#getMessage()}</u>.</li>
 * </ul>
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see IStructuredThrowable
 */
public class StructuredException extends RuntimeException implements IStructuredThrowable
{
    private final Throwable wrappedException;
    private final String tag;

    /**
     * Constructs a new structured exception that wraps the given throwable with a type tag.
     * @param wrappedException the original exception to wrap, must not be null and must not
     *                         implement <u>{@link IStructuredThrowable}</u>
     * @param tag             a non-blank categorical type tag (e.g. {@code "NETWORK"})
     * @throws NullPointerException     if either argument is null
     * @throws IllegalArgumentException if {@code wrappedException} already implements
     *                                  <u>{@link IStructuredThrowable}</u>, or if {@code type} is blank
     */
    public StructuredException(@NotNull Throwable wrappedException, @NotNull String tag)
    {
        super(
            TextUtils.format(
                "<{}:{}> {}",
                checkEx(wrappedException).getClass().getSimpleName(),
                AssertUtils.nonBlank(tag),
                wrappedException.getMessage()
            ),
            wrappedException
        );

        this.wrappedException = wrappedException;
        this.tag = tag;
    }

    /**
     * Creates a failed <u>{@link IResult}</u> by wrapping the given throwable into a <u>{@link StructuredException}</u>.
     * @param wrappedException the original exception to wrap, must not be null
     * @param type             a non-blank type tag
     * @param <T>              the value type parameter of the returned {@link IResult} (unused)
     * @return <u>{@link IResult#ofFailed}</u> containing the new {@link StructuredException}
     */
    public static <T> @NotNull IResult<T, StructuredException> failedResult(@NotNull Throwable wrappedException, @NotNull String type)
        { return IResult.ofFailed(new StructuredException(wrappedException, type)); }

    /**
     * Creates a failed <u>{@link IResult}</u> by constructing a <u>{@link Throwable}</u> from the given message
     * via the provided factory, then wrapping it into a <u>{@link StructuredException}</u>.
     * @param message           the detail message for the inner exception
     * @param exceptionFactory  a factory that creates a <u>{@link Throwable}</u> from the message
     * @param type              a non-blank type tag
     * @param <T>               the value type parameter of the returned <u>{@link IResult}</u> (unused)
     * @return <u>{@link IResult#ofFailed}</u> containing the new <u>{@link StructuredException}</u>
     * @throws NullPointerException if any argument is null
     */
    public static <T> @NotNull IResult<T, StructuredException> failedResult(@NotNull String message, @NotNull Function<String, Throwable> exceptionFactory, @NotNull String type)
    {
        Objects.requireNonNull(message, "Param \"message\" must not be null!");
        Objects.requireNonNull(exceptionFactory, "Param \"exceptionFactory\" must not be null!");
        return IResult.ofFailed(new StructuredException(exceptionFactory.apply(message), type));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public @NotNull Throwable cause() { return wrappedException; }
    
    /**
     * {@inheritDoc}
     */
    @Override public @NotNull String tag() { return tag; }
    
    private static @NotNull Throwable checkEx(@NotNull Throwable wrappedException) throws NullPointerException, IllegalArgumentException
    {
        Objects.requireNonNull(wrappedException, "Param \"wrappedException\" must not be null!");
        
        if(wrappedException instanceof IStructuredThrowable)
            throw new IllegalArgumentException("Wrapping a structured exception is not allowed!");
        
        AssertUtils.nonBlank(wrappedException.getMessage());
        
        return wrappedException;
    }
    
    /**
     * @implNote The original <u>{@link Throwable#getMessage() method}</u>'s result is <b>nullable</b>(<i>despite it has no annotation, and usually won't happen</i>),
     * and this exception's message is clearly 100% NotNull, so we rewrite this method with <u>{@link NotNull annotation}</u>, with also a {@code final} attribute
     * to prevent message edit.
     */
    @Override public final @NotNull String getMessage() { return super.getMessage(); }
}
