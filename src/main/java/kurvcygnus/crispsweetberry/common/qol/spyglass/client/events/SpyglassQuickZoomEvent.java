package kurvcygnus.crispsweetberry.common.qol.spyglass.client.events;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.SpyglassPayload;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import static kurvcygnus.crispsweetberry.common.qol.spyglass.SpyglassClientRegistries.SPYGLASS_ZOOM;

@EventBusSubscriber(modid = CrispSweetberry.ID)
final class SpyglassQuickZoomEvent
{
    private static boolean wasPressed = false;
    private static boolean isQuickZooming = false;
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @SubscribeEvent
    static void onClientTick(@NotNull ClientTickEvent.Post event) throws IllegalAccessException
    {
        final Minecraft minecraft = Minecraft.getInstance();
        final LocalPlayer player = minecraft.player;
        
        if(player == null)
            return;
        
        final boolean isDown = SPYGLASS_ZOOM.isDown();
        
        if(isDown && !wasPressed)
        {
            LOGGER.debug("[SPYGLASS_ZOOM] Hotkey pressed. Starting quick zooming.");
            PacketDistributor.sendToServer(new SpyglassPayload(true));
            isQuickZooming = true;
        }
        else if(!isDown && wasPressed)
        {
            LOGGER.debug("[SPYGLASS_ZOOM] Hotkey unleashed. Finishing quick zooming.");
            PacketDistributor.sendToServer(new SpyglassPayload(false));
            isQuickZooming = false;
            player.stopUsingItem();
        }
        
        if(isDown && isQuickZooming && player.getOffhandItem().is(Items.SPYGLASS))
        {
            CrispFunctionalUtils.throwIf(minecraft.gameMode == null, () -> new IllegalAccessException("Minecraft instance doesn't exist."));
            
            assert minecraft.gameMode != null;
            
            LOGGER.debug("[SPYGLASS_ZOOM] Quick zooming. Currently is{} using item.", player.isUsingItem() ? "" : "n't");
            
            if(!player.isUsingItem())
                player.startUsingItem(InteractionHand.OFF_HAND);
        }
        
        wasPressed = isDown;
    }
}
