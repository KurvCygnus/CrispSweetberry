package kurvmod.crispsweetberry.blocks.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class PaperBoxBlock extends HorizontalDirectionalBlock
{
    public static final MapCodec<PaperBoxBlock> CODEC = simpleCodec(PaperBoxBlock::new);
    
    public PaperBoxBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    public PaperBoxBlock()
    {
        this(BlockBehaviour.Properties.of().
            destroyTime(0.1F).
            explosionResistance(0.1F).
            ignitedByLava().
            sound(SoundType.SCAFFOLDING)
        );
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    
    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
}
