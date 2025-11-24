package kurvmod.crispsweetberry.blocks.custom.abstractbase;

import kurvmod.crispsweetberry.blocks.custom.temporarytorch.ITemporaryTorch;
import kurvmod.crispsweetberry.util.CrispEnums;
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

//WIP
//PROTOTYPE OK

/**
 * The <b>abstract class</b> for <b>temporary torch variants</b>.
 * Most functions are the same, so they are all in {@code ITemporaryTorch}.
 */
public abstract class AbstractTemporaryTorchBlock extends TorchBlock implements ITemporaryTorch
{
    //Property
    private final SimpleParticleType torchParticle;
    
    /**
     * The <b>construct method</b> for <b>block registry</b>.
     * @param torchParticle The particle will be used in animatedTick func.
     * @param brightnessFormula If you don't want to customize this, <b>use <u>{@code DEFAULT_BRIGHTNESS_FORMULA}</u></b>.
     * @see kurvmod.crispsweetberry.blocks.custom.temporarytorch.ITemporaryTorch Formula Source
     */
    protected AbstractTemporaryTorchBlock(SimpleParticleType torchParticle, ToIntFunction<BlockState> brightnessFormula)
    {
        super(torchParticle, TEMP_TORCH_BASIC_PROPERTIES.lightLevel(brightnessFormula));
        this.torchParticle = torchParticle;
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_PROPERTY, CrispEnums.LIGHT_STATE.FULL_BRIGHT));
    }
    
    //This creates the LIGHT_STATE blockstate.
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_PROPERTY);
    }
    
    /**
     * The getter method which <b>passes particle to Interface to use</b>.
     * @return The particle <b>declared in construct method</b>.
     */
    public final SimpleParticleType getTorchParticleType() { return torchParticle; }
    
    //Abstracts
    public abstract SimpleParticleType getSubTorchParticleType();
    
    /**
     * Getter method for <b>each state's life period</b>, <b>if you don't want to customize this, just return <u>{@code DEFAULT_STATE_PERIOD_TICK}</u></b>.
     * @see kurvmod.crispsweetberry.blocks.custom.temporarytorch.ITemporaryTorch Constant Source
     */
    public abstract int getStateLength();
    
    //Interface functions
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
        { ITemporaryTorch.super.onPlace(state, level, pos, oldState, isMoving); }
    
    @Override
    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random)
        { ITemporaryTorch.super.tick(oldState, world, pos, random); }
    
    /**
     * {@inheritDoc}
     * <br>
     * <b>Note</b>: Always passes <b><u>{@code isWallTorch}</b></u> as <b><u>{@code false}</b></u> to the <b>interface implementation</b>.
     * @see kurvmod.crispsweetberry.blocks.custom.temporarytorch.ITemporaryTorch Func Source
     */
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { ITemporaryTorch.super.animateTick(state, level, pos, false); }
    
    @Override
    public @NotNull ItemInteractionResult useItemOn
        (@NotNull ItemStack stack,
         @NotNull BlockState state,
         @NotNull Level level,
         @NotNull BlockPos pos,
         @NotNull Player player,
         @NotNull InteractionHand hand,
         @NotNull BlockHitResult hitResult) {
        return ITemporaryTorch.super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }
}
