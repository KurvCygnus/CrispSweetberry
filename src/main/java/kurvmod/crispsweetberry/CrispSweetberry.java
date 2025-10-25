package kurvmod.crispsweetberry;

import kurvmod.crispsweetberry.blocks.Blocks;
import kurvmod.crispsweetberry.item.Items;
import kurvmod.crispsweetberry.userinterface.CreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(CrispSweetberry.MOD_ID)
public class CrispSweetberry {
    public static final String MOD_ID = "crispsweetberry";

    public CrispSweetberry(IEventBus eventBus, ModContainer modContainer) {
        Blocks.BLOCK_REGISTER.register(eventBus);
        Items.ITEM_REGISTER.register(eventBus);
        CreativeTabs.TAB_REGISTER.register(eventBus);
    }
}