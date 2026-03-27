//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.self;

import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * This is a simple interface which allows a <b>stackable</b> <u>{@link Item}</u> to have durability.
 * @param <I> The <u>{@link Item}</u> that will be bound. This is a typical usage of
 *           <a href="https://en.wikipedia.org/wiki/Curiously_recurring_template_pattern">{@code F-bounded quantification}</a>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @apiNote Notes that once <u>{@link #hurtAndBreak(int, ItemStack, ServerLevel, LivingEntity, Consumer) damaged}</u>, the entire stack
 * will be affected. So don't forget to separate stacks before it gets damaged, unless you want to.
 * <hr>
 * Also, <u>{@link #isBarVisible(ItemStack)}</u>, <u>{@link #getBarColor(ItemStack)}</u> and <u>{@link #getBarWidth(ItemStack)}</u> are defined
 * by <u>{@link Item}</u> class, which means, <b>you need to delegate these methods manually, interface injection is not supported</b>.
 */
@ApiStatus.Internal
public interface IStackableTool<I extends Item & IStackableTool<I>>
{
    @Range(from = 1, to = Integer.MAX_VALUE) int getMaxDurability();
    
    @Range(from = 1, to = Integer.MAX_VALUE) int getPenaltyStandard();
    
    default boolean hurtAndBreak(
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level
    ) { return hurtAndBreak(getPenaltyStandard(), itemStack, level, null, ignored -> {}); }
    
    default boolean hurtAndBreak(
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level,
        @Nullable LivingEntity livingEntity
    ) { return hurtAndBreak(getPenaltyStandard(), itemStack, level, livingEntity, ignored -> {}); }
    
    default boolean hurtAndBreak(
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level,
        @NotNull Consumer<I> breakSequence
    ) { return hurtAndBreak(getPenaltyStandard(), itemStack, level, null, breakSequence); }
    
    default boolean hurtAndBreak(
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level,
        @Nullable LivingEntity livingEntity,
        @NotNull Consumer<I> breakSequence
    ) { return hurtAndBreak(getPenaltyStandard(), itemStack, level, livingEntity, breakSequence); }
    
    @SuppressWarnings("unchecked")//! See comments below: Line 118.
    default boolean hurtAndBreak(
        int damage,
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level,
        @Nullable LivingEntity livingEntity,
        @NotNull Consumer<I> breakSequence
    )
    {
        final MarkLogger logger = getLogger();
        
        if(!(itemStack.getItem() instanceof IStackableTool<?>))
        {
            logger.debug("Param \"itemStack\" is not a stackable tool. Returning false.");
            return false;
        }
        
        if(livingEntity == null)
            return false;
        
        final int durability = Objects.requireNonNullElse(itemStack.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()), getMaxDurability());
        logger.debug("Got the durability of this stack: {}", durability);
        
        if(livingEntity.hasInfiniteMaterials() && damage > 0)
        {
            damage = EnchantmentHelper.processDurabilityChange(level, itemStack, damage);
            
            if(damage < 0)
            {
                logger.debug("No damage. Returning false.");
                return false;
            }
        }
        
        if(durability - damage > 0)
        {
            logger.debug("Stackable tool didn't break, current durability is {}", durability - damage);
            itemStack.set(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get(), durability - damage);
            return false;
        }
        
        //* ItemStack#getItem()'s return type is 'Item',
        //* which means, as long as it is assignable from 'IStackableTool',
        //* and the itemStack's corresponded Item is this method's caller,
        //* it is 100% generic 'I'.
        final I item = (I) itemStack.getItem();
        
        if(livingEntity instanceof Player player)
        {
            logger.debug("Stack is held by player. Try play sound.");
            level.playSound(null, player.getOnPos(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
            
        itemStack.shrink(1);
        breakSequence.accept(item);
        itemStack.remove(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get());
        
        return true;
    }
    
    default boolean isBarVisible(@NotNull ItemStack stack)
    {
        //noinspection DataFlowIssue
        return !(stack.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()) == null ||
            //! Honestly, IDK why IDEA still complains about the null safety at here.
            stack.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()) == getMaxDurability());
    }
    
    default int getBarWidth(@NotNull ItemStack stack)
    {
        //! Obviously that this will only be called when #isBarVisible is true.
        //  noinspection DataFlowIssue
        final int durability = stack.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get());
        
        return Math.round(13.0F - (float) (getMaxDurability() - durability) * 13.0F / (float) getMaxDurability());
    }
    
    default int getBarColor(@NotNull ItemStack stack)
    {
        final float maxDurability = getMaxDurability();
        final float baseHue = Math.max(
            0.0F,
            (float) Objects.requireNonNullElse(stack.get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get()), getMaxDurability()) / maxDurability
        );
        return Mth.hsvToRgb(baseHue / 3.0F, 1.0F, 1.0F);
    }
    
    @NotNull MarkLogger getLogger();
}
