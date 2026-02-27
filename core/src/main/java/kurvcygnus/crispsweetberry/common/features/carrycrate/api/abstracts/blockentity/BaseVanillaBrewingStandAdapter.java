//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.IVanillaBrewingStandAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Set;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.IVanillaBrewingStandAccessor.MAX_BREWING_TIME;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.IVanillaBrewingStandAccessor.MAX_FUEL;

/**
 * This adapter can used by any blockEntity that inherits <u>{@link BrewingStandBlockEntity}</u>, 
 * without editing the core logics({@code #serverTick()}) too heavily.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @param <E> Any blockEntity that inherits <u>{@link BrewingStandBlockEntity}</u>.
 */
public class BaseVanillaBrewingStandAdapter<E extends BrewingStandBlockEntity> 
extends AbstractBlockEntityCarryAdapter<E> implements ISimpleBlockEntityPenaltyLogic
{
    protected static final int OUTPUT_SLOT_START_INDEX = 0;
    protected static final int OUTPUT_SLOT_END_INDEX = 2;
    protected static final int INGREDIENT_SLOT_INDEX = 3;
    protected static final int FUEL_SLOT_INDEX = 4;
    
    public BaseVanillaBrewingStandAdapter(@NotNull E blockEntity) { super(blockEntity); }
    
    @Override public void carryTick(@NotNull ServerLevel level, long carryingTime, @NotNull CarriedContext context)
    {
        final IVanillaBrewingStandAccessor accessor = getAccessor();
        
        if(accessor.getFuel() == 0)
        {
            final ItemStack fuelItem = accessor.getItems().get(FUEL_SLOT_INDEX);
            
            if(!getFuelItemList().contains(fuelItem.getItem()))
                return;
            
            accessor.setFuel(getMaxFuel());
            fuelItem.shrink(1);
            accessor.getItems().set(FUEL_SLOT_INDEX, fuelItem);
        }
        
        if(!isBrewable(level.potionBrewing(), accessor.getItems()))
        {
            accessor.setBrewTime(MAX_BREWING_TIME);
            return;
        }
        
        final boolean canFinishBrew = carryingTime >= accessor.getBrewTime() / getBrewRate();
        
        if(!canFinishBrew) 
            accessor.setBrewTime((int) (accessor.getBrewTime() - carryingTime));
        else
        {
            accessor.setBrewTime(MAX_BREWING_TIME);
            this.doBrew(level, context.pos());
        }
    }
    
    protected boolean isBrewable(@NotNull PotionBrewing potionBrewing, @NotNull NonNullList<ItemStack> items)
    {
        final ItemStack ingredientItem = items.get(INGREDIENT_SLOT_INDEX);
        
        if(!ingredientItem.isEmpty() && potionBrewing.isIngredient(ingredientItem))
        {
            for(int ioIndex = OUTPUT_SLOT_START_INDEX; ioIndex < INGREDIENT_SLOT_INDEX; ioIndex++)
            {
                final ItemStack slotContent = items.get(ioIndex);
                
                if(!slotContent.isEmpty() && potionBrewing.hasMix(slotContent, ingredientItem))
                    return true;
            }
        }
        
        return false;
    }
    
    protected final void doBrew(@NotNull ServerLevel level, @NotNull BlockPos pos)
    {
        final NonNullList<ItemStack> items = getAccessor().getItems();
        
        if(EventHooks.onPotionAttemptBrew(items))
            return;
        
        ItemStack ingredientItem = items.get(INGREDIENT_SLOT_INDEX);
        final PotionBrewing potionbrewing = level.potionBrewing();
        
        for(int outputSlotIndex = OUTPUT_SLOT_START_INDEX; outputSlotIndex < INGREDIENT_SLOT_INDEX; outputSlotIndex++)
            items.set(outputSlotIndex, potionbrewing.mix(ingredientItem, items.get(outputSlotIndex)));
        
        EventHooks.onPotionBrewed(items);
        if(ingredientItem.hasCraftingRemainingItem())
        {
            final ItemStack remainingItem = ingredientItem.getCraftingRemainingItem();
            ingredientItem.shrink(1);
            if(ingredientItem.isEmpty())
                ingredientItem = remainingItem;
            else
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainingItem);
        }
        else
            ingredientItem.shrink(1);
        
        items.set(INGREDIENT_SLOT_INDEX, ingredientItem);
        level.levelEvent(1035, pos, 0);
    }
    
    @Override public void onCarriedSequence(@NotNull CarriedContext context) { super.onCarriedSequence(context); }
    
    @Override public void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) { getAccessor().callSaveAdditional(tag, registries); }
    
    @Override public void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) { getAccessor().callLoadAdditional(tag, registries); }
    
    protected @Range(from = 0, to = Integer.MAX_VALUE) int getMaxFuel() { return MAX_FUEL; }
    
    protected @Range(from = 0, to = Integer.MAX_VALUE) int getBrewRate() { return 1; }
    
    protected @NotNull Set<Item> getFuelItemList() { return Set.of(Items.BLAZE_POWDER); }
    
    protected final @NotNull IVanillaBrewingStandAccessor getAccessor() { return (IVanillaBrewingStandAccessor) this.blockEntity; }
    
    @Override public @NotNull NonNullList<ItemStack> getItems() { return getAccessor().getItems(); }
    
    @Override public float getMiscFactor() { return (float) getAccessor().getFuel() / MAX_FUEL; }
}
