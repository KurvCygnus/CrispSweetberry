//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.registry.objects;

import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record LangEntry(AutoI18n.@NotNull Lang lang, @NotNull String key, @NotNull String translation)
{
    public LangEntry(@NotNull AutoI18n.Lang lang, @NotNull String key, @NotNull String translation)
    {
        Objects.requireNonNull(lang, "Param \"lang\" must not be null!");
        Objects.requireNonNull(key, "Param \"key\" must not be null!");
        Objects.requireNonNull(translation, "Param \"translation\" must not be null!");
        
        this.lang = lang;
        this.key = key;
        this.translation = translation;
    }
}
