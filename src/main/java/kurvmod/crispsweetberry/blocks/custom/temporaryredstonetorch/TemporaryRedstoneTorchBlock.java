package kurvmod.crispsweetberry.blocks.custom.temporaryredstonetorch;

import kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryTorchBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;

import static kurvmod.crispsweetberry.util.CrispConstants.*;

//TODO: onHit系列的Override, 信号传递得完善
//WIP
//DOC

/**
 * The standard block class of throwable redstone torch.
 * @see kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryTorchBlock Super class
 */
public final class TemporaryRedstoneTorchBlock extends AbstractTemporaryTorchBlock
{
    //Constants
    private static final int MAX_BRIGHTNESS = 7;
    private static final int MIN_BRIGHTNESS = 0;
    private static final int TORCH_SIGNAL_SEND_DELAY = 2;
    
    private static final BooleanProperty LIT = BooleanProperty.create("lit");
    
    //Variables
    private boolean signalSent = false;
    
    /**
     * Difference from super method: Brightness formula <b>only returns 7 or 0<b>, based on <b>whether the torch is lit or not</b>.
     * @param torchParticle A placeholder, it <b>won't have any actual effect</b>.
     */
    public TemporaryRedstoneTorchBlock(SimpleParticleType torchParticle)
    {
        super(torchParticle, bs -> bs.getValue(LIT) ? MAX_BRIGHTNESS : MIN_BRIGHTNESS);
        this.registerDefaultState(this.defaultBlockState().setValue(LIT, Boolean.TRUE));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
    }
    
    @Override
    public SimpleParticleType getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
    
    @Override
    public int getStateLength() { return 0; }
    
    @Override
    public boolean getReLitProperty() { return false; }
    
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
    {
        super.onPlace(state, level, pos, oldState, isMoving);
        
        //Immediately execute tick function after 2 ticks of placement, send signal to nearby redstone stuff.
        level.scheduleTick(pos, oldState.getBlock(), TORCH_SIGNAL_SEND_DELAY);
    }
    
    @Override
    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        super.tick(oldState, level, pos, random);
        for(Direction direction : Direction.values())
            level.updateNeighborsAt(pos.relative(direction), this);
        
        if(signalSent && !level.isClientSide)
            level.setBlockAndUpdate(pos, oldState.setValue(LIT, Boolean.FALSE));
        else
        {
            signalSent = true;
            level.scheduleTick(pos, oldState.getBlock(), TORCH_SIGNAL_SEND_DELAY);
        }
    }
    
    /**
     * <b>Returns the signal this block emits in the given direction</b>.
     * <p>
     * <b>NOTE</b>: directions in redstone signal related methods are backwards, so this method
     * checks for the signal emitted in the <i>opposite</i> direction of the one given.
     * </p><br>
     * @see net.minecraft.world.level.block.RedstoneTorchBlock Literally copied from here
     */
    @Override
    protected int getSignal(BlockState blockState, @NotNull BlockGetter blockAccess, @NotNull BlockPos pos, @NotNull Direction side)
        { return blockState.getValue(LIT) && Direction.UP != side ? 15 : 0; }
    
    /**
     * Completely overrides its super method, since it has some issues on this.
     * @see net.minecraft.world.level.block.RedstoneTorchBlock Func Code Source
     */
    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        if(!state.getValue(LIT))
            return;
        
        double X_POS = (double)pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        double Y_POS = (double)pos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.2;
        double Z_POS = (double)pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.2;
        level.addParticle(DustParticleOptions.REDSTONE, X_POS, Y_POS, Z_POS, PROJECTILE_X_NO_SPEED, PROJECTILE_Y_NO_SPEED, PROJECTILE_Z_NO_SPEED);
    }
}
