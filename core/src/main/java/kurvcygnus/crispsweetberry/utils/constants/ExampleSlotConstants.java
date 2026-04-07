//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.constants;

import kurvcygnus.crispsweetberry.utils.UIUtils;
import net.minecraft.world.entity.player.Player;

/**
 * If you are looking for the slot constants of kiln, check <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.KilnConstants here}</u>.
 * @apiNote This class is used for clarifying the meaning of container slot indexes, which means
 * despite <b>some constants are unused</b>, they should always exist.<br><br>
 * Also, this example constants class is only applied on <u>{@link net.minecraft.world.level.block.FurnaceBlock Furnace}</u>.
 * Custom slots should follow its own slots index.
 * @since 1.0 Release
 */
public final class ExampleSlotConstants
{
    private ExampleSlotConstants() { throw new IllegalAccessError(); }
    
    public static final int NO_X_SLOT_OFFSET = 0;
    
    /**
     * <b>{@code GRID}</b> start indexes are used for <u>{@link net.minecraft.world.inventory.AbstractContainerMenu Menu}</u> layout initialization,
     * they are mostly universal, which are different from other constants in this class.
     * @see UIUtils#addGridSlots Recommended Usage
     */
    public static final int INVENTORY_SLOTS_GRID_START_INDEX = 9;
    public static final int INVENTORY_SLOTS_START_X_POS = 8;
    public static final int INVENTORY_SLOTS_START_Y_POS = 84;
    public static final int INVENTORY_SLOTS_TOTAL_ROWS = 3;
    public static final int INVENTORY_SLOTS_TOTAL_COLS = 9;
    
    /**
     * <b>{@code GRID}</b> start indexes are used for <u>{@link net.minecraft.world.inventory.AbstractContainerMenu Menu}</u> layout initialization,
     * they are mostly universal, which are different from other constants in this class.
     * @see UIUtils#addGridSlots Recommended Usage
     */
    public static final int HOTBAR_SLOTS_GRID_START_INDEX = 0;
    public static final int HOTBAR_SLOTS_START_X_POS = 8;
    public static final int HOTBAR_SLOTS_START_Y_POS = 142;
    public static final int HOTBAR_SLOTS_TOTAL_ROWS = 1;
    public static final int HOTBAR_SLOTS_TOTAL_COLS = 9;
    
    /**
     * This constant exists because <b>the source code of the method {@code quickMoveStack()} originally and oddly uses "closedOpen" style to check indexes</b>,
     * thus we <b>need this to make constant's name fits the actual context meaning</b>.<br><b>
     * <i>It is also used for <b>correct {@code endIdx}</b> in method
     * <b>{@link net.minecraft.world.inventory.AbstractContainerMenu#quickMoveStack(Player, int) moveItemStackTo()}</b></i>.
     */
    public static final int CORRECTION_INDEX = 1;
    
    public static final int ERROR = -1;
    
    public static final int INPUT_SLOT = 0;
    
    public static final int FUEL_SLOT = 1;
    
    public static final int OUTPUT_SLOT = 2;
    public static final int BACKPACK_SLOT_START_INDEX = 3;
    public static final int BACKPACK_SLOT_END_INDEX = 29;
    public static final int HOTBAR_SLOT_START_INDEX = 30;
    public static final int HOTBAR_SLOT_END_INDEX = 38;
    
    public static final int ABNORMAL_RANGE = -1;
    public static final int INPUT_RANGE = 0;
    public static final int OUTPUT_RANGE = 1;
    public static final int BACKPACK_RANGE = 2;
    public static final int HOTBAR_RANGE = 3;
    
    public static final int SLOT_GAP = 18;
}
