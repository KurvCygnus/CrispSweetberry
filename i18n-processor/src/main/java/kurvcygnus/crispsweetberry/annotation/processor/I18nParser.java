//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.annotation.processor;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.type.DeclaredType;
import java.util.function.Function;

final class I18nParser
{
    static final String NAMESPACE_PLACE_HOLDER = "~@namespace";
    static final String KEY_PLACE_HOLDER = "~@key";
    
    private I18nParser() { throw new IllegalAccessError("Class \"I18nParser\" is not meant to be instantized!"); }
    
    static @NotNull String recursiveParse(@NotNull ParseContext context, @NotNull Function<ParseContext, String> recurse)
    {
        final var element = context.element();
        final var elementType = context.elementType();
        final var printError = context.printError();
        final var typeUtils = context.typeUtils();
        
        if(elementType instanceof DeclaredType declaredType)
        {
            final var args = declaredType.getTypeArguments();
            
            if(args.isEmpty())
                printError.accept("Can't get the generic arg of %s.".formatted(element.getSimpleName().toString()));
            
            final var rawArg = args.getFirst();
            
            for(final var typeMirror: context.typeMirrors())
                if(typeUtils.isAssignable(typeUtils.erasure(rawArg), typeMirror))
                    return recurse.apply(context.recursed(rawArg));
            
            printError.accept("The detailed generic arg of %s can't be assigned from supported types.".formatted(element.getSimpleName().toString()));
        }
        
        printError.accept("%s has abnormal elementType!".formatted(element.getSimpleName().toString()));
        
        return "Ouch";
    }
    
    static @NotNull String simplestParse(@NotNull String prefix, @NotNull ParseContext context)
        { return simpleParse("%s.%s.%s".formatted(prefix, NAMESPACE_PLACE_HOLDER, KEY_PLACE_HOLDER), context); }
    
    static @NotNull String simpleParse(@NotNull String template, @NotNull ParseContext parseContext)
        { return template.replaceAll(NAMESPACE_PLACE_HOLDER, parseContext.namespace()).replace(KEY_PLACE_HOLDER, parseContext.key()); }
    
    static @NotNull String polymorphismParse(@NotNull ParseContext context)
    {
        final var key = context.key();
        final var namespace = context.namespace();
        
        if(key.contains("%s.".formatted(namespace)))
            return key;
        
        //* Add namespace if the original key doesn't have one.
        return "%s.%s".formatted(namespace, key);
    }
}