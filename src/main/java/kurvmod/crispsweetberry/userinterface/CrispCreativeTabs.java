package kurvmod.crispsweetberry.userinterface;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.blocks.CrispBlocks;
import kurvmod.crispsweetberry.items.CrispItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CrispCreativeTabs
{
    private CrispCreativeTabs() {}
    
    public static final DeferredRegister<CreativeModeTab> CRISP_TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrispSweetberry.MOD_ID);

    public static Holder<CreativeModeTab> FSB_TAB = CRISP_TAB_REGISTER.register("crispsweetberry_tab", () -> CreativeModeTab.builder().title(Component.translatable("crispsweetberry.tabtitle")).withTabsBefore(CreativeModeTabs.COMBAT).
            icon(() -> new ItemStack(net.minecraft.world.item.Items.SWEET_BERRIES)).displayItems(((parameters, output) -> {
                output.accept(CrispItems.THROWABLE_TORCH.value());
                output.accept(new ItemStack(CrispBlocks.KILN.value()));
                output.accept(new ItemStack(CrispBlocks.PAPER_BOX.value()));
                output.accept(CrispItems.TRANSMOG_WAND.value());
                output.accept(CrispItems.ECHO_DISC.value());
                output.accept(CrispItems.GREEDY_CRYSTAL.value());
                output.accept(CrispItems.HONEY_BERRY.value());
            })).build());
}
