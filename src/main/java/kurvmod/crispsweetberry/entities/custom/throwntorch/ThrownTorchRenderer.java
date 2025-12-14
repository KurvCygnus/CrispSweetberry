package kurvmod.crispsweetberry.entities.custom.throwntorch;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.entities.custom.abstractbase.AbstractThrownTorchRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

/**
 * The <b>renderer of thrown torch</b>.
 * @since CSB 1.0 release
 * @author Kurv
 */
public final class ThrownTorchRenderer extends AbstractThrownTorchRenderer<ThrownTorchEntity>
{
    public ThrownTorchRenderer(EntityRendererProvider.Context context) { super(context); }
    
    @Override
    protected String getNamespace() { return CrispSweetberry.MOD_ID; }
    
    @Override
    protected String getTextureName() { return "thrown_torch"; }
    
    @Override
    protected String getAltTextureName() { return "dark"; }
    
    @Override
    protected int getAnimationDurationTicks() { return DEFAULT_ANIMATION_DURATION_TICKS; }
    
    @Override
    protected int getTotalAnimationFrames() { return DEFAULT_ANIMATION_FRAMES_IN_TOTAL; }
    
    @Override
    protected float getTorchScale() { return STANDARD_TORCH_SCALE; }
    
    @Override
    protected boolean hasAnimation() { return true; }
    
    @Override
    protected boolean hasStateVariation() { return true; }
}