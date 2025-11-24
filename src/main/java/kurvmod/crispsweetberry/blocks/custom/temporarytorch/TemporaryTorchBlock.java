package kurvmod.crispsweetberry.blocks.custom.temporarytorch;

import kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryTorchBlock;
import kurvmod.crispsweetberry.util.CrispEnums;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * The standard block version of throwable torch.<br>
 * @see kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryTorchBlock Abstract customization base detail
 * @see kurvmod.crispsweetberry.blocks.custom.temporarytorch.ITemporaryTorch Fuction base detail
 */
public class TemporaryTorchBlock extends AbstractTemporaryTorchBlock
{
    public TemporaryTorchBlock(SimpleParticleType torchParticle)
    {
        super(torchParticle, DEFAULT_BRIGHTNESS_FORMULA);
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_PROPERTY, CrispEnums.LIGHT_STATE.FULL_BRIGHT));
    }
    
    @Override
    public int getStateLength() { return DEFAULT_STATE_PERIOD_TICK; }
    
    @Override
    public SimpleParticleType getSubTorchParticleType() { return TEMP_TORCH_DEFAULT_SUB_PARTICLE; }
}