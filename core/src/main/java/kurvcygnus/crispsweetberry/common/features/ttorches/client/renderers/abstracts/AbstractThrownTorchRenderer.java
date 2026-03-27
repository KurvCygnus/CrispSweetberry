//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.abstracts;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import kurvcygnus.crispsweetberry.common.features.ttorches.client.events.ThrowableTorchesRendererRegisterEvent;
import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.ThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import kurvcygnus.crispsweetberry.utils.ui.collects.CrispRanger;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.List;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchUtilCollection.*;

/**
 * This is the <b>basic renderer</b> of all <b>thrown torches</b>.<br>
 * It implements custom <b>DOOM-styled</b> muti-direction billboard sprite rendering for thrown torches.
 *
 * @param <T> The thrown torch that it renders.
 * @author Kurv Cygnus
 * @see ThrownTorchRenderer Basic implementation
 * @see ThrowableTorchesRendererRegisterEvent Registry
 * @since 1.0 Release
 */
@OnlyIn(Dist.CLIENT)
public abstract class AbstractThrownTorchRenderer<T extends AbstractThrownTorchEntity> extends EntityRenderer<T>
{
    private static final int HORIZONTAL_SIDE_DIRECTION_INDEX = -1;
    
    private static final CrispRanger FRONT_GROUP_1 = CrispRanger.closed(0, 45);
    private static final CrispRanger FRONT_GROUP_2 = CrispRanger.openClosed(315, 360);
    private static final CrispRanger FLIPPED_FRONT_GROUP = CrispRanger.openClosed(135, 225);
    
    private static final List<CrispRanger> HORIZONTAL_FRONT_RANGERS = List.of(FRONT_GROUP_1, FRONT_GROUP_2, FLIPPED_FRONT_GROUP);
    
    private static final int VERTICAL_TOP_RANGE_INDEX = 0;
    private static final int VERTICAL_BOTTOM_RANGE_INDEX = 1;
    private static final int VERTICAL_UPPER_TILT_RANGE_INDEX = 2;
    private static final int VERTICAL_DOWNER_TILT_RANGE_INDEX = 3;
    private static final int VERTICAL_DIRECT_RANGE_INDEX = 4;
    
    private static final CrispRanger VERTICAL_TOP_RANGE = CrispRanger.closed(45, 90);
    private static final CrispRanger VERTICAL_BOTTOM_RANGE = CrispRanger.closed(-90, 45);
    private static final CrispRanger VERTICAL_UPPER_TILT_RANGE = CrispRanger.closedOpen(15, 45);
    private static final CrispRanger VERTICAL_DOWNER_TILT_RANGE = CrispRanger.openClosed(-45, -15);
    private static final CrispRanger VERTICAL_DIRECT_RANGE = CrispRanger.closed(-15, 15);
    
    private static final List<CrispRanger> VERTICAL_DIRECTION_RANGERS = List.of(
        VERTICAL_TOP_RANGE,
        VERTICAL_BOTTOM_RANGE,
        VERTICAL_UPPER_TILT_RANGE,
        VERTICAL_DOWNER_TILT_RANGE,
        VERTICAL_DIRECT_RANGE
    );
    
    public AbstractThrownTorchRenderer(@NotNull EntityRendererProvider.Context context) { super(context); }
    
    @Override
    public void render(@NotNull T entity, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight)
    {
        final FacingTriple relativeFacing = this.getFacing(entity);
        
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());//* Makes entity always face the observer.
        poseStack.mulPose(Axis.YP.rotationDegrees(ROTATION_DEGREES));
        poseStack.scale(getTorchScale(), getTorchScale(), getTorchScale());
        
        final VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity, relativeFacing)));
        
        final PoseStack.Pose lastPose = poseStack.last();
        final Matrix4f poseMatrix = lastPose.pose();
        
        createVertex(vertexConsumer, poseMatrix, lastPose, packedLight, 0, 0, 0, 1, relativeFacing.flipHorizontal);
        createVertex(vertexConsumer, poseMatrix, lastPose, packedLight, 1, 0, 1, 1, relativeFacing.flipHorizontal);
        createVertex(vertexConsumer, poseMatrix, lastPose, packedLight, 1, 1, 1, 0, relativeFacing.flipHorizontal);
        createVertex(vertexConsumer, poseMatrix, lastPose, packedLight, 0, 1, 0, 0, relativeFacing.flipHorizontal);
        
        poseStack.popPose();
        
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
    
    @Contract("_ -> new")
    private @NotNull AbstractThrownTorchRenderer.FacingTriple getFacing(@NotNull T entity)
    {
        final double relativeX = this.entityRenderDispatcher.camera.getPosition().x - entity.getX();
        final double relativeY = this.entityRenderDispatcher.camera.getPosition().y - entity.getEyeY();
        final double relativeZ = this.entityRenderDispatcher.camera.getPosition().z - entity.getZ();
        
        final float entityYaw = entity.getYRot();
        final double angle = Math.toDegrees(Math.atan2(relativeX, relativeZ));
        int relativeYaw = (int) ((angle - entityYaw + 180D) % 360D);
        
        if(relativeYaw < 0)
            relativeYaw += 360;
        
        final HorizontalFacing horizontalFacing;
        final boolean flipHorizontal;
        
        if(CrispRanger.inRangers(relativeYaw, HORIZONTAL_FRONT_RANGERS) != HORIZONTAL_SIDE_DIRECTION_INDEX)
        {
            horizontalFacing = HorizontalFacing.FRONT;
            flipHorizontal = false;
        }
        else
        {
            horizontalFacing = HorizontalFacing.SIDE;
            flipHorizontal = relativeYaw > 180D;
        }
        
        final VerticalFacing verticalFacing = getVerticalFacing(relativeX, relativeY, relativeZ);
        
        return new FacingTriple(horizontalFacing, verticalFacing, flipHorizontal);
    }
    
    private static @NotNull VerticalFacing getVerticalFacing(double relativeX, double relativeY, double relativeZ)
    {
        final double horizontalDistance = Math.sqrt(relativeX * relativeX + relativeZ * relativeZ);
        final int relativeVerticalDegree = (int) Math.toDegrees(Math.atan2(relativeY, horizontalDistance));
        
        final VerticalFacing verticalFacing;
        
        switch(CrispRanger.inRangers(relativeVerticalDegree, VERTICAL_DIRECTION_RANGERS))
        {
            case VERTICAL_TOP_RANGE_INDEX -> verticalFacing = VerticalFacing.TOP;
            case VERTICAL_BOTTOM_RANGE_INDEX -> verticalFacing = VerticalFacing.BOTTOM;
            case VERTICAL_UPPER_TILT_RANGE_INDEX, VERTICAL_DOWNER_TILT_RANGE_INDEX -> verticalFacing = VerticalFacing.TILT;
            case VERTICAL_DIRECT_RANGE_INDEX -> verticalFacing = VerticalFacing.DIRECT;
            default -> throw new IllegalArgumentException("Get a impossible degree value: %d".formatted(relativeVerticalDegree));
        }
        
        return verticalFacing;
    }
    
    private static void createVertex(@NotNull VertexConsumer consumer, @NotNull Matrix4f pose, @NotNull PoseStack.Pose lastPose,
        int lightmapUV, int x, int y, int u, int v, boolean flip)
            {
                final float correctedU = flip ? (1.0F - (float) u) : (float) u;
                
                consumer.addVertex(pose, x - 0.5F, (float) y - 0.25F, 0.0F).
                    setColor(255, 255, 255, 255).
                    setUv(correctedU, (float) v).
                    setOverlay(OverlayTexture.NO_OVERLAY).
                    setLight(lightmapUV).
                    setNormal(lastPose, 0.0F, 1.0F, 0.0F);
            }
    
    /**
     * New method for get throwable throw sprites. It replaces <u>{@link #getTextureLocation(AbstractThrownTorchEntity)}</u> from <u>{@link EntityRenderer}</u>,
     * as it can't use {@link FacingTriple}.
     */
    protected @NotNull ResourceLocation getTextureLocation(@NotNull T entity, @NotNull AbstractThrownTorchRenderer.FacingTriple pair)
    {
        final StringBuilder path = new StringBuilder(BASE_TEXTURE_PATH).append(getTextureName());
        
        appendTextureName(path, entity, pair);
        
        if(hasStateVariation() && entity.getTier() == AbstractThrownTorchEntity.TIER_GONE)
            path.append("_").append(getAltTextureName());
        
        if(hasAnimation())
        {
            final int index = entity.tickCount / getAnimationDurationTicks() % getTotalAnimationFrames() + TEXTURE_INDEX_CORRECTION_STD;
            path.append("_").append(index);
        }
        
        return CrispDefUtils.getModNamespacedLocation(path.append(TEXTURE_SUFFIX).toString());
    }
    
    protected void appendTextureName(@NotNull StringBuilder path, @NotNull T entity, @NotNull AbstractThrownTorchRenderer.FacingTriple pair)
    {
        path.append("_").append(pair.horizontalFacing().getAlias());
        path.append("_").append(pair.verticalFacing().getAlias());
    }
    
    /**
     * @deprecated This method can't support the demand of thrown torches' muti-direction sprites.<br>
     * Use <u>{@link #getTextureLocation(AbstractThrownTorchEntity, FacingTriple)}</u> instead.
     */
    @Contract(value = "_ -> fail", pure = true) @Override
    @Deprecated(forRemoval = true)//! Can't be removed actually. This is used to tip for misuse with red errors.
    public final @NotNull ResourceLocation getTextureLocation(@NotNull T entity)
    { throw new IllegalStateException("This is not supposed to be used by thrown torch renderers. Use #getTextureLocation(T, FacingTriple) instead."); }
    
    protected abstract @NotNull String getTextureName();
    
    protected @NotNull String getAltTextureName() { return "dark"; }
    
    protected int getAnimationDurationTicks() { return DEFAULT_ANIMATION_DURATION_TICKS; }
    
    protected int getTotalAnimationFrames() { return DEFAULT_ANIMATION_FRAMES_IN_TOTAL; }
    
    protected float getTorchScale() { return STANDARD_TORCH_SCALE; }
    
    protected boolean hasAnimation() { return true; }
    
    protected boolean hasStateVariation() { return true; }
    
    protected enum HorizontalFacing
    {
        FRONT,
        SIDE;
        
        private final String alias;
        
        HorizontalFacing() { this.alias = this.name().toLowerCase(); }
        
        public @NotNull String getAlias() { return this.alias; }
    }
    
    protected enum VerticalFacing
    {
        DIRECT,
        TILT,
        TOP,
        BOTTOM;
        
        private final String alias;
        
        VerticalFacing() { this.alias = this.name().toLowerCase(); }
        
        public @NotNull String getAlias() { return this.alias; }
    }
    
    protected record FacingTriple(@NotNull HorizontalFacing horizontalFacing, @NotNull VerticalFacing verticalFacing, boolean flipHorizontal) { }
}
