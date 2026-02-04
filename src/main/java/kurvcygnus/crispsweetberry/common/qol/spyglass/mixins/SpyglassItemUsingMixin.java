package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public final class SpyglassItemUsingMixin
{
    @Unique private static final Logger _$LOGGER = LogUtils.getLogger();
    @Unique private static final Marker _$MARKER = MarkerFactory.getMarker("CRISP_MIXIN");
    
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void spyglassForceOverrideInput(@NotNull CallbackInfo cbi)
    {
        if(SpyglassClientRegistries.SPYGLASS_ZOOM.isDown())
        {
            _$LOGGER.info(_$MARKER, "ItemUse canceled.");
            cbi.cancel();
        }
    }
}
