package kurvcygnus.crispsweetberry.common.features.ttorches;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

/**
 * This handles every constants that relates to throwable torch series.<br>
 * <i>Since the first letter of this series' content are all {@code 'T'}, thus both registry and package are called {@code TTorch}.</i>
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class TTorchConstants
{
    private TTorchConstants() { throw new IllegalAccessError("Class \"TTorchConstants\" is not meant to be instantized!"); }
    
    //  region
    //*:=== Block Constants
    public static final int DEFAULT_LIFECYCLE_TICK = 400;
    
    public static final double HORIZONTAL_WALL_TORCH_OFFSET_VALUE = 0.27;
    public static final double VERTICAL_WALL_TORCH_OFFSET_VALUE = 0.22;
    public static final double HORIZONTAL_TORCH_OFFSET_VALUE = 0.5;
    public static final double VERTICAL_TORCH_OFFSET_VALUE = 0.7;
    
    public static final float TORCH_BURNING_OUT_VOL = 0.2F;
    
    public static final SimpleParticleType DEFAULT_TEMP_TORCH_PARTICLE = ParticleTypes.FLAME;
    public static final SimpleParticleType DEFAULT_TEMP_TORCH_SUB_PARTICLE = ParticleTypes.SMOKE;
    
    public static final EnumProperty<LightState> LIGHT_PROPERTY = EnumProperty.create("torchstate", LightState.class);
    
    public static final ToIntFunction<BlockState> DEFAULT_BRIGHTNESS_FORMULA = bs -> bs.getValue(LIGHT_PROPERTY).toBrightness();
    
    public static final BlockBehaviour.Properties BASIC_TEMP_TORCH_PROPERTIES = BlockBehaviour.Properties.of().
        noLootTable().
        sound(SoundType.WOOD).
        ignitedByLava().
        noCollission().
        instabreak().
        lightLevel(DEFAULT_BRIGHTNESS_FORMULA);
    
    public static final BlockBehaviour.Properties TEMP_TORCH_BASE_PROPERTIES = BlockBehaviour.Properties.of().
        noCollission().
        instabreak().
        ignitedByLava();
    //endregion
    
    //  region
    //*:=== Renderer Constants
    public static final String BASE_TEXTURE_PATH = "textures/entity/";
    public static final String TEXTURE_SUFFIX = ".png";
    public static final float STANDARD_TORCH_SCALE = 0.5F;
    public static final float ROTATION_DEGREES = 180.0F;
    public static final int TEXTURE_INDEX_CORRECTION_STD = 1;
    public static final int DEFAULT_ANIMATION_DURATION_TICKS = 1;
    public static final int DEFAULT_ANIMATION_FRAMES_IN_TOTAL = 8;
    //endregion
    
    //  region
    //*:=== Block & Entity State Machine
    /**
     * The <b>enum property</b> that controls the <b>life cycle and brightness</b> of temporary torches.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public enum LightState implements StringRepresentable
    {
        DARK, DIM, BRIGHT, FULL_BRIGHT;
        
        private static final int BRIGHTNESS_PER_STATE = 4;
        private static final int PROPERTY_CORRECTION_INDEX = 1;
        
        /**
         * The <b>formula</b> to <b>convert enum to actual brightness value</b>.<br><br>
         * <b>e.g.</b><br>
         * <b><u>{@link #FULL_BRIGHT}</u></b> ->
         * <b>3</b>(<u>{@link #FULL_BRIGHT FULL_BRIGHT}</u>{@code .ordinal()})
         * <b>× 4</b>(<u>{@link #BRIGHTNESS_PER_STATE}</u>) <b> = 12</b>
         */
        public int toBrightness() { return this.ordinal() * BRIGHTNESS_PER_STATE; }
        
        public @NotNull LightState getNextState()
        {
            //! Do boundary check at the same time.
            return this.ordinal() - PROPERTY_CORRECTION_INDEX > LightState.DARK.ordinal() ?
                LightState.values()[this.ordinal() - PROPERTY_CORRECTION_INDEX] : LightState.DARK;
        }
        
        /**
         * The <b>essential method</b> for <b>registering the state names correctly</b>.
         * @return The names of <b>corresponded states</b>.
         */
        @Override public @NotNull String getSerializedName() { return this.name().toLowerCase(); }
    }
    //endregion
}