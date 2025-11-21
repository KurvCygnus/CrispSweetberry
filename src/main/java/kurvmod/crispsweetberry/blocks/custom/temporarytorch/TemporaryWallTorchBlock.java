package kurvmod.crispsweetberry.blocks.custom.temporarytorch;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * The ThrowableTorch's <b>Wall Block</b> version.<br>
 * <b>Most methods have been encapsulated into the TemporaryTorchInterface<b>.
 */
public class TemporaryWallTorchBlock extends WallTorchBlock implements TemporaryTorchInterface
{
    /**
     * The construct method for <b>block registry</b>.
     */
    public TemporaryWallTorchBlock()
    {
        super(TORCH_PARTICLE, TORCH_PROPERTIES);
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_PROPERTY, LIGHT_STATE.FULL_BRIGHT));
    }
    
    //This creates the LIGHT_STATE blockstate.
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
    
    @Override
    public @NotNull ItemInteractionResult useItemOn
        (@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult)
        { return TemporaryTorchInterface.super.useItemOn(stack, state, level, pos, player, hand, hitResult); }
}