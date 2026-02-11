//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.datagen;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.utils.registry.objects.LangEntry;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

final class CrispI18NProvider extends LanguageProvider
{
    private final List<LangEntry> langEntries;
    
    CrispI18NProvider(PackOutput output, String locale, @NotNull List<LangEntry> langEntries) 
    {
        super(output, CrispSweetberry.NAMESPACE, locale);
        Objects.requireNonNull(langEntries, "Param \"langEntries\" must not be null!");
        this.langEntries = langEntries.stream().filter(entry -> Objects.equals(entry.lang().getCode(), locale)).toList();
    }
    
    @Override
    protected void addTranslations()
    {
        for(final LangEntry langEntry: langEntries)
            add(langEntry.key(), langEntry.translation());
    }
}
