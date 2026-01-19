package kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts;

import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.TemporaryRedstoneTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.TemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.abstracts.AbstractThrownTorchEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

/**
 * The <b>base</b> of all <b>temporary torch variants</b>.<br>
 * Since torches' behavior has a lot in common, detailed methods are implemented in
 * <b>{@link ITemporaryTorchBehaviors Interface}</b>.
 * @see TemporaryTorchBlock Basic Implementation
 * @see TemporaryRedstoneTorchBlock Redstone variants
 * @since CSB 1.0 release
 * @author Kurv
 */
public abstract class AbstractTemporaryTorchBlock extends TorchBlock implements ITemporaryTorchBehaviors
{
    private final SimpleParticleType torchParticle;
    
    /**
     * The <b>construct method</b> for <b>block registry</b>.
     * @param torchParticle The particle will be used in <b><u>
     * {@link ITemporaryTorchBehaviors#animateTick(BlockState, Level, BlockPos, boolean) animatedTick()}
     * </u></b>.
     * @param brightnessFormula If you don't want to customize this, <b>use
     * <u>{@link ITemporaryTorchBehaviors#DEFAULT_BRIGHTNESS_FORMULA DEFAULT_BRIGHTNESS_FORMULA}</u></b>.
     */
    protected AbstractTemporaryTorchBlock(@NotNull SimpleParticleType torchParticle, @NotNull ToIntFunction<BlockState> brightnessFormula)
    {
        super(torchParticle, TEMP_TORCH_BASIC_PROPERTIES.lightLevel(brightnessFormula));
        this.torchParticle = torchParticle;
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_PROPERTY, AbstractThrownTorchEntity.LightState.FULL_BRIGHT));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_PROPERTY);
    }
    
    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    public final @NotNull SimpleParticleType getTorchParticle() { return this.torchParticle; }
    
    public abstract @NotNull SimpleParticleType getSubTorchParticle();
    
    /**
     * {@inheritDoc}
     */
    public abstract int getStateLength();
    
    public abstract boolean getReLitProperty();
    
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
        { ITemporaryTorchBehaviors.super.onPlace(state, level, pos, oldState, isMoving); }
    
    @Override
    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { ITemporaryTorchBehaviors.super.tick(oldState, level, pos, random); }
    
    /**
     * {@inheritDoc}
     * <br>
     * <b>Note</b>: Always passes <b>{@code isWallTorch}</b> as <b>{@code false}</b> to the <b>interface implementation</b>.
     */
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { ITemporaryTorchBehaviors.super.animateTick(state, level, pos, false); }
    
    @Override
    public @NotNull ItemInteractionResult useItemOn(
         @NotNull ItemStack stack,
         @NotNull BlockState state,
         @NotNull Level level,
         @NotNull BlockPos pos,
         @NotNull Player player,
         @NotNull InteractionHand hand,
         @NotNull BlockHitResult hitResult)
            { return ITemporaryTorchBehaviors.super.useItemOn(stack, state, level, pos, player, hand, hitResult); }
}
