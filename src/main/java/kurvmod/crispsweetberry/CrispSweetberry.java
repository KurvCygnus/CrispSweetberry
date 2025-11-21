package kurvmod.crispsweetberry;

import com.mojang.logging.LogUtils;
import kurvmod.crispsweetberry.blocks.CrispBlocks;
import kurvmod.crispsweetberry.client.CrispClientEvents;
import kurvmod.crispsweetberry.entities.CrispEntities;
import kurvmod.crispsweetberry.items.CrispItems;
import kurvmod.crispsweetberry.userinterface.CrispCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;

//OPTIMIZE NEEDED: It's only a matter of time that I have to rewrite a universal register manager.
//PROTOTYPE OK
/**
 * <b>The main class of the mod, which functions as the main registry handler, for now.
 */
@Mod(CrispSweetberry.MOD_ID)
public class CrispSweetberry
{
    public static final String MOD_ID = "crispsweetberry";
    
    public static final Logger LOGGER = LogUtils.getLogger();
    
    /*
               ___________
             /|           |\
            | |  O    O  | |
            | |___________| |
             \_O_O_O_O_O_/
             Eventbus =O
     */
    
    public CrispSweetberry(IEventBus eventBus/*, ModContainer modContainer*/)
    {
        CrispBlocks.CRISP_BLOCK_REGISTER.register(eventBus);
        CrispItems.CRISP_ITEM_REGISTER.register(eventBus);
        CrispCreativeTabs.CRISP_TAB_REGISTER.register(eventBus);
        CrispEntities.CRISP_ENTITY_TYPE_REGISTER.register(eventBus);
        if(FMLEnvironment.dist.isClient())
            new CrispClientEvents(eventBus);
        LOGGER.info("CrispSweetberry Loaded!");
    }
}