//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.lib.core.registry.IRegistrant;
import net.minecraft.core.Holder;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public enum CrispItems implements IRegistrant<CrispItems>
{
    INST;
    
    @Override public void register(@NotNull Consumer<DeferredRegister<?>> registerLogic) { registerLogic.accept(CRISP_ITEM_REGISTER); }
    
    @Override public boolean isFeature() { return false; }
    
    @Override public @NotNull String getJob() { return "Misc Items"; }
    
    @Override public @NotNull PriorityPair getPriority() { return ofPriority(PriorityRange.BASE, 2); }
    
    public static final DeferredRegister<Item> CRISP_ITEM_REGISTER = DeferredRegister.createItems(CrispSweetberry.NAMESPACE);
    
    public static final Holder<Item> HONEY_BERRY = CRISP_ITEM_REGISTER.register(
        "crisp_sweetberry",
        () ->
        new Item(
            new Item.Properties().
                food(
                    new FoodProperties.Builder().
                    nutrition(1).
                    saturationModifier(8.0F).
                    build()
                )
        )
    );
}
