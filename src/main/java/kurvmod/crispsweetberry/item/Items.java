package kurvmod.crispsweetberry.item;

import kurvmod.crispsweetberry.blocks.Blocks;
import kurvmod.crispsweetberry.CrispSweetberry;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Items {
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.MOD_ID);

    public static Holder<Item> TEMPORARY_TORCH = ITEM_REGISTER.register("throwable_torch", (resourceLocation) ->
            new BlockItem(Blocks.TEMPORARY_TORCH.value(), new Item.Properties()));
    
    public static Holder<Item> PAPER_BOX = ITEM_REGISTER.register("paper_box", (resourceLocation) ->
        new BlockItem(Blocks.PAPER_BOX.value(), new Item.Properties()));
    
    public static Holder<Item> KILN = ITEM_REGISTER.register("kiln", (resourceLocation) ->
        new BlockItem(Blocks.KILN.value(), new Item.Properties()));
    
    
}
