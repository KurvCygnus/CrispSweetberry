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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableBlockEntityExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryRegistryManager;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryBlockPlaceContext;
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
import java.util.function.Function;

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
        @NotNull Function<BlockState, CarryBlockPlaceContext> contextGenerator,
        @Nullable CarryID optionalUUID
    ) { super(level, player, carryCrate, targetPos, targetState, targetEntity, targetBlockEntity, contextGenerator, optionalUUID); }
    
    @Override protected @NotNull HandleResult boxIn()
    {
        final CarryID carryID = generateCarryID();
        final BlockState targetState = getTargetState();
        final BlockPos targetPos = getTargetPos();
        final BlockEntity blockEntity = getTargetBlockEntity();
        
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
        adapter.saveCarryTag(tagData, level.registryAccess());//* #onCarriedSequence() may have side effects on BE's unionData, we should save unionData after it.
        
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
            return handleEx();
        
        final BlockState targetState = getTargetState();
        final BlockPos targetPos = getTargetPos();
        final CarryData data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        final @Nullable CarryID carryID = carryCrate.get(CarryCrateRegistries.CARRY_ID.get());
        final BlockEntity blockEntity = getTargetBlockEntity();
        Objects.requireNonNull(data, MISUSE_FAIL_MSG);
        LOGGER.when(carryID == null).
            warn(
                "BlockEntity \"{}\"'s adapter has no uuid. This is a persistent issue. {}",
                blockEntity.toString(),
                MiscConstants.FEEDBACK_MESSAGE
            );
        
        final CarryData.CarryBlockEntityDataHolder blockEntityDataHolder = data.unionData();
        
        final CarryBlockPlaceContext context = getContextGenerator().apply(blockEntityDataHolder.getState());
        
        //! AbstractBlockEntityAdapter's init implicitly creates blockEntity.
        //! However, Minecraft only allows the creation of a new blockEntity when the targetBlock is corresponded.
        //! We have to treat such a case specially.
        //! THANK YOU, MOJANG
        if(!context.performPlace().equals(InteractionResult.CONSUME))
        {
            LOGGER.debug("Cannot place block to process blockEntity, return the process result as failed.");
            context.cancelPlacement();
            return HandleResult.failed();
        }
        
        final var optionalAdapter = createAdapter(blockEntityDataHolder.getType(), targetPos, targetState);
        
        if(optionalAdapter.isEmpty())
        {
            LOGGER.error("Cannot find blockEntity \"{}\"'s adapter! Mark this interaction as failed.", blockEntity.toString());
            return HandleResult.failed();
        }
        
        final AbstractBlockEntityCarryAdapter<? extends BlockEntity> adapter = optionalAdapter.get();
        
        final CompoundTag tagData = new CompoundTag();
        adapter.loadCarryTag(tagData, this.level.registryAccess());//* #onPlacedProcess() may have side effects on BE's unionData, we should load unionData before it.
        adapter.onPlacedProcess(
            this.level,
            this.level.getGameTime() - data.startTime(),
            new CarriableBlockEntityExtensions.IAtomicCarriable.CarriedContext(
                this.level,
                targetPos,
                this.player,
                carryID == null ?
                    "" :
                    carryID.uuid()
            )
        );
        
        if(!context.cancelPlacement())
        {
            LOGGER.debug("Cannot cancel the emulation of the blockEntity's unbox! return the process result as failed.");
            return HandleResult.failed();
        }
        
        return HandleResult.unbox(
            CarryData.createBlockEntity(
                targetState,
                tagData,
                blockEntityDataHolder.getType(),
                adapter.getPenaltyRate(),
                data.causesOverweight(),
                level.getGameTime()
            ),
            optionalCarryID,
            blockEntityDataHolder.getType(),
            false
        );
    }
    
    @Override protected @NotNull ResourceLocation getCarryResourceLocation()
    {
        return Objects.requireNonNull(
            BlockEntityType.getKey(getTargetBlockEntity().getType()),
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
        final var factory = CarryRegistryManager.INST.getBlockEntityAdapter(blockEntityType);
        
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
