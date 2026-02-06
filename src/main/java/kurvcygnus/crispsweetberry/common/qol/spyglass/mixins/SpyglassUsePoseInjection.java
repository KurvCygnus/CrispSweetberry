package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public final class SpyglassUsePoseInjection
{
    @Unique @Final private static final MarkLogger _$LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CRISP_MIXIN");
    
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)//? TODO: Changed pose to Bow??? Figure out this shit tomorrow
    private static void armPoseInject(CallbackInfoReturnable<HumanoidModel.ArmPose> callback)
    {
        if(SpyglassClientRegistries.SPYGLASS_ZOOM.isDown())
        {
            _$LOGGER.info("Pose changed.");
            callback.setReturnValue(HumanoidModel.ArmPose.SPYGLASS);
        }
    }
}
