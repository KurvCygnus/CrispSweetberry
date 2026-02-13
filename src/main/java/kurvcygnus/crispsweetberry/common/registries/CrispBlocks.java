//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateBlock;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public enum CrispBlocks implements IRegistrant
{
    INSTANCE;
    
    @Override public void register(@NotNull IEventBus bus) { CRISP_BLOCK_REGISTER.register(bus); }
    
    @Override
    public boolean isFeature() { return false; }
    
    @Override
    public @NotNull String getJob() { return "Misc Blocks"; }
    
    @Override
    public int getPriority() { return 0; }
    
    public static final DeferredRegister<Block> CRISP_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
    
    @AutoI18n(value = {
        "en_us = Carry Crate",
        "lol_us = hoom",
        "zh_cn = 搬运箱"
        },
        group = "csb:carry_crate"
    )
    public static final Holder<Block> CARRY_CRATE = CRISP_BLOCK_REGISTER.register("carry_crate", resourceLocation -> new CarryCrateBlock());
}
