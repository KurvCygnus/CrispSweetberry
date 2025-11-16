package kurvmod.crispsweetberry.blocks.custom.temporarytorch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;

public class TemporaryWallTorchBlock extends WallTorchBlock implements TemporaryTorchInterface
{
    public TemporaryWallTorchBlock()
    {
        super(TORCH_PARTICLE, TORCH_PROPERTIES);
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_PROPERTY, LIGHT_STATE.FULL_BRIGHT));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_PROPERTY);
    }
    
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
        { TemporaryTorchInterface.super.onPlace(world, pos); }
    
    @Override
    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { TemporaryTorchInterface.super.tick(oldState, level, pos, random); }
    
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { TemporaryTorchInterface.super.animateTick(state, level, pos, true); }
}