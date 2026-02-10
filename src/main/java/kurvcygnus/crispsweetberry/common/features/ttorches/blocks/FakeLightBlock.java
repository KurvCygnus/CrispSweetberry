package kurvcygnus.crispsweetberry.common.features.ttorches.blocks;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.LIGHT_PROPERTY;

public final class FakeLightBlock extends Block
{
    public FakeLightBlock()
    {
        super(Properties.of().noCollission().noOcclusion().noLootTable().lightLevel(bs -> bs.getValue(LIGHT_PROPERTY).toBrightness()));
        this.registerDefaultState(this.stateDefinition.getOwner().defaultBlockState().setValue(LIGHT_PROPERTY, TTorchConstants.LightState.DARK));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_PROPERTY);
    }
    
    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.INVISIBLE; }
    
    @Override
    public float getShadeBrightness(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) { return 1F; }
    
    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter reader, @NotNull BlockPos pos) { return true; }
    
    @Override
    protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
    {
        if(state.is(oldState.getBlock()))
            return;
        
        level.scheduleTick(pos, this, 1);
    }
    
    @Override
    protected void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) 
        { level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()); }
}
