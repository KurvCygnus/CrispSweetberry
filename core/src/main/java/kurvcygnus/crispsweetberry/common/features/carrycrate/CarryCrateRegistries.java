//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum CarryCrateRegistries implements IRegistrant
{
    INSTANCE;
    
    @Override public void register(@NotNull IEventBus bus) { REGISTRIES.forEach(registry -> registry.register(bus)); }
    
    @Override public boolean isFeature() { return true; }
    
    @Override public @NotNull String getJob() { return "Carry Crate"; }
    
    @Override public @NotNull PriorityPair getPriority() { return new PriorityPair(PriorityRange.FEATURE, 4); }
    
    private static final DeferredRegister<Block> CARRY_CRATE_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
    private static final DeferredRegister<Item> CARRY_CRATE_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.NAMESPACE);
    
    public static final List<DeferredRegister<?>> REGISTRIES = List.of(
        CARRY_CRATE_BLOCK_REGISTER,
        CARRY_CRATE_ITEM_REGISTER
    );
    
    @AutoI18n(value = {
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
    public static final Holder<Item> CARRY_CRATE_ITEM = CARRY_CRATE_ITEM_REGISTER.register("carry_crate", resourceLocation ->
        new BlockItem(CarryCrateRegistries.CARRY_CRATE_BLOCK.value(), new Item.Properties())
    );
}