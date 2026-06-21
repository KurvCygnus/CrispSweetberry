//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.slf4j.helpers.MessageFormatter;

import java.util.Objects;

/**
 * Utility for inspecting the call stack at runtime.
 * <p>
 * All lookup methods resolve the <b>caller's caller</b> — the class and method
 * that called the code which directly invoked this utility. This indirect
 * resolution makes the class useful for logging, diagnostics, and assertions
 * where the intermediate helper is irrelevant.
 *
 * @author Kurv Cygnus
 * @since 1.0
 */
public final class StackDebugger
{
    private StackDebugger() { throw new IllegalAccessError("Class \"StackDebugger\" is not meant to be instantized!"); }

    private static final StackWalker STACK_WALKER = StackWalker.getInstance();
    
    /**
     * Walks the stack and returns the {@link StackWalker.StackFrame} of the
     * <b>caller's caller</b> (skips this class and its direct invoker).
     * <p>
     * Stack layout produced by this helper (from the {@code walk} entry):
     * <ol start="0">
     *     <li><u>{@link #getCallerFrame(int)}</u> — final method</li>
     *     <li>{@code #getCallerFrame()} — this method</li>
     *     <li>the public {@code StackDebugger} method being called</li>
     *     <li>the <b>direct caller</b> (invoker of {@code StackDebugger})</li>
     *     <li>the <b>target</b> — returned</li>
     * </ol>
     */
    private static @NotNull StackWalker.StackFrame getCallerFrame() { return getCallerFrame(4); }
    
    /**
     * Walks the stack and returns the <u>{@link java.lang.StackWalker.StackFrame}</u> of the specific layer's caller, <b>depending on param {@code layer}</b>.
     * Stack layout basic layouts:
     * <ol start="0">
     *     <li>{@code #getCallerFrame(int)} — this method</li>
     *     <li><b>The class that calls this method</b></li>
     *     <li><i>Unknown. From here, count layers by yourself.</i></li>
     * </ol>
     */
    public static @NotNull StackWalker.StackFrame getCallerFrame(@Range(from = 1, to = Integer.MAX_VALUE) int layer)
    {
        if(layer < 1 || layer > Integer.MAX_VALUE)
            throw new IllegalArgumentException("Param \"layer\"'s value must be between 1 and `Integer.MAX_VALUE`!");
        
        return STACK_WALKER.
            walk(frames -> frames.skip(layer).findFirst()).
            orElseThrow(() -> new IllegalStateException("The layer of caller's StackTrace is too shallow!"));
    }
    
    public static @NotNull Class<?> getCallerClass() { return getCallerFrame().getDeclaringClass(); }

    /**
     * Returns the simple class name of the caller's caller.
     *
     * @return the target caller's class name
     * @throws java.util.NoSuchElementException if the stack is too shallow to
     *         contain the target frame
     */
    public static @NotNull String getCallerClassName() { return getCallerFrame().getDeclaringClass().getSimpleName(); }
    
    /**
     * Returns the fully qualified class name of the caller's caller.
     *
     * @return the target caller's class name
     * @throws java.util.NoSuchElementException if the stack is too shallow to
     *                                          contain the target frame
     */
    public static @NotNull String getCallerClassFQCN() { return getCallerFrame().getClassName(); }

    /**
     * Returns the method name of the caller's caller.
     * @return the target caller's method name
     * @throws java.util.NoSuchElementException if the stack is too shallow
     */
    public static @NotNull String getCallerMethodName() { return getCallerFrame().getMethodName(); }

    /**
     * Returns a combined string with the class name, method name, source file,
     * and line number of the caller's caller.
     * <p>
     * Output format:
     * <pre>{@code com.example.Foo#bar(Foo.java:42)}</pre>
     * @return formatted caller information
     * @apiNote <i>Fun fact — this method's return value supports <u><a href="https://www.jetbrains.com/idea/">IDEA</a></u>'s reference navigation.</i>
     * @throws java.util.NoSuchElementException if the stack is too shallow
     */
    public static @NotNull String getFullCallerInfo()
    {
        final var frame = getCallerFrame();
        return MessageFormatter.arrayFormat(
            "{}#{}({}:{})",
            new Object[] {
                frame.getClassName(),
                frame.getMethodName(),
                frame.getFileName() != null ? frame.getFileName() : "UnknownSource",
                frame.getLineNumber()
            }
        ).getMessage();
    }

    /**
     * Returns the source file name of the caller's caller.
     * @return the file name, or {@code null} if the JVM cannot provide one
     * @throws java.util.NoSuchElementException if the stack is too shallow
     */
    public static @NotNull String getCallerFileName() { return Objects.requireNonNullElse(getCallerFrame().getFileName(), "UnknownSource"); }

    /**
     * Returns the source line number at which the caller's caller is currently
     * executing.
     *
     * @return the line number (may be {@code -1} if native or unavailable)
     * @throws java.util.NoSuchElementException if the stack is too shallow
     */
    public static int getCallerLineNumber() { return getCallerFrame().getLineNumber(); }
}