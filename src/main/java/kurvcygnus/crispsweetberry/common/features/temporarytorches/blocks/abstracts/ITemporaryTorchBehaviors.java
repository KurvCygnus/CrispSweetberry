package kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts;

import kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.utils.constants.SoundConstants;
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

import static kurvcygnus.crispsweetberry.utils.constants.ProjectileConstants.*;
import static net.minecraft.world.level.block.WallTorchBlock.FACING;

/**
 * This interface <b>handles the common things</b> for temporary torches like <b>behavior</b>, <b>constants</b> and <b>fields</b>.
 * @apiNote You shouldn't implement this in your own custom temporary torch stuff.
 * It is already be implemented in <b><u>{@link AbstractTemporaryTorchBlock}</u></b> and <b><u>{@link AbstractTemporaryWallTorchBlock}</u></b>.
 * All you have to do is {@code extends} these abstract classes.
 * @since CSB 1.0 release
 * @author Kurv
 */
public interface ITemporaryTorchBehaviors
{
    //  region
    //* Constants & Fields
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
    
    EnumProperty<AbstractThrownTorchEntity.LightState> LIGHT_PROPERTY = EnumProperty.create("torchstate", AbstractThrownTorchEntity.LightState.class);
    
    ToIntFunction<BlockState> DEFAULT_BRIGHTNESS_FORMULA = bs -> bs.getValue(LIGHT_PROPERTY).toBrightness();
    //endregion
    
    //  region
    //* Abstract parameter getters
    /**
     * The getter method which <b>passes particle to Interface to use</b>.
     * @return The particle <b>declared in construct method</b>.
     */
    @NotNull SimpleParticleType getTorchParticle();
    @NotNull SimpleParticleType getSubTorchParticle();
    
    boolean getReLitProperty();
    
    /**
     * Getter method for <b>each state's life period</b>, <b>if you don't want to customize this, just return <u>{@link #DEFAULT_STATE_PERIOD_TICK}</u></b>.
     */
    int getStateLength();
    //endregion
    
    //  region
    //* Interaction Behaviors
    /**
     * This method is designed to <b>override the default method of the {@code Block} classes</b>.<br>
     * It uses <b><u>{@link net.minecraft.world.level.LevelAccessor#scheduleTick(BlockPos, Block, int) scheduleTick()}</u></b> method to improve the performance instead of using block entity.<br><br>
     * <i>NOTE:<p>
     *  If you don't have the demand to modify the behavior heavily, all methods below are unnecessary to implement explicitly.
     *  Adjust the parameters from abstract methods is enough for such a situation.</i></p>
     */
    default void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
    {
        if(state.is(oldState.getBlock()))
            return;
        
        level.scheduleTick(pos, (Block) this, getStateLength());
    }
    
    /**
     * The method which holds <b>special interactions with flint and steel</b>,
     * <b>fire charge</b>, both of which can <b>relight campfire in vanilla</b>.
     */
    default @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, 
        @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, 
        @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult)
            {
                Item itemInHand = stack.getItem();
                final Set<Item> interactableItems = Set.of(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE);
                
                if(!interactableItems.contains(itemInHand) || !getReLitProperty()|| isStillBright(state))
                    return ItemInteractionResult.FAIL;
                
                if(!level.isClientSide)
                {
                    level.setBlockAndUpdate(pos, state.setValue(LIGHT_PROPERTY, AbstractThrownTorchEntity.LightState.FULL_BRIGHT));
                    level.scheduleTick(pos, (Block) this, getStateLength());
                    
                    if(itemInHand == Items.FLINT_AND_STEEL)
                    {
                        // For flint and steel, of course we should damage its durability.
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
    //endregion
    
    //  region
    //* Lifecycle & Logics
    /**
     * This method changes the state of the block.<br>
     */
    default void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        final double VERTICAL_PARTICLE_SPEED = (random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        AbstractThrownTorchEntity.LightState oldLightState = oldState.getValue(LIGHT_PROPERTY);
        BlockState newState = oldState.setValue(LIGHT_PROPERTY, oldLightState.getNextState());
        
        //! Terminates this method if the state is already dark.
        if(oldLightState == AbstractThrownTorchEntity.LightState.DARK)
            return;
        
        if(!level.isClientSide)
        {
            level.setBlockAndUpdate(pos, newState);
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, TORCH_BURNING_OUT_VOL, SoundConstants.NORMAL_SOUND_PITCH);
        }
        else
            level.addParticle(getSubTorchParticle(), pos.getX(), pos.getY(), pos.getZ(),
                X_NO_SPEED, VERTICAL_PARTICLE_SPEED, Z_NO_SPEED);
        
        //* Wait for next state's change.
        level.scheduleTick(pos, (Block) this, getStateLength());
    }
    
    /**
     * This method <b>displays the particle effect on the torch</b> at random rate.<br>
     */
    default void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, boolean isWallTorch)
    {
        //! Dark state means the torch has already burned out, so of course wo should directly terminate this when the state is DARK.
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
            if(isStillBright(state))
                level.addParticle(getTorchParticle(), X_POS, Y_POS, Z_POS, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
            level.addParticle(getSubTorchParticle(), X_POS, Y_POS, Z_POS, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
        }
    }
    
    private static boolean isStillBright(@NotNull BlockState state)
        { return state.getValue(LIGHT_PROPERTY).ordinal() > AbstractThrownTorchEntity.LightState.DIM.ordinal(); }
    //endregion
}
