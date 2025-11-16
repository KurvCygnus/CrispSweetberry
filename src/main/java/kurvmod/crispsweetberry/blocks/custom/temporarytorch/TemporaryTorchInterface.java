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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.WallTorchBlock.FACING;

/**
 * The interface used for both two temporary torches.<br>
 * Both <b>standard torch and the wall one just have the same behavior</b>.
 */
public interface TemporaryTorchInterface
{
    /**
     * The <b>constants</b> for both <b>enum property and tick period</b>, and <b>particles</b>.
     */
    int PROPERTY_INDEX_TO_NEXT_STD = 1,
        BRIGHTNESS_CONVERSION_CONSTANT = 4,
        PHASE_PERIOD_TICK = 400;
    
    double TORCH_PARTICLE_SPEED = 0.0,
           HORIZONTAL_WALL_TORCH_ADD_VALUE = 0.27,
           VERTICAL_WALL_TORCH_ADD_VALUE = 0.22,
           HORIZONTAL_TORCH_ADD_VALUE = 0.5,
           VERTICAL_TORCH_ADD_VALUE = 0.7;
    
    /**
     * The <b>enum property</b> that controls the <b>life cycle and brightness</b> of temporary torches.
     */
    enum LIGHT_STATE implements StringRepresentable
    {
        DARK, DIM, BRIGHT, FULL_BRIGHT;
        
        /**
         * As this method's name suggests,
         * it is the <b>formula</b> to <b>convert enum to actual brightness value</b>.<br><br>
         * <b>e.g.</b><br>
         * <b>FULL_BRIGHT</b> ->
         * <b>3</b>(FULL_BRIGHT.ordinal()) * <b>4</b>(BRIGHTNESS_CONVERSION_CONSTANT) = <b>12</b>
         */
        public int toBrightness() { return this.ordinal() * BRIGHTNESS_CONVERSION_CONSTANT; }
        
        /**
         * The method which returns the <b>next state</b> in order.
         */
        public LIGHT_STATE getNextState()
        {
            //Do bounder check at the same time.
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
    
    /**
     * The <b>declaration</b> of the <b>above enum property</b>.<br>
     * The arg <b>"name"</b> equals to <b>state's namespace</b>.
     */
    EnumProperty<LIGHT_STATE> LIGHT_PROPERTY = EnumProperty.create("torchstate", LIGHT_STATE.class);
    
    /**
     * The <b>constants</b> for <b>torches particle and default properties</b>.<br>
     * Due to the context, the properties have to be here.
     */
    SimpleParticleType TORCH_PARTICLE = ParticleTypes.SMALL_FLAME;
    BlockBehaviour.Properties TORCH_PROPERTIES = BlockBehaviour.Properties.of().
        lightLevel(state -> state.getValue(LIGHT_PROPERTY).toBrightness()).
        noLootTable().
        sound(SoundType.WOOD).
        ignitedByLava().
        noCollission().
        instabreak();
    
    /**
     * The method which designs to <b>override the default method of the class Block</b>.<br>
     * <b>The use of super method is in actual implementation of two torch classes</b>.<br>
     * Compare to the default one, it mainly <b>adds scheduleTick to use tick method</b>.
     */
    default void onPlace(@NotNull Level world, @NotNull BlockPos pos)
        { world.scheduleTick(pos, (Block) this, PHASE_PERIOD_TICK); }
    
    /**
     * The tick method itself.<br>
     * Since it is universal, the implementers only need to use this method.<br>
     * <b>Using interface super method is a must in implementers because of the access priority</b>.
     */
    default void tick(@NotNull BlockState oldState, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        double HORIZONTAL_PARTICLE_SPEED = TORCH_PARTICLE_SPEED,
               VERTICAL_PARTICLE_SPEED = (random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        LIGHT_STATE oldLightState = oldState.getValue(LIGHT_PROPERTY);
        BlockState newState = oldState.setValue(LIGHT_PROPERTY, oldLightState.getNextState());
        
        //Ends this method if the state is already dark.
        if(oldLightState == LIGHT_STATE.DARK)
            return;
        
        if(!world.isClientSide)
        {
            world.setBlockAndUpdate(pos, newState);
            world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.2F, 1.0F);
        }
        else
            world.addParticle(ParticleTypes.SMOKE, pos.getX(), pos.getY(), pos.getZ(),
                HORIZONTAL_PARTICLE_SPEED, VERTICAL_PARTICLE_SPEED, HORIZONTAL_PARTICLE_SPEED);
        
        //Wait for the next state change.
        world.scheduleTick(pos, (Block) this, PHASE_PERIOD_TICK);
    }
    
    /**
     * The method that <b>displays the particle effect on the torch</b>.<br>
     * The main difference from the super method is,<br>
     * <b>when the torch burns out, it won't display *any* particle anymore</b>.
     */
    default void animateTick(BlockState state, Level level, BlockPos pos, boolean isWallTorch)
    {
        double d0 = (double)pos.getX() + HORIZONTAL_TORCH_ADD_VALUE,
            d1 = (double)pos.getY() + VERTICAL_TORCH_ADD_VALUE,
            d2 = (double)pos.getZ() + HORIZONTAL_TORCH_ADD_VALUE;
        
        if(isWallTorch)
        {
            Direction direction = state.getValue(FACING).getOpposite();
            d0 = (d0 + HORIZONTAL_WALL_TORCH_ADD_VALUE) * (double)direction.getStepX();
            d1 += VERTICAL_WALL_TORCH_ADD_VALUE;
            d2 = (d2 + HORIZONTAL_WALL_TORCH_ADD_VALUE) * (double)direction.getStepZ();
        }
        
        if(state.getValue(LIGHT_PROPERTY) != LIGHT_STATE.DARK)
        {
            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, TORCH_PARTICLE_SPEED, TORCH_PARTICLE_SPEED, TORCH_PARTICLE_SPEED);
            level.addParticle(TORCH_PARTICLE, d0, d1, d2, TORCH_PARTICLE_SPEED, TORCH_PARTICLE_SPEED, TORCH_PARTICLE_SPEED);
        }
    }
}
