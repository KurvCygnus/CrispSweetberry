package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries;
import kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin emulates the spyglass using state.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see Player#isScoping() Source Target
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent Client Zoom Implementation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassItemUsingInjection Essential Input Intercept
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassUsePoseInjection Visual Essential Mixin
 * @see SpyglassPayloadHandler#handleData Serverside stuff
 */
@Mixin(Player.class)
public final class SpyglassKeybindScopeInjection
{
    @Inject(method = "isScoping", at = @At("RETURN"), cancellable = true)
    private void scopeInject(CallbackInfoReturnable<Boolean> cir)
    {
        if(SpyglassClientRegistries.SPYGLASS_ZOOM.isDown() && SpyglassQuickZoomEvent.isZooming())
            cir.setReturnValue(true);
    }
}
