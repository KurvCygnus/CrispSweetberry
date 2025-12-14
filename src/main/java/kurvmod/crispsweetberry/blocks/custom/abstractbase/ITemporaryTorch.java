package kurvmod.crispsweetberry.blocks.custom.abstractbase;

import kurvmod.crispsweetberry.entities.custom.abstractbase.AbstractThrownTorchEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.ToIntFunction;

import static kurvmod.crispsweetberry.utils.CrispConstants.ProjectileConstants.*;
import static kurvmod.crispsweetberry.utils.CrispConstants.SoundConstants;
import static net.minecraft.world.level.block.WallTorchBlock.FACING;

//PROTOTYPE

/**
 * The interface used for both <b>two temporary torches</b>,<br>
 * which lies the common behavior of the both <b>standard torch and the wall one</b>.
 * @since CSB 1.0 release
 * @author Kurv
 */
public interface ITemporaryTorch
{
    //Constants
    int DEFAULT_STATE_PERIOD_TICK = 400;//Equals to 20 seconds.
    
    double HORIZONTAL_WALL_TORCH_OFFSET_VALUE = 0.27;
    double VERTICAL_WALL_TORCH_OFFSET_VALUE = 0.22;
    double HORIZONTAL_TORCH_OFFSET_VALUE = 0.5;
    double VERTICAL_TORCH_OFFSET_VALUE = 0.7;
    
    float TORCH_BURNING_OUT_VOL = 0.2F;
    
    SimpleParticleType DEFAULT_TEMP_TORCH_PARTICLE = ParticleTypes.FLAME;
    SimpleParticleType DEFAULT_TEMP_TORCH_SUB_PARTICLE = ParticleTypes.SMOKE;
    
    BlockBehaviour.Properties TEMP_TORCH_BASIC_PROPERTIES = BlockBehaviour.Properties.of().
        noLootTable().
        sound(SoundType.WOOD).
        ignitedByLava().
        noCollission().
        instabreak();
    
    //Properties - Create a unique namespace of the LightState.
    EnumProperty<AbstractThrownTorchEntity.LightState> LIGHT_PROPERTY = EnumProperty.create("torchstate", AbstractThrownTorchEntity.LightState.class);
    
    //Constant, due to property declaration issue, it can only be here.
    ToIntFunction<BlockState> DEFAULT_BRIGHTNESS_FORMULA = bs -> bs.getValue(LIGHT_PROPERTY).toBrightness();
    
    //Abstracts
    /**
     * The getter method which <b>passes particle to Interface to use</b>.
     * @return The particle <b>declared in construct method</b>.
     */
    SimpleParticleType getTorchParticle();
    SimpleParticleType getSubTorchParticle();
    
    boolean getReLitProperty();
    
    /**
     * Getter method for <b>each state's life period</b>, <b>if you don't want to customize this, just return <u>{@code DEFAULT_STATE_PERIOD_TICK}</u></b>.
     */
    int getStateLength();
    
    //Defaults
    /**
     * The method which designs to <b>override the default method of the class Block</b>.<br>
     * <b>The {@code useItemOn()} of super method is in actual implementation of two torch classes</b>.<br>
     * Compare to the default one, it mainly <b>adds scheduleTick to useItemOn tick method</b>.
     */
    default void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
    {
        if(state.is(oldState.getBlock()))
            return;
        level.scheduleTick(pos, (Block) this, getStateLength());
    }
    
    /**
     * The tick method itself.<br>
     * Since it is universal, the implementers only need to useItemOn this method.<br>
     * <b>Using interface super method is a must in implementers because of the access priority</b>.
     */
    default void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        final double VERTICAL_PARTICLE_SPEED = (random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        AbstractThrownTorchEntity.LightState oldLightState = oldState.getValue(LIGHT_PROPERTY);
        BlockState newState = oldState.setValue(LIGHT_PROPERTY, oldLightState.getNextState());
        
        //* Terminates this method if the state is already dark.
        if(oldLightState == AbstractThrownTorchEntity.LightState.DARK)
            return;
        
        if(!level.isClientSide)
        {
            level.setBlockAndUpdate(pos, newState);
            //I originally set the volume to 1.0F, and just ending up feeling my ear is close to explosion UwU.
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, TORCH_BURNING_OUT_VOL, SoundConstants.NORMAL_SOUND_PITCH);
        }
        else
            level.addParticle(getSubTorchParticle(), pos.getX(), pos.getY(), pos.getZ(),
                X_NO_SPEED, VERTICAL_PARTICLE_SPEED, Z_NO_SPEED);
        
        //Wait for the next state change.
        level.scheduleTick(pos, (Block) this, getStateLength());
    }
    
    /**
     * The method that <b>displays the particle effect on the torch</b>.<br>
     * The main difference from the super method is,<br>
     * <b>when the torch burns out, it won't display *any* particle anymore</b>.
     */
    default void animateTick(BlockState state, Level level, BlockPos pos, boolean isWallTorch)
    {
        //* Dark state means the torch has already burned out, so of course wo should directly terminate this when the state is DARK.
        if(state.getValue(LIGHT_PROPERTY) == AbstractThrownTorchEntity.LightState.DARK)
            return;
        
        double X_POS = (double)pos.getX() + HORIZONTAL_TORCH_OFFSET_VALUE;
        double Y_POS = (double)pos.getY() + VERTICAL_TORCH_OFFSET_VALUE;
        double Z_POS = (double)pos.getZ() + HORIZONTAL_TORCH_OFFSET_VALUE;
        
        if(isWallTorch)//* Wall torch's particle position is different from standard one, of course.
        {
            Direction direction = state.getValue(FACING).getOpposite();
            
            X_POS += HORIZONTAL_WALL_TORCH_OFFSET_VALUE * (double)direction.getStepX();
            Y_POS += VERTICAL_WALL_TORCH_OFFSET_VALUE;
            Z_POS += HORIZONTAL_WALL_TORCH_OFFSET_VALUE * (double)direction.getStepZ();
        }
        
        if(level.isClientSide)
        {
            if(state.getValue(LIGHT_PROPERTY).ordinal() > AbstractThrownTorchEntity.LightState.DIM.ordinal())
                level.addParticle(getTorchParticle(), X_POS, Y_POS, Z_POS, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
            level.addParticle(getSubTorchParticle(), X_POS, Y_POS, Z_POS, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
        }
    }
    
    /**
     * The method which holds <b>special interactions with flint and steel</b>,
     * <b>fire charge</b>, both of which can <b>relight campfire in vanilla</b>.
     * The process:<br><pre>
     * I. Check <b>whether the item in its player's hand meets the condition</b>
     * II. Check <b>whether the torch can be relighted</b>
     * III. If the conditions above are passed, <b>set the torch's blockstate to FULL_BRIGHT</b>, and <b>recall scheduleTick</b>
     * IV. <b>Modify the item</b></pre>
     */
    default ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        Item itemInHand = stack.getItem();
        final Set<Item> interactableItems = Set.of(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE);
        
        //* Process I and II
        if(!getReLitProperty() || !interactableItems.contains(itemInHand) || state.getValue(LIGHT_PROPERTY).ordinal() > AbstractThrownTorchEntity.LightState.DIM.ordinal())
            return ItemInteractionResult.FAIL;
        
        if(!level.isClientSide)
        {
            //* Process III(Serverside only)
            level.setBlockAndUpdate(pos, state.setValue(LIGHT_PROPERTY, AbstractThrownTorchEntity.LightState.FULL_BRIGHT));
            level.scheduleTick(pos, (Block) this, getStateLength());
            
            //* Process IV
            if(itemInHand == Items.FLINT_AND_STEEL)
            {
                // For flint 'n steel, of course we should damage its durability.
                final float FLINT_AND_STEEL_PITCH = level.getRandom().nextFloat() * 0.4F + 0.8F;
                
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                    SoundConstants.NORMAL_SOUND_VOLUME, FLINT_AND_STEEL_PITCH);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            else//Fire Charge case
            {
                //For fire charge, of course it should be consumed upon using.
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, SoundConstants.NORMAL_SOUND_VOLUME, SoundConstants.NORMAL_SOUND_PITCH);
                stack.consume(1, player);
            }
        }
        
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
