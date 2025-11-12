package kurvmod.crispsweetberry;

import kurvmod.crispsweetberry.blocks.CrispBlocks;
import kurvmod.crispsweetberry.client.CrispClientEvents;
import kurvmod.crispsweetberry.entities.CrispEntities;
import kurvmod.crispsweetberry.item.CrispItems;
import kurvmod.crispsweetberry.userinterface.CrispCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * The main class of the mod, which functions as the main registry handler.
 */
@Mod(CrispSweetberry.MOD_ID)
public class CrispSweetberry
{
    public static final String MOD_ID = "crispsweetberry";

    public CrispSweetberry(IEventBus eventBus/*, ModContainer modContainer*/)
    {
        CrispBlocks.BLOCK_REGISTER.register(eventBus);
        CrispItems.ITEM_REGISTER.register(eventBus);
        CrispCreativeTabs.TAB_REGISTER.register(eventBus);
        CrispEntities.ENTITY_TYPE_REGISTER.register(eventBus);
        if(FMLEnvironment.dist.isClient())
            new CrispClientEvents(eventBus);
    }
}