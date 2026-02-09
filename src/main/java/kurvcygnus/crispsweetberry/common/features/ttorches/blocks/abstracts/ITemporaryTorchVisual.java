package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import net.minecraft.core.particles.ParticleOptions;
import org.jetbrains.annotations.NotNull;

public interface ITemporaryTorchVisual
{
    @NotNull ParticleOptions getTorchParticle();
    @NotNull ParticleOptions getSubTorchParticle();
}
