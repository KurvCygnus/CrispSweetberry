//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import kurvcygnus.crispsweetberry.utils.base.extension.StatedBlockPlaceContext;
import kurvcygnus.crispsweetberry.utils.constants.DummyFunctionalConstants;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler.OperationType;

@ApiStatus.Internal
enum CarryOperationExecutor
{
    INST;
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CARRY_LOGIC");
    
    //region Exact Actions
    //*:=== Listener
    private void listenerAddAction(@NotNull CarryOperationContext context, @Nullable AtomicReference<InteractionResult> resultReference)
    {
        if(context.optionalData.isEmpty() || context.optionalID.isEmpty())
            return;
        
        final CarryData data = context.optionalData.get();
        final var creationData = data.unionData().getCreationData();
        final var optionalFactory = CarryRegistryManager.INST.searchFactory(data.carryType(), creationData);
        
        optionalFactory.ifPresentOrElse(
            factory -> context.targetMap.put(context.optionalID.get(), factory),
            () -> LOGGER.debug("Can't find the factory! Detailed serach key: {}", creationData.toString())
        );
    }
    
    private void listenerRemoveAction(@NotNull CarryOperationContext context, @Nullable AtomicReference<InteractionResult> resultReference)
    {
        if(context.optionalID.isEmpty())
            return;
        
        context.targetMap.remove(context.optionalID.get());
    }
    
    //*:=== Component
    private void componentInsertAction(@NotNull CarryOperationContext context, @Nullable AtomicReference<InteractionResult> resultReference)
    {
        final BiFunction<ItemStack, String, String> printTemplate =
            (itemStack, name) -> "Can't invoke %s's data insertion on itemStack \"%s\", because it is null!".formatted(name, itemStack.toString());
        
        final ItemStack carryCrate = context.carryCrate.apply(OperationType.COMPONENT, TriState.TRUE);
        
        context.optionalData.ifPresentOrElse(
            data -> carryCrate.set(CarryCrateRegistries.CARRY_CRATE_DATA.get(), data),
            () -> LOGGER.warn(printTemplate.apply(carryCrate, "carryData"))
        );
        
        context.optionalID.ifPresentOrElse(
            id -> carryCrate.set(CarryCrateRegistries.CARRY_ID.get(), id),
            () -> LOGGER.warn(printTemplate.apply(carryCrate, "carryID"))
        );
    }
    
    private void componentRemoveAction(@NotNull CarryOperationContext context, @Nullable AtomicReference<InteractionResult> resultReference)
    {
        final ItemStack carryCrate = context.carryCrate.apply(OperationType.COMPONENT, TriState.FALSE);
        carryCrate.remove(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        carryCrate.remove(CarryCrateRegistries.CARRY_ID.get());
        carryCrate.remove(CarryCrateRegistries.CARRY_TICK_COUNTER.get());
    }
    
    //*:=== Target
    private void blocklikeTargetCaptureAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
    {
        final ServerLevel level = context.level;
        final BlockPos pos = context.pos;
        level.setBlockAndUpdate(pos, Blocks.VOID_AIR.defaultBlockState());
        level.playSound(null, pos, SoundEvents.SCAFFOLDING_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
    
    private void blockTargetReleasePreAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
    {
        context.optionalData.ifPresentOrElse(
            data ->
            {
                final CarryData.CarryBlockDataHolder holder = data.unionData();
                blocklikeTargetReleaseAction(context, holder.getState(), resultReference);
            },
            () -> LOGGER.warn("carryData doesn't exist, but the operation is still \"RELEASE\".\n{}.", MetainfoConstants.FEEDBACK_MESSAGE)
        );
    }
    
    private void blockEntityTargetReleasePreAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
    {
        context.optionalData.ifPresentOrElse(
            data ->
            {
                final CarryData.CarryBlockEntityDataHolder holder = data.unionData();
                blocklikeTargetReleaseAction(context, holder.getState(), resultReference);
            },
            () -> LOGGER.warn("carryData doesn't exist, but the operation is still \"RELEASE\".\n{}.", MetainfoConstants.FEEDBACK_MESSAGE)
        );
    }
    
    private void blocklikeTargetReleaseAction(
        @NotNull CarryOperationContext context,
        @NotNull BlockState stateToPlace,
        @NotNull AtomicReference<InteractionResult> resultReference
    )
    {
        assert context.optionalData.isPresent();
        
        final CarryData data = context.optionalData.get();
        final StatedBlockPlaceContext placeContext = context.placeContextFunction.apply(stateToPlace);
        resultReference.set(placeContext.performPlace());
        
        FunctionalUtils.doIf(
            data.carryType().equals(CarryType.BLOCK_ENTITY),
            () -> blockEntityTargetReleaseExtra(context, stateToPlace)
        );
    }
    
    private void blockEntityTargetReleaseExtra(@NotNull CarryOperationContext context, @NotNull BlockState stateToPlace)
    {
        assert context.optionalData.isPresent();
        final CarryData.CarryBlockEntityDataHolder holder = context.optionalData.get().unionData();
        final BlockPos pos = context.pos;
        
        context.optionalType.ifPresentOrElse(
            type ->
            {
                final BlockEntity blockEntity = Objects.requireNonNull(
                    type.create(pos, stateToPlace),
                    """
                        Fetal:
                        Failed to create blockEntity "%s"'s adapter.
                        This usually means the blockEntity's type registration itself has dataflow issues,
                        or this method is called at improper time.
                        
                        %s
                        """.
                        formatted(
                            type.toString(),
                            MetainfoConstants.FEEDBACK_MESSAGE
                        )
                );
                
                blockEntity.loadCustomOnly(holder.getTagData(), context.level.registryAccess());
                context.level.blockEntityChanged(pos);
            },
            () -> LOGGER.error("The CarryType is BLOCK_ENTITY, but result's blockEntityType is empty!")
        );
    }
    
    private void entityTargetCaptureAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
    {
        context.optionalEntity.ifPresentOrElse(
            entity -> entity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER),
            () -> LOGGER.error("The CarryType is ENTITY, but result's entity is empty!")
        );
    }
    
    private void entityTargetReleaseAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
    {
        context.optionalData.ifPresentOrElse(
            data ->
            {
                final CarryData.CarryEntityDataHolder holder = data.unionData();
                final Optional<Entity> entityToSpawn = EntityType.create(holder.getTagData(), context.level);
                
                entityToSpawn.ifPresentOrElse(
                    entity ->
                    {
                        final BlockPos pos = context.pos;
                        entity.moveTo(pos.getX(), pos.getY(), pos.getZ());
                        context.level.addFreshEntity(entity);
                    },
                    () -> LOGGER.error("The entity that carryData holds doesn't exist!")
                );
            },
            () -> LOGGER.error("The CarryType is ENTITY, but result's carryData is empty!")
        );
    }
    //endregion
    
    //region TriState Operation Dispatchers
    @SuppressWarnings("unchecked")//! As constants named, it does nothing, thus we can cast it.
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> LISTENER_LOGICS = DefinitionUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor.INST::listenerAddAction);
            map.put(TriState.DEFAULT, (BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>) DummyFunctionalConstants.DO_NOTHING_BI);
            map.put(TriState.FALSE, CarryOperationExecutor.INST::listenerRemoveAction);
        }
    );
    
    @SuppressWarnings("unchecked")//! As constants named, it does nothing, thus we can cast it.
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> COMPONENT_LOGICS = DefinitionUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor.INST::componentInsertAction);
            map.put(TriState.DEFAULT, (BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>) DummyFunctionalConstants.DO_NOTHING_BI);
            map.put(TriState.FALSE, CarryOperationExecutor.INST::componentRemoveAction);
        }
    );
    
    @SuppressWarnings("unchecked")//! As constants named, it does nothing, thus we can cast it.
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> BLOCK_TARGET_LOGICS = DefinitionUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor.INST::blocklikeTargetCaptureAction);
            map.put(TriState.DEFAULT, (BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>) DummyFunctionalConstants.DO_NOTHING_BI);
            map.put(TriState.FALSE, CarryOperationExecutor.INST::blockTargetReleasePreAction);
        }
    );
    
    @SuppressWarnings("unchecked")//! As constants named, it does nothing, thus we can cast it.
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> BLOCK_ENTITY_TARGET_LOGICS = DefinitionUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor.INST::blocklikeTargetCaptureAction);
            map.put(TriState.DEFAULT, (BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>) DummyFunctionalConstants.DO_NOTHING_BI);
            map.put(TriState.FALSE, CarryOperationExecutor.INST::blockEntityTargetReleasePreAction);
        }
    );
    
    @SuppressWarnings("unchecked")//! As constants named, it does nothing, thus we can cast it.
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> ENTITY_TARGET_LOGICS = DefinitionUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor.INST::entityTargetCaptureAction);
            map.put(TriState.DEFAULT, (BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>) DummyFunctionalConstants.DO_NOTHING_BI);
            map.put(TriState.FALSE, CarryOperationExecutor.INST::entityTargetReleaseAction);
        }
    );
    //endregion
    
    //region Operation Type Dispatchers
    private static final Map<CarryType, Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>>>
    LISTENER_TYPE_HANDLER = DefinitionUtils.createImmutableEnumMap(
        CarryType.class,
        map ->
        {
            for(final CarryType type: CarryType.values())
                map.put(type, LISTENER_LOGICS);
        }
    );
    
    private static final Map<CarryType, Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>>>
    COMPONENT_TYPE_HANDLER = DefinitionUtils.createImmutableEnumMap(
        CarryType.class,
        map ->
        {
            for(final CarryType type: CarryType.values())
                map.put(type, COMPONENT_LOGICS);
        }
    );
    
    private static final Map<CarryType, Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>>>
    TARGET_TYPE_HANDLER = DefinitionUtils.createImmutableEnumMap(
        CarryType.class,
        map ->
        {
            map.put(CarryType.BLOCK, BLOCK_TARGET_LOGICS);
            map.put(CarryType.BLOCK_ENTITY, BLOCK_ENTITY_TARGET_LOGICS);
            map.put(CarryType.ENTITY, ENTITY_TARGET_LOGICS);
        }
    );
    //endregion
    
    //region Core Dispatch Logics
    private static final Map<OperationType, Map<CarryType, Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>>>>
    ACTION_DISPATCHER =
        DefinitionUtils.createImmutableEnumMap(
        OperationType.class,
        map ->
        {
            map.put(OperationType.LISTENER, LISTENER_TYPE_HANDLER);
            map.put(OperationType.COMPONENT, COMPONENT_TYPE_HANDLER);
            map.put(OperationType.TARGET, TARGET_TYPE_HANDLER);
        }
    );
    
    static void execute(
        @NotNull OperationType operationType,
        @NotNull CarryType carryType,
        @NotNull TriState state,
        @NotNull CarryOperationContext context,
        @Nullable AtomicReference<InteractionResult> resultReference
    )
    {
        ACTION_DISPATCHER.get(operationType).get(carryType).get(state).accept(context, resultReference);
        context.callback.accept(state);
    }
    //endregion
    
    //region Data Object
    record CarryOperationContext(
        @NotNull Optional<CarryData> optionalData,
        @NotNull Optional<CarryID> optionalID,
        @NotNull Optional<BlockEntityType<?>> optionalType,
        @NotNull Optional<Entity> optionalEntity,
        @NotNull BiFunction<OperationType, TriState, ItemStack> carryCrate,
        @NotNull HashMap<CarryID, ICarryRegistry.IBaseCarryAdapterFactory<?, ?>> targetMap,
        @NotNull ServerLevel level,
        @NotNull BlockPos pos,
        @NotNull Function<BlockState, StatedBlockPlaceContext> placeContextFunction,
        @NotNull Consumer<TriState> callback
    ) {}
    //endregion
}
