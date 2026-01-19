package kurvcygnus.crispsweetberry.client.init;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.client.registries.CrispMenus;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = CrispSweetberry.MOD_ID, value = Dist.CLIENT)
public final class CrispMenuScreenBoundEvent
{
    @SubscribeEvent
    public static void registerScreens(final @NotNull RegisterMenuScreensEvent event)
    {
        event.register(CrispMenus.KILN_MENU.get(), KilnScreen::new);
    }
}
