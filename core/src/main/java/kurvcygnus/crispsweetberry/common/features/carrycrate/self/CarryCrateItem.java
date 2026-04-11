//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.self;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.client.CrispClientLiterals;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryType;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryRegistryManager;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.utils.base.extension.StackableToolBlockItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateConstants.*;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContextCollection.CarryBlocklikeInteractContext;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContextCollection.CarryEntityInteractContext;

public final class CarryCrateItem extends StackableToolBlockItem<CarryCrateItem>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public CarryCrateItem() { super(CarryCrateRegistries.CARRY_CRATE_BLOCK.value(), new Properties()); }
    
    @SuppressWarnings("DataFlowIssue")//! Null safety is granted. Null only happens on blocklike case.
    public @NotNull InteractionResult interactLivingEntity(
        @NotNull ItemStack stack,
        @NotNull Player player,
        @NotNull LivingEntity interactionTarget,
        @NotNull InteractionHand hand
    ) { return CarryEngine.interact(new CarryEntityInteractContext(stack, player, interactionTarget)); }
    
    @Override public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
    {
        final @Nullable Player player = context.getPlayer();
        
        if(player == null)
            return InteractionResult.PASS;
        
        if(!player.isShiftKeyDown())
        {
            if(context.getItemInHand().has(CarryCrateRegistries.CARRY_CRATE_DATA.get()) || context.getItemInHand().has(CarryCrateRegistries.CARRY_ID.get()))
                return InteractionResult.PASS;
            
            return super.useOn(context);
        }
        
        return Objects.requireNonNullElseGet(
            CarryEngine.interact(new CarryBlocklikeInteractContext(context)),
            () -> super.useOn(context)
        );
    }
    
    @Override public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected)
    {
        if(
            !(stack.getItem() instanceof CarryCrateItem) ||
            !stack.has(CarryCrateRegistries.CARRY_ID.get()) ||
            !stack.has(CarryCrateRegistries.CARRY_CRATE_DATA.get())
        ) return;//! Due to C/S sync, and also the competitive state between this method and [[CarryEngine]], early return at here can prevent potential NPE.
        
        CarryEngine.carryingTick(this, stack, level, entity, slotId);
    }
    
    @Override public void appendHoverText(
        @NotNull ItemStack stack,
        @NotNull TooltipContext context,
        @NotNull List<Component> tooltipComponents,
        @NotNull TooltipFlag tooltipFlag
    )
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        
        if(!tooltipFlag.hasShiftDown())
        {
            tooltipComponents.add(CrispClientLiterals.UI__SHIFT_FOR_MORE_INFO.get());
            return;
        }
        
        final int durability = Objects.requireNonNullElse(
            stack.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()),
            CARRY_CRATE_MAX_DURABILITY
        );
        
        tooltipComponents.add(DESCRIPTION_DISPATCHER.getValueOrThrow(durability));
        
        final @Nullable CarryID carryID = stack.get(CarryCrateRegistries.CARRY_ID.get());
        
        if(carryID != null)
            CarryRegistryManager.INST.
                getCombinedContentTranslation(ResourceLocation.parse(carryID.id())).ifPresent(tooltipComponents::add);
        
        final Optional<CarryData> optionalData = Optional.ofNullable(stack.get(CarryCrateRegistries.CARRY_CRATE_DATA.get()));
        
        optionalData.ifPresent(
            data ->
            {
                if(!data.carryType().equals(CarryType.BLOCK))
                    return;
                
                final CarryData.CarryBlockDataHolder blockDataHolder = data.unionData();
                
                if(blockDataHolder.getMaxCarryCount() <= 1)
                    return;
                
                tooltipComponents.add(
                    UI__CARRY_CRATE__LAYER_PREFIX.get().
                        append(String.valueOf(blockDataHolder.getCarryCount())).
                        append(UI__CARRY_CRATE__LAYER_SUFFIX.get())
                );
            }
        );
    }
    
    @Override public @NotNull DataComponentType<Integer> getDataComponent() { return CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get(); }
    
    @Override public boolean isBookEnchantable(@NotNull ItemStack stack, @NotNull ItemStack book) { return false; }
    
    @Override public boolean isEnchantable(@NotNull ItemStack stack) { return false; }
    
    @Override public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack, boolean slotChanged)
        { return slotChanged || oldStack.getItem() != newStack.getItem(); }
    
    @Override public @Range(from = 1, to = Integer.MAX_VALUE) int getMaxDurability() { return CARRY_CRATE_MAX_DURABILITY; }
    
    @Override public @Range(from = 1, to = Integer.MAX_VALUE) int getPenaltyStandard() { return PENALTY_QUANTITY; }
    
    @Override public @NotNull Logger getLogger() { return LOGGER; }
}
