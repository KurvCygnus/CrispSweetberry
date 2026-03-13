//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection.LIGHT_PROPERTY;

/**
 * This is a new basic abstraction of vanilla <u>{@link TorchBlock torch series}</u>, which is specially provided for our TTorch series.<br>
 * Compare to <u>{@link TorchBlock}</u>, it offers these advantages:<br>
 * <ul>
 *     <li>
 *         <b>Flexible particle support:</b><br>
 *         The original <u>{@link TorchBlock}</u> only supports <u>{@link net.minecraft.core.particles.SimpleParticleType SimpleParticleType}</u> for 
 *         <u>{@link #animateTick visual display}</u>, which excludes other particle series, like <u>{@link ScalableParticleOptionsBase}</u>, 
 *         <u>{@link ColorParticleOption}</u>. We replaced that with <u>{@link net.minecraft.core.particles.ParticleOptions ParticleOptions}</u>, 
 *         which is the most universal one.
 *     </li>
 *     <li>
 *         <b>One <u>{@link AbstractTemporaryTorchBehavior behavior impl}</u>, 
 *         both usable to <u>{@link AbstractTemporaryTorchBlock floor}</u> and <u>{@link AbstractTemporaryWallTorchBlock wall}</u> torches:</b><br>
 *         The vanilla <u>{@link net.minecraft.world.level.block.WallTorchBlock WallTorchBlock}</u> is implemented with inheriting <u>{@link TorchBlock}</u> 
 *         directly, which leads to the verbosity of torch function implementation, since neither does 
 *         <u>{@link net.minecraft.world.level.block.WallTorchBlock WallTorchBlock}</u>'s inheritance relationship, nor implementing a component to carry 
 *         custom behavior(Generic can only bound one specific class) can be reused to solve this.<br>
 *         However, that can be all solved by this, it offers field <u>{@link #isWallTorch}</u> to identity the componentExecutionType of torch, 
 *         which leads to different behaviors in <u>{@link AbstractTemporaryTorchBehavior behavior class}</u>.
 *     </li>
 *     <li>
 *         <b>Explicit <u>{@link MapCodec}</u> implementation requirement:</b><br>
 *         Vanilla torches are not meant to be reused by external stuff, so codecs are not mandatory, despite they're logically mandatory, since 
 *         this impacts synchronization aspect. This class has fixed these issues up.
 *     </li>
 * </ul>
 * @see AbstractTemporaryTorchBehavior Behavior Abstraction
 * @see AbstractTemporaryTorchBlock Floor Torch implementation
 * @see AbstractTemporaryWallTorchBlock Wall Torch implementation
 * @param <T> The detailed behavior of this torch block will bound to.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public abstract class AbstractGenericTorchBlock<T extends AbstractTemporaryTorchBehavior> extends TorchBlock implements ITemporaryTorchVisualExtensions, ITemporaryTorchLifecycleExtensions
{
    private final boolean isWallTorch;
    protected final T behavior;
    
    @SuppressWarnings("DataFlowIssue")//! We don't use "flameParticle" for display, and its componentExecutionType is "SimpleParticleType", which is not universal, and SUCKS.
    public AbstractGenericTorchBlock(@NotNull Properties properties, @NotNull T behavior, boolean isWallTorch)
    {
        super(null, requireNonNull(properties, "Param \"properties\" must not be null!"));
        requireNonNull(behavior, "Param \"behavior\" must not be null!");
        
        this.behavior = behavior;
        this.isWallTorch = isWallTorch;
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_PROPERTY, TTorchUtilCollection.LightState.FULL_BRIGHT));
    }
    
    @Override protected final void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        builder.add(LIGHT_PROPERTY);
        this.addExtraProperties(builder);
    }
    
    @Override protected final void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
        { this.behavior.onPlace(state, level, pos, oldState); }
    
    @Override protected final void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { this.behavior.tick(oldState, level, pos, random); }
    
    @Override public final void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { this.behavior.animateTick(state, level, pos, isWallTorch); }
    
    @Override protected final @NotNull ItemInteractionResult useItemOn(
        @NotNull ItemStack stack,
        @NotNull BlockState state,
        @NotNull Level level,
        @NotNull BlockPos pos,
        @NotNull Player player,
        @NotNull InteractionHand hand,
        @NotNull BlockHitResult hitResult
    ) { return this.behavior.useItemOn(stack, state, level, pos, player, hand); }
    
    @Override public final @NotNull String getDescriptionId() { return getThrowableTorchItem().getDescriptionId(); }
    
    @NotNull public Item getThrowableTorchItem() { return this.behavior.getThrowableTorchItem(); }
    
    protected void addExtraProperties(StateDefinition.@NotNull Builder<Block, BlockState> builder) {}
    
    public @Range(from = 0, to = Integer.MAX_VALUE) int getStateLength() { return TTorchUtilCollection.DEFAULT_LIFECYCLE_TICK; }
    
    public boolean isStillBright(@NotNull BlockState state) { return state.getValue(LIGHT_PROPERTY).ordinal() > TTorchUtilCollection.LightState.DIM.ordinal(); }
    
    public boolean isWallTorch() { return isWallTorch; }
    
    @Override public final @NotNull MapCodec<? extends AbstractGenericTorchBlock<T>> codec()
        { return simpleCodec(CrispFunctionalUtils.noArgCodec(getCodecConstruct())); }
    
    protected abstract @NotNull Supplier<? extends AbstractGenericTorchBlock<T>> getCodecConstruct();
}
