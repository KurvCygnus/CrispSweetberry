package kurvmod.crispsweetberry.item;

import kurvmod.crispsweetberry.blocks.Blocks;
import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.item.custom.ThrowableTorchItem;
import kurvmod.crispsweetberry.item.custom.TransmogWandItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.minecraft.world.item.Rarity.RARE;

public class Items {
    public static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.MOD_ID);

    public static Holder<Item> THROWABLE_TORCH = ITEM_REGISTER.register("throwable_torch", resourceLocation ->
            new ThrowableTorchItem(new Item.Properties()));
    
    public static Holder<Item> PAPER_BOX = ITEM_REGISTER.register("paper_box", resourceLocation ->
        new BlockItem(Blocks.PAPER_BOX.value(), new Item.Properties()));
    
    public static Holder<Item> KILN = ITEM_REGISTER.register("kiln", resourceLocation ->
        new BlockItem(Blocks.KILN.value(), new Item.Properties()));
    
    public static Holder<Item> TRANSMOG_WAND = ITEM_REGISTER.register("transmog_wand", resourceLocation ->
        new TransmogWandItem(new Item.Properties().
            durability(64).
            stacksTo(1).
            setNoRepair().
            rarity(RARE)));
    
    public static Holder<Item> ECHO_DISC = ITEM_REGISTER.register("echo_disc", resourceLocation ->
        new Item(new Item.Properties().
            stacksTo(16).
            rarity(RARE)));
    
    //TODO:I'll finish these commented stuffs when I could.
//    public static Holder<Item> SCYTHE = ITEM_REGISTER.register("scythe", resourceLocation ->
//        new Item(new Item.Properties().
//            rarity(RARE).
//            durability(84)));
    
//    public static Holder<Item> ADVENTURERS_ATLAS = ITEM_REGISTER.register("adventurers_atlas", resourceLocation ->
//        new MapItem(new Item.Properties().
//            stacksTo(1)));
    
    public static Holder<Item> GREEDY_CRYSTAL = ITEM_REGISTER.register("greedy_crystal", resourceLocation ->
        new Item(new Item.Properties().
            rarity(RARE)));
}
