package kurvmod.crispsweetberry.events.init;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.entities.CrispEntities;
import kurvmod.crispsweetberry.entities.custom.throwntorch.ThrownTorchRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import static kurvmod.crispsweetberry.CrispSweetberry.LOGGER;

/**
 * The class that <b>handles custom entity's renderer registration</b>.
 * @since CSB Release 1.0
 * @author Kurv
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID, value = Dist.CLIENT)
public final class EntityRendererRegisterEvent
{
    @SubscribeEvent
    public static void registerRenderEvents(EntityRenderersEvent.RegisterRenderers event)
    {
        LOGGER.info("Registering EntityRenderers...");
        event.registerEntityRenderer(CrispEntities.THROWN_TORCH.get(), ThrownTorchRenderer::new);
    }
}
