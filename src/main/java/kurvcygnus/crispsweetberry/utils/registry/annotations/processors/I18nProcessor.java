//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.registry.annotations.processors;

import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
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
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;

@SupportedAnnotationTypes("kurvcygnus.crispsweetberry.utils.registry.annotations.AutoI18n")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public final class I18nProcessor extends AbstractProcessor
{
    private static final int HOLDER_INDEX = 0;
    private static final int ITEM_INDEX = 1;
    private static final int BLOCK_INDEX = 2;
    private static final int ENTITY_TYPE_INDEX = 3;
    private static final int CREATIVE_TAB_INDEX = 4;
    private static final int ENCHANTMENT_INDEX = 5;
    private static final int STATUS_EFFECT_INDEX = 6;
    private static final int ATTRIBUTE_INDEX = 7;
    private static final int COMPONENT_INDEX = 8;
    private static final int RESOURCE_LOCATION_INDEX = 9;
    private static final int SUPPLIER_INDEX = 10;
    
    private Messager messager;
    private Filer filer;
    private Types typeUtil;
    private Elements elementUtil;
    private @Nullable String namespace;
    private TypeMirror object;
    private final HashMap<String, ArrayList<TranslationContentPair>> groups = new HashMap<>();
    private final ArrayList<TranslationPair> translationTable = new ArrayList<>();
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.typeUtil = processingEnv.getTypeUtils();
        this.elementUtil = processingEnv.getElementUtils();
        this.namespace = processingEnv.getOptions().get("modid");
        this.object = elementUtil.getTypeElement("java.lang.Object").asType();
    }
    
    @Override
    public boolean process(@NotNull Set<? extends TypeElement> annotations, @NotNull RoundEnvironment roundEnv)
    {
        if(namespace == null || namespace.isBlank() || namespace.equals("unknown"))
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                """
                    Processing error: Unknown namespace. Go to build.gradle and configure it first.
                    
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
        
        if(roundEnv.processingOver())
        {
            //? TODO: Json logic
            return false;
        }
        
        if(annotations.isEmpty())
            return false;
        
        for(final Element element: roundEnv.getElementsAnnotatedWith(AutoI18n.class))
        {
            if(element.getKind() != ElementKind.FIELD)
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Processing error: %s must be a field.".formatted(element.getSimpleName().toString()),
                    element
                );
            
            final AutoI18n autoI18n = element.getAnnotation(AutoI18n.class);
            
            if(autoI18n == null)
            {
                messager.printMessage(
                    Diagnostic.Kind.NOTE,
                    "Processing expr: The AutoI18n annotation of %s seems to be null. Skipped.".
                        formatted(element.getSimpleName().toString()),
                    element
                );
                
                continue;
            }
            
            final String group = autoI18n.group();
            final String[] translations = autoI18n.value();
            final String key = autoI18n.key().isBlank() ?
                element.getSimpleName().toString() :
                autoI18n.key();
            
            final String fullKeyScope = parseKeyScope(element, element.asType(), key);
            
            if(!hasContent(group) && !hasContent(translations))
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Processing error: %s doesn't have a group, and has no translations, or translations include an illegal string.".
                        formatted(element.getSimpleName().toString()),
                    element
                );
            
            CrispFunctionalUtils.doIf(hasContent(translations) && hasContent(group),
                () -> this.parseTranslations(element, translations, fullKeyScope, group)
            );
            CrispFunctionalUtils.doIf(hasContent(translations) && !hasContent(group),
                () -> this.parseTranslations(element, translations, fullKeyScope, null)
            );
            CrispFunctionalUtils.doIf(!hasContent(translations) && hasContent(group),
                () -> this.parseTranslations(element, null, fullKeyScope, group)
            );
        }
        
        return true;
    }
    
    private @NotNull String parseKeyScope(@NotNull Element element, @NotNull TypeMirror elementType, @NotNull String key)
    {
        //! All actually nullable. This can be caused by incorrect configuration of dependency, and other reasons.
        //! In #getTypeMirrorWithClass(Class<?>), we replaced null with Object's TypeMirror to make sure helper methods
        //! are safe to use.
        final TypeMirror MC_HOLDER = getTypeMirrorWithClass(Holder.class);
        final TypeMirror MC_ITEM = getTypeMirrorWithClass(Item.class);
        final TypeMirror MC_BLOCK = getTypeMirrorWithClass(Block.class);
        final TypeMirror MC_ENTITY_TYPE = getTypeMirrorWithClass(EntityType.class);
        final TypeMirror MC_CREATIVE_TAB = getTypeMirrorWithClass(CreativeModeTab.class);
        final TypeMirror MC_ENCHANTMENT = getTypeMirrorWithClass(Enchantment.class);
        final TypeMirror MC_STATUS_EFFECT = getTypeMirrorWithClass(MobEffect.class);
        final TypeMirror MC_ATTRIBUTE = getTypeMirrorWithClass(Attribute.class);
        final TypeMirror MC_COMPONENT = getTypeMirrorWithClass(Component.class);
        final TypeMirror MC_RESOURCE_LOCATION = getTypeMirrorWithClass(ResourceLocation.class);
        final TypeMirror JAVA_SUPPLIER = getTypeMirrorWithClass(Supplier.class);
        
        final List<TypeMirror> TYPE_MIRRORS = List.of(
            MC_HOLDER,
            MC_ITEM,
            MC_BLOCK,
            MC_ENTITY_TYPE,
            MC_CREATIVE_TAB,
            MC_ENCHANTMENT,
            MC_STATUS_EFFECT,
            MC_ATTRIBUTE,
            MC_COMPONENT,
            MC_RESOURCE_LOCATION,
            JAVA_SUPPLIER
        );
        
        switch(inTypes(elementType, TYPE_MIRRORS))
        {
            case -1 -> messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Can't deduce the type of %s. Go check dependency configuration.".
                    formatted(element.getSimpleName().toString()),
                element
            );
            case HOLDER_INDEX, SUPPLIER_INDEX ->
            {
                if(elementType instanceof DeclaredType declaredType)
                {
                    final List<? extends TypeMirror> args = declaredType.getTypeArguments();
                    
                    if(args.isEmpty())
                        messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "Processing error: Can't get the generic arg of %s.".
                                formatted(element.getSimpleName().toString()),
                            element
                        );
                    
                    final TypeMirror rawArg = args.getFirst();
                    
                    for(final TypeMirror typeMirror: TYPE_MIRRORS)
                    {
                        if(typeUtil.isAssignable(rawArg, typeMirror))
                            return parseKeyScope(element, rawArg, key);
                    }
                    
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Processing error: The detailed generic arg of %s can't be assigned from supported types.".
                            formatted(element.getSimpleName().toString()),
                        element
                    );
                }
                messager.printMessage(
                    Diagnostic.Kind.ERROR,
                    "Processing error: %s has abnormal elementType!".
                        formatted(element.getSimpleName().toString()),
                    element
                );
            }
            case ITEM_INDEX -> { return "item.%s.%s".formatted(namespace, key); }
            case BLOCK_INDEX -> { return "block.%s.%s".formatted(namespace, key); }
            case ENTITY_TYPE_INDEX -> { return "entity.%s.%s".formatted(namespace, key); }
            case CREATIVE_TAB_INDEX -> { return "%s.creativetab.%s".formatted(namespace, key); }
            case ENCHANTMENT_INDEX -> { return "enchantment.%s.%s".formatted(namespace, key); }
            case STATUS_EFFECT_INDEX -> { return "effect.%s.%s".formatted(namespace, key); }
            case ATTRIBUTE_INDEX -> { return "attribute.%s.%s".formatted(namespace, key); }
            case RESOURCE_LOCATION_INDEX -> { return key; }
            case COMPONENT_INDEX -> 
            {
                if(key.contains(Objects.requireNonNull(namespace)))
                    return key;
                
                return "%s.%s".formatted(namespace, key);
            }
            default -> messager.printMessage(
                Diagnostic.Kind.ERROR,
                "Processing error: %s holds an illegal, or unknown value!".
                    formatted(element.getSimpleName().toString()),
                element
            );
        }
        
        return key;//* Actually, this is theoretically unachievable.
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
                        "Processing Error: Mismatch translation grammar: %s -> at %s, index %d".
                            formatted(translation, element.getSimpleName().toString(), index),
                        element
                    );
                
                final String lang = matcher.group(1);
                final String content = matcher.group(2);
                
                if(!hasContent(lang))
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Processing error: Language is empty -> at %s, index %d".
                            formatted(element.getSimpleName().toString(), index),
                        element
                    );
                
                if(!hasContent(content))
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Processing error: Content is empty -> at %s, index %d".
                            formatted(element.getSimpleName().toString(), index),
                        element
                    );
                
                final AutoI18n.Lang language = AutoI18n.Lang.parse(lang);
                
                translationContentPairs.add(new TranslationContentPair(language, content));
            }
        }
        
        if(group != null)
        {
            if(!translationContentPairs.isEmpty())//* Initialize group case.
            {
                CrispFunctionalUtils.doIf(groups.containsKey(group), () ->
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Processing error: Group %s has been defined for multiple times!".
                            formatted(group)
                    )
                );
                
                groups.put(group, translationContentPairs);
            }
            else//* Use group case.
            {
                CrispFunctionalUtils.doIf(!groups.containsKey(group), () ->
                    messager.printMessage(
                        Diagnostic.Kind.ERROR,
                        "Processing error: Group \"%s\" doesn't exist in cached groups!".
                            formatted(group),
                        element
                    )
                );
                
                translationContentPairs = groups.get(group);
            }
        }
        
        translationTable.add(new TranslationPair(key, translationContentPairs));
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
    
    private @NotNull TypeMirror getTypeMirrorWithClass(@NotNull Class<?> clazz) 
    {
        final TypeElement typeElement = elementUtil.getTypeElement(clazz.getCanonicalName());
        
        return typeElement != null ? typeElement.asType() : object;
    }
    
    private int inTypes(@NotNull TypeMirror mirror, @NotNull List<TypeMirror> typeMirrors)
    {
        for(int index = 0; index < typeMirrors.size(); index++)
        {
            final @Nullable TypeMirror typeMirror = typeMirrors.get(index);
            
            if(typeUtil.isSameType(typeMirror, object))
            {
                messager.printMessage(
                    Diagnostic.Kind.WARNING,
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
    
    private record TranslationContentPair(@Nullable AutoI18n.Lang lang, @NotNull String content) { }
    
    private record TranslationPair(@NotNull String key, @NotNull List<TranslationContentPair> content) { }
}
