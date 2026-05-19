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
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import kurvcygnus.crispsweetberry.lib.base.extensions.StatedBlockPlaceContext;
import kurvcygnus.crispsweetberry.lib.base.lang.ISealableBox;
import kurvcygnus.crispsweetberry.lib.base.lang.TriVariant;
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

import java.util.Map;
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
 *         Isn't this data class <b>too bloat</b>? It has 17 fields, that's not just a few!
 *         <hr>
 *         It's not, because all of these fields are essential in interact logics. <b>Choping these fields into
 *         smaller fields only makes the post-process's context passing more troublesome. Also, field {@code listenerInsert},
 *         {@code listenerRemove} and {@code placeContext} has already got more decoupling effort than directly passing those raw types.</b>
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
public final class CarryInteractContext implements INestedPrintable<CarryInteractContext>
{
    //*:=== Init fields
    private final @NotNull CarryType actionType;
    private final @NotNull ServerLevel level;
    private final @NotNull ServerPlayer player;
    private final @NotNull BlockPos interactPos;
    private final @NotNull ItemStack carryCrate;
    private final @NotNull BiConsumer<CarryID, ICarryRegistryView.IBaseCarryAdapterFactory<?, ?>> listenerInsert;
    private final @NotNull Consumer<CarryID> listenerRemove;
    private final @Nullable Function<@Nullable BlockState, @NotNull StatedBlockPlaceContext> placeContentGetter;
    private final @NotNull TriVariant<BlockState, LivingEntity, BlockEntity> targets;
    private final @NotNull ISealableBox<CarryID> carryID;
    private @Nullable InteractionResult result = null;
    
    //*:=== Execute Extra Fields
    private final ISealableBox<TriState> listener = ISealableBox.create();
    private final ISealableBox<TriState> component = ISealableBox.create();
    private final ISealableBox<TriState> target = ISealableBox.create();
    private final ISealableBox<CarryData> data = ISealableBox.create();
    private final ISealableBox<BlockEntityType<?>> blockEntityType = ISealableBox.create();
    
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
        assert actionType != null : "Param \"actionType\" must not be null!";
        assert level != null : "Param \"level\" must not be null!";
        assert player != null : "Param \"player\" must not be null!";
        assert carryCrate != null : "Param \"carryCrate\" must not be null!";
        assert interactPos != null : "Param \"interactPos\" must not be null!";
        assert listenerInsert != null : "Param \"listenerInsert\" must not be null!";
        assert listenerRemove != null : "Param \"listenerRemove\" must not be null!";
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
    
    public @NotNull CarryType actionType() { return actionType; }
    public @NotNull ServerLevel level() { return level; }
    public @NotNull ServerPlayer player() { return player; }
    public @NotNull BlockPos interactPos() { return interactPos; }
    public @NotNull ItemStack carryCrate() { return carryCrate; }
    public void insertListener(@NotNull CarryID carryID, @NotNull ICarryRegistryView.IBaseCarryAdapterFactory<?, ?> factory) { this.listenerInsert.accept(carryID, factory); }
    public void removeListener(@NotNull CarryID carryID) { this.listenerRemove.accept(carryID); }
    public @Nullable Function<@Nullable BlockState, @NotNull StatedBlockPlaceContext> placeContentGetter() { return placeContentGetter; }
    public @NotNull TriVariant<BlockState, LivingEntity, BlockEntity> targets() { return targets; }
    public @NotNull ISealableBox<CarryID> carryID() { return carryID; }
    public @Nullable InteractionResult result() { return result; }
    public @NotNull ISealableBox<TriState> listener() { return listener; }
    public @NotNull ISealableBox<TriState> component() { return component; }
    public @NotNull ISealableBox<TriState> target() { return target; }
    public @NotNull ISealableBox<CarryData> data() { return data; }
    public @NotNull ISealableBox<BlockEntityType<?>> blockEntityType() { return blockEntityType; }
    
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
    
    @Override public @NotNull String toString() { return toNestedString(); }
    
    @Override public @NotNull @Unmodifiable Map<String, Function<CarryInteractContext, @Nullable Object>> getFields()
    {
        return INestedPrintable.buildFieldMap(
            map ->
            {
                map.put("actionType", CarryInteractContext::actionType);
                map.put("level", CarryInteractContext::level);
                map.put("player", CarryInteractContext::player);
                map.put("interactPos", CarryInteractContext::interactPos);
                map.put("carryCrate", CarryInteractContext::carryCrate);
                map.put("targets", CarryInteractContext::targets);
                map.put("carryID", CarryInteractContext::carryID);
                map.put("result", CarryInteractContext::result);
                map.put("listener", CarryInteractContext::listener);
                map.put("component", CarryInteractContext::component);
                map.put("target", CarryInteractContext::target);
                map.put("data", CarryInteractContext::data);
                map.put("blockEntityType", CarryInteractContext::blockEntityType);
            },
            13
        );
    }
    
    @Override public boolean takeNullFieldAsOptional() { return true; }
}
