package kurvcygnus.crispsweetberry.common.features.temporarytorches.items.abstracts;

import kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.items.ThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
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

import static kurvcygnus.crispsweetberry.utils.definitions.ProjectileConstants.PROJECTILE_SHOOT_Z_POS;

/**
 * The <b>basic</b> of <b>all throwable torches</b>.
 * @param <T> The <b>torch entity it bounds to</b>.
 * @see ThrowableTorchItem Basic Implementation
 * @since CSB 1.0 release
 * @author Kurv Cygnus
 */
public abstract class AbstractThrowableTorchItem<T extends AbstractThrownTorchEntity> extends Item implements ProjectileItem
{
    //  region
    //* Constants, Fields & Constructors
    protected static final float ALWAYS_ACCURATE = 1.0F;
    protected static final float DEFAULT_TORCH_THROW_VELOCITY = 1.5F;
    
    protected static final int DEFAULT_THROW_COOLDOWN = 12;
    
    protected static final SoundEvent DEFAULT_THROW_SOUND = SoundEvents.SNOWBALL_THROW;
    
    private final float throwAccuracy = getAccuracy();
    private final float throwVelocity = getThrowVelocity();
    
    private final int throwCooldown = getThrowCooldown();
    
    private final SoundEvent throwSound = getThrowSound();
    
    /**
     * The construct function for <b>item registry</b>.
     */
    public AbstractThrowableTorchItem(Properties properties) { super(properties); }
    //endregion
    
    //  region
    //* Abstract parameter getters
    /**
     * Getter method for <b>projectile's accuracy</b>, <b>if you don't want to customize this, just return <u>{@link #ALWAYS_ACCURATE}</u></b>.
     */
    protected abstract float getAccuracy();
    
    /**
     * Getter method for <b>projectile's speed</b>, <b>if you don't want to customize this, just return <u>{@link #DEFAULT_TORCH_THROW_VELOCITY}</u></b>.
     */
    protected abstract float getThrowVelocity();
    
    /**
     * Getter method for <b>item's use cooldown</b>, <b>if you don't want to customize this, just return <u>{@link #DEFAULT_THROW_COOLDOWN}</u></b>.
     */
    protected abstract int getThrowCooldown();
    
    /**
     * Getter method for <b>item's throw sound</b>, <b>if you don't want to customize this, just return <u>{@link #DEFAULT_THROW_SOUND}</u></b>.
     */
    protected abstract @NotNull SoundEvent getThrowSound();
    
    /**
     * <b>Getter method</b> for <b>player's throw action</b>.
     */
    protected abstract @NotNull T createProjectile(LivingEntity shooter, Level level);
    
    /**
     * <b>Getter method</b> for <b>dispenser's throw action</b>.
     */
    protected abstract @NotNull T createProjectile(double x, double y, double z, Level level);
    //endregion
    
    //  region
    //* Use interaction logics
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);
        final float THROW_SOUND_PITCH = 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
        
        player.startUsingItem(hand);
        player.swing(hand, true);//! Flag "updateSelf" forces animation to play when the last one hasn't ended, which makes sure anim plays normally.
        
        level.playSound(
            null, player.getX(), player.getY(), player.getZ(),
            throwSound, SoundSource.NEUTRAL, SoundConstants.QUIET_SOUND_VOLUME, THROW_SOUND_PITCH
        );
        
        if(!level.isClientSide)
        {
            T projectile = this.createProjectile(player, level);
            projectile.setItem(itemstack);
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(),
                PROJECTILE_SHOOT_Z_POS, throwVelocity, throwAccuracy);
            level.addFreshEntity(projectile);
        }
        
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, throwCooldown);
        itemstack.consume(1, player);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    @Override
    public @NotNull Projectile asProjectile(@NotNull Level level, @NotNull Position pos, @NotNull ItemStack stack, @NotNull Direction direction)
    {
        T projectile = this.createProjectile(pos.x(), pos.y(), pos.z(), level);
        projectile.setItem(stack);
        return projectile;
    }
    //endregion
}
