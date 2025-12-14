package kurvmod.crispsweetberry.ui;

import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * @see kurvmod.crispsweetberry.events.init.CrispCreativeTabsRegistryEvent Content add implementation
 */
public final class CrispCreativeTabs
{
    private CrispCreativeTabs() {}
    
    public static final DeferredRegister<CreativeModeTab> CRISP_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrispSweetberry.MOD_ID);
    
    public static final ResourceKey<CreativeModeTab> CRISP_CREATIVE_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
        ResourceLocation.fromNamespaceAndPath(CrispSweetberry.MOD_ID, "crisp_tab"));

    //* All content registrations are processed in CrispCreativeTabsRegistryEvent.java.
    public static final Holder<CreativeModeTab> CSB_TAB = CRISP_TAB_REGISTER.register("crisp_tab", () ->
        CreativeModeTab.builder().title(Component.translatable("crispsweetberry.tabtitle")).withTabsBefore(CreativeModeTabs.COMBAT).
            icon(() -> new ItemStack(net.minecraft.world.item.Items.SWEET_BERRIES)).build());
}
