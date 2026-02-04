package kurvcygnus.crispsweetberry.common.qol.spyglass.client.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.SpyglassPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries.SPYGLASS_ZOOM;

@EventBusSubscriber(modid = CrispSweetberry.ID)
final class SpyglassQuickZoomEvent
{
    private static ZoomState zoomState = ZoomState.IDLE;
    
    enum ZoomState
    {
        IDLE,
        ZOOMING,
        RELEASED
    }
    
    @SuppressWarnings("StatementWithEmptyBody")//! Empty while statement makes sure that "use" flag won't be leaked.
    @SubscribeEvent
    static void spyglassZoom(@NotNull ClientTickEvent.Post event)
    {
        final Minecraft instance = Minecraft.getInstance();
        final LocalPlayer player = instance.player;
        
        if(instance.gameMode == null || player == null)
            return;
        
        final boolean hotkeyPressed = SPYGLASS_ZOOM.isDown();
        
        if(hotkeyPressed)
            switch(zoomState)
            {
                case IDLE ->
                {
                    instance.options.keyUse.setDown(true);
                    PacketDistributor.sendToServer(new SpyglassPayload(true));
                    instance.gameMode.useItem(player, InteractionHand.OFF_HAND);
                    zoomState = ZoomState.ZOOMING;
                }
                case ZOOMING -> {}
                case RELEASED -> zoomState = ZoomState.IDLE;
            }
        else 
            switch(zoomState)
            {
                case ZOOMING ->
                {
                    zoomState = ZoomState.RELEASED;
                    instance.options.keyUse.setDown(false);
                    PacketDistributor.sendToServer(new SpyglassPayload(false));
                    
                    while(SPYGLASS_ZOOM.consumeClick()) {}
                }
                case RELEASED -> zoomState = ZoomState.IDLE;
                default -> {}
            }
    }
}
