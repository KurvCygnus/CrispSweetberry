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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableBlockEntityExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryRegistryManager;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
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
import java.util.Optional;

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
        @Nullable CarryID optionalUUID
    )
    {
        super(level, player, carryCrate, targetPos, targetState, targetEntity, optionalUUID);
        this.blockEntity = level.getBlockEntity(targetPos);
    }
    
    @Override protected @NotNull HandleResult boxIn()
    {
        final CarryID carryID = generateCarryID();
        final BlockState targetState = getTargetState();
        final BlockPos targetPos = getTargetPos();
        LOGGER.debug("Generated a new CarryID \"{}\" for indexing.", carryID);
        
        @SuppressWarnings("DuplicatedCode")//! A little boilerplate code is OK.
        final var optionalAdapter = createAdapter(blockEntity.getType(), targetPos, targetState);
        
        if(optionalAdapter.isEmpty())
        {
            LOGGER.error("Cannot find blockEntity \"{}\"'s adapter! Mark this interaction as failed.", blockEntity.toString());
            return HandleResult.failed();
        }
        
        final AbstractBlockEntityCarryAdapter<?> adapter = optionalAdapter.get();
        
        final CompoundTag tagData = new CompoundTag();
        adapter.onCarriedSequence(new CarriableBlockEntityExtensions.IAtomicCarriable.CarriedContext(this.level, targetPos, this.player, carryID.uuid()));
        adapter.saveCarryTag(tagData, level.registryAccess());//* #onCarriedSequence() may have side effects on BE's data, we should save data after it.
        
        final CarryData insertData = CarryData.createBlockEntity(
            targetState,
            tagData,
            this.blockEntity.getType(),
            adapter.getPenaltyRate(),
            adapter.causesOverweight(),
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
        final @Nullable CarryID carryID = carryCrate.get(CarryCrateRegistries.CARRY_ID.get());
        Objects.requireNonNull(data, MISUSE_FAIL_MSG);
        LOGGER.when(carryID == null).
            warn(
                "BlockEntity \"{}\"'s adapter has no uuid. This is a persistent issue. {}",
                blockEntity.toString(),
                MiscConstants.FEEDBACK_MESSAGE
            );
        
        final CarryData.CarryBlockEntityDataHolder blockEntityDataHolder = data.data();
        final var optionalAdapter = createAdapter(blockEntityDataHolder.getType(), targetPos, targetState);
        
        if(optionalAdapter.isEmpty())
        {
            LOGGER.error("Cannot find blockEntity \"{}\"'s adapter! Mark this interaction as failed.", blockEntity.toString());
            return HandleResult.failed();
        }
        
        final AbstractBlockEntityCarryAdapter<? extends BlockEntity> adapter = optionalAdapter.get();
        
        final CompoundTag tagData = new CompoundTag();
        adapter.onPlacedProcess(
            this.level,
            this.level.getGameTime() - data.startTime(),
            new CarriableBlockEntityExtensions.IAtomicCarriable.CarriedContext(
                this.level,
                targetPos,
                this.player,
                carryID == null ? "" : carryID.uuid()
            )
        );
        adapter.loadCarryTag(tagData, this.level.registryAccess());//* #onPlacedProcess() may have side effects on BE's data, we should load data after it.
        
        return HandleResult.unbox(
            CarryData.createBlockEntity(
                targetState,
                tagData,
                blockEntityDataHolder.getType(),
                adapter.getPenaltyRate(),
                data.causesOverweight(),
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
    
    private static @NotNull Optional<AbstractBlockEntityCarryAdapter<? extends BlockEntity>> createAdapter(
        @NotNull BlockEntityType<? extends BlockEntity> blockEntityType,
        @NotNull BlockPos pos,
        @NotNull BlockState state
    )
    {
        final var factory = CarryRegistryManager.INSTANCE.getBlockEntityAdapter(blockEntityType);
        
        return factory.map(
            adapterFactory ->
                createAdapter(
                    adapterFactory,
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
