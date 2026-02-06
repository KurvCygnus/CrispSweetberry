package kurvcygnus.crispsweetberry.client.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

/**
 * @see kurvcygnus.crispsweetberry.client.init.CrispCreativeTabsRegistryEvent#tabRegistryEvent(BuildCreativeModeTabContentsEvent)  Content addition implementation
 */
public enum CrispCreativeTabs implements IRegistrant
{
    INSTANCE;
    
    @Override
    public void register(@NotNull IEventBus bus) { CRISP_TAB_REGISTER.register(bus); }
    
    @Override
    public boolean isFeature() { return false; }
    
    @Override
    public @NotNull String getJob() { return "Creative Tab"; }
    
    @Override
    public int getPriority() { return 5; }
    
    public static final DeferredRegister<CreativeModeTab> CRISP_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrispSweetberry.NAMESPACE);
    
    public static final ResourceKey<CreativeModeTab> CRISP_CREATIVE_TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
        CrispDefUtils.getModNamespacedLocation("crisp_tab")
    );
    
    @SuppressWarnings("unused")
    public static final Holder<CreativeModeTab> CSB_TAB = CRISP_TAB_REGISTER.register("crisp_tab", CreativeModeTab.builder().
        title(Component.translatable("crispsweetberry.creativetab.tabtitle")).
        withTabsBefore(CreativeModeTabs.COMBAT).
        icon(() -> new ItemStack(Items.SWEET_BERRIES))::build
    );
}
