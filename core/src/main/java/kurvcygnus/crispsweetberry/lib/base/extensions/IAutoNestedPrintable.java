//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.extensions;

import kurvcygnus.crispsweetberry.lib.base.util.TextUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.function.Function;

/**
 * This is a extended interface of <u>{@link INestedPrintable}</u>, which provides auto field map generation with reflection,
 * or <u>{@link java.lang.invoke.MethodHandle MethodHandle}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see INestedPrintable
 * @see OfReflection
 * @see OfRecordHandle
 * @see BaseNestedPrinter.OfAuto
 * @apiNote Reflect is dangerous when the implementer is managed by {@code module-info.java}, and it is slow, so use this on condition.
 */
public interface IAutoNestedPrintable
{
    /**
     * A simple implementation of <u>{@link INestedPrintable}</u>, which creates <u>{@link #getFields() field map}</u> automatically by using
     * <u>{@link java.lang.reflect Reflection}</u>.
     * @apiNote Notes that reflection will be slow,
     * and <span style="color: f84b4b">it will failed to create field map when the implementer is protected by something like Module.</span>
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @see OfRecordHandle OfRecordHandle - If the implementer is a <u>{@link Record}</u>
     * @see BaseNestedPrinter.OfAuto BaseNestedPrinter.OfAuto - If the implementer is a {@code class}, and haven't inherited other class yet.
     */
    non-sealed interface OfReflection<T extends OfReflection<T>> extends INestedPrintable<T>, OfBlacklisted
    {
        /**
         * Decides when reflection goes wrong, whether <u>{@link kurvcygnus.crispsweetberry.lib.base.extensions.IAutoNestedPrintable}</u> shall accept it, or throw the exception.
         */
        default boolean strictOnFail() { return false; }
        
        @Override @ApiStatus.NonExtendable default @NotNull @Unmodifiable INestedFieldMap<T> getFields()
        {
            final var fieldMap = new LinkedHashMap<String, Function<@NotNull T, ? extends @Nullable Object>>();
            final var clazz = this.getClass();
            final var blacklist = getBlacklistedFields();
            
            for(final var field: clazz.getDeclaredFields())
            {
                if(Modifier.isStatic(field.getModifiers()) || field.isSynthetic() || blacklist.contains(field.getName()))
                    continue;
                
                if(!field.trySetAccessible())
                    throw new IllegalStateException(
                        TextUtils.format(
                            "Cannot access class {}: Please check the access permission setups.",
                            this.getClass().getSimpleName()
                        )
                    );
                
                fieldMap.put(
                    field.getName(),
                    inst ->
                    {
                        try { return field.get(inst); }
                        catch(IllegalAccessException e)
                        {
                            if(strictOnFail())
                                throw new IllegalStateException(
                                    TextUtils.format(
                                        "Error Occurred while accessing field \"{}#{}\":\n",
                                        clazz.getName(),
                                        field.getName()
                                    ),
                                    e
                                );
                            
                            return TextUtils.format("Error Occurred while accessing field: ", e);
                        }
                    }
                );
            }
            
            return new NestedFieldMap<>(fieldMap);
        }
    }
    
    non-sealed interface OfRecordHandle<T extends Record & OfRecordHandle<T>> extends INestedPrintable<T>, OfBlacklisted
    {
        @Override @ApiStatus.NonExtendable default @NotNull @Unmodifiable INestedFieldMap<T> getFields()
        {
            final var self = this.getClass();
            
            if(!self.isRecord())
                throw new AssertionError(self.getSimpleName() + "is not record.");
            
            //! Notes that this shouldn't be extracted as an independent constant, because it is [[CallerSensitive]].
            final var methodLookup = MethodHandles.lookup();
            final var fieldMap = new LinkedHashMap<String, Function<@NotNull T, ? extends @NotNull Object>>();
            
            final var blacklist = getBlacklistedFields();
            
            for(final var component: self.getRecordComponents())
            {
                final var fieldName = component.getName();
                
                if(blacklist.contains(fieldName))
                    continue;
                
                try
                {
                    final var getterHandle = methodLookup.unreflect(component.getAccessor());
                    fieldMap.put(
                        fieldName,
                        inst ->
                        {
                            try { return getterHandle.invokeExact(inst); }
                            catch(Throwable e)
                            {
                                throw new IllegalArgumentException(
                                    TextUtils.format(
                                        "Record {}'s getter method \"#{}\" throws an internal exception \"{}\", which shouldn't happen.",
                                        self.getSimpleName(),
                                        fieldName,
                                        e.getClass().getSimpleName(),
                                        e
                                    )
                                );
                            }
                        }
                    );
                }
                catch(IllegalAccessException e)
                {
                    throw new IllegalStateException(
                        TextUtils.format(
                            "Failed to access \"{}#{}\", this usually means that this class is protected by Module System.",
                            self.getSimpleName(),
                            fieldName
                        )
                    );
                }
            }
            
            return new NestedFieldMap<>(fieldMap);
        }
    }
}

sealed interface OfBlacklisted
{
    /**
     * Gets a <u>{@link Set}</u> that contains the name of fields which won't be added into field map.
     */
    default @NotNull Set<String> getBlacklistedFields() { return Set.of(); }
}