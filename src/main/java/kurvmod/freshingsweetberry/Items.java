package kurvmod.freshingsweetberry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Items {
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.createItems(FreshingSweetberry.MOD_ID);

    public static Holder<Item> TEMPORARY_TORCH = ITEM_REGISTER.register("throwable_torch", (resourceLocation) ->
            new BlockItem(Blocks.TEMPORARY_TORCH.value(), new Item.Properties().setId(ResourceKey.create(Registries.ITEM, resourceLocation))));
}
