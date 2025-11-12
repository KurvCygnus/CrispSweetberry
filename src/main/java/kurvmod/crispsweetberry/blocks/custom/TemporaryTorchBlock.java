package kurvmod.crispsweetberry.blocks.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class TemporaryTorchBlock extends TorchBlock
{
    public TemporaryTorchBlock(SimpleParticleType flameParticle, Properties properties) { super(flameParticle, properties); }
    
    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context)
        { return Block.box(7.0D, 0.0D, 7.0D, 9.0D, 10.0D, 9.0D); }
}