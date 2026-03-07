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
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.ICarryRegistry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryRegistryManager;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryData;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public final class CarryEntityInteractHandler extends AbstractCarryInteractHandler
{
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "ENTITY_HANDLE");
    
    public CarryEntityInteractHandler(
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull ItemStack carryCrate,
        @Nullable BlockPos targetPos,
        @Nullable BlockState targetState,
        @NotNull LivingEntity targetEntity,
        @Nullable String optionalCarryID
    )
    { super(level, player, carryCrate, targetPos, targetState, targetEntity, optionalCarryID); }
    
    @Override protected @NotNull HandleResult boxIn() 
    {
        final String uuid = generateCarryID();
        LOGGER.debug("Generated UUID \"{}\" for indexing.", uuid);
        final LivingEntity targetEntity = getTargetEntity();
        
        final CompoundTag tagData = new CompoundTag();
        
        if(!targetEntity.saveAsPassenger(tagData))
        {
            LOGGER.error("Error: Can not get entity \"{}\"'s data, interaction failed.");
            return HandleResult.failed();
        }
        
        
        final var optionalAdapter = createAdapter(targetEntity);
        
        if(optionalAdapter.isEmpty())
        {
            LOGGER.error("Cannot find entity \"{}\"'s adapter!", targetEntity.toString());
            return HandleResult.failed();
        }
        
        final CarryData insertData = CarryData.createEntity(optionalAdapter.get().getPenaltyRate(), targetEntity.getType(), tagData, level.getGameTime());
        
        return HandleResult.boxIn(insertData, InteractionResult.SUCCESS, uuid, false);
    }
    
    @Override protected @NotNull HandleResult unbox() 
    {
        if(!hasData)
            return handleEx();
        
        final CarryData data = carryCrate.get(CarryCrateRegistries.CARRY_CRATE_DATA.get());
        Objects.requireNonNull(data, MISUSE_FAIL_MSG);
        
        return HandleResult.unbox(data, optionalCarryID);
    }
    
    @Override protected @NotNull ResourceLocation getCarryResourceLocation() { return EntityType.getKey(getTargetEntity().getType()); }
    
    private static @NotNull Optional<AbstractEntityCarryAdapter<?>> createAdapter(@NotNull LivingEntity entity)
    {
        final var factory = CarryRegistryManager.INSTANCE.getEntityAdapter(entity.getType());
        
        return factory.map(f -> createAdapter(f, entity));
    }
    
    @SuppressWarnings("unchecked")//! Safe casting UwU
    private static <E extends LivingEntity> AbstractEntityCarryAdapter<? extends E> createAdapter(
        @NotNull ICarryRegistry.ICarryEntityAdapterFactory<E, ?> factory,
        @NotNull LivingEntity entity
    ) { return factory.create((E) entity); }
    
    @Override protected @NotNull MarkLogger getLogger() { return LOGGER; }
}
