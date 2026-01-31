package kurvcygnus.crispsweetberry.common.features.kiln.client.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.jetbrains.annotations.NotNull;

/**
 * A simple event to bound <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu menu}</u> and <u>{@link KilnScreen}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID, value = Dist.CLIENT)
final class KilnMenuScreenBoundEvent
{
    @SubscribeEvent
    static void registerKilnScreen(final @NotNull RegisterMenuScreensEvent event) { event.register(KilnRegistries.KILN_MENU.get(), KilnScreen::new); }
}
