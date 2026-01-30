package kurvcygnus.crispsweetberry.common.features.coins.events;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinTypes;
import kurvcygnus.crispsweetberry.utils.misc.CrispLogUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.function.Supplier;

//? TODO: Compatibility for muti nuggets

/**
 * This event checks the validation of all <u>{@link VanillaCoinTypes}</u>' nugget item, 
 * and also check whether <u>{@link VanillaCoinTypes#COPPER Copper}</u> and <u>{@link VanillaCoinTypes#DIAMOND diamond}</u> coins 
 * should exist.
 * @since Release 1.0
 * @author Kurv Cygnus
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID)
public final class NuggetItemCheckEvent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static Supplier<Item> copperNugget = () -> Items.AIR;
    public static Supplier<Item> diamondNugget = () -> Items.AIR;
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onTagsUpdated(@NotNull TagsUpdatedEvent event)
    {
        if(!event.shouldUpdateStaticData())
            return;
        
        LOGGER.debug("Tags updated! Start searching copper & diamond's nugget...");
        
        final Registry<Item> itemRegistry = event.getRegistryAccess().registryOrThrow(Registries.ITEM);
        
        final Optional<HolderSet.Named<Item>> nuggetTag = itemRegistry.getTag(Tags.Items.NUGGETS);
        
        nuggetTag.ifPresent(holders ->
            {
                copperNugget = () -> Items.AIR;
                diamondNugget = () -> Items.AIR;
                
                for(var holder: holders)
                {
                    if(holder.getKey() == null)
                        continue;
                    
                    ResourceLocation location = holder.getKey().registry();
                    String path = location.getPath();
                    
                    if(path.contains("copper_nugget"))
                        copperNugget = holder::value;
                    else if(path.contains("diamond_nugget"))
                        diamondNugget = holder::value;
                }
            }
        );
        
        LOGGER.info("Validating CoinType nugget tags...");
        
        try
        {
            for(var type: VanillaCoinTypes.VALUES)
            {
                final ItemStack nuggetStack = type.nuggetItem().getDefaultInstance();
                
                CrispLogUtils.logIf(!nuggetStack.is(Tags.Items.NUGGETS), () ->
                    LOGGER.warn("Invalid definition for {}: Item {} is not in the Nuggets tag!",
                        type.id().toUpperCase(), nuggetStack.getItemHolder().getRegisteredName()
                    )
                );
            }
        }
        catch(IllegalArgumentException e) { LOGGER.error("CoinType Validation Failed: {}", e.getMessage()); }
        
        LOGGER.debug("CoinType validation completed.");
    }
}
