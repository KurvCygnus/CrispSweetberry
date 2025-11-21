package kurvmod.crispsweetberry.items.custom;

import kurvmod.crispsweetberry.entities.custom.ThrownTorch.ThrownTorch;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import static kurvmod.crispsweetberry.util.CrispConstants.PROJECTILE_SHOOT_Z_POS;
import static kurvmod.crispsweetberry.util.CrispConstants.QUIET_SOUND_VOLUME;

//PROTOTYPE OK
//WIP
//TODO: Fix swing animation bug(WHY THIS ASS IS ALWAYS BUGGY?????)
//COMMENT NEEDED
public class ThrowableTorchItem extends Item implements ProjectileItem
{
    private static final float ALWAYS_ACCURATE = 1.0F,
        TORCH_THROW_VELOCITY = 1.5F;
    
    private static final int THROW_COOLDOWN = 12;
    
    /**
     * The construct function for <b>item registry</b>.
     */
    public ThrowableTorchItem(Properties properties) { super(properties); }
    
    /**
     * The overridden method to make <b>projectile item throwable</b>.
     * The process:<br><pre>
     *  I. Tell the game that the <b>player is using an item</b> and <b>needs to play swing animation</b>
     *  II. Play throw sound
     *  III. <b>Consume one torch</b> and <b>summon its projectile on the level</b>
     *  IV. <b>Set the player's stat to ITEM_USED</b>, then give them <b>a 0.6 second long cooldown</b></pre>
     */
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand)
    {
        ItemStack itemstack = player.getItemInHand(hand);
        final float THROW_SOUND_PITCH = 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F);
        
        //Process I
        player.startUsingItem(hand);
        player.swing(hand);
        
        //Process II
        level.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.SNOWBALL_THROW,
            SoundSource.NEUTRAL,
            QUIET_SOUND_VOLUME,
            THROW_SOUND_PITCH
        );
        
        if(!level.isClientSide)//Process III, which is on serverside
        {
            ThrownTorch thrownTorch = new ThrownTorch(level, player);
            thrownTorch.setItem(itemstack);
            thrownTorch.shootFromRotation(player, player.getXRot(), player.getYRot(), PROJECTILE_SHOOT_Z_POS,
                TORCH_THROW_VELOCITY, ALWAYS_ACCURATE);
            level.addFreshEntity(thrownTorch);
        }
        
        //Process IV
        player.awardStat(Stats.ITEM_USED.get(this));
        player.getCooldowns().addCooldown(this, THROW_COOLDOWN);
        itemstack.consume(1, player);
        
        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
    
    @Override
    public @NotNull Projectile asProjectile(@NotNull Level level, @NotNull Position pos, @NotNull ItemStack stack, @NotNull Direction direction)
    {
        ThrownTorch torch = new ThrownTorch(level, pos.x(), pos.y(), pos.z());
        torch.setItem(stack);
        return torch;
    }
}
