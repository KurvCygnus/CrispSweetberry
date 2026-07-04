//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.functions;

import kurvcygnus.crispsweetberry.utils.UIUtils;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface to make creating slot for container menu more simple.
 *
 * @param <C> The container that slots will be bound to.
 * @param <S> The slot that will be added to container.
 * @author Kurv Cygnus
 * @implNote Method {@code AbstractMenu#addSlot()} has {@code protected} access, so using a
 * functional interface is a must to make a universal utility.
 * @see UIUtils#addGridSlots Usage
 * @since 1.0 Release
 */
@FunctionalInterface public interface IQuadSlotSupplier<C extends Container, S extends Slot> { @NotNull S create(C container, int index, int x, int y); }
