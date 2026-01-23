package kurvcygnus.crispsweetberry.common.features.temporarytorches.client.renderers;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.client.renderers.abstracts.AbstractThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.ThrownTorchEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

/**
 * The <b>renderer of thrown torch</b>.
 * @see ThrownTorchEntity Render Target
 * @since CSB 1.0 release
 * @author Kurv Cygnus
 */
public final class ThrownTorchRenderer extends AbstractThrownTorchRenderer<ThrownTorchEntity>
{
    public ThrownTorchRenderer(EntityRendererProvider.Context context) { super(context); }
    
    @Override
    protected @NotNull String getNamespace() { return CrispSweetberry.MOD_ID; }
    
    @Override
    protected @NotNull String getTextureName() { return "thrown_torch"; }
    
    @Override
    protected @NotNull String getAltTextureName() { return "dark"; }
    
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