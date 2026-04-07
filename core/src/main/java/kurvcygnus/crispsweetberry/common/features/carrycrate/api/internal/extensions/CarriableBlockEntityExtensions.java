//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.CarriableSimpleLogicCollection;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.BaseVanillaBrewingStandAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.BaseVanillaFurnaceSeriesAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.SimpleContainerBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions.ICarriableLifecycle;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * A collection class holds the all unique abilities of 
 * <u>{@link AbstractBlockEntityCarryAdapter BlockEntity's adapter}</u>.
 * @since 1.0 Release
 * @see CarriableExtensions Basics
 * @see CarriableBlockExtensions Block Basics
 * @see AbstractBlockEntityCarryAdapter Base BlockEntity Adapter
 */
@ApiStatus.Internal
public final class CarriableBlockEntityExtensions
{
    /**
     * This interface holds the ability of carry crate engine's core feature, 
     * <b><u>{@link net.minecraft.world.level.block.entity.BlockEntity}</u> working emulation</b>, which 
     * is composed of pre-process, and post-process.
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @implNote <u>{@link AbstractBlockEntityCarryAdapter BlockEntity Adapter}</u> 
     * uses bridge methods to implement these methods. You can see further explanations at there.
     */
    public interface IAtomicCarriable
    {
        /**
         * The pre-process method, which is called before blockEntity's serialization started, and blockEntity's boxIn.<br>
         * <b>So you can use {@link CarriedContext CarriedContext}</b> to edit unionData, or insert custom behaviors in this method.
         */
        default void onCarriedSequence(@NotNull CarriedContext context) { }
        
        /**
         * The post-process method, which is called before blockEntity is physically placed.<br>
         * @apiNote <b>This method is meant to atomize a blockEntity's main work logic</b>.<br>
         * Which means, you should compress all main work logic in this method, instead of calling {@code serverTick()} 
         * directly, since it will obviously cause great performance penalty.<br><br>
         * 
         * <b>The corresponded blockEntity instance was created before the call of this method. You can access and use it</b>.
         * @see BaseVanillaFurnaceSeriesAdapter Furnace Example(Reusable)
         * @see BaseVanillaBrewingStandAdapter Brewing Stand Example(Reusable)
         */
        void onPlacedProcess(@NotNull ServerLevel level, long elapsedTime, @NotNull CarriedContext context);
        
        record CarriedContext(
            @NotNull ServerLevel level,
            @NotNull BlockPos pos,
            @NotNull ServerPlayer player,
            @NotNull String carryID
        ) {}
    }
    
    /**
     * This is the specialized lifecycle interface for blockEntity, which makes blockEntity's penaltyRate more dynamic, and flexible.
     * @param <E> The blockEntity this adapter takes responsibility of.
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @see CarriableExtensions.ICarriableLifecycle Base Lifecycle Interface
     * @implNote This interface is actually necessary because the existence of 
     * <u>{@link CarriableSimpleLogicCollection.ISimpleBlockEntityPenaltyLogic ISimpleBlockEntityPenaltyLogic}</u>, 
     * without this interface, the implementor of that interface have to do <b>Default Method Delegation</b>, which makes it not "Simple".
     * <br><br>
     * Also, interface methods are used by internal implementation, which doesn't have blockEntity instance at all the time, 
     * with adapter itself has blockEntity field, we shouldn't add blockEntity as method args.
     */
    public interface IBlockEntityCarryLifecycle<E extends BlockEntity> extends ICarriableLifecycle<CarryData.CarryBlockEntityDataHolder>
    {
        String INVALID_CALL_FAIL_MESSAGE = """
            Assertion failed: Field "blockEntity" happens to be null, this shouldn't be happen, which usually means
            method is called at improper time, with improper param.
            
            %s
            """.formatted(MetainfoConstants.FEEDBACK_MESSAGE);
        
        /**
         * A fallback penalty Rate getter for registration validation, and edge case fallback.
         */
        default @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getFallbackPenaltyRate() { return DEFAULT_PENALTY_RATE; }
        
        /**
         * The bridge method between base and this interface.
         */
        @Override default @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getPenaltyRate() { return getPenaltyRate(getBlockEntity()); }
        
        /**
         * An abstract getter, mainly used by <u>{@link #getPenaltyRate()}</u>.
         * @apiNote <span style="color: red">This method is not recommended to use.</span> Only use this when you know what are you doing.<br> 
         * For further details, see <u>{@link AbstractBlockEntityCarryAdapter#getBlockEntity()}</u>.
         */
        @ApiStatus.Experimental @NotNull E getBlockEntity();
        
        /**
         * The new base penaltyRate getter for blockEntity.<br>
         * With the reference of {@code blockEntity}, its return value can be much more flexible.
         * @see CarriableSimpleLogicCollection.ISimpleBlockEntityPenaltyLogic Recommend Universal Implementation
         */
        @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate(@NotNull E blockEntity);
        
        @NotNull CarryData onPenaltyDrop(@NotNull CarriableExtensions.TickingContext context);
    }
    
    /**
     * This interface makes sure that blockEntity's unionData can be serialized.
     * @apiNote This interface allows you save/load specified unionData, <b>if you want to save/load all unionData,
     * use {@link BlockEntity#saveCustomOnly(HolderLookup.Provider)} and {@link BlockEntity#loadCustomOnly(CompoundTag, HolderLookup.Provider)}</b>. 
     * <u>{@link SimpleContainerBlockEntityCarryAdapter Here}</u>'s a simple example.
     * @implNote Interface methods are used by internal implementation, which doesn't have blockEntity instance at all the time, 
     * with adapter itself has blockEntity field, we shouldn't add blockEntity as method args.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public interface ICarrySerializable
    {
        /**
         * Load <u>{@link BlockEntity}</u>'s serialized unionData.<br><br>
         * <span style="color: 95ce6d">If have no specific demand, directly use <u>{@link BlockEntity#loadCustomOnly(CompoundTag, HolderLookup.Provider)}</u> is OK.</span>
         */
        void loadCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries);
        
        /**
         * Save <u>{@link BlockEntity}</u>'s unionData as <u>{@link CompoundTag}</u>.<br><br>
         * <span style="color: 95ce6d">If have no specific demand, you can use such a combination:</span><br>
         * <pre>{@code
         *  void saveCarryTag(
         *      @NotNull CompoundTag tag,
         *      @NotNull HolderLookup.Provider registries
         *  )
         *  {
         *      final CompoundTag dataTag = blockEntity.saveCustomOnly(registries);
         *      tag.union(dataTag);
         *  }
         * }</pre>
         */
        void saveCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries);
    }
}
