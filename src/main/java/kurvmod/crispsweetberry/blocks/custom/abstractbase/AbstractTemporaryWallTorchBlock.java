package kurvmod.crispsweetberry.blocks.custom.abstractbase;

import kurvmod.crispsweetberry.entities.custom.abstractbase.AbstractThrownTorchEntity;
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
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

//PROTOTYPE

/**
 * The <b>abstract class</b> for <b>temporary wall torch variants</b>.
 * Most functions are the same, so they are all in {@code ITemporaryTorch}.
 * @since CSB 1.0 release
 * @author Kurv
 */
public abstract class AbstractTemporaryWallTorchBlock extends WallTorchBlock implements ITemporaryTorch
{
    //Property
    private final SimpleParticleType torchParticle;
    
    /**
     * The <b>construct method</b> for <b>block registry</b>.
     *
     * @param torchParticle     The particle will be used in <b><u>{@code animatedTick()}</b></u> func.
     * @param brightnessFormula If you don't want to customize this, <b>use <u>{@code DEFAULT_BRIGHTNESS_FORMULA}</u></b>.
     * @see ITemporaryTorch Formula Source
     */
    public AbstractTemporaryWallTorchBlock(SimpleParticleType torchParticle, ToIntFunction<BlockState> brightnessFormula)
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
    @Override
    public final SimpleParticleType getTorchParticle() { return this.torchParticle; }
    
    //Abstracts
    public abstract SimpleParticleType getSubTorchParticle();
    
    /**
     * {@inheritDoc}
     * @see ITemporaryTorch Constant Source
     */
    public abstract int getStateLength();
    
    public abstract boolean getReLitProperty();
    
    //Interface functions
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
    { ITemporaryTorch.super.onPlace(state, level, pos, oldState, isMoving); }
    
    @Override
    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
    { ITemporaryTorch.super.tick(oldState, level, pos, random); }
    
    /**
     * {@inheritDoc}
     * <br>
     * <b>Note</b>: Always passes <b><u>{@code isWallTorch}</b></u> as <b><u>{@code true}</b></u> to the <b>interface implementation</b>.
     *
     * @see ITemporaryTorch Func Source
     */
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
    { ITemporaryTorch.super.animateTick(state, level, pos, true); }
    
    @Override
    public @NotNull ItemInteractionResult useItemOn(
        @NotNull ItemStack stack, @NotNull BlockState state,
        @NotNull Level level, @NotNull BlockPos pos,
        @NotNull Player player, @NotNull InteractionHand hand,
        @NotNull BlockHitResult hitResult
    ) {
        return ITemporaryTorch.super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
