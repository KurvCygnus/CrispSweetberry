package kurvcygnus.crispsweetberry.common.qol.spyglass.client.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.SpyglassPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries.SPYGLASS_ZOOM;

@EventBusSubscriber(modid = CrispSweetberry.MOD_ID)
final class SpyglassQuickZoomEvent
{
    private static boolean wasPressed = false;
    
    @SubscribeEvent
    static void onClientTick(@NotNull ClientTickEvent.Post event)
    {
        final LocalPlayer player = Minecraft.getInstance().player;
        if(player == null) 
            return;
        
        final boolean isDown = SPYGLASS_ZOOM.isDown();
        
        if(isDown && !wasPressed)
            PacketDistributor.sendToServer(new SpyglassPayload(true));
        else if(!isDown && wasPressed)
            PacketDistributor.sendToServer(new SpyglassPayload(false));
        
        wasPressed = isDown;
    }
}
