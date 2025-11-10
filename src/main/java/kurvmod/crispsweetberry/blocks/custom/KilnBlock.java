package kurvmod.crispsweetberry.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KilnBlock extends FurnaceBlock
{
//    /**
//     * The facing direction blockstate definition.
//     */
//    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
//
//    @Override
//    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
//
//    @Override
//    public @NotNull BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    
    public KilnBlock(Properties properties)
    {
        super(properties);
        //this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected void openContainer(@NotNull Level level, @NotNull BlockPos pos, @NotNull Player player)
    {
        super.openContainer(level, pos, player);
    }
}
