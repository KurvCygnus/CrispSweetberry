package kurvmod.crispsweetberry.blocks.custom.temporarytorch;

import kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryWallTorchBlock;
import kurvmod.crispsweetberry.blocks.custom.abstractbase.ITemporaryTorch;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * The standard wall block version of throwable torch.
 * @see kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryWallTorchBlock Abstract customization base detail
 * @see ITemporaryTorch Fuction base detail
 */
public final class TemporaryWallTorchBlock extends AbstractTemporaryWallTorchBlock
{
    public TemporaryWallTorchBlock(SimpleParticleType torchParticle) { super(torchParticle, DEFAULT_BRIGHTNESS_FORMULA); }
    
    @Override
    public SimpleParticleType getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
    
    @Override
    public int getStateLength() { return DEFAULT_STATE_PERIOD_TICK; }
    
    @Override
    public boolean getReLitProperty() { return true; }
}