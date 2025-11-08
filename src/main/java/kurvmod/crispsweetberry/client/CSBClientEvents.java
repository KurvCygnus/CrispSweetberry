package kurvmod.crispsweetberry.client;

import kurvmod.crispsweetberry.entities.Entities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class CSBClientEvents
{
    public CSBClientEvents(IEventBus bus) { bus.addListener(this::registerRenderEvents); }
    
    private void registerRenderEvents(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(Entities.THROWN_TORCH.get(), ThrownItemRenderer::new);
    }
}
