package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public final class SpyglassItemUsingInjection
{
    @Unique private static final MarkLogger _$LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CRISP_MIXIN");
    
    @Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
    private void spyglassForceOverrideInput(@NotNull CallbackInfo cbi)
    {
        if(SpyglassClientRegistries.SPYGLASS_ZOOM.isDown())
        {
            _$LOGGER.info("ItemUse canceled.");
            cbi.cancel();
        }
    }
}
