package kurvmod.crispsweetberry.blocks.custom.temporarytorch;

import kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryTorchBlock;
import kurvmod.crispsweetberry.blocks.custom.abstractbase.ITemporaryTorch;
import kurvmod.crispsweetberry.entities.custom.abstractbase.AbstractThrownTorchEntity;
import net.minecraft.core.particles.SimpleParticleType;

/**
 * The standard block version of throwable torch.<br>
 * @see kurvmod.crispsweetberry.blocks.custom.abstractbase.AbstractTemporaryTorchBlock Abstract customization base detail
 * @see ITemporaryTorch Fuction base detail
 */
public final class TemporaryTorchBlock extends AbstractTemporaryTorchBlock
{
    public TemporaryTorchBlock(SimpleParticleType torchParticle)
    {
        super(torchParticle, DEFAULT_BRIGHTNESS_FORMULA);
        this.registerDefaultState(this.defaultBlockState().setValue(LIGHT_PROPERTY, AbstractThrownTorchEntity.LightState.FULL_BRIGHT));
    }
    
    @Override
    public int getStateLength() { return DEFAULT_STATE_PERIOD_TICK; }
    
    @Override
    public boolean getReLitProperty() { return true; }
    
    @Override
    public SimpleParticleType getSubTorchParticle() { return DEFAULT_TEMP_TORCH_SUB_PARTICLE; }
}