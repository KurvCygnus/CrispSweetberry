//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.annotation.processor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

enum ProcessableType
{
    HOLDER("net.minecraft.core.Holder", I18nParser::recursiveParse),
    ITEM(
        "net.minecraft.world.item.Item",
        (context, func) -> I18nParser.simplestParse("item", context)
    ),
    BLOCK(
        "net.minecraft.world.level.block.Block",
        (context, func) -> I18nParser.simplestParse("block", context)
    ),
    ENTITY_TYPE(
        "net.minecraft.world.entity.EntityType",
        (context, func) -> I18nParser.simplestParse("entity", context)
    ),
    CREATIVE_MODE_TAB(
        "net.minecraft.world.item.CreativeModeTab",
        (context, func) -> I18nParser.simpleParse("@namespace.creativetab.@key", context)
    ),
    ENCHANTMENT(
        "net.minecraft.world.item.enchantment.Enchantment",
        (context, func) -> I18nParser.simplestParse("enchantment", context)
    ),
    MOB_EFFECT(
        "net.minecraft.world.effect.MobEffect",
        (context, func) -> I18nParser.simplestParse("effect", context)
    ),
    ATTRIBUTE(
        "net.minecraft.world.entity.ai.attributes.Attribute",
        (context, func) -> I18nParser.simplestParse("attribute", context)
    ),
    COMPONENT(
        "net.minecraft.network.chat.Component",
        (context, func) -> I18nParser.polymorphismParse(context)
    ),
    RESOURCE_LOCATION(
        "net.minecraft.resources.ResourceLocation",
        (context, func) -> context.key()
    ),
    KEY_MAPPING(
        "net.minecraft.client.KeyMapping",
        (context, func) -> I18nParser.polymorphismParse(context)
    ),
    SUPPLIER("java.util.function.Supplier", I18nParser::recursiveParse),
    UNSUPPORTED(
        I18nProcessor.UNSUPPORTED_INDEX,
        "java.lang.Object",
        (context, func) ->
        {
            context.printError().accept(
                "Can't deduce the type of %s. Go check dependency configuration.".
                    formatted(context.element().getSimpleName().toString())
            );
            
            return "Ouch";
        }
    );
    
    private final int index;
    final String fqcn;
    private final BiFunction<ParseContext, Function<ParseContext, String>, String> parseFunction;
    
    ProcessableType(@NotNull String fqcn, @NotNull BiFunction<ParseContext, Function<ParseContext, String>, String> parseFunction)
    {
        this.index = ordinal();
        this.fqcn = fqcn;
        this.parseFunction = parseFunction;
    }
    
    ProcessableType(int index, @NotNull String fqcn, @NotNull BiFunction<ParseContext, Function<ParseContext, String>, String> parseFunction)
    {
        this.index = index;
        this.fqcn = fqcn;
        this.parseFunction = parseFunction;
    }
    
    @NotNull String parse(@NotNull ParseContext context, @NotNull Function<ParseContext, String> parseFunction) { return this.parseFunction.apply(context, parseFunction); }
    
    static @NotNull ProcessableType getType(int index)
    {
        for(final ProcessableType type: ProcessableType.values())
            if(index == type.index)
                return type;
        
        return UNSUPPORTED;
    }
    
    static @NotNull @Unmodifiable List<TypeMirror> initializeTypes(@NotNull Elements elementUtil, @NotNull TypeMirror fallback)
    {
        final List<TypeMirror> types = new ArrayList<>();
        
        for(final ProcessableType type: ProcessableType.values())
            if(!type.equals(UNSUPPORTED))
                types.add(type.getSafeTypeMirror(elementUtil, fallback));
        
        return Collections.unmodifiableList(types);
    }
    
    @NotNull TypeMirror getSafeTypeMirror(@NotNull Elements elementUtil, @NotNull TypeMirror fallback)
    {
        final TypeElement typeElement = elementUtil.getTypeElement(this.fqcn);
        return typeElement != null ? typeElement.asType() : fallback;
    }
}
