//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.glowstick;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

/**
 * This is the glowstick variant of ttorch series, features underwater lighting.<br>
 * <b><i>Correspondingly, glowstick doesn't have a wall variant, since it can't be attached to the wall</i></b>.
 * @author Kurv Cygnus
 * @see GlowStickBehavior Logic Implementation
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.entities.GlowStickEntity Entity
 * @see kurvcygnus.crispsweetberry.common.features.ttorches.items.GlowStickItem Item
 * @since 1.0 Release
 */
public final class GlowStickBlock extends AbstractTemporaryTorchBlock<GlowStickBehavior> implements SimpleWaterloggedBlock
{
    private static final ToIntFunction<BlockState> GLOW_STICK_BRIGHTNESS_FORMULA = bs -> bs.getValue(BlockStateProperties.WATERLOGGED) ? 15 : 9;
    
    private GlowStickBlock(@Nullable Properties properties) { this(); }
    
    public GlowStickBlock()
    {
        super(
            TTorchUtilCollection.BASIC_TEMP_TORCH_PROPERTIES.sound(SoundType.SLIME_BLOCK).lightLevel(GLOW_STICK_BRIGHTNESS_FORMULA),
            new GlowStickBehavior(Lazy.of(TTorchRegistries.GLOW_STICK_BLOCK))
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(BlockStateProperties.WATERLOGGED, false));
    }
    
    @Override public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        if(context.getLevel().getFluidState(context.getClickedPos()).is(FluidTags.WATER))
            return this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
        
        return this.defaultBlockState();
    }
    
    @Override protected void addExtraProperties(StateDefinition.@NotNull Builder<Block, BlockState> builder) { builder.add(BlockStateProperties.WATERLOGGED); }
    
    @Override public @NotNull MapCodec<? extends AbstractGenericTorchBlock<GlowStickBehavior>> codec() { return simpleCodec(GlowStickBlock::new); }
    
    @Override public @NotNull ParticleOptions getTorchParticle() { return ParticleTypes.GLOW_SQUID_INK; }
    
    @Override public @NotNull ParticleOptions getSubTorchParticle() { return ParticleTypes.GLOW_SQUID_INK; }
}
