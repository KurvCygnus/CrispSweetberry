package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.function.ToIntFunction;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.*;

//? TODO: onHit系列的Override, 信号传递得完善

public final class TemporaryRedstoneTorchBlock extends AbstractTemporaryTorchBlock<TemporaryRedstoneTorchBehavior>
{
    private static final int MAX_BRIGHTNESS = 7;
    private static final int MIN_BRIGHTNESS = 0;
    private static final int TORCH_SIGNAL_SEND_DELAY = 2;
    
    private static final BooleanProperty LIT = BooleanProperty.create("lit");
    private static final ToIntFunction<BlockState> REDSTONE_BRIGHTNESS_FORMULA = bs -> bs.getValue(LIT) ? MAX_BRIGHTNESS : MIN_BRIGHTNESS;
    
    public TemporaryRedstoneTorchBlock(@NotNull TemporaryRedstoneTorchBehavior behavior)
    {
        super(TEMP_TORCH_BASE_PROPERTIES.lightLevel(REDSTONE_BRIGHTNESS_FORMULA), behavior);
    }
    
    
    @Override
    public @NotNull ParticleOptions getTorchParticle() { return DustParticleOptions.REDSTONE; }
    
    @Override
    public @NotNull ParticleOptions getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
    
    @Override
    protected void addExtraBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) { builder.add(LIT); }
    
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getStateLength() { return TORCH_SIGNAL_SEND_DELAY; }
    
    @Override
    public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporaryRedstoneTorchBehavior>> codec()
    {
        return null;
    }
    
    @Override
    public boolean isStillBright(@NotNull BlockState state) { return state.getValue(LIGHT_PROPERTY) != LightState.DARK; }

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
}
