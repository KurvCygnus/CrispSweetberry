package kurvcygnus.crispsweetberry.common.features.kiln.client.events;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.registries.CrispItems;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock.LIT_PROPERTY;

/**
 * This event make the sprite of <u>{@link CrispItems#KILN Kiln Item}</u> change with
 * <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock#LIT_PROPERTY LIT_PROPERTY}</u>,
 * which is kept even after the block is destroyed and dropped the corresponded item.
 *
 * @author Kurv Cygnus
 * @see kurvcygnus.crispsweetberry.common.features.kiln.KilnBlock Usage
 * @since 1.0 Release
 */
@EventBusSubscriber(modid = CrispSweetberry.MOD_ID, value = Dist.CLIENT)
final class KilnItemDynamicSpriteSetupEvent
{
    @SubscribeEvent
    public static void SpriteBoundEvent(@NotNull FMLClientSetupEvent event)
    {
        event.enqueueWork(() ->
            ItemProperties.register(
                CrispItems.KILN.value(),
                CrispDefUtils.getModNamespacedLocation("lit"),
                (stack, level, entity, seed) ->
                {
                    CustomData data = stack.get(DataComponents.CUSTOM_DATA);
                    if(data != null && data.contains(LIT_PROPERTY))
                        return data.copyTag().getBoolean(LIT_PROPERTY) ? 1.0F : 0.0F;
                    
                    return 1.0F;
                }
            )
        );
    }
}
