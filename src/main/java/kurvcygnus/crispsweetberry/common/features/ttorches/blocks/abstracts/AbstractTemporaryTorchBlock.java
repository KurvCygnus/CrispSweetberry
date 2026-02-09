package kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts;

import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTemporaryTorchBlock<T extends AbstractTemporaryTorchBehavior> extends AbstractGenericTorchBlock<T>
{
    public AbstractTemporaryTorchBlock(@NotNull SimpleParticleType torchParticle, @NotNull Properties properties, @NotNull T behavior)
        { super(torchParticle, properties, behavior, false); }
}
