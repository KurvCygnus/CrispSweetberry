//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.functions;

import kurvcygnus.crispsweetberry.utils.UIUtils;
import net.minecraft.world.item.ItemStack;

import java.io.Serializable;

/**
 * A simple functional interface which is mainly used for {@code AbstractMenu#quickMoveStack()} method.
 *
 * @author Kurv Cygnus
 * @implNote Method {@code AbstractMenu#quickMoveStack()} has {@code protected} access, so using a
 * functional interface is a must to make a universal utility.
 * @see UIUtils#moveStackByRanger Usage
 * @since 1.0 Release
 */
@FunctionalInterface public interface IQuadMoveStackPredicate extends Serializable
{
    boolean test(ItemStack interactStack, int minIndex, int maxIndex, boolean reverseDirection);
}
