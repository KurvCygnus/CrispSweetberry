package kurvcygnus.crispsweetberry.common.features.coins;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.coins.datagen.IsCrunchingCondition;
import kurvcygnus.crispsweetberry.common.features.coins.datagen.SetCoinCountFunction;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinItem;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinStackItem;
import kurvcygnus.crispsweetberry.utils.registry.annotations.BanFromTabRegistry;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

import static kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinTypes.*;

/**
 * Everything that is related to coin is registered at here.
 * @since Release 1.0
 */
public final class CoinRegistries
{
    private CoinRegistries() { throw new IllegalAccessError(); }
    
    @BanFromTabRegistry private static final DeferredRegister<Item> COIN_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.MOD_ID);
    @BanFromTabRegistry private static final DeferredRegister<Block> COIN_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.MOD_ID);
    @BanFromTabRegistry private static final DeferredRegister<LootItemConditionType> COIN_LOOT_TABLE_CONDITION_REGISTER = DeferredRegister.create(
        Registries.LOOT_CONDITION_TYPE, CrispSweetberry.MOD_ID
    );
    @BanFromTabRegistry private static final DeferredRegister<LootItemFunctionType<?>> COIN_LOOT_TABLE_FUNCTION_REGISTER = DeferredRegister.create(
        Registries.LOOT_FUNCTION_TYPE, CrispSweetberry.MOD_ID
    );
    
    @BanFromTabRegistry public static final List<DeferredRegister<?>> REGISTRIES = List.of(
        COIN_ITEM_REGISTER,
        COIN_BLOCK_REGISTER,
        COIN_LOOT_TABLE_CONDITION_REGISTER,
        COIN_LOOT_TABLE_FUNCTION_REGISTER
    );
    
    @BanFromTabRegistry public static final Holder<LootItemConditionType> IS_CRUNCHING_CONDITION = COIN_LOOT_TABLE_CONDITION_REGISTER.register("is_crunching", () ->
        new LootItemConditionType(IsCrunchingCondition.CODEC)
    );
    
    @BanFromTabRegistry public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetCoinCountFunction>> SET_COIN_COUNT_FUNCTION = COIN_LOOT_TABLE_FUNCTION_REGISTER.register
        ("set_coin_count", () -> new LootItemFunctionType<>(SetCoinCountFunction.CODEC)
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, VanillaCoinItem> COPPER_COIN = COIN_ITEM_REGISTER.register("copper_coin", resourceLocation ->
        new VanillaCoinItem(() -> COPPER)
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, VanillaCoinItem> IRON_COIN = COIN_ITEM_REGISTER.register("iron_coin", resourceLocation ->
        new VanillaCoinItem(() -> IRON)
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, VanillaCoinItem> GOLD_COIN = COIN_ITEM_REGISTER.register("gold_coin", resourceLocation ->
        new VanillaCoinItem(() -> GOLD)
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, VanillaCoinItem> DIAMOND_COIN = COIN_ITEM_REGISTER.register("diamond_coin", resourceLocation ->
        new VanillaCoinItem(() -> DIAMOND)
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, VanillaCoinStackItem> COPPER_COIN_STACK = COIN_ITEM_REGISTER.register("copper_coin_stack", resourceLocation ->
        new VanillaCoinStackItem(() -> COPPER)
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, VanillaCoinStackItem> IRON_COIN_STACK = COIN_ITEM_REGISTER.register("iron_coin_stack", resourceLocation ->
        new VanillaCoinStackItem(() -> IRON)
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, VanillaCoinStackItem> GOLD_COIN_STACK = COIN_ITEM_REGISTER.register("gold_coin_stack", resourceLocation ->
        new VanillaCoinStackItem(() -> GOLD)
    );
    
    @RegisterToTab
    public static final DeferredHolder<Item, VanillaCoinStackItem> DIAMOND_COIN_STACK = COIN_ITEM_REGISTER.register("diamond_coin_stack", resourceLocation ->
        new VanillaCoinStackItem(() -> DIAMOND)
    );
    
    @BanFromTabRegistry
    public static final DeferredHolder<Block, VanillaCoinStackBlock> COPPER_COIN_STACK_BLOCK = COIN_BLOCK_REGISTER.register("copper_coin_stack", resourceLocation ->
        new VanillaCoinStackBlock(Lazy.of(() -> COPPER))
    );
    
    @BanFromTabRegistry
    public static final DeferredHolder<Block, VanillaCoinStackBlock> IRON_COIN_STACK_BLOCK = COIN_BLOCK_REGISTER.register("iron_coin_stack", resourceLocation ->
        new VanillaCoinStackBlock(Lazy.of(() -> IRON))
    );
    
    @BanFromTabRegistry
    public static final DeferredHolder<Block, VanillaCoinStackBlock> GOLD_COIN_STACK_BLOCK = COIN_BLOCK_REGISTER.register("gold_coin_stack", resourceLocation ->
        new VanillaCoinStackBlock(Lazy.of(() -> GOLD))
    );
    
    @BanFromTabRegistry
    public static final DeferredHolder<Block, VanillaCoinStackBlock> DIAMOND_COIN_STACK_BLOCK = COIN_BLOCK_REGISTER.register("diamond_coin_stack", resourceLocation ->
        new VanillaCoinStackBlock(Lazy.of(() -> DIAMOND))
    );
}