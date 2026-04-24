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
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryPipelineTask;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.OverweightEffect;
import kurvcygnus.crispsweetberry.utils.base.lang.IResult;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.util.TriState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;
import java.util.Objects;

@ApiStatus.Internal
enum CarryOperationExecutor
{
    //region Singleton & Constants
    INST;
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CARRY_LOGIC");
    //endregion
    
    //region Main Pipeline
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
                        ex.type(),
                        ex.getMessage(),
                        ex.causeData.toString().replace("\n", "\n    "),
                        MetainfoConstants.FEEDBACK_MESSAGE,
                        ex.wrappedException()
                    );
                    
                    return ex.causeData.result();
                }
            );
    }
    //endregion
    
    //region Sub pipelines
    //*:=== Listener
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> listenerProcess(@NotNull CarryPipelineTask task)
    {
        final @Nullable var carryID = task.id();
        final @Nullable var carryData = task.data();
        final TriState listener = task.listener();
        
        if(carryID == null)
            return BrokenCarryPipelineException.listener(
                task.pass(),
                "Listener's mutation failed, because the CarryID doesn't exist.",
                IllegalArgumentException::new,
                listener
            );
        
        switch(listener)
        {
            case TRUE ->
            {
                if(carryData == null)
                    return BrokenCarryPipelineException.listener(
                        task.pass(),
                        "Listener's mutation failed, because the CarryData doesn't exist, which is required by insertion.",
                        IllegalArgumentException::new,
                        TriState.TRUE
                    );
                
                final Object creationData = carryData.unionData().getCreationData();
                final var factory = CarryRegistryManager.INST.searchFactory(task.type(), creationData);
                
                if(factory.isEmpty())
                    return BrokenCarryPipelineException.listener(
                        task.pass(),
                        "Cannot find %s's Carry Factory!".formatted(creationData),
                        NoSuchElementException::new,
                        TriState.TRUE
                    );
                
                task.insertListener(carryID, factory.get());
            }
            case FALSE -> task.removeListener(carryID);
            default -> {}
        }
        
        if(!listener.isDefault())
        {
            final var saveData = CarryEngine.CarryListenerSaveData.get(task.level().getServer());
            LOGGER.when(!saveData.isDirty()).debug("Listener data changed. Mark saveData as dirtied.");
            saveData.setDirty();
        }
        
        return IResult.of(task.success());
    }
    
    //*:=== Component
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> componentProcess(@NotNull CarryPipelineTask task)
    {
        final @Nullable var carryID = task.id();
        final @Nullable var carryData = task.data();
        final TriState component = task.component();
        
        if(carryID == null || carryData == null)
            return BrokenCarryPipelineException.component(
                task.pass(),
                "Component mutation failed, because part, or all of the parameters' value are invalid. id: %s, data: %s".formatted(carryID, carryData),
                IllegalArgumentException::new,
                component
            );
        
        final ItemStack crate = task.crate();
        
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
    
    //*:=== Target
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
        
        if(task.type().equals(CarryType.BLOCK_ENTITY))
            level.removeBlockEntity(pos);
        
        if(!level.setBlockAndUpdate(pos, Blocks.VOID_AIR.defaultBlockState()))
            return BrokenCarryPipelineException.target(
                task.pass(),
                "Unable to change position <%d, %d, %d>'s blockstate! Original Blockstate: %s".
                    formatted(pos.getX(), pos.getY(), pos.getZ(), level.getBlockState(pos).toString()),
                IllegalAccessError::new,
                task.type(),
                TriState.TRUE
            );
        
        level.playSound(null, pos, SoundEvents.SCAFFOLDING_STEP, SoundSource.BLOCKS, 1.0F, 1.0F);
        
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> blocklikeTargetRelease(@NotNull CarryPipelineTask task)
    {
        final var carryData = task.data();
        final @Nullable var contextFunction = task.placeContext();
        
        if(carryData == null)
            return BrokenCarryPipelineException.target(
                task.pass(),
                "Carry Crate's content release failed because the CarryData does not exist, which is required by insertion.",
                IllegalArgumentException::new,
                task.type(),
                TriState.FALSE
            );
        
        if(contextFunction == null)
            return BrokenCarryPipelineException.target(
                task.pass(),
                "The Function that creating contexts placing blocks is not present!",
                IllegalArgumentException::new,
                task.type(),
                TriState.FALSE
            );
        
        final var stateToPlace = switch(task.type())
        {
            case CarryType that when
                that.equals(CarryType.BLOCK) && carryData.unionData() instanceof CarryData.CarryBlockDataHolder holder ->
                holder.getState();
            case CarryType that when
                that.equals(CarryType.BLOCK_ENTITY) && carryData.unionData() instanceof CarryData.CarryBlockEntityDataHolder holder ->
                holder.getState();
            default -> throw new IllegalArgumentException("Assertion failed: CarryType and CarryData's type doesn't match!");
        };
        
        final InteractionResult placeResult = task.placeContext().apply(stateToPlace).performPlace(false);
        
        if(placeResult.equals(InteractionResult.FAIL))
            return this.listenerProcess(task.insert()).flatMap(CarryOperationExecutor.INST::componentProcess).map(CarryPipelineTask::fail);
        
        if(task.type().equals(CarryType.BLOCK_ENTITY))
            return blockEntityReleaseExtra(task);
        
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> blockEntityReleaseExtra(@NotNull CarryPipelineTask task)
    {
        final @Nullable var blockEntityType = task.blockEntityType();
        
        if(blockEntityType == null)
            return BrokenCarryPipelineException.target(
                task.pass(),
                "The BlockEntityType of this blockEntity is not present!",
                IllegalArgumentException::new,
                CarryType.BLOCK_ENTITY,
                TriState.FALSE
            );
        
        assert task.data() != null : "UwU";
        final CarryData.CarryBlockEntityDataHolder holder = task.data().unionData();
        final var stateToPlace = holder.getState();
        final var pos = task.interactPos();
        final var level = task.level();
        final @Nullable var blockEntity = blockEntityType.create(pos, stateToPlace);
        
        if(blockEntity == null)
            return BrokenCarryPipelineException.target(
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
            );
        
        blockEntity.loadCustomOnly(holder.getTagData(), level.registryAccess());
        level.blockEntityChanged(pos);
        
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> entityTargetCapture(@NotNull CarryPipelineTask task)
    {
        final @Nullable LivingEntity entity = task.entity();
        
        if(entity == null)
            return BrokenCarryPipelineException.target(
                task.pass(),
                "Illegal operation: The entity to capture does not exist!",
                IllegalArgumentException::new,
                CarryType.ENTITY,
                TriState.TRUE
            );
        
        entity.remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        return IResult.of(task.success());
    }
    
    private @NotNull IResult<CarryPipelineTask, BrokenCarryPipelineException> entityTargetRelease(@NotNull CarryPipelineTask task)
    {
        final @Nullable CarryData carryData = task.data();
        final @Nullable var contextGetter = task.placeContext();
        
        if(carryData == null)
            return BrokenCarryPipelineException.target(
                task.pass(),
                "Carry Crate's content release failed because the CarryData's value is invalid, which is required by insert.",
                IllegalArgumentException::new,
                CarryType.ENTITY,
                TriState.FALSE
            );
        
        if(contextGetter == null)
            return BrokenCarryPipelineException.target(
                task.fail(),
                "Carry Crate's content release failed because this interaction is triggered with an unexpected mean!",
                IllegalStateException::new,
                CarryType.ENTITY,
                TriState.FALSE
            );
        
        final var level = task.level();
        final CarryData.CarryEntityDataHolder holder = carryData.unionData();
        final var entityToSpawn = EntityType.create(holder.getTagData(), level);
        
        if(entityToSpawn.isEmpty())
            return BrokenCarryPipelineException.target(
                task.fail(),
                "The entity that CarryData holds doesn't exist!",
                IllegalArgumentException::new,
                CarryType.ENTITY,
                TriState.FALSE
            );
        
        //! Here, we use [[UseOnContext]] to get a usable position,
        //! because [[CarryPipelineTask#interactPos]] doesn't contain [[Direction]], and that is nullable, which doesn't worth be an independent field.
        //! Notes that this method can only be called by [[CarryCrateItem#useOn]], it does has [[UseOnContext]].
        //! And since we doesn't need to place any blocks, we create its children class [[StatedBlockPlaceContext]] with `null`.
        final @Nullable var spawnPos = getSafePosition(level, contextGetter.apply(null), entityToSpawn.get());
        
        if(spawnPos == null)
            return this.listenerProcess(task.insert()).flatMap(CarryOperationExecutor.INST::componentProcess).map(CarryPipelineTask::fail);
        
        final var entity = entityToSpawn.get();
        entity.moveTo(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        level.addFreshEntity(entity);
        
        return IResult.of(task.success());
    }
    //endregion
    
    //region Private Helpers
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
