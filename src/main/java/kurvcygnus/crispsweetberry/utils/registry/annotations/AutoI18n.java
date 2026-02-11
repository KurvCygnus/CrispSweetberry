//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.registry.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

/**
 * This is a simple annotation used for data generation, <b>specially on internationalization(i18n)</b>.
 * @apiNote This annotation only works on field that holds a key of a content(e.g. <u>{@link net.minecraft.world.item.Item Item}</u>, 
 * <u>{@link net.minecraft.world.level.block.Block Block}</u>),
 * as fields that hold no key is actually can't be i18n-ized.<br>
 * <b>Also, if you are using IDEA, you can generate a template with typing keyword {@code i18n}</b>.<br><br>
 * Template:<br><pre>
 *  <span style="color: a7f8e7">&#64;AutoI18n</span>({
 *      <span style="color: 79a85d">"en_us -></span> <span style="color: e47087">$TRANS1$</span><span style="color: 79a85d">"</span>,
 *      <span style="color: 79a85d">"lol_us -></span> <span style="color: e47087">$TRANS2$</span><span style="color: 79a85d">"</span>,
 *      <span style="color: 79a85d">"zh_cn -></span> <span style="color: e47087">$TRANS3$</span><span style="color: 79a85d">"</span>
 *  })
 * </pre>
 * @implSpec <pre>
 *  <span style="color: a7f8e7">&#64;AutoI18n</span>({
 *      <span style="color: 79a85d">"en_us -> Foo"</span>,// In English, this is "Foo".
 *      <span style="color: 79a85d">"lol_us->UwU OwO"</span>,// In da catz languiage, thiz iz happi!!!
 *      <span style="color: 79a85d">"zh_cn-> 甲"</span>,// 在中文语言中, 这个对应"甲".
 *      <span style="color: 79a85d">"ja_jp ->あ"</span>// 日本語では「あ」です.
 *  })
 *  private static final Holder&lt;Item&gt; Foo = ...
 *  
 *  // Grammar: "<span style="color: e47087">${Language}</span> -> <span style="color: e47087">${Translation}</span>"
 *  // The spaces around arrow `->` is optional.
 *  // There can only exist one arrow in translation, or else parser will throw an <u>{@link IllegalArgumentException expr}</u>.
 *  
 *  // <i>Some comments of the usage example above are machine-translated.</i> 
 * </pre>
 * Doing this and data generation will do rest works.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see AutoI18n.Lang Supported Languages
 * @see kurvcygnus.crispsweetberry.datagen.CrispDataGenEvent DataGen Process Implementation
 */
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
