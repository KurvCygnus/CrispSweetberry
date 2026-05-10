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
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public enum CrispBlocks implements IRegistrant<CrispBlocks>
{
    INST;
    
    @Override public void register(@NotNull IEventBus bus) 
    {
        //CRISP_BLOCK_REGISTER.registerUniversal(bus);
    }
    
    @Override public boolean isFeature() { return false; }
    
    @Override public @NotNull String getJob() { return "Misc Blocks"; }
    
    @Override public @NotNull PriorityPair getPriority() { return ofPriority(PriorityRange.BASE, 1); }
    
    public static final DeferredRegister<Block> CRISP_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
}
