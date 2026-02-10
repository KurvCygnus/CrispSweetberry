package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.redstone;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public final class TemporaryRedstoneWallTorchBlock extends AbstractTemporaryWallTorchBlock<TemporaryRedstoneTorchBehavior>
{
    public TemporaryRedstoneWallTorchBlock(@NotNull Properties properties, @NotNull TemporaryRedstoneTorchBehavior behavior)
    {
        super(properties, behavior);
    }
    
    @Override
    protected void addExtraStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder)
    {
        
    }
    
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getStateLength()
    {
        return 0;
    }
    
    @Override
    public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporaryRedstoneTorchBehavior>> codec()
    {
        return null;
    }
    
    @Override
    public boolean isStillBright(@NotNull BlockState state)
    {
        return false;
    }
    
    @Override
    public @NotNull ParticleOptions getTorchParticle()
    {
        return null;
    }
    
    @Override
    public @NotNull ParticleOptions getSubTorchParticle()
    {
        return null;
    }
}
