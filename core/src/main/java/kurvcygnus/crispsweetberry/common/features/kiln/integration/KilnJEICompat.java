//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.integration;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipe;
import kurvcygnus.crispsweetberry.common.features.kiln.recipes.KilnRecipeManager;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.runtime.IJeiRuntime;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//? TODO: Display Stuff

/**
 * This is the JEI compat layer of Kiln, it holds the <u>{@link RecipeType unique recipe type}</u>, IRT recipe sync and 
 * <u>{@link RecipeType}</u>'s display implementation.
 * @see kurvcygnus.crispsweetberry.integrations.JEIEntrypoint Entrypoint
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public enum KilnJEICompat
{
    INSTANCE;
    
    public static final RecipeType<KilnRecipe> JEI_KILN_RECIPE_TYPE = RecipeType.create(
        CrispSweetberry.NAMESPACE,
        "kiln",
        KilnRecipe.class
    );
    
    private static final MarkLogger LOGGER = MarkLogger.marklessLogger(LogUtils.getLogger());
    
    private @Nullable IJeiRuntime runtime = null;
    
    public void setRuntime(@NotNull IJeiRuntime runtime) { this.runtime = runtime; }
    
    public void pushRecipesToJEI()
    {
        if(!ModList.get().isLoaded("jei") || this.runtime == null)
            return;
        
        //! JEI's IRecipeManager doesn't have any methods that removes the old recipe collection, maybe will auto rebuild on datapack reload, 
        //! maybe it has other solutions, anyways, we can only pray about this.
        try { runtime.getRecipeManager().addRecipes(JEI_KILN_RECIPE_TYPE, KilnRecipeManager.INSTANCE.getRecipesList()); }
        catch(Throwable t) { LOGGER.error("Failed to push Kiln Recipes. Details: ", t); }
    }
}
