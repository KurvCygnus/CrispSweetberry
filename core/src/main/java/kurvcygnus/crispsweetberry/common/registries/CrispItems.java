//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.registry.annotations.RegisterToTab;
import net.minecraft.core.Holder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public enum CrispItems implements IRegistrant
{
    INSTANCE;
    
    @Override
    public void register(@NotNull IEventBus bus) { CRISP_ITEM_REGISTER.register(bus); }
    
    @Override
    public boolean isFeature() { return false; }
    
    @Override
    public @NotNull String getJob() { return "Misc Items"; }
    
    @Override
    public @NotNull PriorityPair getPriority() { return new PriorityPair(PriorityRange.MISC, 80); }
    
    public static final DeferredRegister<Item> CRISP_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.NAMESPACE);
    
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
    @AutoI18n(group = "carry_crate")
    public static final Holder<Item> CARRY_CRATE = CRISP_ITEM_REGISTER.register("carry_crate", resourceLocation ->
        new BlockItem(CrispBlocks.CARRY_CRATE.value(), new Item.Properties())
    );
}
