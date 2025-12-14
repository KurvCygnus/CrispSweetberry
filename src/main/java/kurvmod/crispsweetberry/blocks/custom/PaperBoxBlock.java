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
import org.jetbrains.annotations.NotNull;

//TODO: Maybe for months later
//PLACEHOLDER
//DOC
//WIP
public class PaperBoxBlock extends HorizontalDirectionalBlock
{
    public static final MapCodec<PaperBoxBlock> CODEC = simpleCodec(PaperBoxBlock::new);
    
    /**
     * A <b>placeholder construct method, as its superclass demands to implement it</b>.
     */
    public PaperBoxBlock(Properties properties) { super(properties); }
    
    /**
     * This is the actual construct method for <b>block registry</b>.
     */
    public PaperBoxBlock()
    {
        this(BlockBehaviour.Properties.of().
            destroyTime(0.1F).
            explosionResistance(0.1F).
            ignitedByLava().
            sound(SoundType.SCAFFOLDING)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    
    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
}
