//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.integrations;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import org.jetbrains.annotations.NotNull;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class JadeEntrypoint implements IWailaPlugin
{
    @Override
    public void register(@NotNull IWailaCommonRegistration registration)
    {
        //registration.registerBlockDataProvider(KilnBlockInfoProvider.INSTANCE, KilnBlock.class);
    }
    
    @Override
    public void registerClient(@NotNull IWailaClientRegistration registration)
    {
        registration.hideTarget(TTorchRegistries.FAKE_LIGHT_BLOCK.value());
        
        TTorchRegistries.ENTITY_HIDE_LIST.forEach(e -> registration.hideTarget(e.value()));
        
        //registration.registerBlockComponent(KilnBlockInfoProvider.INSTANCE, KilnBlock.class);
    }
}
