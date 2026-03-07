//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate;

import com.mojang.serialization.Codec;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.block.SimpleBlockCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.BaseVanillaBrewingStandAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.BaseVanillaFurnaceSeriesAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.SimpleContainerBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.events.CarryAdapterRegisterEvent;
import kurvcygnus.crispsweetberry.common.features.carrycrate.carriables.PowderSnowCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.CarryCrateBlock;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.CarryCrateItem;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.OverWeightEffect;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@EventBusSubscriber(modid = CrispSweetberry.NAMESPACE)
public enum CarryCrateRegistries implements IRegistrant
{
    INSTANCE;
    
    @Override public void register(@NotNull IEventBus bus) { REGISTRIES.forEach(registry -> registry.register(bus)); }
    
    @Override public @NotNull String getJob() { return "Carry Crate"; }
    
    @Override public @NotNull PriorityPair getPriority() { return new PriorityPair(PriorityRange.FEATURE, 4); }
    
    private static final DeferredRegister<Block> CARRY_CRATE_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<Item> CARRY_CRATE_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<BlockEntityType<?>> CARRY_CRATE_BE_REGISTER = DeferredRegister.create(
        Registries.BLOCK_ENTITY_TYPE,
        CrispSweetberry.NAMESPACE
    );
    private static final DeferredRegister<MobEffect> OVERWEIGHT_EFFECT_REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<DataComponentType<?>> CARRY_CRATE_DATA_COMPONENT_REGISTER = DeferredRegister.create(
        Registries.DATA_COMPONENT_TYPE,
        CrispSweetberry.NAMESPACE
    );
    
    public static final List<DeferredRegister<?>> REGISTRIES = List.of(
        CARRY_CRATE_BLOCK_REGISTER,
        CARRY_CRATE_ITEM_REGISTER,
        CARRY_CRATE_BE_REGISTER,
        OVERWEIGHT_EFFECT_REGISTER,
        CARRY_CRATE_DATA_COMPONENT_REGISTER
    );
    
    @SubscribeEvent static void registerInternalBasicCarriables(@NotNull CarryAdapterRegisterEvent event)
    {
        event.registerUniversal(
            BaseVanillaFurnaceSeriesAdapter::new,
            BlockEntityType.FURNACE,
            BlockEntityType.BLAST_FURNACE,
            BlockEntityType.SMOKER
        );
        event.register(BlockEntityType.BREWING_STAND, BaseVanillaBrewingStandAdapter::new);
        
        //* Despite EnderChest belongs to BlockEntity, it can't, and shouldn't be registered at here.
        //* EnderChest's content stores on player, so we treat it as a normal block.
        event.registerUniversal(
            SimpleContainerBlockEntityCarryAdapter::new,
            BlockEntityType.CHEST,
            BlockEntityType.BARREL,
            BlockEntityType.DISPENSER,
            BlockEntityType.DROPPER,
            BlockEntityType.HOPPER,
            BlockEntityType.TRAPPED_CHEST,
            BlockEntityType.CRAFTER//* Crafter relies on redstone signal to work, so here's no need to support its tick logic LLLLLLMAO
        );
        
        event.registerUniversal(
            SimpleBlockCarryAdapter::new,
            Blocks.ANVIL,
            Blocks.ENCHANTING_TABLE,
            Blocks.SMITHING_TABLE,
            Blocks.CARTOGRAPHY_TABLE,
            Blocks.FLETCHING_TABLE,
            Blocks.CRAFTING_TABLE,
            Blocks.ENDER_CHEST
        );
        
        event.register((PowderSnowBlock) Blocks.POWDER_SNOW, PowderSnowCarryAdapter::new);
    }
    
    @AutoI18n(
        value = {
            "en_us = Carry Crate",
            "lol_us = hoom",
            "zh_cn = 搬运箱"
        },
        group = "carry_crate",
        key = "carry_crate"
    )
    public static final Holder<Block> CARRY_CRATE_BLOCK = CARRY_CRATE_BLOCK_REGISTER.register(
        "carry_crate",
        resourceLocation -> new CarryCrateBlock()
    );
    
    @RegisterToTab
    @AutoI18n(group = "carry_crate", key = "carry_crate")
    public static final Holder<Item> CARRY_CRATE_ITEM = CARRY_CRATE_ITEM_REGISTER.register(
        "carry_crate",
        resourceLocation -> new CarryCrateItem()
    );
    
    @AutoI18n({
        "en_us = Overweight",
        "lol_us = burgered kat",
        "zh_cn = 超重"
    })
    public static final Holder<MobEffect> OVERWEIGHT = OVERWEIGHT_EFFECT_REGISTER.register(
        "overweight",
        () -> OverWeightEffect.register(OVERWEIGHT_EFFECT_REGISTER)
    );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> CARRY_ID = CARRY_CRATE_DATA_COMPONENT_REGISTER.register(
        "carry_crate.carry_id",
        resourceLocation -> DataComponentType.<String>builder().
            persistent(Codec.STRING).
            networkSynchronized(ByteBufCodecs.STRING_UTF8).
            build()
    );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CarryData>> CARRY_CRATE_DATA = CARRY_CRATE_DATA_COMPONENT_REGISTER.register(
        "carry_crate.data",
        resourceLocation -> DataComponentType.<CarryData>builder().
            persistent(CarryData.CODEC).
            networkSynchronized(CarryData.STREAM_CODEC).
            build()
    );
    
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CARRY_TICK_COUNTER = CARRY_CRATE_DATA_COMPONENT_REGISTER.register(
        "carry_crate.tick_counter",
        resourceLocation -> DataComponentType.<Integer>builder().
            persistent(Codec.INT).
            networkSynchronized(ByteBufCodecs.INT).
            build()
    );
}