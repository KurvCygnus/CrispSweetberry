package kurvmod.crispsweetberry.blocks.custom.temporarytorch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class TemporaryTorchBlock extends TorchBlock implements TemporaryTorchInterface
{
    public TemporaryTorchBlock()
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
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
        { return Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D); }
    
    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
        { TemporaryTorchInterface.super.onPlace(world, pos); }
    
    @Override
    public void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { TemporaryTorchInterface.super.tick(oldState, level, pos, random); }
    
    @Override
    public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { TemporaryTorchInterface.super.animateTick(state, level, pos, false); }
}