//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @since 1.0 Release
 */
public final class MathUtils
{
    private MathUtils() { throw new IllegalAccessError("Class \"MathUtils\" is not meant to be instantized!"); }
    
    public static int negativeIf(boolean condition, int value) { return !condition ? value : -value; }
    
    public static double negativeIf(boolean condition, double value) { return !condition ? value : -value; }
    
    public static float negativeIf(boolean condition, float value) { return !condition ? value : -value; }
    
    public static long negativeIf(boolean condition, long value) { return !condition ? value : -value; }
    
    public static int min(int @NotNull ... numbers) { return Arrays.stream(numbers).min().orElseThrow(); }
    
    public static double min(double @NotNull ... numbers) { return Arrays.stream(numbers).min().orElseThrow(); }
    
    public static long min(long @NotNull ... numbers) { return Arrays.stream(numbers).min().orElseThrow(); }
    
    public static int max(int @NotNull ... numbers) { return Arrays.stream(numbers).max().orElseThrow(); }
    
    public static double max(double @NotNull ... numbers) { return Arrays.stream(numbers).max().orElseThrow(); }
    
    public static long max(long @NotNull ... numbers) { return Arrays.stream(numbers).max().orElseThrow(); }
}