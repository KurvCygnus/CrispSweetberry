package kurvmod.crispsweetberry.blocks.custom.temporarytorch;

import kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryWallTorchBlock;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * The standard wall block version of throwable torch.
 * @see kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryWallTorchBlock Abstract customization base detail
 * @see kurvmod.crispsweetberry.blocks.custom.temporarytorch.ITemporaryTorch Fuction base detail
 */
public class TemporaryWallTorchBlock extends AbstractTemporaryWallTorchBlock
{
    public TemporaryWallTorchBlock(SimpleParticleType torchParticle) { super(torchParticle, DEFAULT_BRIGHTNESS_FORMULA); }
    
    @Override
    public SimpleParticleType getSubTorchParticleType() { return TEMP_TORCH_DEFAULT_SUB_PARTICLE; }
    
    @Override
    public int getStateLength() { return DEFAULT_STATE_PERIOD_TICK; }
}