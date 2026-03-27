//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryBlockPlaceContext;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
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

import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler.HandleResult.OperationType;
import static kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils.throwIf;

public enum CarryOperationExecutor
{
    INST;
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CARRY_LOGIC");
    private static final BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>> DO_NOTHING =
        (var1, var2) -> {};
    
    //region Exact Actions
    //*:=== Listener
    private static void listenerAddAction(@NotNull CarryOperationContext context, @Nullable AtomicReference<InteractionResult> resultReference)
    {
        if(context.optionalData.isEmpty() || context.optionalID.isEmpty())
            return;
        
        final CarryData data = context.optionalData.get();
        final var creationData = data.unionData().getCreationData();
        final var factory = CarryRegistryManager.INST.searchFactory(data.carryType(), creationData);
        
        factory.ifPresentOrElse(
            f -> context.targetMap.put(context.optionalID.get(), f),
            () -> LOGGER.debug("Can't find the factory! Detailed serach key: {}", creationData.toString())
        );
    }
    
    private static void listenerRemoveAction(@NotNull CarryOperationContext context, @Nullable AtomicReference<InteractionResult> resultReference)
    {
        if(context.optionalID.isEmpty())
            return;
        
        context.targetMap.remove(context.optionalID.get());
    }
    
    //*:=== Component
    private static void componentInsertAction(@NotNull CarryOperationContext context, @Nullable AtomicReference<InteractionResult> resultReference)
    {
        final BiFunction<ItemStack, String, String> printTemplate =
            (itemStack, name) -> "Can't invoke %s's data insertion on itemStack \"%s\", because it is null!".formatted(name, itemStack.toString());
        
        context.optionalData.ifPresentOrElse(
            data -> insertData(context.carryCrate, CarryCrateRegistries.CARRY_CRATE_DATA.get(), data),
            () -> LOGGER.warn(printTemplate.apply(context.carryCrate, "carryData"))
        );
        
        context.optionalID.ifPresentOrElse(
            id -> insertData(context.carryCrate, CarryCrateRegistries.CARRY_ID.get(), id),
            () -> LOGGER.warn(printTemplate.apply(context.carryCrate, "carryID"))
        );
    }
    
    private static void componentRemoveAction(@NotNull CarryOperationContext context, @Nullable AtomicReference<InteractionResult> resultReference)
    {
        final ItemStack carryCrate = context.carryCrate;
        carryCrate.remove(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        carryCrate.remove(CarryCrateRegistries.CARRY_ID.get());
        carryCrate.remove(CarryCrateRegistries.CARRY_TICK_COUNTER.get());
    }
    
    //*:=== Target
    private static void blocklikeTargetCaptureAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
    {
        final ServerLevel level = context.level;
        final BlockPos pos = context.pos;
        level.setBlockAndUpdate(pos, Blocks.VOID_AIR.defaultBlockState());
        level.playSound(null, pos, SoundEvents.SCAFFOLDING_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        context.carryCrate.shrink(1);
    }
    
    private static void blockTargetReleasePreAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
    {
        context.optionalData.ifPresentOrElse(
            data ->
            {
                final CarryData.CarryBlockDataHolder holder = data.unionData();
                blocklikeTargetReleaseAction(context, holder.getState(), resultReference);
            },
            () -> LOGGER.warn("carryData doesn't exist, but the operation is still release.\n{}.", MiscConstants.FEEDBACK_MESSAGE)
        );
    }
    
    private static void blockEntityTargetReleasePreAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
    {
        context.optionalData.ifPresentOrElse(
            data ->
            {
                final CarryData.CarryBlockEntityDataHolder holder = data.unionData();
                blocklikeTargetReleaseAction(context, holder.getState(), resultReference);
            },
            () -> LOGGER.warn("carryData doesn't exist, but the operation is still release.\n{}.", MiscConstants.FEEDBACK_MESSAGE)
        );
    }
    
    private static void blocklikeTargetReleaseAction(
        @NotNull CarryOperationContext context,
        @NotNull BlockState stateToPlace,
        @NotNull AtomicReference<InteractionResult> resultReference
    )
    {
        assert context.optionalData.isPresent();
        
        final CarryData data = context.optionalData.get();
        final CarryBlockPlaceContext placeContext = context.placeContextFunction.apply(stateToPlace);
        resultReference.set(placeContext.performPlace());
        
        CrispFunctionalUtils.doIf(
            data.carryType().equals(CarryType.BLOCK_ENTITY),
            () -> blockEntityTargetReleaseExtra(context, stateToPlace)
        );
    }
    
    private static void blockEntityTargetReleaseExtra(@NotNull CarryOperationContext context, @NotNull BlockState stateToPlace)
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
                            MiscConstants.FEEDBACK_MESSAGE
                        )
                );
                
                blockEntity.loadCustomOnly(holder.getTagData(), context.level.registryAccess());
                context.level.blockEntityChanged(pos);
            },
            () -> LOGGER.error("The CarryType is BLOCK_ENTITY, but result's blockEntityType is empty!")
        );
    }
    
    private static void entityTargetCaptureAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
    {
        context.optionalEntity.ifPresentOrElse(
            entity -> entity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER),
            () -> LOGGER.error("The CarryType is ENTITY, but result's entity is empty!")
        );
    }
    
    private static void entityTargetReleaseAction(@NotNull CarryOperationContext context, @NotNull AtomicReference<InteractionResult> resultReference)
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
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> LISTENER_LOGICS = CrispDefUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor::listenerAddAction);
            map.put(TriState.DEFAULT, DO_NOTHING);
            map.put(TriState.FALSE, CarryOperationExecutor::listenerRemoveAction);
        }
    );
    
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> COMPONENT_LOGICS = CrispDefUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor::componentInsertAction);
            map.put(TriState.DEFAULT, DO_NOTHING);
            map.put(TriState.FALSE, CarryOperationExecutor::componentRemoveAction);
        }
    );
    
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> BLOCK_TARGET_LOGICS = CrispDefUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor::blocklikeTargetCaptureAction);
            map.put(TriState.DEFAULT, DO_NOTHING);
            map.put(TriState.FALSE, CarryOperationExecutor::blockTargetReleasePreAction);
        }
    );
    
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> BLOCK_ENTITY_TARGET_LOGICS = CrispDefUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor::blocklikeTargetCaptureAction);
            map.put(TriState.DEFAULT, DO_NOTHING);
            map.put(TriState.FALSE, CarryOperationExecutor::blockEntityTargetReleasePreAction);
        }
    );
    
    private static final Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>> ENTITY_TARGET_LOGICS = CrispDefUtils.createImmutableEnumMap(
        TriState.class,
        map ->
        {
            map.put(TriState.TRUE, CarryOperationExecutor::entityTargetCaptureAction);
            map.put(TriState.DEFAULT, DO_NOTHING);
            map.put(TriState.FALSE, CarryOperationExecutor::entityTargetReleaseAction);
        }
    );
    //endregion
    
    //region Operation Type Dispatchers
    private static final Map<CarryType, Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>>>
    LISTENER_TYPE_HANDLER = CrispDefUtils.createImmutableEnumMap(
        CarryType.class,
        map ->
        {
            for(final CarryType type: CarryType.values())
                map.put(type, LISTENER_LOGICS);
        }
    );
    
    private static final Map<CarryType, Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>>>
    COMPONENT_TYPE_HANDLER = CrispDefUtils.createImmutableEnumMap(
        CarryType.class,
        map ->
        {
            for(final CarryType type: CarryType.values())
                map.put(type, COMPONENT_LOGICS);
        }
    );
    
    private static final Map<CarryType, Map<TriState, BiConsumer<CarryOperationContext, AtomicReference<InteractionResult>>>>
    TARGET_TYPE_HANDLER = CrispDefUtils.createImmutableEnumMap(
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
        CrispDefUtils.createImmutableEnumMap(
        OperationType.class,
        map ->
        {
            map.put(OperationType.LISTENER, LISTENER_TYPE_HANDLER);
            map.put(OperationType.COMPONENT, COMPONENT_TYPE_HANDLER);
            map.put(OperationType.TARGET, TARGET_TYPE_HANDLER);
        }
    );
    
    public static void execute(
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
    
    //region Helpers & Data Holder Declaration
    private static <T> void insertData(@NotNull ItemStack stack, @NotNull DataComponentType<T> type, @NotNull T data)
    {
        throwIf(
            stack.isEmpty() || !stack.is(CarryCrateRegistries.CARRY_CRATE_ITEM.value()),
            "Param \"stack\" must be \"%s:carry_crate\"!".formatted(CrispSweetberry.NAMESPACE),
            IllegalArgumentException::new
        );
        
        throwIf(
            stack.getCount() < 1,
            "Param \"stack\"'s count should be an positive integer!",
            IllegalArgumentException::new
        );
        
        if(stack.getCount() == 1)
        {
            stack.set(type, data);
            return;
        }
        
        stack.shrink(1);
        final ItemStack newStack = stack.copy();
        newStack.setCount(1);
        newStack.set(type, data);
    }
    
    public record CarryOperationContext(
        @NotNull Optional<CarryData> optionalData,
        @NotNull Optional<CarryID> optionalID,
        @NotNull Optional<BlockEntityType<?>> optionalType,
        @NotNull Optional<Entity> optionalEntity,
        @NotNull ItemStack carryCrate,
        @NotNull HashMap<CarryID, ICarryRegistry.IBaseCarryAdapterFactory<?, ?>> targetMap,
        @NotNull ServerLevel level,
        @NotNull BlockPos pos,
        @NotNull Function<BlockState, CarryBlockPlaceContext> placeContextFunction,
        @NotNull Consumer<TriState> callback
    ) { }
    //endregion
}
