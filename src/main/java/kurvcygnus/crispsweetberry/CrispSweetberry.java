package kurvcygnus.crispsweetberry;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.client.registries.CrispCreativeTabs;
import kurvcygnus.crispsweetberry.common.config.CrispConfig;
import kurvcygnus.crispsweetberry.common.features.coins.CoinRegistries;
import kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries;
import kurvcygnus.crispsweetberry.common.registries.CrispBlockEntities;
import kurvcygnus.crispsweetberry.common.registries.CrispBlocks;
import kurvcygnus.crispsweetberry.common.registries.CrispEntities;
import kurvcygnus.crispsweetberry.common.registries.CrispItems;
import kurvcygnus.crispsweetberry.utils.registry.annotations.BanFromTabRegistry;
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
    private static final List<String> ANNOTATIONS = List.of(
        RegisterToTab.class.getName(),
        BanFromTabRegistry.class.getName()
    );
    
    public static List<ModFileScanData.AnnotationData> ANNOTATION_CACHE = List.of();
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    /**
     * @implNote DO NOT replace manual registrations with reflection or something else, 
     * it won't work, I have tired.
     */
    public CrispSweetberry(@NotNull IEventBus eventBus, @NotNull ModContainer modContainer)
    {
        LOGGER.info("Collecting annotation info...");
        final ModFileScanData scanData = modContainer.getModInfo().getOwningFile().getFile().getScanResult();
        
        ANNOTATION_CACHE = scanData.getAnnotations().stream().filter(data -> 
            ANNOTATIONS.contains(data.annotationType().getClassName())
        ).toList();
        
        LOGGER.info("Annotation collection completed!");
        
        modContainer.registerConfig(ModConfig.Type.CLIENT, CrispConfig.SPEC);
        LOGGER.info("Initializing Configurations...");
        
        CrispBlocks.CRISP_BLOCK_REGISTER.register(eventBus);
        LOGGER.info("Registering Misc Blocks...");

        CrispItems.CRISP_ITEM_REGISTER.register(eventBus);
        LOGGER.info("Registering Misc Items...");

        CrispBlockEntities.CRISP_BLOCK_ENTITY_REGISTER.register(eventBus);
        LOGGER.info("Registering Misc Block Entities...");

        CrispCreativeTabs.CRISP_TAB_REGISTER.register(eventBus);
        LOGGER.info("Registering Creative Tab...");

        CrispEntities.CRISP_ENTITY_TYPE_REGISTER.register(eventBus);
        LOGGER.info("Registering Misc Entities...");

        CoinRegistries.REGISTRIES.forEach(registry -> registry.register(eventBus));
        LOGGER.info("Registering Coin Features...");

        KilnRegistries.REGISTRIES.forEach(registry -> registry.register(eventBus));
        LOGGER.info("Registering Kiln Features...");

        LOGGER.info("CrispSweetberry loaded!");
    } 
}