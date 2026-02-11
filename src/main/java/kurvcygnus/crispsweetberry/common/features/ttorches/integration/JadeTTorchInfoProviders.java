//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.integration;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.LIGHT_PROPERTY;

public final class JadeTTorchInfoProviders
{
    @AutoI18n({
        "en_us -> State: %s",
        "lol_us -> Shyni: %s",
        "zh_cn -> 状态: %s"
    })
    private static final Component STATE_MSG = Component.translatable("crispsweetberry.integration.jade.ttorch.state_name");
    private static final ResourceLocation STATE_UID = CrispDefUtils.getModNamespacedLocation("integration.jade.ttorch.state_name_id");
    
    public record BaseTemporaryTorchBlockInfoProvider() implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
    {
        @Override
        public void appendTooltip(@NotNull ITooltip iTooltip, @NotNull BlockAccessor blockAccessor, @NotNull IPluginConfig iPluginConfig) 
        {
            if(blockAccessor.getServerData().contains("State"))
            {
                final String stateName = blockAccessor.getServerData().getString("State");
                iTooltip.add(Component.translatable(STATE_MSG.getContents().toString(), stateName));
            }
        }
        
        @Override
        public void appendServerData(@NotNull CompoundTag compoundTag, @NotNull BlockAccessor blockAccessor)
        {
            final BlockState state = blockAccessor.getBlockState();
            
            if(state.hasProperty(LIGHT_PROPERTY))
            {
                final TTorchConstants.LightState lightState = state.getValue(LIGHT_PROPERTY);
                
                compoundTag.putString("State", lightState.getSerializedName());
            }
        }
        
        @Override
        public @NotNull ResourceLocation getUid() { return STATE_UID; }
    }
}
