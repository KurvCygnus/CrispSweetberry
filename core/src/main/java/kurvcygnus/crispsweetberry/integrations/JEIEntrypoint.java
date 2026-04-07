//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.integrations;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnScreen;
import kurvcygnus.crispsweetberry.common.features.kiln.integration.KilnJEICompat;
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.*;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.features.kiln.KilnConstants.*;
import static kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries.KILN_BLOCK_ITEM;
import static kurvcygnus.crispsweetberry.common.features.kiln.KilnRegistries.KILN_MENU_TYPE;
import static kurvcygnus.crispsweetberry.common.features.kiln.integration.KilnJEICompat.JEI_KILN_RECIPE_TYPE;

/**
 * This is the entrypoint of JEI compat.<br>
 * Entrypoint mainly processes custom content, like Kiln's customization(include <u>{@link KilnScreen UI bound}</u>, 
 * <u>{@link KilnMenu quickcrafting layout provide}</u>, <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipeType recipe componentExecutionType provide}</u>, 
 * etc.),
 * as JEI will automatically discover and get vanilla recipes.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
@JeiPlugin
public enum JEIEntrypoint implements IModPlugin
{
    INST;
    
    @Override public @NotNull ResourceLocation getPluginUid() { return DefinitionUtils.getModNamespacedLocation("jei_integration"); }
    
    @Override public void registerRecipes(@NotNull IRecipeRegistration registration)
    {
    }
    
    @Override public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) { KilnJEICompat.INST.setRuntime(jeiRuntime); }
    
    @Override public void registerGuiHandlers(@NotNull IGuiHandlerRegistration registration)
    {
        registration.addRecipeClickArea(KilnScreen.class, KilnScreen.ARROW_X_POS, KilnScreen.ARROW_Y_POS, KilnScreen.BG_WIDTH, KilnScreen.BG_HEIGHT);
    }
    
    @Override public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) 
    {
        registration.addRecipeCatalyst(KILN_BLOCK_ITEM.value(), JEI_KILN_RECIPE_TYPE);
    }
    
    @Override public void registerRecipeTransferHandlers(@NotNull IRecipeTransferRegistration registration)
    {
        registration.addRecipeTransferHandler(
            KilnMenu.class,
            KILN_MENU_TYPE.get(),
            JEI_KILN_RECIPE_TYPE, 
            KILN_INPUT_START_INDEX,
            KILN_SLOT_COUNT_FOR_EACH_TYPE,
            KILN_INVENTORY_SLOTS_RANGE.min(),
            KILN_INVENTORY_SLOTS_RANGE.size()
        );
    }
    
    @Override public void registerModInfo(@NotNull IModInfoRegistration modAliasRegistration)
    {
        modAliasRegistration.addModAliases(CrispSweetberry.NAMESPACE, "csb", "澄莓物语", "TA2TY_FRUT");
    }
}
