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
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryPipelineTask;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.OverweightEffect;
import kurvcygnus.crispsweetberry.utils.base.lang.IResult;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

@ApiStatus.Internal
enum CarryOperationExecutor
{
    INST;
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CARRY_LOGIC");
    
    @NotNull InteractionResult execute(@NotNull CarryPipelineTask task)
    {
        //? return IResult.<CarryPipelineTask, BrokenCarryPipelineException>of(task).
        //?     flatMap(CarryOperationExecutor.INST::listenerProcess).
        //* ↑ Equals to `return this.listenerProcess(task)`.
        //* Despite this one is more semantically friendly, it creates one more [[IResult]], brings more performance penalty.
        
        return this.listenerProcess(task).
            flatMap(CarryOperationExecutor.INST::componentProcess).
            flatMap(CarryOperationExecutor.INST::targetProcess).
            fold(
                CarryPipelineTask::result,
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
                        ex.type,
                        ex.getMessage(),
                        ex.causeData.toString().replace("\n", "\n    "),
                        MetainfoConstants.FEEDBACK_MESSAGE,
                        ex.wrappedException
                    );
                    
                    return ex.causeData.result();
                }
            );
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> listenerProcess(@NotNull CarryPipelineTask task)
    {
        final @Nullable CarryID carryID = task.id();
        final @Nullable CarryData carryData = task.data();
        final TriState listener = task.listener();
        
        if(carryID == null)
            return IResult.ofFailed(
                BrokenCarryPipelineException.listener(
                    task.pass(),
                    "Listener's mutation failed, because the CarryID's value is invalid.",
                    IllegalArgumentException::new,
                    listener
                )
            );
        
        switch(listener)
        {
            case TRUE ->
            {
                if(carryData == null)
                    return IResult.ofFailed(
                        BrokenCarryPipelineException.listener(
                            task.pass(),
                            "Listener's mutation failed because the CarryData's value is invalid, which is required by insert.",
                            IllegalArgumentException::new,
                            TriState.TRUE
                        )
                    );
                
                final Object creationData = carryData.unionData().getCreationData();
                final var factory = CarryRegistryManager.INST.searchFactory(task.type(), creationData);
                
                if(factory.isEmpty())
                    return IResult.ofFailed(
                        BrokenCarryPipelineException.listener(
                            task.pass(),
                            "Cannot find %s's Carry Factory!".formatted(creationData),
                            NoSuchElementException::new,
                            TriState.TRUE
                        )
                    );
                
                task.listenerInsert().accept(carryID, factory.get());
            }
            case FALSE -> task.listenerRemove().accept(carryID);
            default -> {}
        }
        
        if(!listener.isDefault())
        {
            final CarryEngine.CarryListenerSaveData saveData = CarryEngine.CarryListenerSaveData.get(task.level().getServer());
            LOGGER.when(!saveData.isDirty()).debug("Listener data changed. Mark saveData as dirtied.");
            saveData.setDirty();
        }
        
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> componentProcess(@NotNull CarryPipelineTask task)
    {
        final @Nullable CarryID carryID = task.id();
        final @Nullable CarryData carryData = task.data();
        final TriState component = task.component();
        
        if(carryID == null || carryData == null)
            return IResult.ofFailed(
                BrokenCarryPipelineException.component(
                    task.pass(),
                    "Component mutation failed, because part, or all of the parameters' value are invalid. id: %s, data: %s".formatted(carryID, carryData),
                    IllegalArgumentException::new,
                    component
                )
            );
        
        final ItemStack crate = task.crate();
        final boolean producesCopy = component.isTrue() && crate.getCount() > 1;
        LOGGER.debug("Current component process: Copy {}.", producesCopy ? "created" : "ignored");
        
        switch(component)
        {
            case TRUE ->
            {
                final ItemStack crateToMutate = producesCopy ? copyCrate(crate) : crate;
                
                crateToMutate.set(CarryCrateRegistries.CARRY_ID.get(), carryID);
                crateToMutate.set(CarryCrateRegistries.CARRY_CRATE_DATA.get(), carryData);
                
                if(producesCopy)
                {
                    crate.shrink(1);
                    final var player = task.player();
                    if(!player.addItem(crateToMutate))
                    {
                        final var pos = player.getOnPos();
                        Containers.dropItemStack(task.level(), pos.getX(), pos.getY(), pos.getZ(), crateToMutate);
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
        
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> targetProcess(@NotNull CarryPipelineTask task)
    {
        //* This can't be done in [[CarryOperationExecutor#componentProcess]],
        //* because component I/O only means the update of the [[ItemStack]]'s data,
        //* it can't represent whether the player's carryFactor should change.
        OverweightEffect.updateFactorAndEffect(task.player(), task.data(), task.target());
        
        return Objects.requireNonNullElse(
            switch(task.target())
            {
                case TRUE ->
                {
                    if(task.type().equals(CarryType.ENTITY))
                        yield entityTargetCapture(task);
                    yield blocklikeTargetCapture(task);
                }
                case FALSE ->
                {
                    if(task.type().equals(CarryType.ENTITY))
                        yield entityTargetRelease(task);
                    yield blocklikeTargetRelease(task);
                }
                default -> null;
            },
            IResult.of(task.success())
        );
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> blocklikeTargetCapture(@NotNull CarryPipelineTask task)
    {
        final var level = task.level();
        final var pos = task.interactPos();
        
        if(!level.setBlockAndUpdate(pos, Blocks.VOID_AIR.defaultBlockState()))
            return IResult.ofFailed(
                BrokenCarryPipelineException.target(
                    task.pass(),
                    "Unable to change position <%d, %d, %d>'s blockstate! Original Blockstate: %s".
                        formatted(pos.getX(), pos.getY(), pos.getZ(), level.getBlockState(pos).toString()),
                    IllegalAccessError::new,
                    task.type(),
                    TriState.TRUE
                )
            );
        
        level.playSound(null, pos, SoundEvents.SCAFFOLDING_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> blocklikeTargetRelease(@NotNull CarryPipelineTask task)
    {
        final CarryData carryData = task.data();
        final @Nullable var contextFunction = task.placeContext();
        
        if(carryData == null)
            return IResult.ofFailed(
                BrokenCarryPipelineException.target(
                    task.pass(),
                    "Carry Crate's content release failed because the CarryData's value is invalid, which is required by insert.",
                    IllegalArgumentException::new,
                    task.type(),
                    TriState.FALSE
                )
            );
        
        if(contextFunction == null)
            return IResult.ofFailed(
                BrokenCarryPipelineException.target(
                    task.pass(),
                    "The Function that creating contexts placing blocks is not present!",
                    IllegalArgumentException::new,
                    task.type(),
                    TriState.FALSE
                )
            );
        
        final BlockState stateToPlace = getBlocklikeState(carryData, task.type());
        final InteractionResult placeResult = task.placeContext().apply(stateToPlace).performPlace(false);
        
        if(placeResult.equals(InteractionResult.FAIL))
            return IResult.of(task.fail());
        
        if(task.type().equals(CarryType.BLOCK_ENTITY))
            return blockEntityReleaseExtra(task);
        
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> blockEntityReleaseExtra(@NotNull CarryPipelineTask task)
    {
        final @Nullable BlockEntityType<?> blockEntityType = task.blockEntityType();
        
        if(blockEntityType == null)
            return IResult.ofFailed(
                BrokenCarryPipelineException.target(
                    task.pass(),
                    "The BlockEntityType of this blockEntity is not present!",
                    IllegalArgumentException::new,
                    CarryType.BLOCK_ENTITY,
                    TriState.FALSE
                )
            );
        
        assert task.data() != null;
        final CarryData.CarryBlockEntityDataHolder holder = task.data().unionData();
        final BlockState stateToPlace = holder.getState();
        final BlockPos pos = task.interactPos();
        final ServerLevel level = task.level();
        final @Nullable BlockEntity blockEntity = blockEntityType.create(pos, stateToPlace);
        
        if(blockEntity == null)
            return IResult.ofFailed(
                BrokenCarryPipelineException.target(
                    task.fail(),
                    """
                        Failed to create blockEntity "%s"'s adapter.
                        This usually means the blockEntity's type registration itself has dataflow issues,
                        or this method is called at improper time.
                        """.
                        formatted(blockEntityType.toString()),
                    IllegalStateException::new,
                    CarryType.BLOCK_ENTITY,
                    TriState.FALSE
                )
            );
        
        blockEntity.loadCustomOnly(holder.getTagData(), level.registryAccess());
        level.blockEntityChanged(pos);
        
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> entityTargetCapture(@NotNull CarryPipelineTask task)
    {
        final @Nullable LivingEntity entity = task.entity();
        
        if(entity == null)
            return IResult.ofFailed(
                BrokenCarryPipelineException.target(
                    task.pass(),
                    "Illegal operation: The entity to capture does not exist!",
                    IllegalArgumentException::new,
                    CarryType.ENTITY,
                    TriState.TRUE
                )
            );
        
        entity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> entityTargetRelease(@NotNull CarryPipelineTask task)
    {
        final @Nullable CarryData carryData = task.data();
        
        if(carryData == null)
            return IResult.ofFailed(
                BrokenCarryPipelineException.target(
                    task.pass(),
                    "Carry Crate's content release failed because the CarryData's value is invalid, which is required by insert.",
                    IllegalArgumentException::new,
                    CarryType.ENTITY,
                    TriState.FALSE
                )
            );
        
        final ServerLevel level = task.level();
        final CarryData.CarryEntityDataHolder holder = carryData.unionData();
        final Optional<Entity> entityToSpawn = EntityType.create(holder.getTagData(), level);
        
        if(entityToSpawn.isEmpty())
            return IResult.ofFailed(
                BrokenCarryPipelineException.target(
                    task.fail(),
                    "The entity that CarryData holds doesn't exist!",
                    IllegalArgumentException::new,
                    CarryType.ENTITY,
                    TriState.FALSE
                )
            );
        
        final BlockPos spawnPos = task.interactPos();
        final Entity entity = entityToSpawn.get();
        entity.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        level.addFreshEntity(entity);
        
        return IResult.of(task.success());
    }
    
    private static @NotNull ItemStack copyCrate(@NotNull ItemStack crate)
    {
        final ItemStack newCrate = new ItemStack(CarryCrateRegistries.CARRY_CRATE_ITEM.value());
        
        if(crate.has(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()))
            newCrate.set(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get(), crate.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()));
        
        return newCrate;
    }
    
    private static @NotNull BlockState getBlocklikeState(@NotNull CarryData data, @NotNull CarryType type)
    {
        return switch(type)
        {
            case BLOCK ->
            {
                final CarryData.CarryBlockDataHolder holder = data.unionData();
                yield holder.getState();
            }
            case BLOCK_ENTITY ->
            {
                final CarryData.CarryBlockEntityDataHolder holder = data.unionData();
                yield holder.getState();
            }
            case ENTITY -> throw new IllegalArgumentException("Assertion failed: Param \"type\" must not be ENTITY!");
        };
    }
}
