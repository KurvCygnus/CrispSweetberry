package kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers;

import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.abstracts.AbstractThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownRedstoneTorchEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public final class ThrownRedstoneTorchRenderer extends AbstractThrownTorchRenderer<ThrownRedstoneTorchEntity>
{
    public ThrownRedstoneTorchRenderer(EntityRendererProvider.@NotNull Context context) { super(context); }
    
//    @Override
//    protected @NotNull ResourceLocation getTextureLocation(@NotNull ThrownRedstoneTorchEntity entity, @NotNull FacingPair pair)
//    {
//        final StringBuilder path = new StringBuilder(BASE_TEXTURE_PATH).append(getTextureName());
//        
//        path.append("_").append(pair.horizontalFacing().getAlias());
//        path.append("_").append(pair.verticalFacing().getAlias());
//        
//        if(hasStateVariation() && entity.getTier() == AbstractThrownTorchEntity.TIER_GONE)
//            path.append("_").append(getAltTextureName());
//        
//        if(hasAnimation())
//        {
//            final int index = entity.tickCount / getAnimationDurationTicks() % getTotalAnimationFrames() + TEXTURE_INDEX_CORRECTION_STD;
//            path.append("_").append(index);
//        }
//        
//        return CrispDefUtils.getModNamespacedLocation(path.append(TEXTURE_SUFFIX).toString());
//    }
    
    @Override
    protected @NotNull String getTextureName() { return "thrown_redstone_torch"; }
}
