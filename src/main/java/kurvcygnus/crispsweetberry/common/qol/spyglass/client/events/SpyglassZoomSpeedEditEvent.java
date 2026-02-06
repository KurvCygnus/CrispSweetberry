package kurvcygnus.crispsweetberry.common.qol.spyglass.client.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE, value = Dist.CLIENT)
public final class SpyglassZoomSpeedEditEvent
{
    private static final float SPYGLASS_MOVEMENT_FACTOR = 0.2F;
    
    @SubscribeEvent
    static void speedEdit(@NotNull MovementInputUpdateEvent event)
    {
        if(!SpyglassQuickZoomEvent.isZooming())
            return;
        
        event.getInput().leftImpulse *= SPYGLASS_MOVEMENT_FACTOR;
        event.getInput().forwardImpulse *= SPYGLASS_MOVEMENT_FACTOR;
        
        if(event.getEntity().isSprinting())
            event.getEntity().setSprinting(false);
    }
}
