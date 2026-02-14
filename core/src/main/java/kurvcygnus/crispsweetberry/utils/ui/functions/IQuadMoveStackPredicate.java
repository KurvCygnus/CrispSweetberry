//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.ui.functions;

import net.minecraft.world.item.ItemStack;

/**
 * A simple functional interface which is mainly used for {@code AbstractMenu#quickMoveStack()} method.
 * @implNote Method {@code AbstractMenu#quickMoveStack()} has {@code protected} access, so using a
 * functional interface is a must to make a universal utility.
 * @see kurvcygnus.crispsweetberry.utils.ui.CrispUIUtils#moveStackByRanger Usage
 * @author Kurv Cygnus
 * @since 1.0 Release
 */
@FunctionalInterface
public interface IQuadMoveStackPredicate
{
    boolean test(ItemStack interactStack, int minIndex, int maxIndex, boolean reverseDirection);
}
