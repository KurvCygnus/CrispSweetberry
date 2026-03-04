//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core.components;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.IAtomicCarriable;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.registry.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.registry.CarryRegistryManager;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class CarryBlockEntityInteractHandler extends AbstractCarryInteractHandler
{
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "BLOCK_ENTITY_HANDLER");
    
    private final BlockEntity blockEntity;
    
    public CarryBlockEntityInteractHandler(
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull ItemStack carryCrate,
        @NotNull BlockPos targetPos,
        @NotNull BlockState targetState,
        @Nullable LivingEntity targetEntity,
        @Nullable String optionalUUID
    )
    {
        super(level, player, carryCrate, targetPos, targetState, targetEntity, optionalUUID);
        this.blockEntity = level.getBlockEntity(targetPos);
    }
    
    @Override protected @NotNull HandleResult boxIn()
    {
        final String carryID = generateCarryID();
        final BlockState targetState = getTargetState();
        final BlockPos targetPos = getTargetPos();
        LOGGER.debug("Generated a new CarryID \"{}\" for indexing.", carryID);
        
        final AbstractBlockEntityCarryAdapter<?> adapter = createAdapter(blockEntity.getType(), targetPos, targetState);
        final CompoundTag tagData = new CompoundTag();
        adapter.onCarriedSequence(new IAtomicCarriable.CarriedContext(this.level, targetPos, this.player));
        adapter.saveCarryTag(tagData, level.registryAccess());//* Carry sequence may have side effects on BE's data, we should save data after it.
        
        final CarryData insertData = CarryData.createBlockEntity(
            targetState,
            tagData,
            this.blockEntity.getType(),
            adapter.getPenaltyRate(),
            this.level.getGameTime()
        );
        
        return HandleResult.boxIn(insertData, InteractionResult.SUCCESS, carryID, false);
    }
    
    @Override protected @NotNull HandleResult unbox()
    {
        if(!hasData)
            return handleEx();
        
        final BlockState targetState = getTargetState();
        final BlockPos targetPos = getTargetPos();
        final CarryData data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        Objects.requireNonNull(data, MISUSE_FAIL_MSG);
        
        final CarryData.CarryBlockEntityDataHolder blockEntityDataHolder = data.data();
        final AbstractBlockEntityCarryAdapter<?> adapter = createAdapter(blockEntityDataHolder.getType(), targetPos, targetState);
        final CompoundTag tagData = new CompoundTag();
        adapter.loadCarryTag(tagData, this.level.registryAccess());
        adapter.onPlacedProcess(
            this.level,
            this.level.getGameTime() - data.startTime(),
            new IAtomicCarriable.CarriedContext(
                this.level,
                targetPos,
                this.player
            )
        );
        
        return HandleResult.unbox(
            CarryData.createBlockEntity(
                targetState,
                tagData,
                blockEntityDataHolder.getType(),
                adapter.getPenaltyRate(),
                level.getGameTime()
            ),
            optionalCarryID
        );
    }
    
    @Override protected @NotNull ResourceLocation getCarryResourceLocation()
    {
        return Objects.requireNonNull(
            BlockEntityType.getKey(this.blockEntity.getType()),
            "Assertion failed: Param \"blockEntity\"'s ResourceLocation is null. This only means the internal logic is flawed, or get misused. %s".
                formatted(MiscConstants.FEEDBACK_MESSAGE)
        );
    }
    
    private static @NotNull AbstractBlockEntityCarryAdapter<? extends BlockEntity> createAdapter(
        @NotNull BlockEntityType<? extends BlockEntity> blockEntityType,
        @NotNull BlockPos pos,
        @NotNull BlockState state
    )
    {
        return createAdapter(
            CarryRegistryManager.INSTANCE.getBlockEntityRegistry().get(blockEntityType),
            //! This method returns "null" at extreme edge cases.
            Objects.requireNonNull(
                blockEntityType.create(pos, state),
                """
                    Fatal:
                    Failed to create blockEntity "%s"'s adapter. This usually means the blockEntity's type registration itself has dataflow issue, or this
                    method is called at improper time.
                    
                    %s
                    """.
                    formatted(
                        blockEntityType.toString(),
                        MiscConstants.FEEDBACK_MESSAGE
                    )
            )
        );
    }
    
    @SuppressWarnings("unchecked")//! Safe casting OwO
    private static <E extends BlockEntity> @NotNull AbstractBlockEntityCarryAdapter<? extends E> createAdapter(
        @NotNull ICarryRegistry.ICarryBlockEntityAdapterFactory<E, ?> factory,
        @NotNull BlockEntity entity
    ) { return factory.create((E) entity); }
    
    @Override protected @NotNull MarkLogger getLogger() { return LOGGER; }
}
