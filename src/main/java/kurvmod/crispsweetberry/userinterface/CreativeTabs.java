package kurvmod.crispsweetberry.userinterface;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.blocks.Blocks;
import kurvmod.crispsweetberry.item.Items;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TAB_REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CrispSweetberry.MOD_ID);

    public static Holder<CreativeModeTab> FSB_TAB = TAB_REGISTER.register("crispsweetberry_tab", () -> {
        return CreativeModeTab.builder().title(Component.translatable("crispsweetberry.tabtitle")).withTabsBefore(CreativeModeTabs.COMBAT).
                icon(() -> new ItemStack(net.minecraft.world.item.Items.SWEET_BERRIES)).displayItems(((parameters, output) -> {
                    output.accept(Items.THROWABLE_TORCH.value());
                    output.accept(new ItemStack(Blocks.KILN.value()));
                    output.accept(new ItemStack(Blocks.PAPER_BOX.value()));
                    output.accept(Items.TRANSMOG_WAND.value());
                    output.accept(Items.ECHO_DISC.value());
                    output.accept(Items.GREEDY_CRYSTAL.value());
                })).build();
    });
}
