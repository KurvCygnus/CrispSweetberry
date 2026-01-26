package kurvcygnus.crispsweetberry.common.features.carrycrate;

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
import org.jetbrains.annotations.Nullable;

//? TODO
public class CarryCrateBlock extends HorizontalDirectionalBlock
{
    public static final MapCodec<CarryCrateBlock> CODEC = simpleCodec(CarryCrateBlock::new);
    
    /// A **placeholder construct method, as its superclass demands to implement it**.
    public CarryCrateBlock(@Nullable Properties properties) { this(); }
    
    /// This is the actual construct method for **block registry**.
    public CarryCrateBlock()
    {
        super(BlockBehaviour.Properties.of().
            destroyTime(0.1F).
            explosionResistance(0.1F).
            ignitedByLava().
            sound(SoundType.SCAFFOLDING)
        );
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) { builder.add(FACING); }
    
    @Override
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
        { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
    
    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }
}
