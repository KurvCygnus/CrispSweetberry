//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.integration;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.VisualTrend;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum KilnBlockInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
    INSTANCE;
    
    @Override
    public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig)
    {
        
    }
    
    @Override
    public void appendServerData(@NotNull CompoundTag compoundTag, @NotNull BlockAccessor blockAccessor)
    {
        final KilnBlockEntity kilnBlockEntity = (KilnBlockEntity) blockAccessor.getBlockEntity();
        
        final NonNullList<ItemStack> content = kilnBlockEntity.getContainerItems();
        final double visualProgress = kilnBlockEntity.model.getVisualProgress();
        final VisualTrend visualArrow = kilnBlockEntity.model.getTrend();
        
        compoundTag.putDouble("visualProgress", visualProgress);
        compoundTag.putByte("visualArrow", (byte) visualArrow.ordinal());
        
        for(final ItemStack stack: content)
        {
            
        }
    }
    
    @Override
    public @NotNull ResourceLocation getUid()
    {
        return null;
    }
}
