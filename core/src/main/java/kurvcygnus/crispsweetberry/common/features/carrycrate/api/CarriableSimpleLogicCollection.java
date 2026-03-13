//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.BaseVanillaBrewingStandAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableBlockEntityExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableExtensions;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryData;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * This is a simple collection for all types of adapters, providing practical default implementations for 
 * some specific logics.
 * @since 1.0 Release
 */
public final class CarriableSimpleLogicCollection
{
    //  region
    //*:=== Block
    /**
     * A default implementation of block's break logic.<br>
     * The detailed behavior is dropping the <u>{@link BlockItem}</u> of this <u>{@link net.minecraft.world.level.block.Block Block}</u>.
     * @apiNote If the <u>{@link net.minecraft.world.level.block.Block Block}</u> itself doesn't have a corresponded <u>{@link BlockItem}</u>, 
     * you should override default method <u>{@link #getDropItem()}</u>.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public interface ISimpleCarriableBlockBreakLogic extends CarriableExtensions.ICarriableLifecycle<CarryData.CarryBlockDataHolder>
    {
        /**
         * Get the <u>{@link Item}</u> that should be dropped by its <u>{@link net.minecraft.world.level.block.Block Block}</u>.
         */
        default @Nullable Item getDropItem() { return null; }
        
        @Override default void onBreak(@NotNull Level level, @NotNull BlockPos pos, @NotNull CarryData.CarryBlockDataHolder dataHolder, long elapsedTime)
        {
            final Item itemToDrop = Objects.requireNonNullElse(getDropItem(), dataHolder.getState().getBlock().asItem());
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(itemToDrop));
        }
    }
    //endregion
    
    //  region
    //*:=== BlockEntity
    /**
     * This interface provides a simple yet universal <u>{@link #getPenaltyRate(E) penaltyRate formula}</u> for blockEntity adapters.
     * @param <E> The blockEntity this adapter takes responsibility of.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public interface ISimpleBlockEntityPenaltyLogic<E extends BlockEntity> extends CarriableBlockEntityExtensions.IBlockEntityCarryLifecycle<E>
    {
        /**
         * A abstract method to get item list, which is essential for penaltyRate's <u>{@link #getPenaltyRate() calculation}</u>.
         */
        @NotNull NonNullList<ItemStack> getItems(@NotNull E blockEntity);
        
        /**
         * Provides {@code blockEntity} for further customization on final penaltyRate.
         * @see BaseVanillaBrewingStandAdapter#getMiscFactor(BrewingStandBlockEntity) Use Example 
         */
        default float getMiscFactor(@NotNull E blockEntity) { return 0F; }
        
        @Override default int getPenaltyRate(@NotNull E blockEntity)
        {
            final float itemTotalFactor = getItems(blockEntity).stream().
                map(i -> ((float) i.getCount() / i.getMaxStackSize())).
                reduce(Float::sum).
                orElse(0F);
            
            return (int) (DEFAULT_PENALTY_RATE / (1 + itemTotalFactor + getMiscFactor(blockEntity)));
        }
    }
    
    /**
     * Provides a simple break logic for <b><u>{@link BlockEntity}</u> that has container items.
     * @apiNote If your custom <b>{@link BlockEntity}</b> uses other name to represent its items, 
     * you should override method <u>{@link #getItemsTagID()}</u>.
     * @param <E> The blockEntity this adapter takes responsibility of.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public interface ISimpleBlockEntityBreakLogic<E extends BlockEntity> extends CarriableBlockEntityExtensions.IBlockEntityCarryLifecycle<E>
    {
        /**
         * Get the tag ID for this <u>{@link BlockEntity}</u>'s items.
         * @implNote <b>{@code "Items"}</b> is the default ID of a <u>{@link BlockEntity}</u>'s items, 
         * it is hard-coded in <u>{@link net.minecraft.world.ContainerHelper ContainerHelper}</u>.
         */
        default @NotNull String getItemsTagID() { return "Items"; }
        
        @Override default void onBreak(@NotNull Level level, @NotNull BlockPos pos, @NotNull CarryData.CarryBlockEntityDataHolder dataHolder, long elapsedTime)
        {
            final CompoundTag dataTag = dataHolder.getTagData();
            final NonNullList<ItemStack> items = NonNullList.create();
            
            loadAllItems(dataTag, items, level.registryAccess(), getItemsTagID());
            
            Containers.dropContents(level, pos, items);
        }
    }
    
    private static void loadAllItems(@NotNull CompoundTag tag, @NotNull NonNullList<ItemStack> items, HolderLookup.@NotNull Provider levelRegistry, @NotNull String id)
    {
        Objects.requireNonNull(tag, "Param \"tag\" must not be null!");
        Objects.requireNonNull(items, "Param \"items\" must not be null!");
        Objects.requireNonNull(levelRegistry, "Param \"levelRegistry\" must not be null!");
        Objects.requireNonNull(id, "Param \"id\" must not be null!");
        
        final ListTag listtag = tag.getList(id, 10);
        
        for(int index = 0; index < listtag.size(); index++)
        {
            final CompoundTag compoundtag = listtag.getCompound(index);
            final int slotIndex = compoundtag.getByte("Slot") & 255;
            
            //noinspection ConstantValue
            if(slotIndex >= 0 && slotIndex < items.size())//! Defensive check, and also follow vanilla's logic.
                items.set(slotIndex, ItemStack.parse(levelRegistry, compoundtag).orElse(ItemStack.EMPTY));
        }
    }
    //endregion
    
    //  region
    //*:=== Entity
    /**
     * Provides a simple break logic for <u>{@link net.minecraft.world.entity.LivingEntity Entites}</u>.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public interface ISimpleCarriableEntityBreakLogic extends CarriableExtensions.ICarriableLifecycle<CarryData.CarryEntityDataHolder>
    {
        @Override default void onBreak(@NotNull Level level, @NotNull BlockPos pos, @NotNull CarryData.CarryEntityDataHolder dataHolder, long elapsedTime)
        {
            final CompoundTag dataTag = dataHolder.getTagData();
            final Optional<Entity> optionalEntity = EntityType.create(dataTag, level);
            
            if(optionalEntity.isPresent())
            {
                final Entity entity = optionalEntity.get();
                entity.moveTo(pos.getX(), pos.getY(), pos.getZ());
                
                level.addFreshEntity(entity);
            }
            else
                getLogger().error(
                    "Cannot instantiate entity with its data \"{}\". This is a serious serialization issue. {}", 
                    dataTag.toString(),
                    MiscConstants.FEEDBACK_MESSAGE
                );
        }
        
        @NotNull MarkLogger getLogger();
    }
    //endregion
}
