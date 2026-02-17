//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate;

import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;

public enum CarryCrateRegistries implements IRegistrant
{
    INSTANCE;
    
    @Override
    public void register(@NotNull IEventBus bus)
    {
        
    }
    
    @Override
    public boolean isFeature() { return true; }
    
    @Override
    public @NotNull String getJob() { return "Carry Crate"; }
    
    @Override
    public @NotNull PriorityPair getPriority() { return new PriorityPair(PriorityRange.FEATURE, 4); }
}