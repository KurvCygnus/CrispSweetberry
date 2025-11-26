package kurvmod.crispsweetberry.blocks.custom.abstractbase;

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
    //Variables
    private final SimpleParticleType torchParticle;
    
    /**
     * The <b>construct method</b> for <b>block registry</b>.
     * @param torchParticle The particle will be used in <b><u>{@code animatedTick()}</u></b> func.
     * @param brightnessFormula If you don't want to customize this, <b>use <u>{@code DEFAULT_BRIGHTNESS_FORMULA}</u></b>.
     * @see ITemporaryTorch Formula Source
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
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
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
     * <b>Note</b>: Always passes <b><u>{@code isWallTorch}</b></u> as <b><u>{@code false}</b></u> to the <b>interface implementation</b>.
     * @see ITemporaryTorch Func Source
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
