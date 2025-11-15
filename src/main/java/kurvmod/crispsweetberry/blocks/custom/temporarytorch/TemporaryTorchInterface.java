package kurvmod.crispsweetberry.blocks.custom.temporarytorch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

/**
 * The interface used for both two temporary torches.
 */
public interface TemporaryTorchInterface
{
    int PROPERTY_INDEX_TO_NEXT_STD = 1,
        BRIGHTNESS_CONVERSION_CONSTANT = 4,
        PHASE_PERIOD_TICK = 400;
    
    enum LIGHT_STATE implements StringRepresentable
    {
        DARK, DIM, BRIGHT, FULL_BRIGHT;
        
        public String toString() { return this.name().toLowerCase(); }
        
        public int toBrightness() { return this.ordinal() * BRIGHTNESS_CONVERSION_CONSTANT; }
        
        public LIGHT_STATE getNextState() { return LIGHT_STATE.values()[this.ordinal() - PROPERTY_INDEX_TO_NEXT_STD]; }
        
        @Override
        public @NotNull String getSerializedName() { return this.name().toLowerCase(); }
    }
    
    EnumProperty<LIGHT_STATE> LIGHT_PROPERTY = EnumProperty.create("torchstate", LIGHT_STATE.class);
    
    SimpleParticleType TORCH_PARTICLE = ParticleTypes.SMALL_FLAME;
    BlockBehaviour.Properties TORCH_PROPERTIES = BlockBehaviour.Properties.of().
        lightLevel(state -> state.getValue(LIGHT_PROPERTY).toBrightness()).
        noLootTable().
        sound(SoundType.WOOD).
        ignitedByLava().
        noCollission().
        instabreak();
    
    default void onPlace(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
        { world.scheduleTick(pos, (Block) this, PHASE_PERIOD_TICK); }
    
    default void tick(@NotNull BlockState oldState, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        LIGHT_STATE oldLightState = oldState.getValue(LIGHT_PROPERTY);
        BlockState newState = oldState.setValue(LIGHT_PROPERTY, oldLightState.getNextState());
        
        if(oldLightState == LIGHT_STATE.DARK)
            return;
        
        if(!world.isClientSide)
            world.setBlockAndUpdate(pos, newState);
        
        world.scheduleTick(pos, (Block) this, PHASE_PERIOD_TICK);
    }
}
