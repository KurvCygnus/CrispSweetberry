//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.self;

import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CarryCrateItem extends BlockItem
{
    public static final int CARRY_CRATE_MAX_DURABILITY = 120;
    
    public CarryCrateItem() 
    {
        super(
            CarryCrateRegistries.CARRY_CRATE_BLOCK.value(),
            new Properties().
                durability(CARRY_CRATE_MAX_DURABILITY).
                setNoRepair().//* "durability" sets the max stack size to 1 implicitly, we need to adjust this.
                stacksTo(MiscConstants.STANDARD_MAX_STACK_SIZE)//* and for flexibility, we take maxStackCount as a param.
        );
    }
    
    public @NotNull InteractionResult interactLivingEntity(
        @NotNull ItemStack stack,
        @NotNull Player player,
        @NotNull LivingEntity interactionTarget,
        @NotNull InteractionHand hand
    ) { return CarryEngine.interactOnEntity(stack, player, interactionTarget); }
    
    @Override public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
    {
        final @Nullable Player player = context.getPlayer();
        
        if(player == null)
            return InteractionResult.PASS;
        
        final boolean isCrunching = player.isShiftKeyDown();
        
        if(!isCrunching)
            return super.useOn(context);
        
        return CarryEngine.interactOnBlock(context);
    }
    
    @Override public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected)
        { CarryEngine.carryingTick(stack, level, entity, slotId); }
}
