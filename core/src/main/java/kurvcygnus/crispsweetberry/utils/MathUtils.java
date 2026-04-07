//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils;

/**
 * @since 1.0 Release
 */
public final class MathUtils
{
    private MathUtils() { throw new IllegalAccessError("Class \"MathUtils\" is not meant to be instantized!"); }
    
    public static int negativeIf(boolean condition, int value)
    {
        if(!condition)
            return value;
        
        return -value;
    }
    
    public static double negativeIf(boolean condition, double value)
    {
        if(!condition)
            return value;
        
        return -value;
    }
    
    public static float negativeIf(boolean condition, float value)
    {
        if(!condition)
            return value;
        
        return -value;
    }
    
    public static long negativeIf(boolean condition, long value)
    {
        if(!condition)
            return value;
        
        return -value;
    }
}