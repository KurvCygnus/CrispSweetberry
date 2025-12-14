package kurvmod.crispsweetberry.ui;

import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CrispMenus
{
    public static final DeferredRegister<MenuType<?>> CRISP_MENU_REGISTER = DeferredRegister.create(Registries.MENU, CrispSweetberry.MOD_ID);
}
