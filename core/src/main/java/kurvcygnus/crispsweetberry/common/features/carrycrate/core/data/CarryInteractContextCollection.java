//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core.data;

import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateConstants;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * This is the collection of data classes that <b>unifies the universal data getters for common usage data, and also the unique data for specific cases.</b>
 * @see kurvcygnus.crispsweetberry.common.features.carrycrate.self.CarryCrateItem CarryCrateItem
 * @see kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine#interact(ICarryInteractContext) Usage 
 */
@ApiStatus.Internal
public final class CarryInteractContextCollection
{
    /**
     * The basic interface which defines common data's getters.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public sealed interface ICarryInteractContext
    {
        @NotNull ItemStack getCarryCrate();
        @NotNull Level getLevel();
        @NotNull BlockPos getInteractPos();
        @NotNull Optional<Player> getPlayer();
        
        default @Nullable CarryID getCarryID() { return getCarryCrate().get(CarryCrateRegistries.CARRY_ID.get()); }
        default @Nullable CarryData getCarryData() { return getCarryCrate().get(CarryCrateRegistries.CARRY_CRATE_DATA.get()); }
        
        default boolean isDamaged()
        {
            final @Nullable Integer durability = getCarryCrate().get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get());
            
            return durability == null || durability < CarryCrateConstants.CARRY_CRATE_MAX_DURABILITY;
        }
    }
    
    /**
     * The specific context for <u>{@link net.minecraft.world.level.block.Block Block}</u>, and <u>{@link net.minecraft.world.level.block.entity.BlockEntity BlockEntity}</u>
     * cases.<br><br>
     * <i>That's why it is called "blocklike".</i>
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @param context The data this class provides as extra, <b>it is a must in a legal blocklike interaction</b>.
     */
    public record CarryBlocklikeInteractContext(@NotNull UseOnContext context) implements ICarryInteractContext
    {
        public CarryBlocklikeInteractContext { Objects.requireNonNull(context, "Param \"context\" must not be null!"); }
        
        @Override public @NotNull ItemStack getCarryCrate() { return context.getItemInHand(); }
        
        @Override public @NotNull Level getLevel() { return context.getLevel(); }
        
        @Override public @NotNull BlockPos getInteractPos() { return context.getClickedPos(); }
        
        @Override public @NotNull Optional<Player> getPlayer() { return Optional.ofNullable(context.getPlayer()); }
    }
    
    /**
     * The specific context for <u>{@link LivingEntity Entity}</u> interaction.
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @param carryCrate The <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.self.CarryCrateItem Carry Crate Intsance}</u> to be mutated.
     *                   <span style="color: 95cc6d">Essential data for interaction.</span>
     * @param player Also one of the <span style="color: 95cc6d">essential data.</span> However, different from <u>{@link CarryBlocklikeInteractContext CarryBlocklikeInteractContext}</u>,
     *               this cannot be {@code null}, since such an interaction will be invalid in that case.
     * @param target The <u>{@link LivingEntity Entity}</u> that will be processed. <b>This is the unique and extra data that is required in entity interaction.</b>
     */
    public record CarryEntityInteractContext(@NotNull ItemStack carryCrate, @NotNull Player player, @NotNull LivingEntity target) implements ICarryInteractContext
    {
        public CarryEntityInteractContext
        {
            Objects.requireNonNull(carryCrate, "Param \"carryCrate\" must not be null!");
            Objects.requireNonNull(player, "Param \"player\" must not be null!");
            Objects.requireNonNull(target, "Param \"target\" must not be null!");
        }
        
        @Override public @NotNull ItemStack getCarryCrate() { return carryCrate; }
        
        @Override public @NotNull Level getLevel() { return player.level(); }
        
        @Override public @NotNull BlockPos getInteractPos() { return target.getOnPos(); }
        
        @Override public @NotNull Optional<Player> getPlayer() { return Optional.of(player); }
    }
}
