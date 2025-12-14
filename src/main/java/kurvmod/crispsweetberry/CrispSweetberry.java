package kurvmod.crispsweetberry;

import com.mojang.logging.LogUtils;
import kurvmod.crispsweetberry.blockentities.CrispBlockEntities;
import kurvmod.crispsweetberry.blocks.CrispBlocks;
import kurvmod.crispsweetberry.entities.CrispEntities;
import kurvmod.crispsweetberry.items.CrispItems;
import kurvmod.crispsweetberry.ui.CrispCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CrispSweetberry.MOD_ID)
public final class CrispSweetberry
{
    public static final String MOD_ID = "crispsweetberry";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    /*
     *         ___________
     *       /|           |\
     *      | |  O    O  | |
     *      | |___________| |
     *       \_O_O_O_O_O_/
     *       Eventbus =O
     *       OMG this is stupid LOL
     */
    
    public CrispSweetberry(IEventBus eventBus/*, ModContainer modContainer*/)
    {
        CrispBlocks.CRISP_BLOCK_REGISTER.register(eventBus);
        LOGGER.info("Registering Blocks...");
        
        CrispBlockEntities.CRISP_BLOCK_ENTITY_REGISTER.register(eventBus);
        LOGGER.info("Registering Block Entities...");
        
        CrispItems.CRISP_ITEM_REGISTER.register(eventBus);
        LOGGER.info("Registering Items...");
        
        CrispCreativeTabs.CRISP_TAB_REGISTER.register(eventBus);
        LOGGER.info("Registering Creative Tab...");
        
        CrispEntities.CRISP_ENTITY_TYPE_REGISTER.register(eventBus);
        LOGGER.info("Registering Entities...");
        
        LOGGER.info("CrispSweetberry loaded!");
    }
}