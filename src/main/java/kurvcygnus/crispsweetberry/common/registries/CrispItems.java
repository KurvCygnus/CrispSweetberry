package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.items.ThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.registry.annotations.BanFromTabRegistry;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static kurvcygnus.crispsweetberry.common.misc.items.CoinCollections.*;
import static net.minecraft.world.item.Rarity.RARE;

public final class CrispItems
{
    private CrispItems() { throw new IllegalAccessError(); }
    
    @BanFromTabRegistry
    public static final DeferredRegister<Item> CRISP_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.MOD_ID);
    
    @RegisterToTab
    public static final Holder<Item> THROWABLE_TORCH = CRISP_ITEM_REGISTER.register("throwable_torch", resourceLocation ->
        new ThrowableTorchItem(new Item.Properties())
    );
    
    public static final Holder<Item> HONEY_BERRY = CRISP_ITEM_REGISTER.register("crisp_sweetberry", resourceLocation ->
        new Item(
            new Item.Properties().
                food(new FoodProperties.Builder().
                    nutrition(1).
                    saturationModifier(8.0F).
                    build()
                )
        )
    );
    
    @RegisterToTab
    public static final Holder<Item> CARRY_CRATE = CRISP_ITEM_REGISTER.register("carry_crate", resourceLocation ->
        new BlockItem(CrispBlocks.CARRY_CRATE.value(), new Item.Properties())
    );
    
    @RegisterToTab
    public static final Holder<Item> KILN = CRISP_ITEM_REGISTER.register("kiln", resourceLocation ->
        new BlockItem(CrispBlocks.KILN.value(), new Item.Properties())
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, CopperCoinItem> COPPER_COIN = CRISP_ITEM_REGISTER.register("copper_coin", resourceLocation -> 
        new CopperCoinItem()
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, IronCoinItem> IRON_COIN = CRISP_ITEM_REGISTER.register("iron_coin", resourceLocation -> new IronCoinItem());
    
    @RegisterToTab
    public static final DeferredHolder<Item, GoldCoinItem> GOLD_COIN = CRISP_ITEM_REGISTER.register("gold_coin", resourceLocation -> new GoldCoinItem());
    
    @RegisterToTab
    public static final DeferredHolder<Item, DiamondCoinItem> DIAMOND_COIN = CRISP_ITEM_REGISTER.register("diamond_coin", resourceLocation ->
        new DiamondCoinItem()
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, BlockItem> COPPER_COIN_STACK = CRISP_ITEM_REGISTER.register("copper_coin_stack", resourceLocation ->
        new BlockItem(CrispBlocks.COPPER_COIN_STACK.value(), new Item.Properties()));
    
    @RegisterToTab
    public static final DeferredHolder<Item, BlockItem> IRON_COIN_STACK = CRISP_ITEM_REGISTER.register("iron_coin_stack", resourceLocation ->
        new BlockItem(CrispBlocks.IRON_COIN_STACK.value(), new Item.Properties()));
    
    @RegisterToTab
    public static final DeferredHolder<Item, BlockItem> GOLD_COIN_STACK = CRISP_ITEM_REGISTER.register("gold_coin_stack", resourceLocation ->
        new BlockItem(CrispBlocks.GOLD_COIN_STACK.value(), new Item.Properties()));
    
    @RegisterToTab
    public static final DeferredHolder<Item, BlockItem> DIAMOND_COIN_STACK = CRISP_ITEM_REGISTER.register("diamond_coin_stack", resourceLocation ->
        new BlockItem(CrispBlocks.DIAMOND_COIN_STACK.value(), new Item.Properties()));
    
    public static final Holder<Item> ECHO_DISC = CRISP_ITEM_REGISTER.register("echo_disc", resourceLocation ->
        new Item(new Item.Properties().
            stacksTo(16).
            rarity(RARE)
        )
    );
}
