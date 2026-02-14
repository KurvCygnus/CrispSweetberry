//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.annotation.processor;

import kurvcygnus.crispsweetberry.annotations.AutoI18n;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.regex.Matcher;

/**
 * This is the processor of annotation <u>{@link AutoI18n}</u>, it iterates, checks, collects, and finally generates 
 * the language files.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see AutoI18n Annotation
 */
@SupportedAnnotationTypes("kurvcygnus.crispsweetberry.annotations.AutoI18n")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class I18nProcessor extends AbstractProcessor
{
    private static final String OBJECT = "java.lang.Object";
    private static final String HOLDER = "net.minecraft.core.Holder";
    private static final String ITEM = "net.minecraft.world.item.Item";
    private static final String BLOCK = "net.minecraft.world.level.block.Block";
    private static final String ENTITY_TYPE = "net.minecraft.world.entity.EntityType";
    private static final String CREATIVE_MODE_TAB = "net.minecraft.world.item.CreativeModeTab";
    private static final String ENCHANTMENT = "net.minecraft.world.item.enchantment.Enchantment";
    private static final String MOB_EFFECT = "net.minecraft.world.effect.MobEffect";
    private static final String ATTRIBUTE = "net.minecraft.world.entity.ai.attributes.Attribute";
    private static final String COMPONENT = "net.minecraft.network.chat.Component";
    private static final String RESOURCE_LOCATION = "net.minecraft.resources.ResourceLocation";
    private static final String SUPPLIER = "java.util.function.Supplier";
    
    /**
     * <u>{@link Messager}</u> is the logger of Compile-Time.<br><br>
     * Different from normal coding, <u>{@link AbstractProcessor annotation processor}</u> can't use loggers because 
     * context passing issues, and more importantly, <u>{@link Messager#printMessage(Diagnostic.Kind, CharSequence)}</u> can 
     * terminate compile and throw error when <u>{@link javax.tools.Diagnostic.Kind Diagnostic Kind}</u> is <u>{@link javax.tools.Diagnostic.Kind#ERROR ERROR}</u>, 
     * this is essential in <u>{@link AbstractProcessor annotation processor}</u>, despite directly use {@code throw new SomeExpr} will also terminate the compile, 
     * however, it will splat a lot of garbage error stack traces to make your head explode in the end.<br><br>
     * Thus, using <u>{@link Messager}</u> to log stuff is basically a must in <u>{@link AbstractProcessor annotation processor}</u>.
     */
    private Messager messager;
    private Filer filer;
    
    /**
     * <u>{@link Types}</u> is the utility of <u>{@link TypeMirror}</u>, it is a must in deducing detailed types.
     * @see Types
     */
    private Types typeUtil;
    
    /**
     * <u>{@link Elements}</u> is the utility of <u>{@link TypeMirror}</u>, it is a must in getting detailed types.
     * @see Elements
     */
    private Elements elementUtil;
    private @Nullable String namespace;
    private TypeMirror object;
    
    /**
     * All actually nullable. This can be caused by incorrect configuration of dependency, and other reasons.<br>
     * In <u>{@link #getSafeTypeMirror(String)}</u>, we replaced null with <u>{@link Object}</u>'s <u>{@link TypeMirror}</u> to make sure helper methods
     * are safe to use.
     */
    private List<TypeMirror> types;
    private final HashMap<String, ArrayList<TranslationContentPair>> groups = new HashMap<>();
    private final ArrayList<TranslationPair> translationLookUp = new ArrayList<>();
    private final HashMap<AutoI18n.Lang, HashMap<String, String>> translationTable = new HashMap<>();
    private boolean generated = false;
    
    @Override
    public synchronized void init(@NotNull ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.typeUtil = processingEnv.getTypeUtils();
        this.elementUtil = processingEnv.getElementUtils();
        this.namespace = processingEnv.getOptions().get("modid");
        this.object = elementUtil.getTypeElement(OBJECT).asType();
        this.types = this.getSafeTypeMirrors(
            HOLDER,
            ITEM,
            BLOCK,
            ENTITY_TYPE,
            CREATIVE_MODE_TAB,
            ENCHANTMENT,
            MOB_EFFECT,
            ATTRIBUTE,
            COMPONENT,
            RESOURCE_LOCATION,
            SUPPLIER
        );
        
        if(namespace == null || namespace.isBlank() || namespace.equals("unknown"))
        {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                """
                    Unknown namespace. Go to build.gradle and configure it first.
                    
                    Example:
                    ```
                    java {
                        tasks.withType(JavaCompile).configureEach {
                            options.compilerArgs.add("-Amodid=" + (project.findProperty('mod_id') ?: 'unknown'))
                        }
                    }
                    ```
                    """
            );
            
            throw new IllegalArgumentException("Namespace is illegal.");
        }
    }
    
    @Override
    public boolean process(@NotNull Set<? extends TypeElement> annotations, @NotNull RoundEnvironment roundEnv)
    {
        if(roundEnv.processingOver() && !generated)
        {
            generated = true;
            generateJSON();
            return false;
        }
        
        if(annotations.isEmpty())
            return false;
        
        final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoI18n.class);
        
        for(final Element element: elements)
        {
            if(element.getKind() != ElementKind.FIELD)
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "%s must be a field.".formatted(element.getSimpleName().toString()),
                    element
                );
            
            final AutoI18n autoI18n = element.getAnnotation(AutoI18n.class);
            
            if(autoI18n == null)
            {
                messager.printMessage(
                    Diagnostic.Kind.WARNING,
                    "Processing expr: The AutoI18n annotation of %s seems to be null. Skipped.".
                        formatted(element.getSimpleName().toString()),
                    element
                );
                
                continue;//* Theoretically unachievable.
            }
            
            final String group = autoI18n.group();
            final String[] translations = autoI18n.value();
            final String key = autoI18n.key().isBlank() ?
                element.getSimpleName().toString().toLowerCase().replace("__", ".") :
                autoI18n.key();
            
            final String fullKeyScope = parseKeyScope(element, element.asType(), key);
            
            if(!hasContent(group) && !hasContent(translations))
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "%s doesn't have a group, and has no translations, or translations include an illegal string.".
                        formatted(element.getSimpleName().toString()),
                    element
                );
            
            final boolean hasTranslation = hasContent(translations);
            final boolean hasGroup = hasContent(group);
            
            if(hasTranslation)
            {
                if(hasGroup)
                    this.parseTranslations(element, translations, fullKeyScope, group);
                else
                    this.parseTranslations(element, translations, fullKeyScope, null);
            }
            else if(hasGroup)
                this.parseTranslations(element, null, fullKeyScope, group);
        }
        
        return true;
    }
    
    private void generateJSON()
    {
        for(final TranslationPair pair: translationLookUp)
        {
            final String key = pair.key;
            final List<TranslationContentPair> contentPairs = pair.content;
            
            for(final TranslationContentPair contentPair: contentPairs)
            {
                final AutoI18n.Lang lang = contentPair.lang;
                final String content = contentPair.content;
                
                final HashMap<String, String> map = translationTable.computeIfAbsent(lang, l -> new HashMap<>());
                
                if(map.containsKey(key))
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Duplicated translation key -> key: %s".
                            formatted(key)
                    );
                
                map.put(key, content);
            }
        }
        
        translationTable.forEach((l, t) ->
            {
                try
                {
                    final FileObject output = filer.createResource(
                        StandardLocation.CLASS_OUTPUT,
                        "",
                        "assets/%s/lang/%s.json".formatted(this.namespace, l.getCode())
                    );
                    
                    try(final Writer writer = output.openWriter())
                    {
                        writer.write("{\n");
                        final List<String> keys = new ArrayList<>(t.keySet());
                        keys.sort(String::compareTo);
                        
                        for(int index = 0; index < keys.size(); index++)
                        {
                            final String key = keys.get(index);
                            final String content = t.get(key);
                            final String entry = "    \"%s\": \"%s\"".formatted(key, escape(content));
                            writer.write(entry);
                            
                            if(index < keys.size() - 1)
                                writer.write(",");
                            writer.write("\n");
                        }
                        
                        writer.write("}");
                        messager.printMessage(
                            Diagnostic.Kind.NOTE,
                            "Generating %s.json...".
                                formatted(l.name())
                        );
                    }
                }
                catch(IOException e)
                {
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Can't create JSON file. Details: %s".formatted(e.getMessage())
                    );
                }
            }
        );
    }
    
    private static @NotNull String escape(@NotNull String text)
    {
        return text.replace("\\", "\\\\").
            replace("\"", "\\\"").
            replace("\n", "\\n").
            replace("\r", "\\r").
            replace("\t", "\\t");
    }
    
    private @NotNull String parseKeyScope(@NotNull Element element, @NotNull TypeMirror elementType, @NotNull String key)
    {
        final ProcessableType type = ProcessableType.getType(inTypes(elementType, types));
        
        switch(type)
        {
            case UNSUPPORTED -> messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't deduce the type of %s. Go check dependency configuration.".
                    formatted(element.getSimpleName().toString()),
                element
            );
            case HOLDER, SUPPLIER ->
            {
                if(elementType instanceof DeclaredType declaredType)
                {
                    final List<? extends TypeMirror> args = declaredType.getTypeArguments();
                    
                    if(args.isEmpty())
                        messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "Can't get the generic arg of %s.".
                                formatted(element.getSimpleName().toString()),
                            element
                        );
                    
                    final TypeMirror rawArg = args.getFirst();
                    
                    for(final TypeMirror typeMirror: types)
                    {
                        if(typeUtil.isAssignable(typeUtil.erasure(rawArg), typeMirror))
                            return parseKeyScope(element, rawArg, key);
                    }
                    
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "The detailed generic arg of %s can't be assigned from supported types.".
                            formatted(element.getSimpleName().toString()),
                        element
                    );
                }
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "%s has abnormal elementType!".
                        formatted(element.getSimpleName().toString()),
                    element
                );
            }
            case ITEM -> { return "item.%s.%s".formatted(namespace, key); }
            case BLOCK -> { return "block.%s.%s".formatted(namespace, key); }
            case ENTITY_TYPE -> { return "entity.%s.%s".formatted(namespace, key); }
            case CREATIVE_MODE_TAB -> { return "%s.creativetab.%s".formatted(namespace, key); }
            case ENCHANTMENT -> { return "enchantment.%s.%s".formatted(namespace, key); }
            case MOB_EFFECT -> { return "effect.%s.%s".formatted(namespace, key); }
            case ATTRIBUTE -> { return "attribute.%s.%s".formatted(namespace, key); }
            case RESOURCE_LOCATION -> { return key; }
            case COMPONENT -> 
            {
                if(key.contains("%s.".formatted(Objects.requireNonNull(namespace))))
                    return key;
                
                //* Add namespaces if the original key doesn't have one.
                return "%s.%s".formatted(namespace, key);
            }
            default -> messager.printMessage(
                Diagnostic.Kind.ERROR,
                "%s holds an illegal, or unknown value!".
                    formatted(element.getSimpleName().toString()),
                element
            );
        }
        
        throw new AssertionError("Can't deduce the type of %s.".formatted(element.getSimpleName().toString()));
    }
    
    private void parseTranslations(@NotNull Element element, String @Nullable [] translations, @NotNull String key, @Nullable String group)
    {
        ArrayList<TranslationContentPair> translationContentPairs = new ArrayList<>();
        
        if(translations != null)
        {
            for(int index = 0; index < translations.length; index++)
            {
                final String translation = translations[index];
                final Matcher matcher = AutoI18n.TRANSLATION_PATTERN.matcher(translation);
                
                if(!matcher.matches())
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Mismatched translation grammar: %s -> at %s, index %d".
                            formatted(translation, element.getSimpleName().toString(), index),
                        element
                    );
                
                final String lang = matcher.group(1);
                final String content = matcher.group(2);
                
                if(!hasContent(lang))
                {
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Language is empty -> at %s, index %d".
                            formatted(element.getSimpleName().toString(), index),
                        element
                    );
                    
                    throw new IllegalArgumentException();
                }
                
                if(!hasContent(content))
                {
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Content is empty -> at %s, index %d".
                            formatted(element.getSimpleName().toString(), index),
                        element
                    );
                    
                    throw new IllegalArgumentException();
                }
                
                final Optional<AutoI18n.Lang> language = AutoI18n.Lang.parse(lang);
                
                if(language.isPresent())
                    translationContentPairs.add(new TranslationContentPair(language.get(), content));
                else
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Language %s doesn't exist!".
                            formatted(lang),
                        element
                    );
            }
        }
        
        if(group != null)
        {
            if(!translationContentPairs.isEmpty())//* Initialize group case.
            {
                if(groups.containsKey(group))
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Group %s has been defined for multiple times!".
                            formatted(group)
                    );
                
                groups.put(group, translationContentPairs);
            }
            else//* Use group case.
            {
                if(!groups.containsKey(group))
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Group \"%s\" doesn't exist in cached groups!".
                            formatted(group),
                        element
                    );
                    
                translationContentPairs = groups.get(group);
            }
        }
        
        if(translationLookUp.stream().anyMatch(pair -> pair.key.equals(key)))
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Key %s has been defined for multiple times!".
                    formatted(key),
                element
            );
        
        //* Neither this parsed content belongs to a group, it should always be added to table.
        translationLookUp.add(new TranslationPair(key, translationContentPairs));
    }
    
    @Contract(value = "null -> false", pure = true)
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")//! Inverting name makes meaning of this method confusing.
    private static boolean hasContent(@Nullable String string) { return string != null && !string.isBlank(); }
    
    @Contract(value = "null -> false", pure = true)
    private static boolean hasContent(String @Nullable [] strings)
    {
        if(strings == null)
            return false;
        
        for(final @Nullable String string: strings)
            if(!hasContent(string))
                return false;
        
        return true;
    }
    
    private @NotNull TypeMirror getSafeTypeMirror(@NotNull String typeName) 
    {
        final TypeElement typeElement = elementUtil.getTypeElement(typeName);
        
        return typeElement != null ? typeElement.asType() : object;
    }
    
    @SuppressWarnings("SameParameterValue")//! Util shouldn't inline detailed paras.
    private @NotNull List<TypeMirror> getSafeTypeMirrors(String @NotNull ... typeNames)
    {
        final List<TypeMirror> types = new ArrayList<>();
        
        for(final String typeName: typeNames)
            types.add(getSafeTypeMirror(typeName));
        
        return types;
    }
    
    private int inTypes(@NotNull TypeMirror mirror, @NotNull List<TypeMirror> typeMirrors)
    {
        for(int index = 0; index < typeMirrors.size(); index++)
        {
            final @Nullable TypeMirror typeMirror = typeUtil.erasure(typeMirrors.get(index));
            
            if(typeUtil.isSameType(typeMirror, object))
            {
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "The type at index %d seems to be null. Stopping procession.".
                        formatted(index)
                );
                return -1;
            }
            
            if(typeUtil.isAssignable(mirror, typeMirror))
                return index;
        }
        
        return -1;
    }
    
    private record TranslationContentPair(@NotNull AutoI18n.Lang lang, @NotNull String content) { }
    
    private record TranslationPair(@NotNull String key, @NotNull List<TranslationContentPair> content) { }
    
    private enum ProcessableType
    {
        HOLDER,
        ITEM,
        BLOCK,
        ENTITY_TYPE,
        CREATIVE_MODE_TAB,
        ENCHANTMENT,
        MOB_EFFECT,
        ATTRIBUTE,
        COMPONENT,
        RESOURCE_LOCATION,
        SUPPLIER,
        UNSUPPORTED(-1);
        
        private final int index;
        
        ProcessableType() { this.index = ordinal(); }
        
        ProcessableType(int index) 
        {
            this.index = index;
        }
        
        public static @NotNull ProcessableType getType(int index)
        {
            for(final ProcessableType type: ProcessableType.values())
                if(index == type.index)
                    return type;
            
            return ProcessableType.UNSUPPORTED;
        }
    }
}
