//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.CarriableSimpleLogicCollection;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableVanillaBlockEntityAccessors;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Set;

import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableVanillaBlockEntityAccessors.IVanillaBrewingStandAccessor.MAX_BREWING_TIME;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableVanillaBlockEntityAccessors.IVanillaBrewingStandAccessor.MAX_FUEL;

/**
 * This adapter can used by any blockEntity that inherits <u>{@link BrewingStandBlockEntity}</u>,
 * without editing the core logics({@code #serverTick()}) too heavily.
 *
 * @param <E> Any blockEntity that inherits <u>{@link BrewingStandBlockEntity}</u>.
 * @author Kurv Cygnus
 * @since 1.0 Release
 * @implNote The access the vanilla data relies on <u>{@link CarriableVanillaBlockEntityAccessors.IVanillaBrewingStandAccessor mixin accessor}</u>.
 */
public class BaseVanillaBrewingStandAdapter<E extends BrewingStandBlockEntity>
extends AbstractBlockEntityCarryAdapter<E> 
implements CarriableSimpleLogicCollection.ISimpleBlockEntityPenaltyLogic<E>, CarriableSimpleLogicCollection.ISimpleBlockEntityBreakLogic<E>
{
    //  region
    //*:=== Contacts & Constructor
    protected static final int OUTPUT_SLOT_START_INDEX = 0;
    protected static final int OUTPUT_SLOT_END_INDEX = 2;
    protected static final int INGREDIENT_SLOT_INDEX = 3;
    protected static final int FUEL_SLOT_INDEX = 4;
    
    public BaseVanillaBrewingStandAdapter(@NotNull BlockEntity blockEntity) { super(blockEntity); }
    
    @Override public @NotNull Class<?> getSupportedType() { return BrewingStandBlockEntity.class; }
    //endregion
    
    //  region
    //*:=== Atomic processions
    @Override protected void onPlacedProcess(@NotNull ServerLevel level, long elapsedTime, @NotNull CarriedContext context, @NotNull E blockEntity)
    {
        final CarriableVanillaBlockEntityAccessors.IVanillaBrewingStandAccessor accessor = (CarriableVanillaBlockEntityAccessors.IVanillaBrewingStandAccessor) blockEntity;
        
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
        
        final boolean canFinishBrew = elapsedTime >= accessor.getBrewTime() / getBrewRate();
        
        if(!canFinishBrew)
            accessor.setBrewTime((int) (accessor.getBrewTime() - elapsedTime));
        else
        {
            accessor.setBrewTime(MAX_BREWING_TIME);
            this.doBrew(level, context.pos(), accessor.getItems());
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
    
    protected final void doBrew(@NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull NonNullList<ItemStack> items)
    {
        if(EventHooks.onPotionAttemptBrew(items))
            return;
        
        ItemStack ingredientItem = items.get(INGREDIENT_SLOT_INDEX);
        final PotionBrewing potionbrewing = level.potionBrewing();
        
        for(int outputSlotIndex = OUTPUT_SLOT_START_INDEX; outputSlotIndex < INGREDIENT_SLOT_INDEX; outputSlotIndex++)
            items.set(outputSlotIndex, potionbrewing.mix(ingredientItem, items.get(outputSlotIndex)));
        
        EventHooks.onPotionBrewed(items);
        if(ingredientItem.hasCraftingRemainingItem())//* Process only once, since potion cannot be stacked.
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
        level.levelEvent(1035, pos, 0);//* This plays the sound of brewing stand.
    }
    
    @Override protected void onCarriedSequence(@NotNull CarriedContext context, @NotNull E blockEntity) { super.onCarriedSequence(context, blockEntity); }
    //endregion
    
    //  region
    //*:=== Serialization & Getter Hooks
    @Override protected void saveCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries, @NotNull E blockEntity)
        { ((CarriableVanillaBlockEntityAccessors.IVanillaBrewingStandAccessor) blockEntity).callSaveAdditional(tag, registries); }
    
    @Override protected void loadCarryTag(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries, @NotNull E blockEntity)
        { ((CarriableVanillaBlockEntityAccessors.IVanillaBrewingStandAccessor) blockEntity).callLoadAdditional(tag, registries); }
    
    protected @Range(from = 0, to = Integer.MAX_VALUE) int getMaxFuel() { return MAX_FUEL; }
    
    protected @Range(from = 0, to = Integer.MAX_VALUE) int getBrewRate() { return 1; }
    
    protected @NotNull Set<Item> getFuelItemList() { return Set.of(Items.BLAZE_POWDER); }
    
    @Override public @NotNull NonNullList<ItemStack> getItems(@NotNull E blockEntity) 
        { return ((CarriableVanillaBlockEntityAccessors.IVanillaBrewingStandAccessor) blockEntity).getItems(); }
    
    @Override public float getMiscFactor(@NotNull E blockEntity) 
        { return (float) ((CarriableVanillaBlockEntityAccessors.IVanillaBrewingStandAccessor) blockEntity).getFuel() / MAX_FUEL; }
    //endregion
}
