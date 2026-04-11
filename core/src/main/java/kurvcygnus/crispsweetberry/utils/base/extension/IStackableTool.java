//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.extension;

import com.mojang.serialization.Codec;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import kurvcygnus.crispsweetberry.utils.constants.DummyFunctionalConstants;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
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
import org.slf4j.Logger;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

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
 * by <u>{@link Item}</u> class, which means, <b>you need to delegate these methods manually, as <u>{@link Item}</u> forces devs to implement that
 * (<i>Or, you can use <u>{@link StackableToolItem}</u> and <u>{@link StackableToolBlockItem}</u></i>, they are
 * <u><a href="https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html">Bridge Abstracts</a></u>, which don't have that issue)</b>.
 */
public interface IStackableTool<I extends Item & IStackableTool<I>>
{
    @Range(from = 1, to = Integer.MAX_VALUE) int getMaxDurability();
    
    @Range(from = 1, to = Integer.MAX_VALUE) int getPenaltyStandard();
    
    @SuppressWarnings("unchecked")//! As constants named, it does nothing, thus we can cast it.
    default boolean hurtAndBreak(
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level
    ) { return hurtAndBreak(getPenaltyStandard(), itemStack, level, null, (Consumer<I>) DummyFunctionalConstants.DO_NOTHING); }
    
    @SuppressWarnings("unchecked")//! As constants named, it does nothing, thus we can cast it.
    default boolean hurtAndBreak(
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level,
        @Nullable LivingEntity livingEntity
    ) { return hurtAndBreak(getPenaltyStandard(), itemStack, level, livingEntity, (Consumer<I>) DummyFunctionalConstants.DO_NOTHING); }
    
    @ApiStatus.NonExtendable default boolean hurtAndBreak(
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level,
        @NotNull Consumer<I> breakSequence
    ) { return hurtAndBreak(getPenaltyStandard(), itemStack, level, null, breakSequence); }
    
    @ApiStatus.NonExtendable default boolean hurtAndBreak(
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level,
        @Nullable LivingEntity livingEntity,
        @NotNull Consumer<I> breakSequence
    ) { return hurtAndBreak(getPenaltyStandard(), itemStack, level, livingEntity, breakSequence); }
    
    @SuppressWarnings("unchecked")//! See comments below: Line 127.
    @ApiStatus.NonExtendable default boolean hurtAndBreak(
        int damage,
        @NotNull ItemStack itemStack,
        @NotNull ServerLevel level,
        @Nullable LivingEntity livingEntity,
        @NotNull Consumer<I> breakSequence
    )
    {
        final Logger logger = getLogger();
        final DataComponentType<Integer> dataComponent = getDataComponent();
        final BiConsumer<String, @Nullable Integer> debug = (msg, args) -> FunctionalUtils.doIfNonNull(logger, log -> log.debug(msg, args));
        
        if(!(itemStack.getItem() instanceof IStackableTool<?>))
        {
            debug.accept("Param \"itemStack\" is not a stackable tool. Returning false.", null);
            return false;
        }
        
        if(livingEntity == null)
            return false;//! Duar penalty should only happen when an [[Entity]] holds it.
        
        final int durability = Objects.requireNonNullElse(itemStack.get(dataComponent), getMaxDurability());
        debug.accept("Got the durability of this stack: {}", durability);
        
        if(livingEntity.hasInfiniteMaterials() && damage > 0)
        {
            damage = EnchantmentHelper.processDurabilityChange(level, itemStack, damage);
            
            if(damage < 0)
            {
                debug.accept("No damage. Returning false.", null);
                return false;
            }
        }
        
        if(durability - damage > 0)
        {
            debug.accept("Stackable tool didn't break, current durability is {}", durability - damage);
            itemStack.set(dataComponent, durability - damage);
            return false;
        }
        
        //* [[ItemStack#getItem()]]'s return type is [[Item]],
        //* which means, as long as it is assignable from [[IStackableTool]],
        //* and the itemStack's corresponded Item is this method's caller,
        //* it is 100% generic 'I'.
        final I item = (I) itemStack.getItem();
        
        if(livingEntity instanceof Player player)
        {
            debug.accept("Stack is held by player. Try play sound.", null);
            level.playSound(null, player.getOnPos(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
            
        itemStack.shrink(1);
        breakSequence.accept(item);
        itemStack.remove(dataComponent);
        
        return true;
    }
    
    @SuppressWarnings("unused")//! This is implicitly used by [[StackableToolItem]] and [[StackableToolBlockItem]], with interface-class inheritance trick.
    @ApiStatus.NonExtendable default boolean isBarVisible(@NotNull ItemStack stack)
    {
        //noinspection DataFlowIssue
        return !(!stack.has(this::getDataComponent) ||//! IDEA's dataflow is seemingly flawed at here.
            stack.get(this::getDataComponent) == getMaxDurability());
    }
    
    @SuppressWarnings("unused")//! This is implicitly used by [[StackableToolItem]] and [[StackableToolBlockItem]], with interface-class inheritance trick.
    @ApiStatus.NonExtendable default int getBarWidth(@NotNull ItemStack stack)
    {
        //! Obviously that this will only be called when [[Item#isBarVisible]] is true.
        //  noinspection DataFlowIssue
        final int durability = stack.get(this::getDataComponent);
        
        return Math.round(13.0F - (float) (getMaxDurability() - durability) * 13.0F / (float) getMaxDurability());
    }
    
    @SuppressWarnings("unused")//! This is implicitly used by [[StackableToolItem]] and [[StackableToolBlockItem]], with interface-class inheritance trick.
    default int getBarColor(@NotNull ItemStack stack)
    {
        final float maxDurability = getMaxDurability();
        final float baseHue = Math.max(
            0.0F,
            (float) Objects.requireNonNullElse(stack.get(this::getDataComponent), getMaxDurability()) / maxDurability
        );
        return Mth.hsvToRgb(baseHue / 3.0F, 1.0F, 1.0F);
    }
    
    @NotNull DataComponentType<Integer> getDataComponent();
    
    @ApiStatus.OverrideOnly default @Nullable Logger getLogger() { return null; }
    
    /**
     * Produces a simple <u>{@link DataComponentType}</u>'s definition template, which is can, and should be used by <u>{@link IStackableTool#getDataComponent()}</u>.
     */
    static @NotNull Function<ResourceLocation, DataComponentType<Integer>> getDataComponentTemplate()
    {
        return resourceLocation -> DataComponentType.<Integer>builder().
            persistent(Codec.INT).
            networkSynchronized(ByteBufCodecs.INT).
            build();
    }
}
