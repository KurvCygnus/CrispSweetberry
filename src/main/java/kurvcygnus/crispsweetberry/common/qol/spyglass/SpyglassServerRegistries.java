package kurvcygnus.crispsweetberry.common.qol.spyglass;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayload;
import kurvcygnus.crispsweetberry.common.qol.spyglass.server.sync.SpyglassPayloadHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public final class SpyglassServerRegistries
{
    private SpyglassServerRegistries() {}
    
    @SubscribeEvent
    static void registerPacket(@NotNull RegisterPayloadHandlersEvent event)
    {
        final PayloadRegistrar registrar = event.registrar("1.0 Release");
        
        registrar.playToServer(
            SpyglassPayload.TYPE,
            SpyglassPayload.CODEC,
            SpyglassPayloadHandler::handleData
        );
    }
}
