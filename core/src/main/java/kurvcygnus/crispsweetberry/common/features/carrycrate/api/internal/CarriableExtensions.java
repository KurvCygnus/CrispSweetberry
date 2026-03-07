//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.ISimpleBlockEntityPenaltyLogic;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * This is a collection for universal carriable interfaces.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@ApiStatus.Internal
public final class CarriableExtensions
{
    /**
     * This represents the lifecycle part of carry crate system.
     * <br><br>
     * <h3><b>About Penalty Rate:</b></h3>
     * <br>
     * Penalty Rate is used by Carry Crate, which is a <u>{@link net.minecraft.world.item.BlockItem BlockItem}</u>, despite it has durability.
     * <br>
     * Carry Crate will gradually went break when it holds content, its durability determines the probability of content loss/dropping.<br>
     * <b>And Penalty Rate decides the speed of durability's decreasement</b>, it represents the ticks that how much ticks will 1 durability spent.
     * <br>
     * <b>The smaller the Penalty Rate is, the faster the durability drops</b>.
     * @since 1.0 Release
     * @see ISimpleBlockEntityPenaltyLogic Container Item Based Penalty Logic Implementation
     * @author Kurv Cygnus
     */
    public interface ICarriableLifecycle
    {
        int DEFAULT_PENALTY_RATE = 20;
        
        /**
         * {@code 0} is treated as a special value, it means <b>no penalty</b> on carry crate.
         */
        int NO_PENALTY = 0;
        
        @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getPenaltyRate();
        
        /**
         * This decides whether this stuff causes player's speed decrease.<br>
         * @apiNote <span style="color: red">Setting value to {@code false} doesn't mean it won't cause overweight.</span><br>
         * <b>When player holds stuff that doesn't cause overweight more than 1, overweight will always be applied to player.</b>
         */
        default boolean causesOverweight() { return true; }
    }
    
    /**
     * This interface makes adapter alive in player's inventory through ticking.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public interface ICarryTickable
    {
        /**
         * This method will be called during carrying.
         * @apiNote All <u>{@link AbstractCarryAdapter adapters}</u> are variables with short lifecycle. 
         * During the execution of {@code #carryTick(TickingContext)}, <b>adapters will be created with {@code null} values</b>.<br>
         * So, <span style="color: red">do not try to get adapters field(block, blockEntity...) and use, <u>{@link NullPointerException NPE}</u> will be thrown.</span>
         */
        default void carryingTick(@NotNull CarriableExtensions.ICarryTickable.TickingContext context) {}
        
        record TickingContext(@NotNull ItemStack carryCrate, @NotNull Level level, @NotNull Entity entity, int slotId) {}
    }
    
    //? TODO: UI Ass stuff
    public static interface ICarryDisplayable
    {
        default void initRenderComponents(@NotNull ClientTooltipComponent... components) {}
        
        default void display() {}
    }
}
