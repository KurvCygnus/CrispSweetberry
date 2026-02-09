package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.*;

public final class TemporaryWallTorchBlock extends AbstractTemporaryWallTorchBlock<TemporaryTorchBehavior>
{
    private static final MapCodec<TemporaryWallTorchBlock> CODEC = simpleCodec(TemporaryWallTorchBlock::new);
    
    private TemporaryWallTorchBlock(@Nullable Properties properties) { this(); }
    
    public TemporaryWallTorchBlock() { super(DEFAULT_TEMP_TORCH_PARTICLE, BASIC_TEMP_TORCH_PROPERTIES, new TemporaryTorchBehavior(TTorchRegistries.TEMPORARY_WALL_TORCH.get())); }
    
    @Override
    protected void addExtraStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {}
    
    @Override
    public int getStateLength() { return DEFAULT_LIFECYCLE_TICK; }
    
    @Override
    public @NotNull MapCodec<? extends AbstractGenericTorchBlock<TemporaryTorchBehavior>> codec() { return CODEC; }
    
    @Override
    public boolean isStillBright(@NotNull BlockState state) { return state.getValue(LIGHT_PROPERTY).ordinal() > AbstractThrownTorchEntity.LightState.DIM.ordinal(); }
    
    @Override
    public @NotNull ParticleOptions getTorchParticle() { return DEFAULT_TEMP_TORCH_PARTICLE; }
    
    @Override
    public @NotNull ParticleOptions getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
}
