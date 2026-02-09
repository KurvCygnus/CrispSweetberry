package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;
import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.LIGHT_PROPERTY;

public abstract class AbstractGenericTorchBlock<T extends AbstractTemporaryTorchBehavior> extends TorchBlock implements ITemporaryTorchVisual, ITemporaryTorchLifecycle
{
    protected final boolean isWallTorch;
    protected final T behavior;
    
    public AbstractGenericTorchBlock(@NotNull SimpleParticleType torchParticle, @NotNull Properties properties, @NotNull T behavior, boolean isWallTorch)
    {
        super(
            requireNonNull(torchParticle, "Param \"torchParticle\" must not be null!"),
            requireNonNull(properties, "Param \"properties\" must not be null!")
        );
        requireNonNull(behavior, "Param \"behavior\" must not be null!");
        
        this.behavior = behavior;
        this.isWallTorch = isWallTorch;
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_PROPERTY, AbstractThrownTorchEntity.LightState.FULL_BRIGHT));
    }
    
    @Override
    protected final void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(LIGHT_PROPERTY);
        this.addExtraBlockStateDefinition(builder);
    }
    
    protected abstract void addExtraBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder);
    
    public abstract int getStateLength();
    
    protected final void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
        { this.behavior.onPlace(state, level, pos, oldState); }
    
    protected final void tick(@NotNull BlockState oldState, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { this.behavior.tick(oldState, level, pos, random); }
    
    public final void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
        { this.behavior.animateTick(state, level, pos, isWallTorch); }
    
    protected final @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
        @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult)
            { return this.behavior.useItemOn(stack, state, level, pos, player, hand); }
    
    public abstract @NotNull MapCodec<? extends AbstractGenericTorchBlock<T>> codec();
    
    public abstract boolean isStillBright(@NotNull BlockState state);
}
