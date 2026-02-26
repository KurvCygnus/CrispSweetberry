//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.misc;

/**
 * @since 1.0 Release
 */
public final class MiscConstants
{
    private MiscConstants() { throw new IllegalAccessError("Class \"MiscConstants\" is not meant to be instantized!"); }
    
    public static final int STANDARD_MAX_STACK_SIZE = 64;
    public static final int FURNACE_SMELTING_TIME = 200;
    public static final int ADVANCED_HEATING_CONTAINER_TIME = 100;
    public static final String FEEDBACK_MESSAGE = "If you found this log, please feedback to us at %PLACEHOLDER%";
}
