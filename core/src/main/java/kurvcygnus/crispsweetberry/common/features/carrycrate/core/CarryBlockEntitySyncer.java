//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core;

import com.google.errorprone.annotations.DoNotCall;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.lib.base.extensions.IAutoNestedPrintable;
import kurvcygnus.crispsweetberry.lib.base.util.AssertUtils;
import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import kurvcygnus.crispsweetberry.lib.core.log.IMarkLogger;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A specialized synchronization engine designed to deferred-load BlockEntity NBT data.<br>
 * @implNote
 * During unboxing operations inside <b>{@code CarryOperationExecutor#blocklikeTargetRelease}</b>, placing a block
 * with a <u>{@link BlockEntity}</u> directly onto the world triggers structural execution chains within Minecraft
 * (e.g. <u>{@link net.neoforged.neoforge.common.CommonHooks#onPlaceItemIntoWorld CommonHooks#onPlaceItemIntoWorld}</u>
 * and <u>{@link net.minecraft.world.level.Level#capturedBlockSnapshots Level#capturedBlockSnapshots}</u>).
 * If a BlockEntity's NBT payload is deserialized prematurely during these capture or placement phases, Minecraft's underlying chunk
 * architecture (specifically <b>{@code ChunkAccess#blockEntities}</b> inside <u>{@link net.minecraft.world.level.chunk.LevelChunk LevelChunk}</u>)
 * <span style="color: f84b4b">routinely invalidates,
 * erases, or rejects the synchronized data due to threading constraints and strict networking lifecycle rules.</span>
 * <hr>
 * <b>To circumvent these architectural limitations without breaking vanilla conventions, this class implements a
 * <i>Deferred Execution Strategy</i> via a scheduled ticking task</b>. Instead of instantly binding NBT tags to
 * the target BlockEntity during the initial placement phase, {@link CarryOperationExecutor} delegates data emulation
 * and I/O to this syncer by calling {@link #pushTask(BlockPos, CarryData)}.
 * <hr>
 * The pushed {@link SyncTask} queues the objective data. Upon the next server tick pre-phase
 * ({@link net.neoforged.neoforge.event.tick.ServerTickEvent.Pre Pre-Tick}), when the block state lifecycle has
 * thoroughly stabilized and network packet cycles are clear, {@link #completeTask(ServerTickEvent.Pre)} safely
 * invokes {@link BlockEntity#loadCustomOnly(CompoundTag, HolderLookup.Provider)} to inject the original data state,
 * achieving a perfectly synchronized client/server environment.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
@ApiStatus.Internal enum CarryBlockEntitySyncer
{
    INST;
    
    private static final IMarkLogger LOGGER = IMarkLogger.marklessLogger();
    
    private final List<SyncTask> tasks = new ArrayList<>();
    
    /**
     * @implNote See <u>{@link net.neoforged.neoforge.event.tick.ServerTickEvent.Pre ServerTickEvent.Pre}</u>'s comment.
     * Its attribute decides that the task this tick pushes will only get executed at next tick.
     */
    @SubscribeEvent @DoNotCall static void completeTask(@NotNull ServerTickEvent.Pre event)
    {
        final var tasks = CarryBlockEntitySyncer.INST.tasks;
        
        if(tasks.isEmpty())
            return;
        
        final var level = event.getServer().overworld();
        for(final var task: tasks)
        {
            final var pos = task.pos();
            final var blockState = level.getBlockState(pos);
            
            if(!(blockState.getBlock() instanceof EntityBlock))
            {
                AssertUtils.throwOnDevOrLogError(
                    IllegalStateException::new,
                    LOGGER,
                    "Assertion failed: the data sync task at position {}'s block \"{}\" is not even a EntityBlock.",
                    DefinitionUtils.formatPos(pos),
                    blockState.getBlock().getDescriptionId()
                );
                continue;
            }
            
            final @Nullable var blockEntity = level.getBlockEntity(pos);
            final boolean msgFlag = blockEntity == null;
            
            if(msgFlag || blockState != blockEntity.getBlockState())
            {
                AssertUtils.throwOnDevOrLogError(
                    IllegalStateException::new,
                    LOGGER,
                    "Assertion failed: {}",
                    msgFlag ?
                        TextUtils.format(
                            "Position {} doesn't exist any blockEntity.",
                            DefinitionUtils.formatPos(pos)
                        ) :
                        TextUtils.format("Inconsistent corresponding block for blockState \"{}\" and blockEntity \"{}\".", blockState, blockEntity)
                );
                continue;
            }
            
            blockEntity.loadCustomOnly(task.tag(), level.registryAccess());
        }
        
        CarryBlockEntitySyncer.INST.tasks.clear();
    }
    
    void pushTask(@NotNull BlockPos pos, @NotNull CarryData data)
    {
        if(!data.carryType.equals(CarryType.BLOCK_ENTITY))
            throw new IllegalArgumentException("You just write the CarryOperationExecutor's implementation in a wrong way, stupid! FIX IT RN!!!");
        
        final var task = new SyncTask(pos, data.<CarryData.OfBlockEntityUniqueData>matchUnique().tagData);
        this.tasks.add(task);
    }
}

@ApiStatus.Internal record SyncTask(@NotNull BlockPos pos, @NotNull CompoundTag tag) implements IAutoNestedPrintable.OfRecordHandle<SyncTask>
    { @Override public @NotNull String toString() { return toNestedString(); } }
