package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries;
import kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * This mixin intercepts the mouse inputs when player is using Spyglass, 
 * which is same as Vanilla.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see Minecraft Source Target(Go to method startUseItem() & startAttack(), they're private and can't be accessed by Javadoc)
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.client.events.SpyglassQuickZoomEvent Client Zoom Implementation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassKeybindScopeInjection Essential Input Emulation
 * @see kurvcygnus.crispsweetberry.common.qol.spyglass.mixins.SpyglassUsePoseInjection Visual Essential Mixin
 * @see SpyglassPayloadHandler#handleData  Serverside stuff
 */
@Mixin(Minecraft.class)
public final class SpyglassItemUsingInjection
{
    @Shadow public MultiPlayerGameMode gameMode;
    
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void interceptInteract(@NotNull CallbackInfo cbi)
    {
        if(SpyglassClientRegistries.SPYGLASS_ZOOM.isDown() && SpyglassQuickZoomEvent.isZooming())
            cbi.cancel();
    }
    
    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void interceptAttack(CallbackInfoReturnable<Boolean> cir)
    {
        if(SpyglassQuickZoomEvent.isZooming())
            cir.setReturnValue(false);
    }
    
    @Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
    private void onContinueAttack(boolean leftClick, CallbackInfo ci)
    {
        if(SpyglassQuickZoomEvent.isZooming())
        {
            if(gameMode != null)
                gameMode.stopDestroyBlock();
            
            ci.cancel();
        }
    }
}
