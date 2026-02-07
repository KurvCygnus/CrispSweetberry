package kurvcygnus.crispsweetberry.utils.registry.objects;

import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
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
