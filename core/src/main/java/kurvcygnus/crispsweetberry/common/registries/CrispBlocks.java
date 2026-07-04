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
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Predicate;

public enum CrispBlocks implements IRegistrant<CrispBlocks>
{
    INST;
    
    @Override public void register(@NotNull Consumer<DeferredRegister<?>> registerLogic) { registerLogic.accept(CRISP_BLOCK_REGISTER); }
    
    @Override public boolean isFeature() { return false; }
    
    @Override public @NotNull Predicate<ModContainer> isActivated() { return BANNED; }
    
    
    @Override public @NotNull String getJob() { return "Misc Blocks"; }
    
    @Override public @NotNull PriorityPair getPriority() { return ofPriority(PriorityRange.BASE, 1); }
    
    public static final DeferredRegister<Block> CRISP_BLOCK_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.NAMESPACE);
}
