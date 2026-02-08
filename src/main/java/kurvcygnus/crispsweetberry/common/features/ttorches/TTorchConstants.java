package kurvcygnus.crispsweetberry.common.features.ttorches;

import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.function.ToIntFunction;

public final class TTorchConstants
{
    private TTorchConstants() { throw new IllegalAccessError(); }
    
    public static final int DEFAULT_LIFECYCLE_TICK = 400;
    
    public static final double HORIZONTAL_WALL_TORCH_OFFSET_VALUE = 0.27;
    public static final double VERTICAL_WALL_TORCH_OFFSET_VALUE = 0.22;
    public static final double HORIZONTAL_TORCH_OFFSET_VALUE = 0.5;
    public static final double VERTICAL_TORCH_OFFSET_VALUE = 0.7;
    
    public static final float TORCH_BURNING_OUT_VOL = 0.2F;
    
    public static final SimpleParticleType DEFAULT_TEMP_TORCH_PARTICLE = ParticleTypes.FLAME;
    public static final SimpleParticleType DEFAULT_TEMP_TORCH_SUB_PARTICLE = ParticleTypes.SMOKE;
    
    public static final BlockBehaviour.Properties TEMP_TORCH_BASIC_PROPERTIES = BlockBehaviour.Properties.of().
        noLootTable().
        sound(SoundType.WOOD).
        ignitedByLava().
        noCollission().
        instabreak();
    
    public static final EnumProperty<AbstractThrownTorchEntity.LightState> LIGHT_PROPERTY =
        EnumProperty.create("torchstate", AbstractThrownTorchEntity.LightState.class);
    
    public static final ToIntFunction<BlockState> DEFAULT_BRIGHTNESS_FORMULA = bs -> bs.getValue(LIGHT_PROPERTY).toBrightness();
}