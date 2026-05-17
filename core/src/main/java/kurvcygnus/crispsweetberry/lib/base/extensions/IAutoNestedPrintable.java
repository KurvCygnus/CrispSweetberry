//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.extensions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * This is a extended interface of <u>{@link INestedPrintable}</u>, which provides auto field map generation with reflect.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see INestedPrintable
 * @apiNote Reflect is dangerous when the implementer is managed by {@code module-info.java}, and it is slow, so use this on condition.
 */
public interface IAutoNestedPrintable<T extends IAutoNestedPrintable<T>> extends INestedPrintable<T>
{
    /**
     * Gets a <u>{@link Set}</u> that contains the name of fields which won't be added into field map.
     */
    default @NotNull @Unmodifiable Set<String> getBlacklistedFields() { return Set.of(); }
    
    /**
     * Decides when reflection goes wrong, whether <u>{@link IAutoNestedPrintable}</u> shall accept it, or throw the exception.
     */
    default boolean gonnaBeCruel() { return false; }
    
    @Override default @NotNull @Unmodifiable Map<String, Function<T, @Nullable Object>> getFields()
    {
        final var fieldMap = new LinkedHashMap<String, Function<T, @Nullable Object>>();
        final var clazz = this.getClass();
        final var blacklist = getBlacklistedFields();
        
        for(final Field field: clazz.getDeclaredFields())
        {
            if(Modifier.isStatic(field.getModifiers()) || field.isSynthetic() || blacklist.contains(field.getName()))
                continue;
            
            field.setAccessible(true);
            
            fieldMap.put(
                field.getName(),
                inst ->
                {
                    try { return field.get(inst); }
                    catch(IllegalAccessException e)
                    {
                        if(gonnaBeCruel())
                            throw new IllegalStateException("Error Occurred while accessing field \"%s#%s\":\n".formatted(clazz.getName(), field.getName()), e);
                        
                        return "Error Occurred while accessing field: %s".formatted(e.getMessage());
                    }
                }
            );
        }
        
        return Collections.unmodifiableMap(fieldMap);
    }
}
