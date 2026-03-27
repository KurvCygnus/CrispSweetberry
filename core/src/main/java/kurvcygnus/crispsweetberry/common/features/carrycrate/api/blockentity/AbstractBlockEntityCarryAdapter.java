//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableBlockEntityExtensions.IAtomicCarriable;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableBlockEntityExtensions.IBlockEntityCarryLifecycle;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableBlockEntityExtensions.ICarrySerializable;

/**
 * This is the basic of BlockEntity Adapters, which doesn't include any usable logics.
 *
 * @param <E> The blockEntity this adapter takes responsibility of.
 * @author Kurv Cygnus
 * @apiNote <b>BlockEntity's adapter is independent.</b><br>
 * Unlike vanilla design, blockEntity's adapter doesn't relies on its corresponded Block,
 * thus, registering blockEntity itself only is the proper way to do compat.
 * @see BaseVanillaFurnaceSeriesAdapter Furnace Series Adapter
 * @see BaseVanillaBrewingStandAdapter Brewing Stand Adapter
 * @see SimpleContainerBlockEntityCarryAdapter Universal Storge Container Adapter
 * @since 1.0 Release
 */
public abstract class AbstractBlockEntityCarryAdapter<E extends BlockEntity>
extends AbstractCarryAdapter<CarryData.CarryBlockEntityDataHolder> implements IAtomicCarriable, ICarrySerializable, IBlockEntityCarryLifecycle<E>
{
    //  region
    //*:=== Core Logics
    /**
     * @apiNote During <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code blockEntity} during <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u>
     * will throw <u>{@link NullPointerException}</u></b>.
     */
    private final @Nullable E blockEntity;
    
    @SuppressWarnings("unchecked")//! Due to the limitation of javac's generic deduction, we have to choose runtime check.
    public AbstractBlockEntityCarryAdapter(@Nullable BlockEntity blockEntity) 
    {
        if(blockEntity == null)
        {
            this.blockEntity = null;
            return;
        }
        
        try { this.blockEntity = (E) blockEntity; }
        catch(ClassCastException e) 
        {
            throw new IllegalArgumentException(
                "Adapter Type Mismatch! Attempted to bind adapter to blockEntity: %s, but this adapter expects: %s".
                    formatted(blockEntity.getClass().getSimpleName(), this.getClass().getSimpleName())
            );
        }
    }
    
    /**
     * @apiNote During <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code #getBlockEntity()} during <u>{@link #carryingTick(CarriableExtensions.TickingContext) #carryingTick(TickingContext)}</u>
     * will throw <u>{@link NullPointerException}</u>, and you shouldn't use this at most cases.</b>
     */
    @Override public final @NotNull E getBlockEntity()
    {
        Objects.requireNonNull(blockEntity, INVALID_CALL_FAIL_MESSAGE);
        return blockEntity;
    }
    
    /**
     * The pre-process method, which is called before blockEntity's serialization started, and blockEntity's boxIn.<br>
     * <b>So you can use {@link CarriedContext CarriedContext}</b> to edit unionData, or insert custom behaviors in this method.
     */
    protected void onCarriedSequence(@NotNull CarriedContext context, @NotNull E blockEntity) {}
    
    /**
     * The post-process method, which is called before blockEntity is physically placed.<br>
     *
     * @apiNote <b>This method is meant to atomize a blockEntity's main work logic</b>.<br>
     * Which means, you should compress all main work logic in this method, instead of calling {@code serverTick()}
     * directly, since it will obviously cause great performance penalty.<br><br>
     *
     * <b>The corresponded blockEntity instance was created before the call of this method. You can access and use it</b>.
     * @see BaseVanillaFurnaceSeriesAdapter Furnace Example(Reusable)
     * @see BaseVanillaBrewingStandAdapter Brewing Stand Example(Reusable)
     */
    protected void onPlacedProcess(@NotNull ServerLevel level, long elapsedTime, @NotNull CarriedContext context, @NotNull E blockEntity) {}
    
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
     *      tag.merge(dataTag);
     *  }
     * }</pre>
     */
    protected abstract void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, @NotNull E blockEntity);
    
    /**
     * Load <u>{@link BlockEntity}</u>'s serialized unionData.<br><br>
     * <span style="color: 95ce6d">If have no specific demand, directly use <u>{@link BlockEntity#loadCustomOnly(CompoundTag, HolderLookup.Provider)}</u> is OK.</span>
     */
    protected abstract void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, @NotNull E blockEntity);
    //endregion
    
    //  region
    //*:=== Bridge methods
    
    /**
     * {@inheritDoc}
     */
    @Override public final void onCarriedSequence(@NotNull CarriedContext context)
    {
        Objects.requireNonNull(this.blockEntity, INVALID_CALL_FAIL_MESSAGE);//! See blockEntity's Javadoc.
        this.onCarriedSequence(context, blockEntity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public final void onPlacedProcess(@NotNull ServerLevel level, long elapsedTime, @NotNull CarriedContext context)
    {
        Objects.requireNonNull(this.blockEntity, INVALID_CALL_FAIL_MESSAGE);//! See blockEntity's Javadoc.
        this.onPlacedProcess(level, elapsedTime, context, this.blockEntity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public final void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        Objects.requireNonNull(this.blockEntity, INVALID_CALL_FAIL_MESSAGE);//! See blockEntity's Javadoc.
        this.saveCarryTag(tag, registries, this.blockEntity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override public final void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        Objects.requireNonNull(this.blockEntity, INVALID_CALL_FAIL_MESSAGE);//! See blockEntity's Javadoc.
        this.loadCarryTag(tag, registries, this.blockEntity);
    }
    
    @Override public final @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getPenaltyRate()
    {
        if(this.blockEntity == null)
            return this.getFallbackPenaltyRate();
        
        return this.getPenaltyRate(this.blockEntity);
    }
    //endregion
}
