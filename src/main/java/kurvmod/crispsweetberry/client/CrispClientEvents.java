package kurvmod.crispsweetberry.client;

import kurvmod.crispsweetberry.entities.CrispEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class CrispClientEvents
{
    public CrispClientEvents(IEventBus bus) { bus.addListener(this::registerRenderEvents); }
    
    private void registerRenderEvents(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(CrispEntities.THROWN_TORCH.get(), ThrownItemRenderer::new);
    }
}
