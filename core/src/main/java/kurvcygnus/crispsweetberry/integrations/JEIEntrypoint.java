//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.integrations;

import kurvcygnus.crispsweetberry.common.features.kiln.integration.KilnJEICompat;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * This is the entrypoint of JEI compat.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@JeiPlugin
public enum JEIEntrypoint implements IModPlugin
{
    INSTANCE;
    
    @Override
    public @NotNull ResourceLocation getPluginUid() { return CrispDefUtils.getModNamespacedLocation("jei_integration"); }
    
    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration)
    {
    }
    
    @Override public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) { KilnJEICompat.INSTANCE.setRuntime(jeiRuntime); }
}
