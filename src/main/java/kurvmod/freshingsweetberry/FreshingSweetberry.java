package kurvmod.freshingsweetberry;

import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(FreshingSweetberry.MOD_ID)
public class FreshingSweetberry {
    public static final String MOD_ID = "freshingsweetberry";

    public FreshingSweetberry(IEventBus eventBus, ModContainer modContainer) {
        Blocks.BLOCK_REGISTER.register(eventBus);
        Items.ITEM_REGISTER.register(eventBus);
        CreativeTabs.TAB_REGISTER.register(eventBus);
    }
}
