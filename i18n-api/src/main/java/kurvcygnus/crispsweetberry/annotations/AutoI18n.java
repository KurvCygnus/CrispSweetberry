//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is a simple annotation used for data generation, <b>specially on internationalization(i18n)</b>.
 * @apiNote {@code AutoI18n} relies on annotation processor, 
 * which means you should <u><a href="https://www.jetbrains.com/help/idea/annotation-processors-support.html">enable config in IDEA</a></u>.<br>
 * For details, please see {@code GettingStarted.md}.
 * @implSpec <pre>
 *  &#64;AutoI18n(value = {
 *      "en_us = Dirt",
 *      "lol_us = Cofe Sdak",
 *      "zh_cn = 泥土"
 *  })
 *  public static final Holder&lt;Block&gt; DIRT = ...
 *  // Creates `"block.${modid}.dirt": "Dirt"` in en_us.json.
 * </pre>
 * <b><i>
 * This is only the simplest usage example.<br>
 * For details, please see {@code GettingStarted.md}, or <u>{@link #value()}</u>, <u>{@link #key()}</u> and <u>{@link #group()}</u>
 * </i></b>.
 * 
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see AutoI18n.Lang Supported Languages
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
public @interface AutoI18n
{
    /**
     * This is the translation entry, every single <u>{@link String}</u> inside is a translation in a single language.<br>
     * @apiNote The grammar of translation is rather straightforward.
     * <br>
     * <br>
     * Grammar: <pre>{@code 
     *  "${Language} = ${Translation}"
     * }</pre>
     * Here are some extra explanations:
     * <ul>
     *     <li>
     *         <b>{@code ${Language}} can only contain letters, {@code .} and {@code _}</b>.<br>
     *         However, <b>{@code ${Translation}} can contain most of daily symbols</b>.
     *     </li>
     *     <li>
     *         <b>{@code =} is not optional.</b> The whitespaces around {@code =} is optional, but {@code =} itself isn't.<br>
     *         And more importantly, <b>one <u>{@link String}</u> can only have one {@code =}</b>.
     *     </li>
     *     <li>
     *         {@code ${Language}}'s parse is strict. For all supported languages, you can found them at <u>{@link Lang}</u>.
     *     </li>
     * </ul><br>
     * A simple example: <pre>{@code 
     *  value = {
     *      "en_us = Foo",
     *      "lol_us= UwU",
     *      "zh_cn =甲",
     *      "ja_jp=あ"
     *  }// All works.
     * }</pre>
     */
    @NotNull String[] value() default "";
    
    /**
     * This is translation key, it should point to a content owns translation.
     * @apiNote <b>{@code key()} is actually optional</b>.<br>
     * The processor will automatically 
     * deduce the key of annotated target, <b>based on its type, and declared name(<i>for naming rules, we'll cover it later</i>)</b>, 
     * thus, <b>explicitly writing {@code key()} will override the deduction</b>.
     * <br>
     * <br>
     * Explanations about deduction:
     * <ul>
     *     <li>
     *         <b>Field name's deduction is strict, but also easy:</b><br>
     *         All letters will be lowercased, with {@code __} being converted to {@code .}, 
     *         {@code _} won't be affected.
     *     </li>
     *     <li>
     *         About key override, it doesn't have conversion rules.<br>
     *         <b>However, both two follow the following principles</b>.
     *     </li>
     *     <li>
     *         <b>Most types are not required modId explicitly in their field names, expect {@code ResourceLocation} and 
     *         {@code Component}</b>.
     *         <ul>
     *             <li>
     *                 {@code ResourceLocation} can be {@code Stat}, 
     *                 sprite holder or custom stuffs, thus can't be deduced, you have to type full qualified key.
     *             </li>
     *             <li>
     *                 {@code Component} is special.<br>
     *                 <b>Whether having modId explicitly in name doesn't matter.</b> If have, 
     *                 processor won't do anything. 
     *                 If have not, processor will add 
     *                 modId at the head of key.
     *             </li>
     *         </ul>
     *     </li>
     * </ul><br>
     * Two simple examples:<pre>{@code 
     *  ...
     *  {
     *      value = {"en_us = Bar"},
     *      key = "bar",
     *  }// Final key: "bar", which should be edited.
     *  public static final Holder<ResourceLocation> UWU = ...
     *  
     *  ...
     *  { value = ... }// Final key: "${modid}.ui.hello_world".
     *  public static final Component UI__HELLO_WORLD = ...
     * }</pre>
     */
    @NotNull String key() default "";
    
    /**
     * This declares a group, which can be inherited by other translation entries.
     * @apiNote {@code group()} follows <u><a href="https://en.wikipedia.org/wiki/One_Definition_Rule">one definition rule</a></u>, 
     * <b>which means, you should only define it once</b>.
     * <br><br>
     * Declaring a {@code group()} is simple: Write your <u>{@link #value() translations}</u> as usual, then put a group on it, that's all, adding modId
     * is not required, processor will handle this.<br>
     * Using {@code group()} is also simple: Do not write <u>{@link #value() translations}</u>, directly write {@code group()}, that's all.
     * <br><br>
     * {@code group()} is compatible with <u>{@link #key()}</u>.
     */
    @NotNull String group() default "";
    
    Pattern TRANSLATION_PATTERN = Pattern.compile("^\\s*([a-zA-Z._]+)\\s*=\\s*(.+?)\\s*$", Pattern.DOTALL);
    
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
