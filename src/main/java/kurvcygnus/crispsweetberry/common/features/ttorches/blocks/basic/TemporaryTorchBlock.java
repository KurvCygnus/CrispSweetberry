package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.*;

public final class TemporaryTorchBlock extends AbstractTemporaryTorchBlock<TemporaryTorchBehavior>
{
    private TemporaryTorchBlock(@Nullable Properties properties) { this(); }
    
    public TemporaryTorchBlock() { super(BASIC_TEMP_TORCH_PROPERTIES, new TemporaryTorchBehavior(Lazy.of(TTorchRegistries.TEMPORARY_TORCH))); }
    
    @Override
    protected void addExtraBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {}
    
    @Override
    public @Range(from = 0, to = Integer.MAX_VALUE) int getStateLength() { return DEFAULT_LIFECYCLE_TICK; }
    
    @Override
    public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporaryTorchBehavior>> codec() { return simpleCodec(TemporaryTorchBlock::new); }
    
    @Override
    public boolean isStillBright(@NotNull BlockState state) { return state.getValue(LIGHT_PROPERTY).ordinal() > LightState.DIM.ordinal(); }
    
    @Override
    public @NotNull ParticleOptions getTorchParticle() { return DEFAULT_TEMP_TORCH_PARTICLE; }
    
    @Override
    public @NotNull ParticleOptions getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
}
