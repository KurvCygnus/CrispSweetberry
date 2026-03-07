//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.AbstractCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableBlockEntityExtensions.IAtomicCarriable;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableBlockEntityExtensions.IBlockEntityCarryLifecycle;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableBlockEntityExtensions.ICarrySerializable;

/**
 * This is the basic of BlockEntity Adapters, which doesn't include any usable logics.
 *
 * @param <E> The blockEntity this adapter takes responsibility of
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
extends AbstractCarryAdapter implements IAtomicCarriable, ICarrySerializable, IBlockEntityCarryLifecycle<E>
{
    //  region
    //*:=== Core Logics
    /**
     * @apiNote During <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code blockEntity} during <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>
     * will throw <u>{@link NullPointerException}</u></b>.
     */
    private final @Nullable E blockEntity;
    
    public AbstractBlockEntityCarryAdapter(@Nullable E blockEntity) { this.blockEntity = blockEntity; }
    
    /**
     * @apiNote During <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>, the implementation will create an adapter
     * with <b>{@code null}</b> as adapter's param, since <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u> shouldn't use it.
     * <br><br>
     * In a nutshell, <b>using {@code #getBlockEntity()} during <u>{@link #carryingTick(TickingContext) #carryingTick(TickingContext)}</u>
     * will throw <u>{@link NullPointerException}</u>, and you shouldn't use this at most cases.</b>
     */
    @Override public final @NotNull E getBlockEntity()
    {
        Objects.requireNonNull(blockEntity, INVALID_CALL_FAIL_MESSAGE);
        return blockEntity;
    }
    
    protected void onCarriedSequence(@NotNull CarriedContext context, @NotNull E blockEntity) {}
    protected void onPlacedProcess(@NotNull ServerLevel level, long elapsedTime, @NotNull CarriedContext context, @NotNull E blockEntity) {}
    
    protected abstract void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, @NotNull E blockEntity);
    protected abstract void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, @NotNull E blockEntity);
    //endregion
    
    //  region
    //*:=== Bridge methods
    @Override public final void onCarriedSequence(@NotNull CarriedContext context)
    {
        Objects.requireNonNull(this.blockEntity, INVALID_CALL_FAIL_MESSAGE);//! See blockEntity's Javadoc.
        this.onCarriedSequence(context, blockEntity);
    }
    
    @Override public final void onPlacedProcess(@NotNull ServerLevel level, long elapsedTime, @NotNull CarriedContext context)
    {
        Objects.requireNonNull(this.blockEntity, INVALID_CALL_FAIL_MESSAGE);//! See blockEntity's Javadoc.
        this.onPlacedProcess(level, elapsedTime, context, this.blockEntity);
    }
    
    @Override public final void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        Objects.requireNonNull(this.blockEntity, INVALID_CALL_FAIL_MESSAGE);//! See blockEntity's Javadoc.
        this.saveCarryTag(tag, registries, this.blockEntity);
    }
    
    @Override public final void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries)
    {
        Objects.requireNonNull(this.blockEntity, INVALID_CALL_FAIL_MESSAGE);//! See blockEntity's Javadoc.
        this.loadCarryTag(tag, registries, this.blockEntity);
    }
    
    @Override public final @Range(from = NO_PENALTY, to = Integer.MAX_VALUE) int getPenaltyRate()
    {
        Objects.requireNonNull(this.blockEntity, INVALID_CALL_FAIL_MESSAGE);//! See blockEntity's Javadoc.
        return this.getPenaltyRate(this.blockEntity);
    }
    //endregion
}
