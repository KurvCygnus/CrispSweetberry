//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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
    
    public static final BlockBehaviour.Properties STANDARD_TEMPORARY_TORCH_PROPERTIES = BlockBehaviour.Properties.of().
        noLootTable().
        sound(SoundType.WOOD).
        ignitedByLava().
        noCollission().
        instabreak().
        lightLevel(DEFAULT_BRIGHTNESS_FORMULA);
    
    public static final BlockBehaviour.Properties BASIC_TEMP_TORCH_PROPERTIES = BlockBehaviour.Properties.of().
        noCollission().
        instabreak().
        ignitedByLava();
    
    public static final int REDSTONE_MAX_BRIGHTNESS = 7;
    public static final int REDSTONE_MIN_BRIGHTNESS = 0;
    public static final int REDSTONE_TORCH_SIGNAL_SEND_DELAY = 2;
    
    public static final BooleanProperty REDSTONE_LIT = BooleanProperty.create("lit");
    public static final ToIntFunction<BlockState> REDSTONE_BRIGHTNESS_FORMULA = bs -> bs.getValue(REDSTONE_LIT) ? REDSTONE_MAX_BRIGHTNESS : REDSTONE_MIN_BRIGHTNESS;
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
    
    public static final ResourceLocation SOUL_FIRE_0 = ResourceLocation.withDefaultNamespace("block/soul_fire_0");
    
    public static @NotNull TextureAtlasSprite getTextureByResourceLocation(@NotNull ResourceLocation resourceLocation) 
        { return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(resourceLocation); }
    //endregion
    
    //  region
    //*:=== Entity Constants
    public static final String SOUL_FIRE_PERSISTENT_TAG = "%s:is_lit_by_soul_fire".formatted(CrispSweetberry.NAMESPACE);
    
    public static boolean isLitBySoulFire(@NotNull Entity entity) { return entity.getPersistentData().contains(SOUL_FIRE_PERSISTENT_TAG); }
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
         * The <b>essential method</b> for <b>registering the attachTag names correctly</b>.
         * @return The names of <b>corresponded states</b>.
         */
        @Override public @NotNull String getSerializedName() { return this.name().toLowerCase(); }
    }
    //endregion
}