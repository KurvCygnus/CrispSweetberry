package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class AbstractTemporaryWallTorchBlock<T extends AbstractTemporaryTorchBehavior> extends AbstractGenericTorchBlock<T>
{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    
    private static final Map<Direction, VoxelShape> AABBS = Maps.newEnumMap(
        ImmutableMap.of(
            Direction.NORTH,
            Block.box(5.5, 3.0, 11.0, 10.5, 13.0, 16.0),
            Direction.SOUTH,
            Block.box(5.5, 3.0, 0.0, 10.5, 13.0, 5.0),
            Direction.WEST,
            Block.box(11.0, 3.0, 5.5, 16.0, 13.0, 10.5),
            Direction.EAST,
            Block.box(0.0, 3.0, 5.5, 5.0, 13.0, 10.5)
        )
    );
    
    public AbstractTemporaryWallTorchBlock(@NotNull Properties properties, @NotNull T behavior)
    {
        super(properties, behavior, true);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    protected final void addExtraBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        builder.add(FACING);
        this.addExtraStateDefinition(builder);
    }
    
    protected abstract void addExtraStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder);
    
    @Override
    public final @NotNull String getDescriptionId() { return this.asItem().getDescriptionId(); }
    
    @Override
    protected final @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
        { return AABBS.get(state.getValue(FACING)); }
    
    @Override
    public final boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos)
    {
        final Direction facing = state.getValue(FACING);
        final BlockPos blockpos = pos.relative(facing);
        final BlockState blockstate = level.getBlockState(blockpos);
        
        return blockstate.isFaceSturdy(level, blockpos, facing);
    }
    
    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        BlockState blockstate = this.defaultBlockState();
        final LevelReader levelreader = context.getLevel();
        final BlockPos blockpos = context.getClickedPos();
        final Direction[] possibleDirections = context.getNearestLookingDirections();
        
        for(final Direction direction: possibleDirections)
        {
            if(direction.getAxis().isHorizontal())
            {
                final Direction currentDirection = direction.getOpposite();
                blockstate = blockstate.setValue(FACING, currentDirection);
                
                if(blockstate.canSurvive(levelreader, blockpos))
                    return blockstate;
            }
        }
        
        return null;
    }
    
    @Override
    protected final @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull Direction facing, @NotNull BlockState facingState,
        @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos)
            { return facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos) ? Blocks.AIR.defaultBlockState() : state; }
    
    @Override
    protected final @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation)
        { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }
    
    @Override
    protected final @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror)
        { return this.rotate(state, mirror.getRotation(state.getValue(FACING))); }
}
