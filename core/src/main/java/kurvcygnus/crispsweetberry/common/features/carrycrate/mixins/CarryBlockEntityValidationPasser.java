//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.mixins;

import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * A Mixin that suppresses <u>{@link BlockEntity}</u>'s init validation during carry crate interactions.
 *
 * <p>This mixin injects at the head of
 * <u>{@link BlockEntity}</u> 's init validation and cancels
 * the call when the {@link CarryEngine} reports an ongoing carry interaction. This prevents
 * spurious validation errors that would otherwise occur when a block entity is temporarily
 * placed in an <span style="color: f84b4b">invalid context</span> during carry operations.</p>
 *
 * @implNote <span style="color: f84b4b">An earlier design attempted to extend
 * <u>{@link net.minecraft.world.level.block.entity.BlockEntityType BlockEntityType}</u> and bypass validation
 * through a Wrapper method.</span> This approach fundamentally failed because
 * {@code BlockEntityType.builtInRegistryHolder}'s initial value is set during bootstrap and
 * cannot be reassigned, which would inevitably cause game logic errors and
 * <span style="color: f84b4b">illegal registry access</span>.
 * <b>Do not attempt to replace this bypass logic</b> &mdash; the current mixin-based approach
 * is the most stable and reasonable implementation available for this use case.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@Mixin(BlockEntity.class)
public abstract class CarryBlockEntityValidationPasser
{
    @Inject(method = "validateBlockState", at = @At("HEAD"), cancellable = true)
    private void tryPass(@NotNull BlockState state, @NotNull CallbackInfo ci)
    {
        if(!CarryEngine.isInteracting())
            return;
        
        ci.cancel();
    }
}
