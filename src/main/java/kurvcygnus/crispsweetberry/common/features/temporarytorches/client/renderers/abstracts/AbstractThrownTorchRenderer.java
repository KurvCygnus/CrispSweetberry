package kurvcygnus.crispsweetberry.common.features.temporarytorches.client.renderers.abstracts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.client.events.ThrowableTorchesRendererRegisterEvent;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.client.renderers.ThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.abstracts.AbstractThrownTorchEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

//? TODO: 方位性贴图实现
/**
 * The <b>basic renderer</b> of all <b>thrown torches</b>.
 * @param <T> The thrown torch that it renders.
 * @see ThrownTorchRenderer Basic implementation
 * @see ThrowableTorchesRendererRegisterEvent Registry
 * @since CSB 1.0 release
 * @author Kurv Cygnus
 */
@OnlyIn(Dist.CLIENT)
public abstract class AbstractThrownTorchRenderer<T extends AbstractThrownTorchEntity> extends EntityRenderer<T>
{
    //  region
    //* Constants, Fields & Constructors
    protected static final String BASE_TEXTURE_PATH = "textures/entity/";
    protected static final String TEXTURE_SUFFIX = ".png";
    
    protected static final float STANDARD_TORCH_SCALE = 0.5F;
    protected static final float ROTATION_DEGREES = 180.0F;
    
    protected static final int TEXTURE_INDEX_CORRECTION_STD = 1;
    protected static final int DEFAULT_ANIMATION_DURATION_TICKS = 1;
    protected static final int DEFAULT_ANIMATION_FRAMES_IN_TOTAL = 8;
    
    protected final String NAMESPACE = getNamespace();
    protected final String TEXTURE_NAME = getTextureName();
    protected final String ALT_TEXTURE_STATE_NAME = getAltTextureName();
    
    protected final float TORCH_SCALE = getTorchScale();
    
    protected final int ANIMATION_DURATION_TICKS = getAnimationDurationTicks();
    protected final int ANIMATION_FRAMES_IN_TOTAL = getTotalAnimationFrames();
    
    protected final boolean HAS_ANIMATION = hasAnimation();
    protected final boolean HAS_STATE_VARIATION = hasStateVariation();
    
    /**
     * This constructor is used for initialization registry, thus implementing this is a must.
     */
    public AbstractThrownTorchRenderer(EntityRendererProvider.Context context) { super(context); }
    //endregion
    
    //  region
    //* Core logics
    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());//* Makes entity always face the observer.
        poseStack.mulPose(Axis.YP.rotationDegrees(ROTATION_DEGREES));//Fix the sprite mirror issue.
        poseStack.scale(TORCH_SCALE, TORCH_SCALE, TORCH_SCALE);
        
        //Set entity's RenderType to NoCull, preventing rendering issue.
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        
        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f poseMatrix = lastPose.pose();
        
        createVertex(vertexConsumer, poseMatrix, lastPose, packedLight, 0.0F, 0, 0, 1);
        createVertex(vertexConsumer, poseMatrix, lastPose, packedLight, 1.0F, 0, 1, 1);
        createVertex(vertexConsumer, poseMatrix, lastPose, packedLight, 1.0F, 1, 1, 0);
        createVertex(vertexConsumer, poseMatrix, lastPose, packedLight, 0.0F, 1, 0, 0);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
    
    /**
     * The method which <b>create vertexes for rendering</b>, and <b>set the properties at the same time</b>.
     */
    protected static void createVertex(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose lastPose, int lightmapUV, float x, int y, int u, int v)
    {
        consumer.addVertex(pose, x - 0.5F, (float) y - 0.25F, 0.0F)
            .setColor(255, 255, 255, 255)
            .setUv((float) u, (float) v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(lightmapUV)
            .setNormal(lastPose, 0.0F, 1.0F, 0.0F);
    }
    
    /**
     * The method which <b>returns the location of the sprite which the renderer will use</b>.
     */
    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T entity)
    {
        final int TEXTURE_ANIMATION_INDEX = entity.tickCount / ANIMATION_DURATION_TICKS % ANIMATION_FRAMES_IN_TOTAL + TEXTURE_INDEX_CORRECTION_STD;
        
        String finalTexturePath = BASE_TEXTURE_PATH + TEXTURE_NAME;
        
        if(HAS_STATE_VARIATION && entity.getTier() == AbstractThrownTorchEntity.TIER_GONE)
            finalTexturePath += "_" + ALT_TEXTURE_STATE_NAME;
        
        if(HAS_ANIMATION)
            finalTexturePath += "_" + TEXTURE_ANIMATION_INDEX;
        finalTexturePath += TEXTURE_SUFFIX;
        
        return ResourceLocation.fromNamespaceAndPath(NAMESPACE, finalTexturePath);
    }
    //endregion
    
    //  region
    //* Abstract parameter getters
    protected abstract @NotNull String getNamespace();
    protected abstract @NotNull String getTextureName();
    protected abstract @NotNull String getAltTextureName();
    
    protected abstract int getAnimationDurationTicks();
    protected abstract int getTotalAnimationFrames();
    
    protected abstract float getTorchScale();
    
    protected abstract boolean hasAnimation();
    protected abstract boolean hasStateVariation();
    //endregion
}
