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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.AbstractBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryRegistryManager;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.utils.base.extension.StatedBlockPlaceContext;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * The handler of <u>{@link Block}</u>.
 * @author Kurv Cygnus
 * @see AbstractCarryInteractHandler
 * @since 1.0 Release
 */
public final class CarryBlockInteractHandler extends AbstractCarryInteractHandler
{
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "BLOCK_HANDLER");
    
    public CarryBlockInteractHandler(
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull ItemStack carryCrate,
        @NotNull BlockPos targetPos,
        @NotNull BlockState targetState,
        @Nullable LivingEntity targetEntity,
        @Nullable BlockEntity targetBlockEntity,
        @NotNull Function<BlockState, StatedBlockPlaceContext> contextGenerator,
        @Nullable CarryID optionalCarryID
    ) { super(level, player, carryCrate, targetPos, targetState, targetEntity, targetBlockEntity, contextGenerator, optionalCarryID); }
    
    @Override public @NotNull HandleResult boxIn()
    {
        final CarryID carryID = generateCarryID();
        final BlockState targetState = getTargetState();

        final var optionalAdapter = createAdapter(targetState.getBlock());
        
        if(optionalAdapter.isEmpty())
        {
            LOGGER.error("Cannot find block \"{}\"'s adapter!", targetState.getBlock().getDescriptionId());
            return HandleResult.FAILED;
        }
        
        final AbstractBlockCarryAdapter<?> adapter = optionalAdapter.get();
        final int carryCount = targetState.hasProperty(BlockStateProperties.LAYERS) ? targetState.getValue(BlockStateProperties.LAYERS) : 1;
        
        final CarryData insertData = CarryData.createBlock(
            targetState,
            adapter.getPenaltyRate(),
            carryCount,
            adapter.getAcceptableCount(),
            adapter.causesOverweight(),
            level.getGameTime()
        );
        
        return HandleResult.boxIn(insertData, carryID);
    }
    
    @Override public @NotNull HandleResult unbox()
    {
        if(!hasData)
            return handleException();
        
        final BlockState targetState = getTargetState();
        final CarryData data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        Objects.requireNonNull(data, MISUSE_FAIL_MSG);
        
        final CarryData.CarryBlockDataHolder blockDataHolder = data.unionData();
        
        if(targetState.is(blockDataHolder.getState().getBlock()) && blockDataHolder.getCarryCount() < blockDataHolder.getMaxCarryCount())
        {
            final CarryData insertData = CarryData.createBlock(
                targetState,
                blockDataHolder.getPenaltyRate(),
                blockDataHolder.getCarryCount() + 1,
                blockDataHolder.getMaxCarryCount(),
                data.causesOverweight(),
                level.getGameTime()
            );
            
            return HandleResult.boxIn(insertData, null, true);
        }
        
        final CarryData.CarryBlockDataHolder dataHolder = data.unionData();
        
        if(dataHolder.getCarryCount() > 1)
            return HandleResult.unbox(
                CarryData.createBlock(
                    dataHolder.getState(),
                    dataHolder.getPenaltyRate(),
                    dataHolder.getCarryCount() - 1,
                    dataHolder.getMaxCarryCount(),
                    data.causesOverweight(),
                    data.startTime()
                ),
                optionalCarryID,
                true
            );
        
        return HandleResult.unbox(data, optionalCarryID);
    }
    
    @Override protected @NotNull ResourceLocation getCarryResourceLocation() { return BuiltInRegistries.BLOCK.getKey(this.getTargetState().getBlock()); }
    
    private static @NotNull Optional<AbstractBlockCarryAdapter<? extends Block>> createAdapter(@NotNull Block block)
        { return CarryRegistryManager.INST.getBlockAdapter(block).map(factory -> createAdapter(factory, block)); }
    
    @SuppressWarnings("unchecked")//! Safe casting awa
    private static <B extends Block> @NotNull AbstractBlockCarryAdapter<? extends B> createAdapter(
        @NotNull ICarryRegistry.ICarryBlockAdapterFactory<B, ?> factory,
        @NotNull Block block
    ) { return factory.create((B) block); }
    
    @Override protected @NotNull MarkLogger getLogger() { return LOGGER; }
}
