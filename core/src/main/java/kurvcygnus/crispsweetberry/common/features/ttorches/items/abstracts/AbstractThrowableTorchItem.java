//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.items.abstracts;

import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.ThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
import kurvcygnus.crispsweetberry.utils.projectile.ITriProjectileFunction;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

import static kurvcygnus.crispsweetberry.utils.projectile.ProjectileConstants.PROJECTILE_SHOOT_Z_POS;

/**
 * The <b>basic</b> of <b>all throwable torches</b>.
 * @param <T> The <b>torch entity it bounds to</b>.
 * @see ThrowableTorchItem Basic Implementation
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public abstract class AbstractThrowableTorchItem<T extends AbstractThrownTorchEntity> extends Item implements ProjectileItem
{
    //  region
    //*:=== Constants, Fields & Constructors
    private static final float ALWAYS_ACCURATE = 1.0F;
    private static final float DEFAULT_TORCH_THROW_VELOCITY = 1.5F;
    
    private static final int DEFAULT_THROW_COOLDOWN = 12;
    
    private static final SoundEvent DEFAULT_THROW_SOUND = SoundEvents.SNOWBALL_THROW;
    
    /**
     * The construct function for <b>item registry</b>.
     */
    public AbstractThrowableTorchItem(@NotNull Properties properties) 
        { super(Objects.requireNonNull(properties, "Param \"properties\" must not be null!")); }
    //endregion
    
    //  region
    //*:=== Abstract parameter getters
    protected float getAccuracy() { return ALWAYS_ACCURATE; }
    
    protected float getThrowVelocity() { return DEFAULT_TORCH_THROW_VELOCITY; }
    
    protected int getThrowCooldown() { return DEFAULT_THROW_COOLDOWN; }
    
    protected @NotNull SoundEvent getThrowSound() { return DEFAULT_THROW_SOUND; }
    
    protected abstract @NotNull BiFunction<LivingEntity, Level, T> getPlayerUsedProjectile();
    
    protected abstract @NotNull ITriProjectileFunction<T> getDispenserUsedProjectile();
    //endregion
    
    //  region
    //*:=== Use interaction logics
    @Override public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand)
    {
        final ItemStack itemstack = player.getItemInHand(hand);
        final float throwSoundPitch = 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
        
        player.startUsingItem(hand);
        player.swing(hand, true);//! Flag "updateSelf" forces animation to play when the last one hasn't ended, which makes sure anim plays normally.
        
        level.playSound(
            null, player.getX(), player.getY(), player.getZ(),
            getThrowSound(), SoundSource.NEUTRAL, SoundConstants.QUIET_SOUND_VOLUME, throwSoundPitch
        );
        
        if(!level.isClientSide)
        {
            final T projectile = this.createProjectile(player, level);
            projectile.setItem(itemstack);
            
            projectile.shootFromRotation(
                player,
                player.getXRot(),
                player.getYRot(),
                PROJECTILE_SHOOT_Z_POS,
                getThrowVelocity(),
                getAccuracy()
            );
            
            level.addFreshEntity(projectile);
        }
        
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, getThrowCooldown());
        itemstack.consume(1, player);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    @Override
    public @NotNull Projectile asProjectile(@NotNull Level level, @NotNull Position pos, @NotNull ItemStack stack, @NotNull Direction direction)
    {
        final T projectile = this.createProjectile(pos.x(), pos.y(), pos.z(), level);
        projectile.setItem(stack);
        
        return projectile;
    }
    
    private @NotNull T createProjectile(@NotNull LivingEntity shooter, @NotNull Level level) { return getPlayerUsedProjectile().apply(shooter, level); }
    
    private @NotNull T createProjectile(double x, double y, double z, @NotNull Level level) { return getDispenserUsedProjectile().apply(x, y, z, level); }
    //endregion
}
