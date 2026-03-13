//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.block;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.CarriableSimpleLogicCollection;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableBlockExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableExtensions;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * <h3>This is an universal adapter that supports most blocks.</h3><br>
 * It works perfectly on:
 * <ul>
 *     <li>
 *         Standard blocks(e.g. <u>{@link net.minecraft.world.level.block.GrassBlock Grass Block}</u>)
 *     </li>
 *     <li>
 *         Interactable blocks that doesn't belongs to <u>{@link net.minecraft.world.level.block.BaseEntityBlock BaseEntityBlock}</u>
 *         (
 *         e.g. <u>{@link net.minecraft.world.level.block.CraftingTableBlock Crafting Table}</u>,
 *         <u>{@link net.minecraft.world.level.block.AnvilBlock Anvil}</u>
 *         )
 *         <br>
 *         <i>
 *             To more precisely,
 *             this supports blocks that doesn't have a corresponded <u>{@link net.minecraft.world.level.block.entity.BlockEntity BlockEntity}</u>.
 *         </i>
 *     </li>
 *     <li>
 *         <i>
 *             <u>{@link net.minecraft.world.level.block.LiquidBlock Liquids}</u>, despite doing this is odd and not recommended.
 *         </i>
 *     </li>
 * </ul>
 * As it named, it is simple, thus all customizable content uses default value.<br>
 * Inherit this if you want to customize, and you can see the function, and introductions about these customizable contents in following references.
 * @apiNote It is recommend to use <u>{@link net.neoforged.neoforge.registries.DeferredHolder DeferredHolder}</u>, 
 * rather than <u>{@link net.minecraft.core.Holder Holder}</u> for 
 * {@link kurvcygnus.crispsweetberry.common.features.carrycrate.api.events.CarryAdapterRegisterEvent registry}.<br>
 * Carry Crate's register system has strict generic constraints, and <u>{@link net.minecraft.core.Holder Holder}</u> will erase the 
 * detailed Block Type.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see CarriableExtensions.ICarriableLifecycle Lifecycle Interface
 * @see CarriableBlockExtensions.ICarryBlockStackable Block Carry Support Interface
 * @see kurvcygnus.crispsweetberry.common.features.carrycrate.api.events.CarryAdapterRegisterEvent Register Event
 * @param <B> The block this adapter takes responsibility of.
 */
public class SimpleBlockCarryAdapter<B extends Block> extends AbstractBlockCarryAdapter<B> implements CarriableSimpleLogicCollection.ISimpleCarriableBlockBreakLogic
{
    protected static final int DEFAULT_ACCEPTABLE_COUNT = 1;
    
    public SimpleBlockCarryAdapter(@NotNull Block block) { super(block); }
    
    @Override public @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getPenaltyRate() { return DEFAULT_PENALTY_RATE; }
    
    @Override public @Range(from = 1, to = Integer.MAX_VALUE) int getAcceptableCount() { return DEFAULT_ACCEPTABLE_COUNT; }
    
    @Override public @NotNull Class<?> getSupportedType() { return Block.class; }
}
