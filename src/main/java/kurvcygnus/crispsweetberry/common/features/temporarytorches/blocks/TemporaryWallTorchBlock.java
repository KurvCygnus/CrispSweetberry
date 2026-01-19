package kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks;

import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.ITemporaryTorchBehaviors;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.ThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.items.ThrowableTorchItem;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

/**
 * The standard wall block version of throwable torch.
 * @see ITemporaryTorchBehaviors Common Function Implementation Details
 * @see ThrownTorchEntity Projectile Part
 * @see ThrowableTorchItem Item Part
 */
public final class TemporaryWallTorchBlock extends AbstractTemporaryWallTorchBlock
{
    public TemporaryWallTorchBlock(@NotNull SimpleParticleType torchParticle) { super(torchParticle, DEFAULT_BRIGHTNESS_FORMULA); }
    
    @Override
    public @NotNull SimpleParticleType getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
    
    @Override
    public int getStateLength() { return DEFAULT_STATE_PERIOD_TICK; }
    
    @Override
    public boolean getReLitProperty() { return true; }
}