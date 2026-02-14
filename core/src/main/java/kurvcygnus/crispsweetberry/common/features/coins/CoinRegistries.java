//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.coins;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.common.features.coins.datagen.IsCrunchingCondition;
import kurvcygnus.crispsweetberry.common.features.coins.datagen.SetCoinCountFunction;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinItem;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinStackBlock;
import kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinStackItem;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static kurvcygnus.crispsweetberry.common.features.coins.vanilla.VanillaCoinTypes.*;

/**
 * Everything that is related to coin is registered at here.
 * @since Release 1.0
 */
public enum CoinRegistries implements IRegistrant
{
    INSTANCE;
    
    @Override
    public void register(@NotNull IEventBus bus) { REGISTRIES.forEach(register -> register.register(bus)); }
    
    @Override
    public boolean isFeature() { return true; }
    
    @Override
    public @NotNull String getJob() { return "Coin"; }
    
    @Override
    public int getPriority() { return 3; }
    
    private static final DeferredRegister<Item> COIN_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<Block> COIN_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<LootItemConditionType> COIN_LOOT_TABLE_CONDITION_REGISTER = DeferredRegister.create(
        Registries.LOOT_CONDITION_TYPE, CrispSweetberry.NAMESPACE
    );
    private static final DeferredRegister<LootItemFunctionType<?>> COIN_LOOT_TABLE_FUNCTION_REGISTER = DeferredRegister.create(
        Registries.LOOT_FUNCTION_TYPE, CrispSweetberry.NAMESPACE
    );
    
    public static final List<DeferredRegister<?>> REGISTRIES = List.of(
        COIN_ITEM_REGISTER,
        COIN_BLOCK_REGISTER,
        COIN_LOOT_TABLE_CONDITION_REGISTER,
        COIN_LOOT_TABLE_FUNCTION_REGISTER
    );
    
    public static final Holder<LootItemConditionType> IS_CRUNCHING_CONDITION = COIN_LOOT_TABLE_CONDITION_REGISTER.register("is_crunching", () ->
        new LootItemConditionType(IsCrunchingCondition.CODEC)
    );
    
    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetCoinCountFunction>> SET_COIN_COUNT_FUNCTION = COIN_LOOT_TABLE_FUNCTION_REGISTER.register
        ("set_coin_count", () -> new LootItemFunctionType<>(SetCoinCountFunction.CODEC)
    );
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Copper Coin",
        "lol_us = Kop rounz thin'",
        "zh_cn = 铜币"
    })
    public static final DeferredHolder<Item, VanillaCoinItem> COPPER_COIN = COIN_ITEM_REGISTER.register("copper_coin", resourceLocation ->
        new VanillaCoinItem(() -> COPPER)
    );
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Iron Coin",
        "lol_us = Airn rounz thin'",
        "zh_cn = 铁币"
    })
    public static final DeferredHolder<Item, VanillaCoinItem> IRON_COIN = COIN_ITEM_REGISTER.register("iron_coin", resourceLocation ->
        new VanillaCoinItem(() -> IRON)
    );
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Gold Coin",
        "lol_us = Shyni rounz thin'",
        "zh_cn = 金币"
    })
    public static final DeferredHolder<Item, VanillaCoinItem> GOLD_COIN = COIN_ITEM_REGISTER.register("gold_coin", resourceLocation ->
        new VanillaCoinItem(() -> GOLD)
    );
    
    @RegisterToTab
    @AutoI18n({
        "en_us = Diamond Coin",
        "lol_us = pez of $$$",
        "zh_cn = 钻石币"
    })
    public static final DeferredHolder<Item, VanillaCoinItem> DIAMOND_COIN = COIN_ITEM_REGISTER.register("diamond_coin", resourceLocation ->
        new VanillaCoinItem(() -> DIAMOND)
    );
    
    @RegisterToTab
    @AutoI18n(value = {
        "en_us = Copper Coin Stack",
        "lol_us = Kop rounz thinz",
        "zh_cn = 铜币堆"
        },
        group = "csb:copper_coin"
    )
    public static final DeferredHolder<Item, VanillaCoinStackItem> COPPER_COIN_STACK = COIN_ITEM_REGISTER.register("copper_coin_stack", resourceLocation ->
        new VanillaCoinStackItem(() -> COPPER)
    );
    
    @RegisterToTab
    @AutoI18n(value = {
        "en_us = Iron Coin Stack",
        "lol_us = Airn rounz thinz",
        "zh_cn = 铁币堆"
        },
        group = "csb:iron_coin"
    )
    public static final DeferredHolder<Item, VanillaCoinStackItem> IRON_COIN_STACK = COIN_ITEM_REGISTER.register("iron_coin_stack", resourceLocation ->
        new VanillaCoinStackItem(() -> IRON)
    );
    
    @RegisterToTab
    @AutoI18n(value = {
        "en_us = Gold Coin Stack",
        "lol_us = OW GOD TOO SHYNI",
        "zh_cn = 金币堆"
        },
        group = "csb:gold_coin"
    )
    public static final DeferredHolder<Item, VanillaCoinStackItem> GOLD_COIN_STACK = COIN_ITEM_REGISTER.register("gold_coin_stack", resourceLocation ->
        new VanillaCoinStackItem(() -> GOLD)
    );
    
    @RegisterToTab
    @AutoI18n(value = {
        "en_us = Diamond Coin Stack",
        "lol_us = Loz of $$$",
        "zh_cn = 钻石币堆"
        },
        group = "csb:diamond_coin"
    )
    public static final DeferredHolder<Item, VanillaCoinStackItem> DIAMOND_COIN_STACK = COIN_ITEM_REGISTER.register("diamond_coin_stack", resourceLocation ->
        new VanillaCoinStackItem(() -> DIAMOND)
    );
    
    @AutoI18n(group = "csb:copper_coin")
    public static final DeferredHolder<Block, VanillaCoinStackBlock> COPPER_COIN_STACK_BLOCK = COIN_BLOCK_REGISTER.register("copper_coin_stack", resourceLocation ->
        new VanillaCoinStackBlock(Lazy.of(() -> COPPER))
    );
    
    @AutoI18n(group = "csb:iron_coin")
    public static final DeferredHolder<Block, VanillaCoinStackBlock> IRON_COIN_STACK_BLOCK = COIN_BLOCK_REGISTER.register("iron_coin_stack", resourceLocation ->
        new VanillaCoinStackBlock(Lazy.of(() -> IRON))
    );
    
    @AutoI18n(group = "csb:gold_coin")
    public static final DeferredHolder<Block, VanillaCoinStackBlock> GOLD_COIN_STACK_BLOCK = COIN_BLOCK_REGISTER.register("gold_coin_stack", resourceLocation ->
        new VanillaCoinStackBlock(Lazy.of(() -> GOLD))
    );
    
    @AutoI18n(group = "csb:diamond_coin")
    public static final DeferredHolder<Block, VanillaCoinStackBlock> DIAMOND_COIN_STACK_BLOCK = COIN_BLOCK_REGISTER.register("diamond_coin_stack", resourceLocation ->
        new VanillaCoinStackBlock(Lazy.of(() -> DIAMOND))
    );
}