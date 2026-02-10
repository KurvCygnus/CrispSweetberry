package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.*;

public final class TemporaryWallTorchBlock extends AbstractTemporaryWallTorchBlock<TemporaryTorchBehavior>
{
    private TemporaryWallTorchBlock(@Nullable Properties properties) { this(); }
    
    public TemporaryWallTorchBlock() { super(BASIC_TEMP_TORCH_PROPERTIES, new TemporaryTorchBehavior(TTorchRegistries.TEMPORARY_WALL_TORCH.get())); }
    
    @Override protected void addExtraStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {}
    
    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getStateLength() { return DEFAULT_LIFECYCLE_TICK; }
    
    @Override public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporaryTorchBehavior>> codec() { return simpleCodec(TemporaryWallTorchBlock::new); }
    
    @Override public boolean isStillBright(@NotNull BlockState state) { return state.getValue(LIGHT_PROPERTY).ordinal() > LightState.DIM.ordinal(); }
    
    @Override public @NotNull ParticleOptions getTorchParticle() { return DEFAULT_TEMP_TORCH_PARTICLE; }
    
    @Override public @NotNull ParticleOptions getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
}
