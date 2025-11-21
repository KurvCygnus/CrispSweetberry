package kurvmod.crispsweetberry.blocks.custom.temporarytorch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
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

import static kurvmod.crispsweetberry.util.CrispConstants.*;
import static net.minecraft.world.level.block.WallTorchBlock.FACING;

//PROTOTYPE OK: No issues, but there may still have some.
/**
 * The interface used for both <b>two temporary torches</b>,<br>
 * which lies the common behavior of the both <b>standard torch and the wall one</b>.
 */
public interface TemporaryTorchInterface
{
    //Constants
    int PROPERTY_INDEX_TO_NEXT_STD = 1,
        BRIGHTNESS_CONVERSION_CONSTANT = 4,//4 brightness per stage.
        STATE_PERIOD_TICK = 400;//Equals 20 seconds.
    
    double HORIZONTAL_WALL_TORCH_OFFSET_VALUE = 0.27,
           VERTICAL_WALL_TORCH_OFFSET_VALUE = 0.22,
           HORIZONTAL_TORCH_OFFSET_VALUE = 0.5,
           VERTICAL_TORCH_OFFSET_VALUE = 0.7;
    
    float TORCH_BURNING_OUT_VOL = 0.2F;
    
    /**
     * The <b>enum property</b> that controls the <b>life cycle and brightness</b> of temporary torches.
     */
    enum LIGHT_STATE implements StringRepresentable
    {
        DARK, DIM, BRIGHT, FULL_BRIGHT;//DARK coordinates burned out, but to put the state name more simple,
                                       //I deprecated the later name.
        
        /**
         * The <b>formula</b> to <b>convert enum to actual brightness value</b>.<br><br>
         * <b>e.g.</b><br>
         * <b>FULL_BRIGHT</b> ->
         * <b>3</b>(FULL_BRIGHT.ordinal()) <b>× 4</b>(BRIGHTNESS_CONVERSION_CONSTANT) <b> = 12</b>
         */
        public int toBrightness() { return this.ordinal() * BRIGHTNESS_CONVERSION_CONSTANT; }
        
        public LIGHT_STATE getNextState()
        {
            //Do boundary check at the same time.
            return this.ordinal() - PROPERTY_INDEX_TO_NEXT_STD > LIGHT_STATE.DARK.ordinal() ?
                LIGHT_STATE.values()[this.ordinal() - PROPERTY_INDEX_TO_NEXT_STD] : LIGHT_STATE.DARK;
        }
        
        /**
         * The <b>essential method</b> for <b>registering the state names correctly</b>.
         * @return The names of <b>corresponded states</b>.
         */
        @Override
        public @NotNull String getSerializedName() { return this.name().toLowerCase(); }
    }
    
    //Create a unique namespace of the LIGHT_STATE.
    EnumProperty<LIGHT_STATE> LIGHT_PROPERTY = EnumProperty.create("torchstate", LIGHT_STATE.class);
    
    //Constants
    SimpleParticleType TORCH_PARTICLE = ParticleTypes.SMALL_FLAME,
        SUB_PARTICLE = ParticleTypes.SMOKE;
    
    BlockBehaviour.Properties TORCH_PROPERTIES = BlockBehaviour.Properties.of().
        lightLevel(state -> state.getValue(LIGHT_PROPERTY).toBrightness()).
        noLootTable().
        sound(SoundType.WOOD).
        ignitedByLava().
        noCollission().
        instabreak();
    
    /**
     * The method which designs to <b>override the default method of the class Block</b>.<br>
     * <b>The useItemOn of super method is in actual implementation of two torch classes</b>.<br>
     * Compare to the default one, it mainly <b>adds scheduleTick to useItemOn tick method</b>.
     */
    default void onPlace(@NotNull Level world, @NotNull BlockPos pos)
        { world.scheduleTick(pos, (Block) this, STATE_PERIOD_TICK); }
    
    /**
     * The tick method itself.<br>
     * Since it is universal, the implementers only need to useItemOn this method.<br>
     * <b>Using interface super method is a must in implementers because of the access priority</b>.
     */
    default void tick(@NotNull BlockState oldState, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        final double VERTICAL_PARTICLE_SPEED = (random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        LIGHT_STATE oldLightState = oldState.getValue(LIGHT_PROPERTY);
        BlockState newState = oldState.setValue(LIGHT_PROPERTY, oldLightState.getNextState());
        
        //Ends this method if the state is already dark.
        if(oldLightState == LIGHT_STATE.DARK)
            return;
        
        if(!world.isClientSide)
        {
            world.setBlockAndUpdate(pos, newState);
            //I originally set the volume to 1.0F, and just ending up feeling my ear is close to explosion UwU.
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, TORCH_BURNING_OUT_VOL, NORMAL_SOUND_PITCH);
        }
        else
            world.addParticle(SUB_PARTICLE, pos.getX(), pos.getY(), pos.getZ(),
                PROJECTILE_X_NO_SPEED, VERTICAL_PARTICLE_SPEED, PROJECTILE_Z_NO_SPEED);
        
        //Wait for the next state change.
        world.scheduleTick(pos, (Block) this, STATE_PERIOD_TICK);
    }
    
    /**
     * The method that <b>displays the particle effect on the torch</b>.<br>
     * The main difference from the super method is,<br>
     * <b>when the torch burns out, it won't display *any* particle anymore</b>.
     */
    default void animateTick(BlockState state, Level level, BlockPos pos, boolean isWallTorch)
    {
        //Dark state means the torch has already burned out, so of course wo should directly terminate this when the state is DARK.
        if(state.getValue(LIGHT_PROPERTY) == LIGHT_STATE.DARK)
            return;
        
        double X_POS = (double)pos.getX() + HORIZONTAL_TORCH_OFFSET_VALUE,
            Y_POS = (double)pos.getY() + VERTICAL_TORCH_OFFSET_VALUE,
            Z_POS = (double)pos.getZ() + HORIZONTAL_TORCH_OFFSET_VALUE;
        
        if(isWallTorch)//Wall torch's particle position is different from standard one, o' course.
        {
            Direction direction = state.getValue(FACING).getOpposite();
            X_POS = (X_POS + HORIZONTAL_WALL_TORCH_OFFSET_VALUE) * (double)direction.getStepX();
            Y_POS += VERTICAL_WALL_TORCH_OFFSET_VALUE;
            Z_POS = (Z_POS + HORIZONTAL_WALL_TORCH_OFFSET_VALUE) * (double)direction.getStepZ();
        }
        
        level.addParticle(TORCH_PARTICLE, X_POS, Y_POS, Z_POS, PROJECTILE_X_NO_SPEED, PROJECTILE_Y_NO_SPEED, PROJECTILE_Z_NO_SPEED);
        level.addParticle(SUB_PARTICLE, X_POS, Y_POS, Z_POS, PROJECTILE_X_NO_SPEED, PROJECTILE_Y_NO_SPEED, PROJECTILE_Z_NO_SPEED);
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
        Set<Item> interactableItems = Set.of(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE);
        
        //Process I and II
        if(!interactableItems.contains(itemInHand) || state.getValue(LIGHT_PROPERTY).ordinal() > LIGHT_STATE.DIM.ordinal())
            return ItemInteractionResult.FAIL;
        
        if(!level.isClientSide)
        {
            //Process III(Serverside only)
            level.setBlockAndUpdate(pos, state.setValue(LIGHT_PROPERTY, LIGHT_STATE.FULL_BRIGHT));
            level.scheduleTick(pos, (Block) this, STATE_PERIOD_TICK);
            
            //Process IV
            if(itemInHand == Items.FLINT_AND_STEEL)
            {
                //For flint 'n steel, of course we should damage its durability.
                final float FLINT_AND_STEEL_PITCH = level.getRandom().nextFloat() * 0.4F + 0.8F;
                
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS,
                    NORMAL_SOUND_VOLUME, FLINT_AND_STEEL_PITCH);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            else//Fire Charge case
            {
                //For fire charge, o' course it should be consumed upon using.
                level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, NORMAL_SOUND_VOLUME, NORMAL_SOUND_PITCH);
                stack.consume(1, player);
            }
        }
        
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
