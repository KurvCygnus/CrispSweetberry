package kurvcygnus.crispsweetberry.common.features.temporarytorches.client.events;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.client.renderers.ThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.registries.CrispEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * The class that <b>handles custom entity's renderer registration</b>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID, value = Dist.CLIENT)
public final class ThrowableTorchesRendererRegisterEvent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    public static void registerRenderEvents(final EntityRenderersEvent.@NotNull RegisterRenderers event)
    {
        LOGGER.info("Registering EntityRenderers...");
        event.registerEntityRenderer(CrispEntities.THROWN_TORCH.get(), ThrownTorchRenderer::new);
    }
}
