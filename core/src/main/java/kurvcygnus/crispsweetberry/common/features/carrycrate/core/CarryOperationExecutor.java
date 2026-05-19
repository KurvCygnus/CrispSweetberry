//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core;

import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableBlockEntityExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContext;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.exceptions.CarryInteractHandleException;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.OverweightEffect;
import kurvcygnus.crispsweetberry.lib.base.lang.IResult;
import kurvcygnus.crispsweetberry.lib.base.lang.TriVariant;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

@ApiStatus.Internal
enum CarryOperationExecutor
{
    //region Singleton & Constants
    INST;
    
    private static final Map<CarryType, Function<CarryInteractContext, IResult<CarryInteractContext, CarryInteractHandleException>>> BOX_IN_METHODS =
        DefinitionUtils.createImmutableEnumMapWithCheck(
            CarryType.class,
            map ->
            {
                map.put(CarryType.BLOCK_ENTITY, CarryOperationExecutor.INST::blockEntityBoxIn);
                map.put(CarryType.ENTITY, CarryOperationExecutor.INST::entityBoxIn);
                map.put(CarryType.BLOCK, CarryOperationExecutor.INST::blockBoxIn);
            }
        );
    
    private static final Map<CarryType, Function<CarryInteractContext, IResult<CarryInteractContext, CarryInteractHandleException>>> UNBOX_METHODS =
        DefinitionUtils.createImmutableEnumMapWithCheck(
            CarryType.class,
            map ->
            {
                map.put(
                    CarryType.BLOCK_ENTITY,
                    IResult.andThenFlat(CarryOperationExecutor.INST::unboxCheck, CarryOperationExecutor.INST::blockEntityUnbox)
                );
                map.put(
                    CarryType.ENTITY,
                    IResult.andThenFlat(CarryOperationExecutor.INST::unboxCheck, CarryOperationExecutor.INST::entityUnbox)
                );
                map.put(
                    CarryType.BLOCK,
                    IResult.andThenFlat(CarryOperationExecutor.INST::unboxCheck, CarryOperationExecutor.INST::blockUnbox)
                );
            }
        );
    
    private static final Map<Boolean, Map<CarryType, Function<CarryInteractContext, IResult<CarryInteractContext, CarryInteractHandleException>>>> HANDLE_METHODS =
        Map.of(true, BOX_IN_METHODS, false, UNBOX_METHODS);
    
    private static final IMarkLogger LOGGER = IMarkLogger.markedLogger("CARRY_LOGIC");
    //endregion
    
    //region Main Pipeline
    @Nullable InteractionResult handle(@NotNull CarryInteractContext context)
    {
        //? return IResult.<CarryInteractContext, CarryInteractHandleException>of(context).
        //?     flatmap(CarryOperationExecutor.INST::carryIDProcess).
        //* ↑ Equals to `return this.carryIDProcess(context)`.
        //* Despite this one is more semantically friendly, it creates one more [[IResult]], brings more performance penalty.
        
        final var actionType = context.actionType();
        
        return this.carryIDProcess(context).
            flatMap(HANDLE_METHODS.get(!context.carryCrate().has(CarryCrateRegistries.CARRY_CRATE_DATA.get())).get(actionType)).
            flatMap(CarryOperationExecutor.INST::listenerProcess).
            flatMap(CarryOperationExecutor.INST::componentProcess).
            flatMap(CarryOperationExecutor.INST::targetProcess).
            fold(
                CarryInteractContext::result,
                ex ->
                {
                    LOGGER.error(
                        """
                            
                            An error occurred while executing carry pipeline tasks.
                            
                            Happens at: {}
                            Error Details: {}
                            
                            The detailed pipeline task of this round of execution:
                            {}
                            
                            This is a serious logic issue.
                            {}
                            """,
                        ex.type(),
                        ex.getMessage(),
                        ex.causeData().toString(),
                        MetainfoConstants.FEEDBACK_MESSAGE,
                        ex.cause()
                    );
                    
                    return ex.rollback();
                }
            );
    }
    //endregion
    
    //region Interact Process Pipelines
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> carryIDProcess(@NotNull CarryInteractContext context)
    {
        final var actionType = context.actionType();
        
        //* If item has [[CarryData]], but no [[CarryID]], that's counted as a persistent issue.
        //* However, we can't take that as an error, because data still exists, which is still processable, having no [[CarryID]] mainly affects the handle of
        //* [[CarryOperationExecutor#blockEntityUnbox]]'s partial logic.
        if(context.carryID().isAssignable() && !context.carryCrate().has(CarryCrateRegistries.CARRY_CRATE_DATA.get()))
        {
            final @Nullable var carryID = generateCarryID(actionType, context.targets());
            
            if(carryID == null)
                return CarryInteractHandleException.miscFailed(
                    context.fallback(),
                    "Can't generate CarryID for persistent and monitor, because the target has not been registered into carry registry yet!",
                    IllegalArgumentException::new,
                    "CARRY_ID_PRE_PROCESSING"
                );
            
            context.carryID().bound(carryID);
        }
        
        return IResult.of(context);
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> blockEntityBoxIn(@NotNull CarryInteractContext context)
    {
        final var targetState = context.targets().left();
        final var targetPos = context.interactPos();
        final var blockEntity = context.targets().right();
        final var level = context.level();
        final @Nullable var carryID = context.carryID().value();
        
        final Optional<AbstractBlockEntityCarryAdapter<? extends BlockEntity>> optionalAdapter = CarryRegistryManager.INST.
            getBlockEntityAdapter(blockEntity.getType()).map(
                adapterFactory ->
                    createBlockEntityAdapter(
                        adapterFactory,
                        blockEntity
                    )
            );
        
        if(optionalAdapter.isEmpty())
            return CarryInteractHandleException.boxInFailed(
                context.pass(),
                DefinitionUtils.quickFormat("Cannot find blockEntity \"{}\"'s adapter!", blockEntity),
                NoSuchElementException::new,
                CarryType.BLOCK_ENTITY
            );
        
        final var adapter = optionalAdapter.get();
        
        final CompoundTag tagData = new CompoundTag();
        adapter.onCarriedSequence(
            new CarriableBlockEntityExtensions.IAtomicCarriable.CarriedContext(
                level, targetPos,
                context.player(),
                carryID != null ? carryID.uuid() : null
            )
        );
        //* [[IAtomicCarriable#onCarriedSequence()]] may have side effects on BE's data, we should save data after it.
        adapter.saveCarryTag(tagData, level.registryAccess());
        
        final CarryData insertData = CarryData.createBlockEntity(
            targetState,
            tagData,
            blockEntity.getType(),
            adapter.getPenaltyRate(),
            adapter.causesOverweight(),
            level.getGameTime()
        );
        
        return IResult.of(context.success().boxIn(insertData));
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> blockBoxIn(@NotNull CarryInteractContext context)
    {
        final var targetState = context.targets().left();
        final var targetBlock = targetState.getBlock();
        
        final Optional<AbstractBlockCarryAdapter<? extends Block>> optionalAdapter = CarryRegistryManager.INST.
            getBlockAdapter(targetBlock).map(factory -> createBlockAdapter(factory, targetBlock));
        
        if(optionalAdapter.isEmpty())
            return CarryInteractHandleException.boxInFailed(
                context.fallback(),
                DefinitionUtils.quickFormat("Cannot find block \"{}\"'s adapter!", targetBlock.getDescriptionId()),
                NoSuchElementException::new,
                CarryType.BLOCK
            );
        
        final AbstractBlockCarryAdapter<?> adapter = optionalAdapter.get();
        final int carryCount = targetState.hasProperty(BlockStateProperties.LAYERS) ? targetState.getValue(BlockStateProperties.LAYERS) : 1;
        
        final CarryData insertData = CarryData.createBlock(
            targetState,
            adapter.getPenaltyRate(),
            carryCount,
            adapter.getAcceptableCount(),
            adapter.causesOverweight(),
            context.level().getGameTime()
        );
        
        return IResult.of(context.success().boxIn(insertData));
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> entityBoxIn(@NotNull CarryInteractContext context)
    {
        final var targetEntity = context.targets().middle();
        final var tagData = new CompoundTag();
        
        if(!targetEntity.saveAsPassenger(tagData))
            return CarryInteractHandleException.boxInFailed(
                context.fail(),
                DefinitionUtils.quickFormat("Can not get, or use entity \"{}\"'s data, interaction failed.", targetEntity),
                IOException::new,
                CarryType.ENTITY
            );
        
        final Optional<AbstractEntityCarryAdapter<?>> optionalAdapter = CarryRegistryManager.INST.
            getEntityAdapter(targetEntity.getType()).map(factory -> createEntityAdapter(factory, targetEntity));
        
        if(optionalAdapter.isEmpty())
            return CarryInteractHandleException.boxInFailed(
                context.fail(),
                DefinitionUtils.quickFormat("Cannot find entity \"{}\"'s adapter!", targetEntity.toString()),
                NoSuchElementException::new,
                CarryType.ENTITY
            );
        
        final var adapter = optionalAdapter.get();
        
        final CarryData insertData = CarryData.createEntity(
            adapter.getPenaltyRate(),
            targetEntity.getType(),
            tagData,
            adapter.causesOverweight(),
            context.level().getGameTime()
        );
        
        return IResult.of(context.success().boxIn(insertData));
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> blockEntityUnbox(@NotNull CarryInteractContext context)
    {
        final var targetState = context.targets().left();
        final var targetPos = context.interactPos();
        final var level = context.level();
        final var carryCrate = context.carryCrate();
        final var data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        final @Nullable var carryID = carryCrate.get(CarryCrateRegistries.CARRY_ID.get());
        assert data != null : "UwU";
        
        final CarryData.CarryBlockEntityDataHolder blockEntityDataHolder = data.unionData();
        final var blockEntityType = blockEntityDataHolder.getType();
        
        //* NOTE: This logic is illegal on Vanilla. It is implemented with the help of [[CarryEngine#IS_INTERACTING_WITH_BE]] and [[CarryBlockEntityValidationPasser]].
        final Optional<AbstractBlockEntityCarryAdapter<? extends BlockEntity>> optionalAdapter = CarryRegistryManager.INST.
            getBlockEntityAdapter(blockEntityType).map(
                adapterFactory ->
                    createBlockEntityAdapter(
                        adapterFactory,
                        Objects.requireNonNull(
                            blockEntityDataHolder.getType().create(targetPos, targetState),
                            """
                                Fatal:
                                Failed to create blockEntity "%s"'s adapter. This usually means the blockEntity's type registration itself has dataflow issue, or this
                                method is called at improper time.
                                
                                %s
                                """.
                                formatted(
                                    blockEntityType.toString(),
                                    MetainfoConstants.FEEDBACK_MESSAGE
                                )
                        )
                    )
            );
        
        if(optionalAdapter.isEmpty())
            return CarryInteractHandleException.unboxFailed(
                context.fail(),
                DefinitionUtils.quickFormat("Cannot find blockEntity \"{}\"'s adapter! Mark this interaction as failed.", blockEntityType),
                NoSuchElementException::new,
                CarryType.BLOCK_ENTITY
            );
        
        final AbstractBlockEntityCarryAdapter<? extends BlockEntity> adapter = optionalAdapter.get();
        
        final CompoundTag tagData = new CompoundTag();
        adapter.loadCarryTag(tagData, level.registryAccess());//* #onPlacedProcess() may have side effects on BE's data, we should load data before it.
        
        adapter.onPlacedProcess(
            level,
            level.getGameTime() - data.startTime(),
            new CarriableBlockEntityExtensions.IAtomicCarriable.CarriedContext(
                level,
                targetPos,
                context.player(),
                carryID != null ? carryID.uuid() : null
            )
        );
        
        return IResult.of(
            context.success().unbox(
                CarryData.createBlockEntity(
                    targetState,
                    tagData,
                    blockEntityDataHolder.getType(),
                    adapter.getPenaltyRate(),
                    data.causesOverweight(),
                    level.getGameTime()
                )
            )
        );
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> blockUnbox(@NotNull CarryInteractContext context)
    {
        final var carryData = context.carryCrate().get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        assert carryData != null : "UwU";
        final var targetState = context.targets().left();
        final CarryData.CarryBlockDataHolder blockDataHolder = carryData.unionData();
        
        if(targetState.is(blockDataHolder.getState().getBlock()) && blockDataHolder.getCarryCount() < blockDataHolder.getMaxCarryCount())
        {
            final CarryData insertData = CarryData.createBlock(
                targetState,
                blockDataHolder.getPenaltyRate(),
                blockDataHolder.getCarryCount() + 1,
                blockDataHolder.getMaxCarryCount(),
                carryData.causesOverweight(),
                context.level().getGameTime()
            );
            
            return IResult.of(context.success().boxIn(insertData, true));
        }
        
        if(blockDataHolder.getCarryCount() > 1)
            return IResult.of(
                context.unbox(
                    CarryData.createBlock(
                        blockDataHolder.getState(),
                        blockDataHolder.getPenaltyRate(),
                        blockDataHolder.getCarryCount() - 1,
                        blockDataHolder.getMaxCarryCount(),
                        carryData.causesOverweight(),
                        carryData.startTime()
                    ),
                    true
                )
            );
        
        return IResult.of(context.success().unbox(carryData));
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> entityUnbox(@NotNull CarryInteractContext context)
    {
        final var carryData = context.carryCrate().get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        assert carryData != null : "UwU";
        return IResult.of(context.unbox(carryData));
    }
    //endregion
    
    //region Post-Process pipelines
    //*:=== Listener
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> listenerProcess(@NotNull CarryInteractContext context)
    {
        final @Nullable var carryID = context.carryID().value();
        final @Nullable var carryData = context.data().value();
        final TriState listener = context.listener().orThrow();
        
        if(carryID == null)
            return CarryInteractHandleException.listener(
                context.pass(),
                "Listener's mutation miscFailed, because the CarryID doesn't exist.",
                IllegalArgumentException::new,
                listener
            );
        
        switch(listener)
        {
            case TRUE ->
            {
                if(carryData == null)
                    return CarryInteractHandleException.listener(
                        context.pass(),
                        "Listener's mutation miscFailed, because the CarryData doesn't exist, which is required by insertion.",
                        IllegalArgumentException::new,
                        TriState.TRUE
                    );
                
                final Object creationData = carryData.unionData().getCreationData();
                final var factory = CarryRegistryManager.INST.searchFactory(context.actionType(), creationData);
                
                if(factory.isEmpty())
                    return CarryInteractHandleException.listener(
                        context.pass(),
                        DefinitionUtils.quickFormat("Cannot find {}'s Carry Factory!", creationData),
                        NoSuchElementException::new,
                        TriState.TRUE
                    );
                
                context.insertListener(carryID, factory.get());
            }
            case FALSE -> context.removeListener(carryID);
            default -> {}
        }
        
        if(!listener.isDefault())
        {
            final var saveData = CarryEngine.CarryListenerSaveData.get(context.level().getServer());
            
            if(!saveData.isDirty())
            {
                LOGGER.debug("Listener data changed. Mark saveData as dirtied.");
                saveData.setDirty();
            }
        }
        
        return IResult.of(context.success());
    }
    
    //*:=== Component
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> componentProcess(@NotNull CarryInteractContext context)
    {
        final @Nullable var carryID = context.carryID().value();
        final @Nullable var carryData = context.data().value();
        final TriState component = context.component().orThrow();
        
        if(carryID == null || carryData == null)
            return CarryInteractHandleException.component(
                context.pass(),
                DefinitionUtils.quickFormat("Component mutation failed, because part, or all of the parameters' value are invalid. id: {}, data: {}", carryID, carryData),
                IllegalArgumentException::new,
                component
            );
        
        final ItemStack crate = context.carryCrate();
        
        switch(component)
        {
            case TRUE ->
            {
                final boolean producesCopy = component.isTrue() && crate.getCount() > 1;
                final ItemStack crateToMutate = producesCopy ? copyCrate(crate) : crate;
                
                crateToMutate.set(CarryCrateRegistries.CARRY_ID.get(), carryID);
                crateToMutate.set(CarryCrateRegistries.CARRY_CRATE_DATA.get(), carryData);
                
                if(producesCopy)
                {
                    crate.shrink(1);
                    final var player = context.player();
                    if(!player.addItem(crateToMutate))
                    {
                        final var pos = player.getOnPos();
                        Containers.dropItemStack(context.level(), pos.getX(), pos.getY(), pos.getZ(), crateToMutate);
                    }
                }
            }
            case FALSE ->
            {
                crate.remove(CarryCrateRegistries.CARRY_CRATE_DATA.get());
                crate.remove(CarryCrateRegistries.CARRY_ID.get());
                crate.remove(CarryCrateRegistries.CARRY_TICK_COUNTER.get());
            }
            default -> {}
        }
        
        return IResult.of(context.success());
    }
    
    //*:=== Target
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> targetProcess(@NotNull CarryInteractContext context)
    {
        //* This can't be done in [[CarryOperationExecutor#componentProcess]],
        //* because component I/O only means the update of the [[ItemStack]]'s data,
        //* it can't represent whether the player's carryFactor should change.
        OverweightEffect.updateFactorAndEffect(context.player(), context.data().orThrow(), context.target().orThrow());
        
        return Objects.requireNonNullElse(
            switch(context.target().orThrow())
            {
                case TRUE ->
                {
                    if(context.actionType().equals(CarryType.ENTITY))
                        yield entityTargetCapture(context);
                    yield blocklikeTargetCapture(context);
                }
                case FALSE ->
                {
                    if(context.actionType().equals(CarryType.ENTITY))
                        yield entityTargetRelease(context);
                    yield blocklikeTargetRelease(context);
                }
                default -> null;
            },
            IResult.of(context.success())
        );
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> blocklikeTargetCapture(@NotNull CarryInteractContext context)
    {
        final var level = context.level();
        final var pos = context.interactPos();
        
        if(context.actionType().equals(CarryType.BLOCK_ENTITY))
            level.removeBlockEntity(pos);
        
        if(!level.setBlockAndUpdate(pos, Blocks.VOID_AIR.defaultBlockState()))
            return CarryInteractHandleException.target(
                context.pass(),
                "Unable to change position <%d, %d, %d>'s blockstate! Original Blockstate: %s".
                    formatted(pos.getX(), pos.getY(), pos.getZ(), level.getBlockState(pos).toString()),
                IllegalAccessError::new,
                context.actionType(),
                TriState.TRUE
            );
        
        level.playSound(null, pos, SoundEvents.SCAFFOLDING_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        return IResult.of(context.success());
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> blocklikeTargetRelease(@NotNull CarryInteractContext context)
    {
        final var carryData = context.data().value();
        final @Nullable var contextFunction = context.placeContentGetter();
        
        if(carryData == null)
            return CarryInteractHandleException.target(
                context.pass(),
                "Carry Crate's content release miscFailed because the CarryData does not exist, which is required by insertion.",
                IllegalArgumentException::new,
                context.actionType(),
                TriState.FALSE
            );
        
        if(contextFunction == null)
            return CarryInteractHandleException.target(
                context.pass(),
                "The Function that creating contexts placing blocks is not present!",
                IllegalArgumentException::new,
                context.actionType(),
                TriState.FALSE
            );
        
        final var stateToPlace = switch(context.actionType())
        {
            case CarryType that when
                that.equals(CarryType.BLOCK) && carryData.unionData() instanceof CarryData.CarryBlockDataHolder holder ->
                holder.getState();
            case CarryType that when
                that.equals(CarryType.BLOCK_ENTITY) && carryData.unionData() instanceof CarryData.CarryBlockEntityDataHolder holder ->
                holder.getState();
            default -> throw new IllegalArgumentException("Assertion miscFailed: CarryType and CarryData's type doesn't match!");
        };
        
        final InteractionResult placeResult = contextFunction.apply(stateToPlace).performPlace(false);
        
        if(placeResult.equals(InteractionResult.FAIL))
            return this.listenerProcess(context.rollback()).flatMap(CarryOperationExecutor.INST::componentProcess).map(CarryInteractContext::fail);
        
        if(context.actionType().equals(CarryType.BLOCK_ENTITY))
            return blockEntityReleaseExtra(context);
        
        return IResult.of(context.success());
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> blockEntityReleaseExtra(@NotNull CarryInteractContext context)
    {
        final @Nullable var blockEntityType = context.blockEntityType().value();
        
        if(blockEntityType == null)
            return CarryInteractHandleException.target(
                context.pass(),
                "The BlockEntityType of this blockEntity is not present!",
                IllegalArgumentException::new,
                CarryType.BLOCK_ENTITY,
                TriState.FALSE
            );
        
        assert context.data().value() != null : "UwU";
        final CarryData.CarryBlockEntityDataHolder holder = context.data().orThrow().unionData();
        final var stateToPlace = holder.getState();
        final var pos = context.interactPos();
        final var level = context.level();
        final @Nullable var blockEntity = blockEntityType.create(pos, stateToPlace);
        
        if(blockEntity == null)
            return CarryInteractHandleException.target(
                context.fail(),
                """
                    Failed to create blockEntity "%s"'s adapter.
                    This usually means the blockEntity's type registration itself has dataflow issues,
                    or this method is called at improper time.
                    """.
                    formatted(blockEntityType.toString()),
                IllegalStateException::new,
                CarryType.BLOCK_ENTITY,
                TriState.FALSE
            );
        
        blockEntity.loadCustomOnly(holder.getTagData(), level.registryAccess());
        level.blockEntityChanged(pos);
        
        return IResult.of(context.success());
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> entityTargetCapture(@NotNull CarryInteractContext context)
    {
        if(!context.targets().isMiddlePresent())
            return CarryInteractHandleException.target(
                context.pass(),
                "The entity to capture does not exist!",
                IllegalArgumentException::new,
                CarryType.ENTITY,
                TriState.TRUE
            );
        
        context.targets().middle().remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        return IResult.of(context.success());
    }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> entityTargetRelease(@NotNull CarryInteractContext context)
    {
        final @Nullable CarryData carryData = context.data().value();
        final @Nullable var contextGetter = context.placeContentGetter();
        
        if(carryData == null)
            return CarryInteractHandleException.target(
                context.pass(),
                "Carry Crate's content release miscFailed because the CarryData's value is invalid, which is required by insert.",
                IllegalArgumentException::new,
                CarryType.ENTITY,
                TriState.FALSE
            );
        
        if(contextGetter == null)
            return CarryInteractHandleException.target(
                context.fail(),
                "Carry Crate's content release miscFailed because this interaction is triggered with an unexpected mean!",
                IllegalStateException::new,
                CarryType.ENTITY,
                TriState.FALSE
            );
        
        final var level = context.level();
        final CarryData.CarryEntityDataHolder holder = carryData.unionData();
        final var entityToSpawn = EntityType.create(holder.getTagData(), level);
        
        if(entityToSpawn.isEmpty())
            return CarryInteractHandleException.target(
                context.fail(),
                "The entity that CarryData will hold doesn't exist!",
                IllegalArgumentException::new,
                CarryType.ENTITY,
                TriState.FALSE
            );
        
        //! Here, we use [[UseOnContext]] to get a usable position,
        //! because [[CarryInteractContext#interactPos]] doesn't contain [[Direction]], and that is nullable, which doesn't worth be an independent field.
        //! Notes that this method can only be called by [[CarryCrateItem#useOn]], it does has [[UseOnContext]].
        //! And since we doesn't need to place any blocks, we create its children class [[StatedBlockPlaceContext]] with `null`.
        final @Nullable var spawnPos = getSafePosition(level, contextGetter.apply(null), entityToSpawn.get());
        
        if(spawnPos == null)
            return this.listenerProcess(context.rollback()).flatMap(CarryOperationExecutor.INST::componentProcess).map(CarryInteractContext::fail);
        
        final var entity = entityToSpawn.get();
        entity.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        level.addFreshEntity(entity);
        
        return IResult.of(context.success());
    }
    //endregion
    
    //region Private Helpers
    private static @Nullable CarryID generateCarryID(@NotNull CarryType type, @NotNull TriVariant<BlockState, LivingEntity, BlockEntity> targets)
    {
        assert type != null : "Param \"type\" must not be null!";
        assert targets != null : "Param \"targets\" must not be null!";
        
        final @Nullable var resourceID = targets.fold(
            l -> checkThenGet(type, l.getBlock(), BuiltInRegistries.BLOCK::getKey),
            m -> checkThenGet(type, m.getType(), EntityType::getKey),
            r -> checkThenGet(type, r.getType(), BlockEntityType::getKey),
            true//! Blocklike includes both Block and BlockEntity, so we should try BlockEntity first.
        );
        
        if(resourceID == null)
            return null;
        
        return CarryID.create(resourceID, UUID.randomUUID());
    }
    
    private static <T> @Nullable ResourceLocation checkThenGet(@NotNull CarryType type, @NotNull T key, @NotNull Function<T, ResourceLocation> resourceGetter)
    {
        assert key != null : "Param \"key\" must not be null!";
        assert resourceGetter != null : "Param \"resourceGetter\" must not be null!";
        
        if(!type.boundClass().isInstance(key))
            return null;
        
        return resourceGetter.apply(key);
    }
    
    @SuppressWarnings("unchecked")//! Safe casting awa
    private static <B extends Block> @NotNull AbstractBlockCarryAdapter<? extends B> createBlockAdapter(
        @NotNull ICarryRegistryView.ICarryBlockAdapterFactory<B, ?> factory,
        @NotNull Block block
    ) { return factory.create((B) block); }
    
    @SuppressWarnings("unchecked")//! Safe casting OwO
    private static <E extends BlockEntity> @NotNull AbstractBlockEntityCarryAdapter<? extends E> createBlockEntityAdapter(
        @NotNull ICarryRegistryView.ICarryBlockEntityAdapterFactory<E, ?> factory,
        @NotNull BlockEntity entity
    ) { return factory.create((E) entity); }
    
    @SuppressWarnings("unchecked")//! Safe casting UwU
    private static <E extends LivingEntity> @NotNull AbstractEntityCarryAdapter<? extends E> createEntityAdapter(
        @NotNull ICarryRegistryView.ICarryEntityAdapterFactory<E, ?> factory,
        @NotNull LivingEntity entity
    ) { return factory.create((E) entity); }
    
    private @NotNull IResult<CarryInteractContext, CarryInteractHandleException> unboxCheck(@NotNull CarryInteractContext context)
    {
        final ItemStack carryCrate = context.carryCrate();
        if(!carryCrate.has(CarryCrateRegistries.CARRY_CRATE_DATA.get()))
            return CarryInteractHandleException.miscFailed(
                context.fallback(),
                "ItemStack \"%s\" has no CarryData, can't release content.",
                IllegalArgumentException::new,
                "UNBOX_DATA_PRECHECK"
            );
        
        if(!carryCrate.has(CarryCrateRegistries.CARRY_ID.get()))
            LOGGER.error(
                "\"{}\"'s adapter has no uuid.",
                switch(context.actionType())
                {
                    case BLOCK -> context.targets().left().getBlock();
                    case ENTITY -> context.targets().middle().getType();
                    case BLOCK_ENTITY -> context.targets().right().getType();
                }
            );
        
        return IResult.of(context);
    }
    
    private static @NotNull ItemStack copyCrate(@NotNull ItemStack crate)
    {
        final ItemStack newCrate = new ItemStack(CarryCrateRegistries.CARRY_CRATE_ITEM.value());
        
        if(crate.has(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()))
            newCrate.set(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get(), crate.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()));
        
        return newCrate;
    }
    
    private static @Nullable BlockPos getSafePosition(@NotNull Level level, @NotNull UseOnContext context, @NotNull Entity entity)
    {
        final var targetPos = context.getClickedPos();
        final var entityAABB = entity.getType().getSpawnAABB(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        
        if(level.noCollision(entityAABB))
            return targetPos;
        
        return null;
    }
    //endregion
}
