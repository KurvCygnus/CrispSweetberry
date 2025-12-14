package kurvmod.crispsweetberry.events.qol;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import static kurvmod.crispsweetberry.misc.CrispKeymap.SPYGLASS_ZOOM;

//WIP
public class SpyglassQuickZoomEvent
{
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        while(SPYGLASS_ZOOM.consumeClick())
        {
            System.out.println("Placeholder");
        }
    }
}
