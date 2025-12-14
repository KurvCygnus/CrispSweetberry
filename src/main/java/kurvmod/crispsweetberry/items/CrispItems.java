package kurvmod.crispsweetberry.items;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.blocks.CrispBlocks;
import kurvmod.crispsweetberry.items.custom.ThrowableTorchItem;
import kurvmod.crispsweetberry.utils.annotations.NoCreativeBus;
import kurvmod.crispsweetberry.utils.annotations.TakeCreativeBus;
import net.minecraft.core.Holder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.world.item.Rarity.RARE;

public final class CrispItems
{
    private CrispItems() {}
    
    @NoCreativeBus
    public static final DeferredRegister<Item> CRISP_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.MOD_ID);

    @TakeCreativeBus
    public static final Holder<Item> THROWABLE_TORCH = CRISP_ITEM_REGISTER.register("throwable_torch", resourceLocation ->
            new ThrowableTorchItem(new Item.Properties()));
    
    public static final Holder<Item> HONEY_BERRY = CRISP_ITEM_REGISTER.register("crisp_sweetberry", resourceLocation ->
        new Item(
            new Item.Properties().
                food(
                    new FoodProperties.Builder().
                    nutrition(1).saturationModifier(8.0F).
                    //TODO
                    // effect().
                    build()
                )
            )
        );
    
    @TakeCreativeBus
    public static Holder<Item> PAPER_BOX = CRISP_ITEM_REGISTER.register("paper_box", resourceLocation ->
        new BlockItem(CrispBlocks.PAPER_BOX.value(), new Item.Properties()));
    
    @TakeCreativeBus
    public static Holder<Item> KILN = CRISP_ITEM_REGISTER.register("kiln", resourceLocation ->
        new BlockItem(CrispBlocks.KILN.value(), new Item.Properties()));
    
    public static final Holder<Item> ECHO_DISC = CRISP_ITEM_REGISTER.register("echo_disc", resourceLocation ->
        new Item(new Item.Properties().
            stacksTo(16).
            rarity(RARE)
        )
    );
    
    public static final Holder<Item> GREEDY_CRYSTAL = CRISP_ITEM_REGISTER.register("greedy_crystal", resourceLocation ->
        new Item(new Item.Properties().
            rarity(RARE)
        )
    );
}
