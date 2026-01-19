package kurvcygnus.crispsweetberry;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.client.registries.CrispCreativeTabs;
import kurvcygnus.crispsweetberry.client.registries.CrispMenus;
import kurvcygnus.crispsweetberry.common.registries.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(CrispSweetberry.MOD_ID)
public final class CrispSweetberry
{
    public static final String MOD_ID = "crispsweetberry";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public CrispSweetberry(IEventBus eventBus/*, ModContainer modContainer*/)
    {
        CrispBlocks.CRISP_BLOCK_REGISTER.register(eventBus);
        LOGGER.info("Registering Blocks...");
        
        CrispItems.CRISP_ITEM_REGISTER.register(eventBus);
        LOGGER.info("Registering Items...");
        
        CrispBlockEntities.CRISP_BLOCK_ENTITY_REGISTER.register(eventBus);
        LOGGER.info("Registering Block Entities...");
        
        CrispMenus.CRISP_MENU_REGISTER.register(eventBus);
        LOGGER.info("Registering Menus...");
        
        CrispCreativeTabs.CRISP_TAB_REGISTER.register(eventBus);
        LOGGER.info("Registering Creative Tab...");
        
        CrispEntities.CRISP_ENTITY_TYPE_REGISTER.register(eventBus);
        LOGGER.info("Registering Entities...");
        
        CrispRecipes.CRISP_RECIPE_REGISTER.register(eventBus);
        CrispRecipes.CRISP_SERIALIZER_REGISTER.register(eventBus);
        LOGGER.info("Registering Recipes...");
        
        LOGGER.info("CrispSweetberry loaded!");
    }
}