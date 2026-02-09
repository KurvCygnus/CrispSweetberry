package kurvcygnus.crispsweetberry.common.features.ttorches.blocks;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic.TemporaryTorchBehavior;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.DEFAULT_TEMP_TORCH_SUB_PARTICLE;

//TODO: onHit系列的Override, 信号传递得完善

public final class TemporaryRedstoneTorchBlock extends AbstractTemporaryTorchBlock<TemporaryTorchBehavior>
{
    private static final int MAX_BRIGHTNESS = 7;
    private static final int MIN_BRIGHTNESS = 0;
    private static final int TORCH_SIGNAL_SEND_DELAY = 2;
    
    private static final BooleanProperty LIT = BooleanProperty.create("lit");
    private static final ToIntFunction<BlockState> REDSTONE_BRIGHTNESS_FORMULA = bs -> bs.getValue(LIT) ? MAX_BRIGHTNESS : MIN_BRIGHTNESS;
    
    private boolean signalSent = false;
    
    public TemporaryRedstoneTorchBlock(@NotNull SimpleParticleType torchParticle, @NotNull Properties properties, @NotNull TemporaryTorchBehavior behavior)
    {
        super(torchParticle, properties, behavior);
    }
    
    @Override
    public @NotNull ParticleOptions getTorchParticle()
    {
        return DustParticleOptions.REDSTONE;
    }
    
    @Override
    public @NotNull ParticleOptions getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
    
    @Override
    protected void addExtraBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) { builder.add(LIT); }
    
    @Override
    public int getStateLength() { return TORCH_SIGNAL_SEND_DELAY; }
    
    @Override
    public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporaryTorchBehavior>> codec()
    {
        return null;
    }
    
    @Override
    public boolean isStillBright(@NotNull BlockState state)
    {
        return false;
    }

//    @Override
//    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
//    {
//        super.tick(oldState, level, pos, random);
//        for(final Direction direction : Direction.values())
//            level.updateNeighborsAt(pos.relative(direction), this);
//        
//        if(signalSent && !level.isClientSide)
//            level.setBlockAndUpdate(pos, oldState.setValue(LIT, Boolean.FALSE));
//        else
//        {
//            signalSent = true;
//            level.scheduleTick(pos, oldState.getBlock(), TORCH_SIGNAL_SEND_DELAY);
//        }
//    }
    
    /**
     * <b>Returns the signal this block emits in the given direction</b>.
     * <p>
     * <b>NOTE</b>: directions in redstone signal related methods are backwards, so this method
     * checks for the signal emitted in the <i>opposite</i> direction of the one given.
     * </p>
     */
    @Override
    protected int getSignal(@NotNull BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side)
        { return blockState.getValue(LIT) && Direction.UP != side ? 15 : 0; }
    
//    /**
//     * Completely overrides its super method, since it has some issues on this.
//     * @see net.minecraft.world.level.block.RedstoneTorchBlock#animateTick(BlockState, Level, BlockPos, RandomSource)  Func Code Source
//     */
//    @Override
//    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
//    {
//        if(!state.getValue(LIT))
//            return;
//        
//        final double X_POS = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
//        final double Y_POS = (double)pos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.2;
//        final double Z_POS = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
//        level.addParticle(DustParticleOptions.REDSTONE, X_POS, Y_POS, Z_POS, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
//    }
}
