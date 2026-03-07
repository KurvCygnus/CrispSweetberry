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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryRegistryManager;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryData;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

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
        @Nullable String optionalCarryID
    )
    { super(level, player, carryCrate, targetPos, targetState, targetEntity, optionalCarryID); }
    
    @Override public @NotNull HandleResult boxIn()
    {
        final String carryID = generateCarryID();
        final BlockState targetState = getTargetState();
        LOGGER.debug("Generated a new CarryID \"{}\" for indexing.", carryID);

        final var optionalAdapter = createAdapter(targetState.getBlock());
        
        if(optionalAdapter.isEmpty())
        {
            LOGGER.error("Cannot find block \"{}\"'s adapter!", targetState.getBlock().getDescriptionId());
            return HandleResult.failed();
        }
        
        final AbstractBlockCarryAdapter<?> adapter = optionalAdapter.get();
        
        final CarryData insertData = CarryData.createBlock(
            targetState,
            adapter.getPenaltyRate(),
            1,//? TODO: Make here more flexible, perhaps. Layered blocks are obviously not following this.
            adapter.getAcceptableCount(),
            level.getGameTime()
        );
        
        return HandleResult.boxIn(insertData, InteractionResult.SUCCESS, carryID, false);
    }
    
    @Override public @NotNull HandleResult unbox()
    {
        if(!hasData)
            return handleEx();
        
        final BlockState targetState = getTargetState();
        final CarryData data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        Objects.requireNonNull(data, MISUSE_FAIL_MSG);
        
        final CarryData.CarryBlockDataHolder blockDataHolder = data.data();
        
        if(targetState.is(blockDataHolder.getState().getBlock()) && blockDataHolder.getCarryCount() < blockDataHolder.getMaxCarryCount())
        {
            final CarryData insertData = CarryData.createBlock(
                targetState,
                blockDataHolder.getPenaltyRate(),
                blockDataHolder.getCarryCount() + 1,
                blockDataHolder.getMaxCarryCount(),
                level.getGameTime()
            );
            
            return HandleResult.boxIn(insertData, InteractionResult.SUCCESS, null, true);
        }
        
        return HandleResult.unbox(data, optionalCarryID);
    }
    
    @Override protected @NotNull ResourceLocation getCarryResourceLocation() { return BuiltInRegistries.BLOCK.getKey(this.getTargetState().getBlock()); }
    
    private static @NotNull Optional<AbstractBlockCarryAdapter<? extends Block>> createAdapter(@NotNull Block block)
    {
        final var factory = CarryRegistryManager.INSTANCE.getBlockAdapter(block);
        
        return factory.map(
            f ->
            createAdapter(f, block)
        );
    }
    
    @SuppressWarnings("unchecked")//! Safe casting awa
    private static <B extends Block> AbstractBlockCarryAdapter<? extends B> createAdapter(
        @NotNull ICarryRegistry.ICarryBlockAdapterFactory<B, ?> factory,
        @NotNull Block block
    ) { return factory.create((B) block); }
    
    @Override protected @NotNull MarkLogger getLogger() { return LOGGER; }
}
