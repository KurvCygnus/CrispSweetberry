package kurvcygnus.crispsweetberry.utils.registry.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoI18n
{
    @NotNull String[] value();
    
    Pattern TRANSLATION_PATTERN = Pattern.compile("^\\s*([a-zA-Z._]+)\\s*->\\s*(.+?)\\s*$");
    
    enum Lang
    {
        EN_US, EN_GB, LOL_US,
        ZH_CN, ZH_TW, JA_JP,
        KO_KR, RU_RU, FR_FR,
        DE_DE, ES_ES;
        
        private final String code;
        
        Lang() { this.code = this.name().toLowerCase(); }
        
        public @NotNull String getCode() { return code; }
        
        public static Lang parse(@NotNull String prefix) 
        {
            try { return Lang.valueOf(prefix.toUpperCase().trim()); }
            catch(IllegalArgumentException e) { throw new IllegalArgumentException("Unknown language: %s. Details: %s".formatted(prefix, e)); }
        }
    }
}
