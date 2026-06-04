//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core.data;

import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.products.CarryCrateItem;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * This is the collection of data classes that <b>unifies the universal data getters for common usage data, and also the unique data for specific cases.</b>
 * @see CarryCrateItem CarryCrateItem
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
        static @NotNull ICarryInteractContext ofBlocklike(@NotNull UseOnContext context) { return new CarryBlocklikeInteractContext(context); }
        
        static @NotNull ICarryInteractContext ofEntity(@NotNull ItemStack carryCrate, @NotNull Player player, @NotNull LivingEntity target)
            { return new CarryEntityInteractContext(carryCrate, player, target); }
        
        @NotNull ItemStack getCarryCrate();
        @NotNull Level getLevel();
        @NotNull BlockPos getInteractPos();
        @NotNull Optional<Player> getPlayer();
        
        default @Nullable CarryID getCarryID() { return getCarryCrate().get(CarryCrateRegistries.CARRY_ID.get()); }
        default @Nullable CarryData getCarryData() { return getCarryCrate().get(CarryCrateRegistries.CARRY_CRATE_DATA.get()); }
    }
    
    /**
     * The specific context for <u>{@link Block Block}</u>, and <u>{@link BlockEntity BlockEntity}</u>
     * cases.<br><br>
     * <i>That's why it is called "blocklike".</i>
     * @author Kurv Cygnus
     * @since 1.0 Release
     */
     public static final class CarryBlocklikeInteractContext implements ICarryInteractContext
     {
         private final @NotNull UseOnContext context;
         
         private CarryBlocklikeInteractContext(@NotNull UseOnContext context)
         {
             Objects.requireNonNull(context, "Param \"context\" must not be null!");
             this.context = context;
         }
         
         @Override public @NotNull ItemStack getCarryCrate() { return context.getItemInHand(); }
         
         @Override public @NotNull Level getLevel() { return context.getLevel(); }
         
         @Override public @NotNull BlockPos getInteractPos() { return context.getClickedPos(); }
         
         @Override public @NotNull Optional<Player> getPlayer() { return Optional.ofNullable(context.getPlayer()); }
         
         public @NotNull UseOnContext context() { return context; }
         
         @Override public boolean equals(Object obj) { return this == obj || obj instanceof CarryBlocklikeInteractContext that && this.context.equals(that.context); }
         
         @Override public int hashCode() { return Objects.hash(context); }
         
         @Override public @NotNull String toString() { return "CarryBlocklikeInteractContext: " + context; }
         
     }
    
    /**
     * The specific context for <u>{@link LivingEntity Entity}</u> interaction.
     * @author Kurv Cygnus
     * @since 1.0 Release
     */
     public static final class CarryEntityInteractContext implements ICarryInteractContext, INestedPrintable<CarryEntityInteractContext>
     {
         private static final @NotNull @Unmodifiable Map<String, Function<CarryEntityInteractContext, @Nullable Object>> FIELD_MAP = INestedPrintable.buildFieldMap(
             map ->
             {
                 map.put("carryCrate", CarryEntityInteractContext::getCarryCrate);
                 map.put("player", CarryEntityInteractContext::getPlayer);
                 map.put("target", CarryEntityInteractContext::target);
             },
             3
         );
         
         private final @NotNull ItemStack carryCrate;
         private final @NotNull Player player;
         private final @NotNull LivingEntity target;
         
         private CarryEntityInteractContext(@NotNull ItemStack carryCrate, @NotNull Player player, @NotNull LivingEntity target)
         {
             Objects.requireNonNull(carryCrate, "Param \"carryCrate\" must not be null!");
             Objects.requireNonNull(player, "Param \"player\" must not be null!");
             Objects.requireNonNull(target, "Param \"target\" must not be null!");
             this.carryCrate = carryCrate;
             this.player = player;
             this.target = target;
         }
         
         @Override public @NotNull ItemStack getCarryCrate() { return carryCrate; }
         
         @Override public @NotNull Level getLevel() { return player.level(); }
         
         @Override public @NotNull BlockPos getInteractPos() { return target.getOnPos(); }
         
         @Override public @NotNull Optional<Player> getPlayer() { return Optional.of(player); }
         
         @Override public @NotNull @Unmodifiable Map<String, Function<CarryEntityInteractContext, @Nullable Object>> getFields() { return FIELD_MAP; }
         
         public @NotNull LivingEntity target() { return target; }
         
         @Override public boolean equals(Object obj)
         {
             return this == obj || obj instanceof CarryEntityInteractContext that &&
                 this.carryCrate.equals(that.carryCrate) &&
                 this.player.equals(that.player) &&
                 this.target.equals(that.target);
         }
         
         @Override public int hashCode() { return Objects.hash(carryCrate, player, target); }
         
         @Override public @NotNull String toString() { return toNestedString(); }
     }
}
