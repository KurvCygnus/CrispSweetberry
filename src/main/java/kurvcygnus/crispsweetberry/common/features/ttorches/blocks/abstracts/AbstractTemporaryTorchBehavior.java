package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.*;
import static kurvcygnus.crispsweetberry.utils.definitions.SoundConstants.NORMAL_SOUND_PITCH;
import static kurvcygnus.crispsweetberry.utils.definitions.SoundConstants.NORMAL_SOUND_VOLUME;
import static kurvcygnus.crispsweetberry.utils.projectile.ProjectileConstants.*;
import static net.minecraft.world.level.block.WallTorchBlock.FACING;

public abstract class AbstractTemporaryTorchBehavior
{
    private final AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior> torchBlock;
    private boolean isStateLengthLegal = false;
    
    public <T extends AbstractGenericTorchBlock<? extends AbstractTemporaryTorchBehavior>> AbstractTemporaryTorchBehavior(@NotNull T torchBlock) 
    {
        Objects.requireNonNull(torchBlock, "Param \"torchBlock\" must not be null!");
        this.torchBlock = torchBlock;
    }
    
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState)
    {
        final int stateLength = this.torchBlock.getStateLength();
        
        if(!isStateLengthLegal)
        {
            //noinspection NonStrictComparisonCanBeEquality 
            CrispFunctionalUtils.throwIf(
                stateLength <= 0,//! Defensive check.
                () -> new IllegalArgumentException("The state length of tempo torches should be a positive integer! Current length: %d".formatted(stateLength))
            );
            isStateLengthLegal = true;
        }
        
        if(state.is(oldState.getBlock()))
            return;
        
        level.scheduleTick(pos, this.torchBlock, stateLength);
    }
    
    public @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
        @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand)
            {
                if(!isRelitable() || torchBlock.isStillBright(state))
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
                    level.setBlockAndUpdate(pos, state.setValue(LIGHT_PROPERTY, LightState.FULL_BRIGHT));
                    
                    if(isDamageable)
                        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                    else
                        stack.consume(1, player);
                }
                
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
    
    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        final double verticalParticleSpeed = (random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        final LightState oldLightState = oldState.getValue(LIGHT_PROPERTY);
        final BlockState newState = oldState.setValue(LIGHT_PROPERTY, oldLightState.getNextState());
        
        //! Terminates this method if the state is already dark.
        if(oldLightState == LightState.DARK)
            return;
        
        if(!level.isClientSide)
        {
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, TORCH_BURNING_OUT_VOL, SoundConstants.NORMAL_SOUND_PITCH);
        }
        else
            level.addParticle(torchBlock.getSubTorchParticle(), pos.getX(), pos.getY(), pos.getZ(),
                X_NO_SPEED, verticalParticleSpeed, Z_NO_SPEED
            );
        
        //* Wait for next state's change.
        level.scheduleTick(pos, this.torchBlock, torchBlock.getStateLength());
    }
    
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, boolean isWallTorch)
    {
        //! Dark state means the torch has already burned out, so of course wo should directly terminate this when the state is DARK.
        if(state.getValue(LIGHT_PROPERTY) == LightState.DARK)
            return;
        
        double xPos = (double) pos.getX() + HORIZONTAL_TORCH_OFFSET_VALUE;
        double yPos = (double) pos.getY() + VERTICAL_TORCH_OFFSET_VALUE;
        double zPos = (double) pos.getZ() + HORIZONTAL_TORCH_OFFSET_VALUE;
        
        if(isWallTorch)//* Wall torch's particle position is different from standard one, of course.
        {
            final Direction direction = state.getValue(FACING).getOpposite();
            
            xPos += HORIZONTAL_WALL_TORCH_OFFSET_VALUE * (double) direction.getStepX();
            yPos += VERTICAL_WALL_TORCH_OFFSET_VALUE;
            zPos += HORIZONTAL_WALL_TORCH_OFFSET_VALUE * (double) direction.getStepZ();
        }
        
        if(level.isClientSide)
        {
            if(torchBlock.isStillBright(state))
                level.addParticle(torchBlock.getTorchParticle(), xPos, yPos, zPos, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
            level.addParticle(torchBlock.getSubTorchParticle(), xPos, yPos, zPos, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
        }
    }
    
    private static boolean canLitStuff(@NotNull ItemStack stack, Item itemInHand)
    {
        return stack.is(ItemTags.CREEPER_IGNITERS) ||
            stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT) ||
            itemInHand instanceof FlintAndSteelItem ||
            itemInHand instanceof FireChargeItem;
    }
    
    protected abstract boolean isRelitable();
}
