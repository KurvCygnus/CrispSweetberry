package kurvcygnus.crispsweetberry.common.qol.spyglass.mixins;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemInHandRenderer.class)
public final class SpyglassItemDegreeFixInjection
{
    @Unique private static final MarkLogger _$LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "CRISP_MIXIN");
    
    
}
