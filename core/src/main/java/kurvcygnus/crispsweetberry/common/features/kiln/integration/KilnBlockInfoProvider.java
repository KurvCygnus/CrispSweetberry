//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.integration;

import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnEnumCollections;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.KilnProgressModel;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

public enum KilnBlockInfoProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor>
{
    INST;
    
    private static final ResourceLocation UNIQUE_JADE_KILN_ID = CrispDefUtils.getModNamespacedLocation("integration.jade.kiln");
    private static final String VISUAL_PROGRESS = "visualProgress";
    private static final String VISUAL_ARROW = "visualArrow";
    private static final String CONTENT = "content";
    
    @Override
    public void appendTooltip(ITooltip tooltip, @NotNull BlockAccessor accessor, IPluginConfig config)
    {
        final CompoundTag syncTag = accessor.getServerData();
        
        if(!syncTag.contains(VISUAL_PROGRESS) || !syncTag.contains(VISUAL_ARROW) || !syncTag.contains(CONTENT))
            return;
        
        final IElementHelper elementHelper = IElementHelper.get();
        
        final double visualProgress = syncTag.getDouble(VISUAL_PROGRESS);
        final KilnEnumCollections.VisualTrend visualArrow = KilnEnumCollections.VisualTrend.values()[syncTag.getByte(VISUAL_ARROW)];
        final ListTag content = syncTag.getList("content", Tag.TAG_COMPOUND);
        
        for(int index = 0; index < content.size(); index++)
        {
            final CompoundTag tag = content.getCompound(index);
            final ItemStack currentStack = ItemStack.parseOptional(accessor.getLevel().registryAccess(), tag);
            final IElement itemIcon = elementHelper.item(currentStack, 0.5F);
            itemIcon.message(null);
            
            tooltip.add(itemIcon);
        }
    }
    
    @Override public void appendServerData(@NotNull CompoundTag compoundTag, @NotNull BlockAccessor blockAccessor)
    {
        final KilnBlockEntity kilnBlockEntity = (KilnBlockEntity) blockAccessor.getBlockEntity();
        
        final NonNullList<ItemStack> content = kilnBlockEntity.getContainerItems();
        final KilnProgressModel model = kilnBlockEntity.getModel();
        final double visualProgress = model.getVisualProgress();
        final KilnEnumCollections.VisualTrend visualArrow = model.getTrend();
        
        compoundTag.putDouble(VISUAL_PROGRESS, visualProgress);
        compoundTag.putByte(VISUAL_ARROW, (byte) visualArrow.ordinal());
        
        final ListTag listTag = new ListTag();
        
        for(int index = 0, contentSize = content.size(); index < contentSize; index++)
        {
            final ItemStack stack = content.get(index);
            
            if(stack.isEmpty())
                continue;
            
            final CompoundTag tag = new CompoundTag();
            
            tag.putByte("slot_index", (byte) index);
            stack.save(blockAccessor.getLevel().registryAccess(), tag);
            listTag.add(index, tag);
        }
        
        compoundTag.put(CONTENT, listTag);
    }
    
    @Override
    public @NotNull ResourceLocation getUid() { return UNIQUE_JADE_KILN_ID; }
}
