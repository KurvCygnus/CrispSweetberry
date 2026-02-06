package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public final class SpyglassKeybindScopeInjection
{
    @Inject(method = "isScoping", at = @At("RETURN"), cancellable = true)
    private void scopeInject(CallbackInfoReturnable<Boolean> cir)
    {
        if(SpyglassClientRegistries.SPYGLASS_ZOOM.isDown())
            cir.setReturnValue(true);
    }
}
