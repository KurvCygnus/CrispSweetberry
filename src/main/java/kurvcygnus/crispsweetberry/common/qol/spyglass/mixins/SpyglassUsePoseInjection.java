package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries;
import kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

/**
 * This mixin makes the player who is using spyglass to do corresponded pose.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see PlayerRenderer Source Taget(Go to method getArmPose(), it is private and can't be accessed by Javadoc) 
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent Client Zoom Implementation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassKeybindScopeInjection Essential Input Emulation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemUsingInjection Input Interception Stuff
 * @see SpyglassPayloadHandler#handleData Serverside Stuff
 */
@Mixin(PlayerRenderer.class)
public final class SpyglassUsePoseInjection
{
    @Inject(method = "getArmPose", at = @At("HEAD"), cancellable = true)//? TODO: Item's degree is incorrect.
    private static void armPoseInject(@NotNull AbstractClientPlayer player, @NotNull InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> callback)
    {
        if(Objects.equals(hand, InteractionHand.OFF_HAND) && SpyglassClientRegistries.SPYGLASS_ZOOM.isDown() && SpyglassQuickZoomEvent.isZooming())
            callback.setReturnValue(HumanoidModel.ArmPose.SPYGLASS);
    }
}
