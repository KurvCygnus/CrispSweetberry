//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.sync;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * This handles the sync for soul fire's tag persistent.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class SoulFireTagPayloadHandler
{
    /**
     * Attaches, or deattaches the soul fire tag on the entity.
     */
    public static void attachTag(@NotNull SoulFireTagPayload packet, @NotNull IPayloadContext context)
    {
        context.enqueueWork(() ->
            {
                final Entity target = context.player().level().getEntity(packet.targetId());
                
                if(target == null)
                    return;
                
                if(packet.attachTag())
                {
                    if(target.getPersistentData().contains(TTorchConstants.SOUL_FIRE_PERSISTENT_TAG))
                        return;
                    
                    target.getPersistentData().putByte(TTorchConstants.SOUL_FIRE_PERSISTENT_TAG, (byte) 1);
                }
                else
                {
                    if(!target.getPersistentData().contains(TTorchConstants.SOUL_FIRE_PERSISTENT_TAG))
                        return;
                    
                    target.getPersistentData().remove(TTorchConstants.SOUL_FIRE_PERSISTENT_TAG);
                }
            }
        );
    }
}
