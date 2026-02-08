package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.LIGHT_PROPERTY;
import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.TORCH_BURNING_OUT_VOL;
import static kurvcygnus.crispsweetberry.utils.definitions.ProjectileConstants.X_NO_SPEED;
import static kurvcygnus.crispsweetberry.utils.definitions.ProjectileConstants.Z_NO_SPEED;
import static kurvcygnus.crispsweetberry.utils.definitions.SoundConstants.NORMAL_SOUND_PITCH;
import static kurvcygnus.crispsweetberry.utils.definitions.SoundConstants.NORMAL_SOUND_VOLUME;

public abstract class AbstractTemporaryTorchBehaviors<T extends BaseTorchBlock & ITemporaryTorchBehaviors>
{
    private final T temporaryTorchBlock;
    
    protected AbstractTemporaryTorchBehaviors(@NotNull T temporaryTorchBlock)
    {
        Objects.requireNonNull(temporaryTorchBlock, "Param \"temporaryTorchBlock\" must not be null!");
        this.temporaryTorchBlock = temporaryTorchBlock;
    }
    
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState)
    {
        if(state.is(oldState.getBlock()))
            return;
        
        level.scheduleTick(pos, this.temporaryTorchBlock, this.temporaryTorchBlock.getStateLength());
    }
    
    public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
        @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand)
            {
                if(!isRelitable() || ITemporaryTorchBehaviors.isStillBright(state))
                    return ItemInteractionResult.FAIL;
                
                final Item itemInHand = stack.getItem();
                
                if(!canLitStuff(stack, itemInHand))
                    return ItemInteractionResult.FAIL;
                
                final boolean isDamageable = stack.isDamageableItem();
                final float DAMAGEABLE_ITEM_PITCH = level.getRandom().nextFloat() * 0.4F + 0.8F;
                
                level.playSound(null, pos, isDamageable ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE,
                    SoundSource.BLOCKS, NORMAL_SOUND_VOLUME, isDamageable ? DAMAGEABLE_ITEM_PITCH : NORMAL_SOUND_PITCH
                );
                
                if(!level.isClientSide)
                {
                    level.setBlockAndUpdate(pos, state.setValue(LIGHT_PROPERTY, AbstractThrownTorchEntity.LightState.FULL_BRIGHT));
                    
                    if(isDamageable)
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    else
                        stack.consume(1, player);
                }
                
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
    
    private static boolean canLitStuff(@NotNull ItemStack stack, Item itemInHand)
    {
        return stack.is(ItemTags.CREEPER_IGNITERS) ||
            stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT) ||
            itemInHand instanceof FlintAndSteelItem ||
            itemInHand instanceof FireChargeItem;
    }
    
    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        final double verticalParticleSpeed = (random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        final AbstractThrownTorchEntity.LightState oldLightState = oldState.getValue(LIGHT_PROPERTY);
        final BlockState newState = oldState.setValue(LIGHT_PROPERTY, oldLightState.getNextState());
        
        //! Terminates this method if the state is already dark.
        if(oldLightState == AbstractThrownTorchEntity.LightState.DARK)
            return;
        
        if(!level.isClientSide)
        {
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, TORCH_BURNING_OUT_VOL, SoundConstants.NORMAL_SOUND_PITCH);
        }
        else
            level.addParticle(temporaryTorchBlock.getSubTorchParticle(), pos.getX(), pos.getY(), pos.getZ(),
                X_NO_SPEED, verticalParticleSpeed, Z_NO_SPEED
            );
        
        //* Wait for next state's change.
        level.scheduleTick(pos, this.temporaryTorchBlock, temporaryTorchBlock.getStateLength());
    }
    
    protected abstract boolean isRelitable();
}
