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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class CarryInteractContextCollection
{
    public sealed interface ICarryInteractContext
    {
        @NotNull ItemStack getCarryCrate();
        @NotNull Level getLevel();
        @NotNull BlockPos getInteractPos();
        @NotNull Optional<Player> getPlayer();
        
        default @Nullable CarryID getCarryID() { return getCarryCrate().get(CarryCrateRegistries.CARRY_ID.get()); }
        default @NotNull Optional<CarryData> getCarryData() { return Optional.ofNullable(getCarryCrate().get(CarryCrateRegistries.CARRY_CRATE_DATA.get())); }
        
        default boolean isDamaged()
        {
            final @Nullable Integer durability = getCarryCrate().get(CarryCrateRegistries.STACKABLE_TOOL_DURABILITY.get());
            
            return durability == null || durability < CarryCrateConstants.CARRY_CRATE_MAX_DURABILITY;
        }
    }
    
    public record CarryBlocklikeInteractContext(@NotNull UseOnContext context) implements ICarryInteractContext
    {
        @Override public @NotNull ItemStack getCarryCrate() { return context.getItemInHand(); }
        
        @Override public @NotNull Level getLevel() { return context.getLevel(); }
        
        @Override public @NotNull BlockPos getInteractPos() { return context.getClickedPos(); }
        
        @Override public @NotNull Optional<Player> getPlayer() { return Optional.ofNullable(context.getPlayer()); }
    }
    
    public record CarryEntityInteractContext(@NotNull ItemStack carryCrate, @NotNull Player player, @NotNull LivingEntity target) implements ICarryInteractContext
    {
        @Override public @NotNull ItemStack getCarryCrate() { return carryCrate; }
        
        @Override public @NotNull Level getLevel() { return player.level(); }
        
        @Override public @NotNull BlockPos getInteractPos() { return target.getOnPos(); }
        
        @Override public @NotNull Optional<Player> getPlayer() { return Optional.of(player); }
    }
}
