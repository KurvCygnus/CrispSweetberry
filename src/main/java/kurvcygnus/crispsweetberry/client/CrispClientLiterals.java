package kurvcygnus.crispsweetberry.client;

import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import net.minecraft.network.chat.Component;

public final class CrispClientLiterals
{
    private CrispClientLiterals() { throw new IllegalAccessError("Class \"CrispClientLiterals\" is not meant to be instantized!"); }
    
    @AutoI18n({
        "en_us -> Crisp Sweetberry-In Game Keymappings",
        "lol_us -> TAZTY FRUT IN GAYM BUTTONZ",
        "zh_cn -> 澄莓物语-游戏按键"
    })
    public static final Component CRISP_CONTROL_MENU_CATEGORY = Component.translatable("crispsweetberry.menu.control.title");
}
