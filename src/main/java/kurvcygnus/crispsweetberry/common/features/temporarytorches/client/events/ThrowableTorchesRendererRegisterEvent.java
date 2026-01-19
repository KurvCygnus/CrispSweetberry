package kurvcygnus.crispsweetberry.common.features.temporarytorches.client.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.client.renderers.ThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.registries.CrispEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.CrispSweetberry.LOGGER;

/**
 * The class that <b>handles custom entity's renderer registration</b>.
 * @since CSB Release 1.0
 * @author Kurv
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID, value = Dist.CLIENT)
public final class ThrowableTorchesRendererRegisterEvent
{
    @SubscribeEvent
    public static void registerRenderEvents(final EntityRenderersEvent.@NotNull RegisterRenderers event)
    {
        LOGGER.info("Registering EntityRenderers...");
        event.registerEntityRenderer(CrispEntities.THROWN_TORCH.get(), ThrownTorchRenderer::new);
    }
}
