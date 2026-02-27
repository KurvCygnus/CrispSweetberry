//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.abstracts.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.blockentity.IVanillaFurnaceSeriesAccessor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 
 * @param <E>
 */
public class BaseVanillaFurnaceSeriesAdapter<E extends AbstractFurnaceBlockEntity> 
extends AbstractBlockEntityCarryAdapter<E> implements ISimpleBlockEntityPenaltyLogic
{
    protected static final int INPUT_SLOT_INDEX = 0;
    protected static final int FUEL_SLOT_INDEX = 1;
    protected static final int OUTPUT_SLOT_INDEX = 2;
    
    public BaseVanillaFurnaceSeriesAdapter(@NotNull E blockEntity) { super(blockEntity); }
    
    @Override public void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) { getAccessor().callLoadAdditional(tag, registries); }
    
    @Override public void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) { getAccessor().callSaveAdditional(tag, registries); }
    
    @Override public void onCarriedSequence(@NotNull CarriedContext context) { super.onCarriedSequence(context); }
    
    
    @Override public void carryTick(@NotNull ServerLevel level, long carryingTime, @NotNull CarriedContext context)
    {
        final IVanillaFurnaceSeriesAccessor accessor = getAccessor();
        final int maxStackSize = blockEntity.getMaxStackSize();
        
        final ItemStack input = getInputItem();
        if(input.isEmpty())
        {
            cooldown(carryingTime);
            return;
        }
        
        final @Nullable RecipeHolder<? extends AbstractCookingRecipe> recipeHolder = getAccessor().getQuickCheck().
            getRecipeFor(new SingleRecipeInput(input), level).orElse(null);
        
        if(recipeHolder == null)
        {
            cooldown(carryingTime);
            return;
        }
        
        final int totalCookTime = getTotalCookTime(level);
        
        long remainingTime = carryingTime;
        
        while(remainingTime > 0)
        {
            if(!accessor.callIsLit() || canBurn(recipeHolder, maxStackSize, level))
            {
                final ItemStack fuel = getFuelItem();
                if(fuel.isEmpty())
                {
                    accessor.setCookingProgress((int) Math.max(0, accessor.getCookingProgress() - (remainingTime * 2)));
                    break;
                }
                
                accessor.setLitTime(accessor.callGetBurnDuration(fuel));
                
                if(accessor.getLitTime() > 0)
                    handleFuelConsumption(fuel);
                else 
                    break;
            }
            
            final long canBurnFor = Math.min(remainingTime, accessor.getLitTime());
            
            if(canBurn(recipeHolder, maxStackSize, level))
            {
                final long totalUsableTime = canBurnFor + accessor.getCookingProgress();
                final long quantityCooked = totalUsableTime / totalCookTime;
                
                final int possibleQuantityToCook = getMaxPossibleCrafts(recipeHolder, maxStackSize, level);
                
                final long actualCookedQuantity = Math.min(possibleQuantityToCook, quantityCooked);
                
                if(actualCookedQuantity > 0)
                {
                    bulkBurn(recipeHolder, (int) actualCookedQuantity, level);
                    
                    if(actualCookedQuantity == quantityCooked)
                        accessor.setCookingProgress((int) totalUsableTime % totalCookTime);
                    else
                    {
                        resetProgress();
                        final long spentTime = (actualCookedQuantity * totalCookTime) - accessor.getCookingProgress();
                        accessor.setLitTime(accessor.getLitTime() - (int) Math.max(0, spentTime));
                        break;
                    }
                }
                else 
                    accessor.setCookingProgress((int) Math.min(totalCookTime - 1, accessor.getCookingProgress() + canBurnFor));
            }
            else
                resetProgress();
            
            accessor.setLitTime(accessor.getLitTime() - (int) canBurnFor);
            remainingTime -= canBurnFor;
        }
    }
    
    protected final int getTotalCookTime(@NotNull ServerLevel level)
    {
        return getAccessor().getQuickCheck().getRecipeFor(new SingleRecipeInput(getInputItem()), level).
            map(r -> r.value().getCookingTime()).
            orElse(200);
    }
    
    protected boolean canBurn(
        @Nullable RecipeHolder<? extends AbstractCookingRecipe> recipeHolder,
        int maxStackSize,
        @NotNull ServerLevel level
    )
    {
        if(getInputItem().isEmpty() || recipeHolder == null)
            return false;
        
        final ItemStack result = recipeHolder.value().
            assemble(
                new SingleRecipeInput(getInputItem()),
                level.registryAccess()
            );
        
        if(result.isEmpty())
            return false;
        
        final ItemStack existingResult = getResultItem();
        
        if(existingResult.isEmpty())
            return true;
        
        if(!ItemStack.isSameItemSameComponents(existingResult, result))
            return false;
        
        return existingResult.getCount() + result.getCount() <= Math.min(maxStackSize, existingResult.getMaxStackSize());
    }
    
    protected void handleFuelConsumption(@NotNull ItemStack fuel)
    {
        if(fuel.hasCraftingRemainingItem())
            blockEntity.setItem(FUEL_SLOT_INDEX, fuel.getCraftingRemainingItem());
        else
        {
            fuel.shrink(1);
            if(!fuel.isEmpty())
                return;
            
            blockEntity.setItem(FUEL_SLOT_INDEX, fuel.getCraftingRemainingItem());
        }
    }
    
    protected int getMaxPossibleCrafts(
        @NotNull RecipeHolder<? extends AbstractCookingRecipe> recipeHolder,
        int maxStackSize,
        @NotNull ServerLevel level
    )
    {
        final ItemStack input = getInputItem();
        
        if(input.isEmpty())
            return 0;
        
        final ItemStack result = recipeHolder.value().
            assemble(new SingleRecipeInput(input), level.registryAccess());
        
        final ItemStack outputSlot = getResultItem();
        final int inputCount = input.getCount();
        
        if(outputSlot.isEmpty())
            return Math.min(inputCount, maxStackSize / result.getCount());
        
        if(!ItemStack.isSameItemSameComponents(outputSlot, result))
            return 0;
        
        final int spaceInOutput = Math.min(maxStackSize, outputSlot.getMaxStackSize()) - outputSlot.getCount();
        final int craftsByOutputSpace = spaceInOutput / result.getCount();
        
        return Math.min(inputCount, craftsByOutputSpace);
    }
    
    protected void bulkBurn(@NotNull RecipeHolder<? extends AbstractCookingRecipe> recipe, int count, @NotNull ServerLevel level)
    {
        if(count <= 0)
            return;
        
        final ItemStack input = getInputItem();
        final ItemStack result = recipe.value().assemble(new SingleRecipeInput(input), level.registryAccess());
        
        final ItemStack outputSlot = getResultItem();
        if(outputSlot.isEmpty())
        {
            final ItemStack finalResult = result.copy();
            finalResult.setCount(count * result.getCount());
            blockEntity.setItem(OUTPUT_SLOT_INDEX, finalResult);
        }
        else
            outputSlot.grow(count * result.getCount());
        
        if(input.is(Blocks.WET_SPONGE.asItem()) && !getFuelItem().isEmpty() && getFuelItem().is(Items.BUCKET))
            blockEntity.setItem(FUEL_SLOT_INDEX, new ItemStack(Items.WATER_BUCKET));
        
        input.shrink(count);
        getAccessor().callSetRecipeUsed(recipe);
    }
    
    protected final @NotNull ItemStack getFuelItem() { return blockEntity.getItem(FUEL_SLOT_INDEX); }
    
    protected final @NotNull ItemStack getInputItem() { return blockEntity.getItem(INPUT_SLOT_INDEX); }
    
    protected final @NotNull ItemStack getResultItem() { return blockEntity.getItem(OUTPUT_SLOT_INDEX); }
    
    protected final void cooldown(long carryingTime)
    {
        final IVanillaFurnaceSeriesAccessor accessor = getAccessor();
        
        accessor.setLitTime((int) Math.max(0, accessor.getLitTime() - carryingTime));
        accessor.setCookingProgress(0);
    }
    
    protected final void resetProgress() { getAccessor().setCookingProgress(0); }
    
    protected final @NotNull IVanillaFurnaceSeriesAccessor getAccessor() { return (IVanillaFurnaceSeriesAccessor) this.blockEntity; }
    
    @Override public @NotNull NonNullList<ItemStack> getItems() { return getAccessor().callGetItems(); }
}
