package kurvcygnus.crispsweetberry;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.client.registries.CrispCreativeTabs;
import kurvcygnus.crispsweetberry.client.registries.CrispMenus;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.common.registries.*;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.List;

@Mod(CrispSweetberry.MOD_ID)
public final class CrispSweetberry
{
    public static final String MOD_ID = "crispsweetberry";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String REGISTER_TO_TAB_ANNOTATION_PACKAGE = RegisterToTab.class.getName();
    
    public static List<ModFileScanData.AnnotationData> ANNOTATION_CACHE = List.of();
    
    public CrispSweetberry(IEventBus eventBus, @NotNull ModContainer modContainer)
    {
        LOGGER.info("Collecting annotation info...");
        final ModFileScanData scanData = modContainer.getModInfo().getOwningFile().getFile().getScanResult();
        
        ANNOTATION_CACHE = scanData.getAnnotations().stream().
            filter(data -> data.annotationType().getClassName().equals(REGISTER_TO_TAB_ANNOTATION_PACKAGE)).
            toList();
        
        modContainer.registerConfig(ModConfig.Type.CLIENT, CrispConfig.SPEC);
        LOGGER.info("Initializing Configurations...");
        
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