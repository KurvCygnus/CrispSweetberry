package kurvmod.crispsweetberry.entities.custom.throwntorch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import static kurvmod.crispsweetberry.CrispSweetberry.MOD_ID;

//PROTOTYPE OK
//WIP

/**
 * A <b>custom renderer for ThrownTorchEntity</b>, which allows to <b>break the limitation of ThrownItemRenderer</b>.<br>
 * Still work in progress.
 */
public class ThrownTorchRenderer extends EntityRenderer<ThrownTorchEntity>
{
    //Constants
    private static final String TEXTURE_PATH = "textures/entity/thrown_torch";
    private static final String TEXTURE_DARK_STATE = "_dark";
    private static final String TEXTURE_SUFFIX = ".png";
    
    private static final float TORCH_SCALE = 0.5F;
    private static final float ROTATION_DEGREES = 180.0F;
    
    private static final int ANIMATION_DURATION_TICKS = 1;
    private static final int ANIMATION_FRAMES_IN_TOTAL = 8;
    private static final int TEXTURE_INDEX_CORRECTION_STD = 1;
    
    /**
     * The <b>construct method</b> which <b>provides context for rendering</b>.
     */
    public ThrownTorchRenderer(EntityRendererProvider.Context context) {super(context);}
    
    @Override
    public boolean shouldRender(@NotNull ThrownTorchEntity livingEntity, @NotNull Frustum camera, double camX, double camY, double camZ)
    {
        return super.shouldRender(livingEntity, camera, camX, camY, camZ);
    }
    
    @Override
    public void render(@NotNull ThrownTorchEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight)
    {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());//Makes entity always face the observer.
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
    private static void createVertex(VertexConsumer consumer, Matrix4f pose, PoseStack.Pose lastPose, int lightmapUV, float x, int y, int u, int v)
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
    public @NotNull ResourceLocation getTextureLocation(@NotNull ThrownTorchEntity entity)
    {
        int TEXTURE_ANIMATION_INDEX = entity.externalTickCount / ANIMATION_DURATION_TICKS % ANIMATION_FRAMES_IN_TOTAL + TEXTURE_INDEX_CORRECTION_STD;
        
        String finalTexturePath = TEXTURE_PATH;
        if(entity.getTier() == ThrownTorchEntity.TIER_GONE)//Only when ThrownTorchEntity has contracted with water, then the method shall return a different sprite.
            finalTexturePath += TEXTURE_DARK_STATE;
        finalTexturePath += "_" + TEXTURE_ANIMATION_INDEX + TEXTURE_SUFFIX;
        
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, finalTexturePath);
    }
}