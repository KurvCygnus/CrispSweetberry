//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.lib.base.trait;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This is a trait-styled interface for <u>{@link org.spongepowered.asm.mixin.Mixin mixin}</u> classes,
 * which provides some practical and convenient methods for developing.
 * @param <T> The type of this mixin class shall inject.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @apiNote Despite it is obvious, just remember that don't use this on normal classes.
 */
public interface IMixinCaster<T>
{
    @SuppressWarnings("unchecked")//! This is daily stuff in mixin, man.
    default @NotNull T _$csb_lib_getSelf() { return (T) this; }
    default <U> U _$csb_lib_mapSelf(@NotNull Function<? super T, ? extends U> mapper)
    {
        if(_$csb_lib_withNonNullCheck())
            Objects.requireNonNull(mapper, "Param \"mapper\" must not be null!");
        return mapper.apply(_$csb_lib_getSelf());
    }
    default void _$csb_lib_doSelf(@NotNull Consumer<? super T> consumer)
    {
        if(_$csb_lib_withNonNullCheck())
            Objects.requireNonNull(consumer, "Param \"consumer\" must not be null!");
        consumer.accept(_$csb_lib_getSelf());
    }
    default boolean _$csb_lib_testSelf(@NotNull Predicate<? super T> predicate) 
    {
        if(_$csb_lib_withNonNullCheck())
            Objects.requireNonNull(predicate, "Param \"predicate\" must not be null!");
        return (predicate.test(_$csb_lib_getSelf()));
    }
    default void _$csb_lib_doWhenSelf(@NotNull Predicate<? super T> predicate, @NotNull Consumer<? super T> consumer)
    {
        if(_$csb_lib_testSelf(predicate))
            _$csb_lib_doSelf(consumer);
    }
    default void _$csb_lib_doWhenSelf(@NotNull Predicate<? super T> predicate, @NotNull Runnable runnable)
    {
        if(_$csb_lib_withNonNullCheck())
            Objects.requireNonNull(runnable, "Param \"runnable\" must not be null!");
        if(_$csb_lib_testSelf(predicate))
            runnable.run();
    }
    
    default boolean _$csb_lib_withNonNullCheck() { return false; }
}
