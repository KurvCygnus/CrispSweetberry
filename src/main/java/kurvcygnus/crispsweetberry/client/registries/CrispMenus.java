package kurvcygnus.crispsweetberry.client.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class CrispMenus
{
    public static final DeferredRegister<MenuType<?>> CRISP_MENU_REGISTER = DeferredRegister.create(Registries.MENU, CrispSweetberry.MOD_ID);
    
    public static final Supplier<MenuType<KilnMenu>> KILN_MENU = CRISP_MENU_REGISTER.register("kiln_menu", () ->
        new MenuType<>(KilnMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
