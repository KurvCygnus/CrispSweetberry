package kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks;

import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.ITemporaryTorchBehaviors;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.ThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.items.ThrowableTorchItem;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

/**
 * The standard block version of throwable torch.<br>
 * @see ITemporaryTorchBehaviors Common Function Implementation Details
 * @see ThrownTorchEntity Projectile Part
 * @see ThrowableTorchItem Item Part
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class TemporaryTorchBlock extends AbstractTemporaryTorchBlock
{
    public TemporaryTorchBlock(@NotNull SimpleParticleType torchParticle)
    {
        super(torchParticle, DEFAULT_BRIGHTNESS_FORMULA);
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_PROPERTY, AbstractThrownTorchEntity.LightState.FULL_BRIGHT));
    }
    
    @Override
    public int getStateLength() { return DEFAULT_STATE_PERIOD_TICK; }
    
    @Override
    public boolean getReLitProperty() { return true; }
    
    @Override
    public @NotNull SimpleParticleType getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
}