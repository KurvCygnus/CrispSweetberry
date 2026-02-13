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
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is a simple annotation used for data generation, <b>specially on internationalization(i18n)</b>.
 * @apiNote This annotation only works on field that holds a key of a content(e.g. <u>{@link net.minecraft.world.item.Item Item}</u>, 
 * <u>{@link net.minecraft.world.level.block.Block Block}</u>, <u>{@link net.minecraft.network.chat.Component Component}</u>, etc.),
 * as fields that hold no key is actually can't be i18n-ized.
 * <br><br>
 * <b>NOTE: Please makes sure that the field name matches the translation key(For {@code .} literals, use {@code __} instead), 
 * or else the <u>{@link kurvcygnus.crispsweetberry.utils.registry.annotations.processors.I18nProcessor processor}</u> will throw an error</b>, 
 * also, please remember going to {@code build.gradle} and add this:
 * <pre>{@code 
 *  java {
 *      tasks.withType(JavaCompile).configureEach {
 *          options.compilerArgs.add
 *              ("-Amodid=" + (project.findProperty('mod_id') ?: 'unknown'))
 *      }
 *  }
 * }</pre>
 * <b>This makes sure that the <u>{@link kurvcygnus.crispsweetberry.utils.registry.annotations.processors.I18nProcessor processor}</u> will get your 
 * mod's namespace, and process correctly(Thus, you don't need to write your mod's namespace in field, or <u>{@link #key()}</u>'s name).</b>
 * <br><br>
 * <b>Also, if you are using IDEA, you can generate a template with typing keyword {@code i18n}</b>.<br><br>
 * Template:<br><pre>
 *  <span style="color: a7f8e7">&#64;AutoI18n</span>({
 *      <span style="color: 79a85d">"en_us =</span> <span style="color: e47087">$TRANS1$</span><span style="color: 79a85d">"</span>,
 *      <span style="color: 79a85d">"lol_us =</span> <span style="color: e47087">$TRANS2$</span><span style="color: 79a85d">"</span>,
 *      <span style="color: 79a85d">"zh_cn =</span> <span style="color: e47087">$TRANS3$</span><span style="color: 79a85d">"</span>
 *  })
 * </pre>
 * @implSpec <pre>
 *  <span style="color: 79a85d">// Basic usage: Note that the field name 'BLOCK__DIRT' translates to 'block.dirt'.</span>
 *  <span style="color: a7f8e7">&#64;AutoI18n</span>({
 *      <span style="color: 79a85d">"en_us = Dirt"</span>,
 *      <span style="color: 79a85d">"lol_us= Cofee Blok UwU"</span>,
 *      <span style="color: 79a85d">"zh_cn =泥土"</span>
 *  })
 *  <span style="color: 927dcb"><i>private static final</i></span> Holder&lt;Block&gt; BLOCK__DIRT = ...
 *
 *  <span style="color: 79a85d">// Reusable groups: Define translations once and apply them elsewhere via 'group'</span>
 *  <span style="color: a7f8e7">&#64;AutoI18n</span>({
 *      <span style="color: 79a85d">"en_us=Sweetberry"</span>,
 *      <span style="color: 79a85d">"lol_us = TA2Y FRU1T"</span>
 *      <span style="color: 79a85d">"zh_cn = 甜莓"</span>
 *      }, 
 *      <span style="color: cdd6f4">group</span> = <span style="color: 79a85d">"BerryBase"</span>
 *  )
 *  <span style="color: 927dcb"><i>private static final</i></span> Holder&lt;Item&gt; SWEETBERRY_ITEM = ...
 * 
 *  <span style="color: a7f8e7">&#64;AutoI18n</span>(
 *      <span style="color: cdd6f4">group</span> = <span style="color: 79a85d">"BerryBase"</span>,<span style="color: 79a85d">// Inherits all translations from the "BerryBase" group.</span>
 *      <span style="color: cdd6f4">key</span> = <span style="color: 79a85d">"sweetberry_bush"// Forces using trans key "${modid}.sweetberry_bush".</span>
 *  )
 *  <span style="color: 927dcb"><i>private static final</i></span> Holder&lt;Block&gt; SWEETBERRY_BLOCK = ...
 * 
 *  <span style="color: 79a85d">// Grammar: "${Language Code} = ${Translation Text}"</span>
 *  <span style="color: 79a85d">// 1. Field naming: Use '__' to represent '.' in the translation key.</span>
 *  <span style="color: 79a85d">// 2. Format: Whitespace around '=' is ignored. Only one '=' is allowed per string.</span>
 *  <span style="color: 79a85d">// 3. Special case: Component's modid(aka namespace) is optional. 
 *  //    If it doesn't contains namespace, processor will add it, 
 *  //    just like the SWEETBERRY_BLOCK above.</span>
 * </pre>
 *  
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see AutoI18n.Lang Supported Languages
 * @see kurvcygnus.crispsweetberry.utils.registry.annotations.processors.I18nProcessor Processor Implementation
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoI18n
{
    @NotNull String[] value() default "";
    @NotNull String key() default "";
    @NotNull String group() default "";
    
    Pattern TRANSLATION_PATTERN = Pattern.compile("^\\s*([a-zA-Z._]+)\\s*=\\s*(.+?)\\s*$");
    
    enum Lang
    {
        EN_US, EN_GB, LOL_US,
        ZH_CN, ZH_TW, JA_JP,
        KO_KR, RU_RU, FR_FR,
        DE_DE, ES_ES;
        
        private final String code;
        
        Lang() { this.code = this.name().toLowerCase(); }
        
        public @NotNull String getCode() { return this.code; }
        
        public static @NotNull Optional<Lang> parse(@NotNull String prefix) 
        {
            try { return Optional.of(Lang.valueOf(prefix.toUpperCase())); }
            catch(IllegalArgumentException e) { return Optional.empty(); }
        }
    }
}
