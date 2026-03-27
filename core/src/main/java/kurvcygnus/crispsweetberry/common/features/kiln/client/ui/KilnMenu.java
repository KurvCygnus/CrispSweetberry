//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.client.ui;

import kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnContainerData;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnRecipeCacheEvent;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnDummyBlockEntity;
import kurvcygnus.crispsweetberry.utils.ui.CrispUIUtils;
import kurvcygnus.crispsweetberry.utils.ui.collects.CrispRanger;
import kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static kurvcygnus.crispsweetberry.common.features.kiln.KilnConstants.*;
import static kurvcygnus.crispsweetberry.utils.ui.constants.ExampleSlotConstants.*;

/**
 * The <b>user interface part</b> of the Kiln Block.<br>
 * <b>{@code Menu}</b> is the gateway between <b>client and server interaction</b>.
 *
 * @author Kurv Cygnus
 * @see net.minecraft.world.inventory.AbstractContainerMenu Source
 * @see KilnBlock Holder of the menu
 * @see KilnBlockEntity Logical part
 * @see KilnContainerData Data Sync Part
 * @see KilnRecipeCacheEvent Recipe Initialization
 * @since 1.0 Release
 */
public final class KilnMenu extends AbstractContainerMenu
{
    //  region
    //*: Constants & Fields
    //*:=== Constants
    //*                                                          Layout Shape --> [□]
    //* Kiln uses pyramid layout, so we should have two types of start index.    [□ □]
    private static final int INPUT_SLOT_TOP_X_POS = 47;
    private static final int INPUT_SLOT_START_X_POS = 38;
    private static final int OUTPUT_SLOT_TOP_X_POS = 120;
    private static final int OUTPUT_SLOT_START_X_POS = 111;
    private static final int KILN_SLOTS_TOP_Y_POS = 25;
    private static final int KILN_SLOTS_LOWER_Y_POS = 43;
    private static final int KILN_SLOTS_X_GAP = 18;
    
    private static final List<CrispRanger> SLOT_RANGERS = List.of(
        KILN_INPUT_SLOTS_RANGE,
        KILN_OUTPUT_SLOTS_RANGE,
        KILN_BACKPACK_SLOTS_RANGE,
        KILN_HOTBAR_SLOTS_RANGE
    );
    
    //*:=== Fields
    private final KilnBlockEntity container;
    public final ContainerData data;
    //endregion
    
    //  region
    //* Constructors & Menu Basics
    
    /**
     * The constructor method for <b>client-side UI</b>, it makes sure that the <b>UI can be open normally</b>.<br>
     * <i>You can see the reason of using </i>{@code KilnDummyBlockEntity}<i> <u>{@link KilnDummyBlockEntity here}</u></i>.
     */
    public KilnMenu(int containerId, @NotNull Inventory inventory) { this(containerId, inventory, new KilnDummyBlockEntity()); }
    
    /**
     * The constructor method for <b>server-side</b>, it <b>settles the basic ghost block problem</b>.
     *
     * @implNote As mentioned above(see {@link #INPUT_SLOT_TOP_X_POS}), the layout shape of Kiln is pyramid shape,
     * <b>so DO NOT wrap "addSlots" to private methods to just make it visually more graceful, that's a bad code smell.</b>
     */
    public KilnMenu(int containerId, @NotNull Inventory inventory, @NotNull KilnBlockEntity container)
    {
        super(KilnRegistries.KILN_MENU_TYPE.get(), containerId);
        this.container = container;
        this.data = this.container.getData();
        
        this.addSlot(new KilnInputSlot(container,
                KILN_INPUT_SLOTS_RANGE.getMin(),
                INPUT_SLOT_TOP_X_POS, KILN_SLOTS_TOP_Y_POS
            )
        );
        this.addSlot(new KilnInputSlot(container,
                KILN_INPUT_SLOTS_RANGE.getMin() + 1,
                INPUT_SLOT_START_X_POS, KILN_SLOTS_LOWER_Y_POS
            )
        );
        this.addSlot(new KilnInputSlot(container,
                KILN_INPUT_SLOTS_RANGE.getMax(),
                INPUT_SLOT_START_X_POS + KILN_SLOTS_X_GAP, KILN_SLOTS_LOWER_Y_POS
            )
        );
        
        this.addSlot(new KilnOutputSlot(container,
                KILN_OUTPUT_SLOTS_RANGE.getMin(),
                OUTPUT_SLOT_TOP_X_POS, KILN_SLOTS_TOP_Y_POS
            )
        );
        this.addSlot(new KilnOutputSlot(container,
                KILN_OUTPUT_SLOTS_RANGE.getMin() + 1,
                OUTPUT_SLOT_START_X_POS, KILN_SLOTS_LOWER_Y_POS
            )
        );
        this.addSlot(new KilnOutputSlot(container,
                KILN_OUTPUT_SLOTS_RANGE.getMax(),
                OUTPUT_SLOT_START_X_POS + KILN_SLOTS_X_GAP, KILN_SLOTS_LOWER_Y_POS
            )
        );
        
        CrispUIUtils.addGridSlots(inventory, INVENTORY_SLOTS_GRID_START_INDEX,
            INVENTORY_SLOTS_START_X_POS, INVENTORY_SLOTS_START_Y_POS,
            INVENTORY_SLOTS_TOTAL_ROWS, INVENTORY_SLOTS_TOTAL_COLS,
            Slot::new,
            this::addSlot
        );
        CrispUIUtils.addGridSlots(inventory, HOTBAR_SLOTS_GRID_START_INDEX,
            HOTBAR_SLOTS_START_X_POS, HOTBAR_SLOTS_START_Y_POS,
            HOTBAR_SLOTS_TOTAL_ROWS, HOTBAR_SLOTS_TOTAL_COLS,
            Slot::new,
            this::addSlot
        );
        
        this.addDataSlots(data);
    }
    
    /**
     * @apiNote <p>
     * The <b>actual validity logic must be implemented in the underlying container</b>
     * (e.g. a <u>{@link net.minecraft.world.level.block.entity.BaseContainerBlockEntity BlockEntity}</u> such as <u>{@link KilnBlockEntity}</u>),
     * <b>if you are using {@code container.stillvaild()}</b>.
     * </p>
     * <p>
     * <u>{@link AbstractContainerMenu#stillValid}</u> only <b>forwards</b> this check and is responsible
     * for UI interaction, <b>not world attachTag validation</b>.
     * </p>
     * <p>
     * Failing to override <u>{@link KilnBlockEntity#stillValid blockEntity.stillValid()}</u> in the container may cause
     * <b>desynchronization or improper menu behavior</b>.
     * </p>
     */
    @Override public boolean stillValid(@NotNull Player player) { return this.container.stillValid(player); }
    
    /**
     * This method is called when the <b>server side shutdown</b> to make sure <b>container behaves correctly</b>.
     */
    @Override public void removed(@NotNull Player player)
    {
        super.removed(player);
        if(!player.level().isClientSide)
            container.stopOpen(player);
    }
    //endregion
    
    //  region
    //* Interaction Logics
    @Override public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index)
    {
        final Slot slot = this.slots.get(index);
        
        if(!slot.hasItem())
            return ItemStack.EMPTY;
        
        final ItemStack interactStack = slot.getItem();
        final ItemStack temporaryStack = interactStack.copy();
        
        final int rangeIndex = CrispRanger.inRangers(index, SLOT_RANGERS);
        final boolean hasMoved;
        
        switch(rangeIndex)
        {
            case ABNORMAL_RANGE -> throw new IllegalStateException("Unexpected range index: " + rangeIndex);
            case INPUT_RANGE, OUTPUT_RANGE ->
            {
                final boolean isInputRange = (rangeIndex == INPUT_RANGE);
                hasMoved = moveToSlotRange(interactStack, KILN_INVENTORY_SLOTS_RANGE, isInputRange);
                
                if(!hasMoved)
                    return ItemStack.EMPTY;
                //* No onQuickCraft method here. We have already processed it in "KilnOutputSlot.java".
            }
            case BACKPACK_RANGE ->
            {
                hasMoved = moveToSlotRange(interactStack, KILN_INPUT_SLOTS_RANGE) || moveToSlotRange(interactStack, KILN_HOTBAR_SLOTS_RANGE);
                if(!hasMoved)
                    return ItemStack.EMPTY;
            }
            case HOTBAR_RANGE ->
            {
                hasMoved = moveToSlotRange(interactStack, KILN_INPUT_SLOTS_RANGE) || moveToSlotRange(interactStack, KILN_BACKPACK_SLOTS_RANGE);
                
                if(!hasMoved)
                    return ItemStack.EMPTY;
            }
        }
        
        if(interactStack.isEmpty())
            slot.setByPlayer(ItemStack.EMPTY);
        else
            slot.setChanged();
        
        if(interactStack.getCount() == temporaryStack.getCount())
            return ItemStack.EMPTY;
        
        slot.onTake(player, interactStack);
        
        return temporaryStack;
    }
    //endregion
    
    /**
     * A helper method for {@link #quickMoveStack(Player, int)}.<br>
     * It was used to solve the problem of verbose method parameters of {@link #moveItemStackTo moveItemStackTo()},
     * making the logic clear and easy to modify.
     *
     * @apiNote The problem of vanilla closedOpen range has been fixed here.
     * @see ExampleSlotConstants More details about these constants
     */
    private boolean moveToSlotRange(ItemStack interactStack, CrispRanger ranger, boolean reverseDirection)
        { return CrispUIUtils.moveStackByRanger(interactStack, ranger, reverseDirection, this::moveItemStackTo); }
    
    /**
     * A helper method for {@link #quickMoveStack(Player, int)}.<br>
     * It was used to solve the problem of verbose method parameters of {@link #moveItemStackTo moveItemStackTo()},
     * making the logic clear and easy to modify.
     *
     * @apiNote Don't forget flag {@code reverseDirection} is false in this overloaded method.
     * @see ExampleSlotConstants More details about these constants
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") //! Inverted usage is at least better than "failedToMoveSlotRange", that will lead to confusion.
    private boolean moveToSlotRange(ItemStack interactStack, CrispRanger ranger)
        { return moveToSlotRange(interactStack, ranger, false); }
    //endregion
}
