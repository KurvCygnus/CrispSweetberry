//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.annotation.processor;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

record ParseContext(
    @NotNull String namespace,
    @NotNull String key,
    @NotNull Element element,
    @NotNull TypeMirror elementType,
    @NotNull Consumer<String> printError,
    @NotNull Types typeUtils,
    @NotNull List<TypeMirror> typeMirrors
)
{
    public ParseContext
    {
        Objects.requireNonNull(namespace, "Param \"namespace\" must not be null!");
        Objects.requireNonNull(key, "Param \"key\" must not be null!");
        Objects.requireNonNull(element, "Param \"element\" must not be null!");
        Objects.requireNonNull(elementType, "Param \"elementType\" must not be null!");
        Objects.requireNonNull(printError, "Param \"printError\" must not be null!");
        Objects.requireNonNull(typeUtils, "Param \"typeUtils\" must not be null!");
        Objects.requireNonNull(typeMirrors, "Param \"typeMirrors\" must not be null!");
    }
    
    @NotNull ParseContext recursed(@NotNull TypeMirror genericArg)
    {
        Objects.requireNonNull(genericArg, "Param \"genericArg\" must not be null!");
        return new ParseContext(
            namespace,
            key,
            element,
            genericArg,
            printError,
            typeUtils,
            typeMirrors
        );
    }
}
