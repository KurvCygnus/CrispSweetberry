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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistryView;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableBlockEntityExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryRegistryManager;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * The handler of <u>{@link BlockEntity}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see AbstractCarryInteractHandler
 */
public final class CarryBlockEntityInteractHandler extends AbstractCarryInteractHandler
{
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "BLOCK_ENTITY_HANDLER");
    
    public CarryBlockEntityInteractHandler(
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull ItemStack carryCrate,
        @NotNull BlockPos targetPos,
        @NotNull BlockState targetState,
        @Nullable LivingEntity targetEntity,
        @NotNull BlockEntity targetBlockEntity,
        @Nullable CarryID carryID
    ) { super(level, player, carryCrate, targetPos, targetState, targetEntity, targetBlockEntity, carryID); }
    
    @Override protected @NotNull HandleResult boxIn()
    {
        final CarryID carryID = generateCarryID();
        final BlockState targetState = getTargetState();
        final BlockPos targetPos = getTargetPos();
        final BlockEntity blockEntity = getTargetBlockEntity();
        
        final Optional<AbstractBlockEntityCarryAdapter<? extends BlockEntity>> optionalAdapter = CarryRegistryManager.INST.
            getBlockEntityAdapter(blockEntity.getType()).map(
                adapterFactory ->
                    createAdapter(
                        adapterFactory,
                        blockEntity
                    )
            );
        
        if(optionalAdapter.isEmpty())
        {
            LOGGER.error("Cannot find blockEntity \"{}\"'s adapter! Mark this interaction as FAILED.", blockEntity.toString());
            return HandleResult.FAILED;
        }
        
        final var adapter = optionalAdapter.get();
        
        final CompoundTag tagData = new CompoundTag();
        adapter.onCarriedSequence(new CarriableBlockEntityExtensions.IAtomicCarriable.CarriedContext(this.level, targetPos, this.player, carryID.uuid()));
        //* [[IAtomicCarriable#onCarriedSequence()]] may have side effects on BE's data, we should save data after it.
        adapter.saveCarryTag(tagData, level.registryAccess());
        
        final CarryData insertData = CarryData.createBlockEntity(
            targetState,
            tagData,
            blockEntity.getType(),
            adapter.getPenaltyRate(),
            adapter.causesOverweight(),
            this.level.getGameTime()
        );
        
        return HandleResult.boxIn(insertData, carryID);
    }
    
    @Override protected @NotNull HandleResult unbox()
    {
        if(!hasData)
            return handleException();
        
        final BlockState targetState = getTargetState();
        final BlockPos targetPos = getTargetPos();
        final CarryData data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        final @Nullable CarryID carryID = carryCrate.get(CarryCrateRegistries.CARRY_ID.get());
        final BlockEntity blockEntity = getTargetBlockEntity();
        
        assert data != null : MISUSE_FAIL_MSG;
        assert carryID != null : "BlockEntity \"%s\"'s adapter has no uuid. This is a persistent issue. %s".
            formatted(blockEntity.toString(), MetainfoConstants.FEEDBACK_MESSAGE);
        
        final CarryData.CarryBlockEntityDataHolder blockEntityDataHolder = data.unionData();
        final var blockEntityType = blockEntityDataHolder.getType();
        
        //* NOTE: This logic is illegal on Vanilla. It is implemented with the help of [[CarryEngine#IS_INTERACTING_WITH_BE]] and [[CarryBlockEntityValidationPasser]].
        final Optional<AbstractBlockEntityCarryAdapter<? extends BlockEntity>> optionalAdapter = CarryRegistryManager.INST.
            getBlockEntityAdapter(blockEntityType).map(
                adapterFactory ->
                    createAdapter(
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
        {
            LOGGER.error("Cannot find blockEntity \"{}\"'s adapter! Mark this interaction as failed.", blockEntity.toString());
            return HandleResult.FAILED;
        }
        
        final AbstractBlockEntityCarryAdapter<? extends BlockEntity> adapter = optionalAdapter.get();
        
        final CompoundTag tagData = new CompoundTag();
        adapter.loadCarryTag(tagData, this.level.registryAccess());//* #onPlacedProcess() may have side effects on BE's data, we should load data before it.
        adapter.onPlacedProcess(
            this.level,
            this.level.getGameTime() - data.startTime(),
            new CarriableBlockEntityExtensions.IAtomicCarriable.CarriedContext(
                this.level,
                targetPos,
                this.player,
                carryID.uuid()
            )
        );
        
        return HandleResult.unbox(
            CarryData.createBlockEntity(
                targetState,
                tagData,
                blockEntityDataHolder.getType(),
                adapter.getPenaltyRate(),
                data.causesOverweight(),
                level.getGameTime()
            ),
            this.carryID,
            blockEntityDataHolder.getType(),
            false
        );
    }
    
    @Override protected @NotNull ResourceLocation getCarryResourceLocation()
    {
        return Objects.requireNonNull(
            BlockEntityType.getKey(getTargetBlockEntity().getType()),
            "Assertion failed: Param \"blockEntity\"'s ResourceLocation is null. This only means the internal logic is flawed, or get misused. %s".
                formatted(MetainfoConstants.FEEDBACK_MESSAGE)
        );
    }
    
    @SuppressWarnings("unchecked")//! Safe casting OwO
    private static <E extends BlockEntity> @NotNull AbstractBlockEntityCarryAdapter<? extends E> createAdapter(
        @NotNull ICarryRegistryView.ICarryBlockEntityAdapterFactory<E, ?> factory,
        @NotNull BlockEntity entity
    ) { return factory.create((E) entity); }
    
    @Override protected @NotNull MarkLogger getLogger() { return LOGGER; }
}