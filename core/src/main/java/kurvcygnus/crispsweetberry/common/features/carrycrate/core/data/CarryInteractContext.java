//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core.data;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView;
import kurvcygnus.crispsweetberry.lib.base.extensions.BaseNestedPrinter;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import kurvcygnus.crispsweetberry.lib.base.extensions.StatedBlockPlaceContext;
import kurvcygnus.crispsweetberry.lib.base.lang.ISealableBox;
import kurvcygnus.crispsweetberry.lib.base.lang.TriVariant;
import kurvcygnus.crispsweetberry.utils.AssertUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is a mutable data class which is used in the
 * <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine#interact(CarryInteractContextCollection.ICarryInteractContext) CarryEngine#interact(ICarryInteractContext)}</u>'s
 * post-process phase.
 *
 * @author Kurv Cygnus
 * @implNote <h4><b>Some simple Q&A:</b></h4>
 * <ul>
 *     <li>
 *         Isn't this data class <b>too bloat</b>? It has 16 fields, that's not just a few!
 *         <hr>
 *         It's not, because all of these fields are essential in interact logics. <b>Choping these fields into
 *         smaller fields only makes the post-process's context passing more troublesome. Also, field {@code listenerInsert},
 *         {@code listenerRemove} and {@code placeContentGetter} has already got more decoupling effort than directly passing those raw types.</b>
 *     </li>
 *     <li>
 *         Why not using <b><u>{@link Record}</u></b>, or at least <b>immutable data class</b>?
 *         <hr>
 *         The post-process logic implementation is a typical example of <u><a href="https://fsharpforfunandprofit.com/rop/"><b>Railway Oriented Programming</b> </a></u>, which produces some use-then-discard objects,
 *         thus, if this is immutable, mutating {@code result} will creating a new object, which obviously increases the stress of garbage collector, so
 *         we decide to take mutable class, which can be passed as a reference, with no those problems.
 *     </li>
 * </ul>
 * @since 1.0 Release
 */
public final class CarryInteractContext extends BaseNestedPrinter<CarryInteractContext>
{
    private static final @NotNull @Unmodifiable INestedFieldMap<CarryInteractContext> FIELD_MAP = INestedPrintable.buildFieldMap(
        map ->
        {
            map.put("actionType", c -> c.actionType);
            map.put("level", c -> c.level);
            map.put("player", c -> c.player);
            map.put("interactPos", c -> c.interactPos);
            map.put("carryCrate", c -> c.carryCrate);
            map.put("targets", c -> c.targets);
            map.put("carryID", c -> c.carryID);
            map.put("result", c -> c.result);
            map.put("listener", c -> c.listener);
            map.put("component", c -> c.component);
            map.put("target", c -> c.target);
            map.put("data", c -> c.data);
            map.put("blockEntityType", c -> c.blockEntityType);
        },
        13
    );
    
    //*:=== Init fields
    public final @NotNull CarryType actionType;
    public final @NotNull ServerLevel level;
    public final @NotNull ServerPlayer player;
    public final @NotNull BlockPos interactPos;
    public final @NotNull ItemStack carryCrate;
    public final @Nullable Function<@Nullable BlockState, @NotNull StatedBlockPlaceContext> placeContentGetter;
    public final @NotNull TriVariant<BlockState, LivingEntity, BlockEntity> targets;
    public final @NotNull ISealableBox<CarryID> carryID;
    
    private final @NotNull BiConsumer<CarryID, ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> listenerInsert;
    private final @NotNull Consumer<CarryID> listenerRemove;
    private @Nullable InteractionResult result = null;
    
    //*:=== Execute Extra Fields
    public final ISealableBox<TriState> listener = ISealableBox.create();
    public final ISealableBox<TriState> component = ISealableBox.create();
    public final ISealableBox<TriState> target = ISealableBox.create();
    public final ISealableBox<CarryData> data = ISealableBox.create();
    public final ISealableBox<BlockEntityType<?>> blockEntityType = ISealableBox.create();
    
    private CarryInteractContext(
        @NotNull CarryType actionType,
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull BlockPos interactPos,
        @NotNull ItemStack carryCrate,
        @NotNull BiConsumer<CarryID, ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> listenerInsert,
        @NotNull Consumer<CarryID> listenerRemove,
        @Nullable Function<@Nullable BlockState, @NotNull StatedBlockPlaceContext> placeContentGetter,
        @Nullable BlockState targetState,
        @Nullable LivingEntity targetEntity,
        @Nullable BlockEntity targetBlockEntity,
        @Nullable CarryID carryID
    )
    {
        AssertUtils.nonNullCheckOnDev(actionType, "actionType");
        AssertUtils.nonNullCheckOnDev(level, "level");
        AssertUtils.nonNullCheckOnDev(player, "player");
        AssertUtils.nonNullCheckOnDev(carryCrate, "carryCrate");
        AssertUtils.nonNullCheckOnDev(interactPos, "interactPos");
        AssertUtils.nonNullCheckOnDev(listenerInsert, "listenerInsert");
        AssertUtils.nonNullCheckOnDev(listenerRemove, "listenerRemove");
        
        this.actionType = actionType;
        this.level = level;
        this.player = player;
        this.interactPos = interactPos;
        this.carryCrate = carryCrate;
        this.listenerInsert = listenerInsert;
        this.listenerRemove = listenerRemove;
        this.placeContentGetter = placeContentGetter;
        this.targets = TriVariant.ofNullable(targetState, targetEntity, targetBlockEntity);
        this.carryID = ISealableBox.ofNullable(carryID);
    }
    
    public static @NotNull CarryInteractContext init(
        @NotNull CarryType actionType,
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull BlockPos interactPos,
        @NotNull ItemStack carryCrate,
        @NotNull BiConsumer<CarryID, ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> listenerInsert,
        @NotNull Consumer<CarryID> listenerRemove,
        @Nullable Function<@Nullable BlockState, @NotNull StatedBlockPlaceContext> placeContentGetter,
        @Nullable BlockState targetState,
        @Nullable LivingEntity targetEntity,
        @Nullable BlockEntity targetBlockEntity,
        @Nullable CarryID carryID
    )
    {
        return new CarryInteractContext(
            actionType,
            level,
            player,
            interactPos,
            carryCrate,
            listenerInsert,
            listenerRemove,
            placeContentGetter,
            targetState,
            targetEntity,
            targetBlockEntity,
            carryID
        );
    }
    
    private @NotNull CarryInteractContext assignResultAndReturn(@Nullable InteractionResult result)
    {
        if(this.result == result)
            return this;
        
        this.result = result;
        return this;
    }
    
    private @NotNull CarryInteractContext toExecutePhase(
        @NotNull TriState listener,
        @NotNull TriState component,
        @NotNull TriState target,
        @NotNull CarryData data
    )
    {
        AssertUtils.nonNullCheckOnDev(listener, "listener");
        AssertUtils.nonNullCheckOnDev(component, "component");
        AssertUtils.nonNullCheckOnDev(target, "target");
        AssertUtils.nonNullCheckOnDev(data, "data");
        
        this.listener.assign(listener);
        this.component.assign(component);
        this.target.assign(target);
        this.data.bound(data);
        
        if(targets.isRightPresent())
            this.blockEntityType.bound(targets.right().getType());
        
        return this;
    }
    
    public @NotNull CarryInteractContext boxIn(@NotNull CarryData data, boolean addAsExtra)
    {
        return toExecutePhase(
            addAsExtra ? TriState.DEFAULT : TriState.TRUE,
            TriState.TRUE,
            TriState.TRUE,
            data
        );
    }
    
    public @NotNull CarryInteractContext boxIn(@NotNull CarryData data) { return boxIn(data, false); }
    
    public @NotNull CarryInteractContext unbox(@NotNull CarryData data, boolean hasRemaining)
    {
        return toExecutePhase(
            hasRemaining ? TriState.DEFAULT : TriState.FALSE,
            hasRemaining ? TriState.TRUE : TriState.FALSE,
            TriState.FALSE,
            data
        );
    }
    
    public @NotNull CarryInteractContext unbox(@NotNull CarryData data) { return unbox(data, false); }
    
    //! Using SUCCESS may lead to [[ItemStack#shrink]].
    public @NotNull CarryInteractContext success() { return assignResultAndReturn(InteractionResult.SUCCESS_NO_ITEM_USED); }
    public @NotNull CarryInteractContext fail() { return assignResultAndReturn(InteractionResult.FAIL); }
    public @NotNull CarryInteractContext pass() { return assignResultAndReturn(InteractionResult.PASS); }
    public @NotNull CarryInteractContext fallback() { return assignResultAndReturn(null); }
    
    public @NotNull CarryInteractContext rollback()
    {
        this.listener.bound(TriState.TRUE);
        this.component.bound(TriState.TRUE);
        this.target.bound(TriState.TRUE);
        return this;
    }
    
    public void insertListener(@NotNull CarryID carryID, @NotNull ICarryRegistryView.IBaseCarryAdapterFactory<?, ?> factory) { this.listenerInsert.accept(carryID, factory); }
    public void removeListener(@NotNull CarryID carryID) { this.listenerRemove.accept(carryID); }
    public @NotNull TriState listener() { return listener.orThrow(); }
    public @NotNull TriState component() { return component.orThrow(); }
    public @NotNull TriState target() { return target.orThrow(); }
    public @Nullable InteractionResult result() { return result; }
    
    @Override public boolean equals(Object obj)
    {
        return this == obj || obj instanceof CarryInteractContext that &&
            actionType == that.actionType &&
            Objects.equals(level, that.level) &&
            Objects.equals(player, that.player) &&
            Objects.equals(interactPos, that.interactPos) &&
            Objects.equals(carryCrate, that.carryCrate) &&
            Objects.equals(listenerInsert, that.listenerInsert) &&
            Objects.equals(listenerRemove, that.listenerRemove) &&
            Objects.equals(placeContentGetter, that.placeContentGetter) &&
            Objects.equals(targets, that.targets) &&
            Objects.equals(carryID, that.carryID) &&
            result == that.result &&
            Objects.equals(listener, that.listener) &&
            Objects.equals(component, that.component) &&
            Objects.equals(target, that.target) &&
            Objects.equals(data, that.data) &&
            Objects.equals(blockEntityType, that.blockEntityType);
    }
    
    @Override public int hashCode() { return Objects.hash(actionType, level, player, interactPos, carryCrate, targets, carryID, result, listener, component, target, data, blockEntityType); }
    
    @Override public @NotNull @Unmodifiable INestedFieldMap<CarryInteractContext> getFields() { return FIELD_MAP; }
    
    @Override public boolean takeNullFieldAsOptional() { return true; }
}
