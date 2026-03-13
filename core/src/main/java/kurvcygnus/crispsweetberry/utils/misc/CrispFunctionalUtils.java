//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.misc;

import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @since 1.0 Release
 */
public final class CrispFunctionalUtils
{
    private CrispFunctionalUtils() { throw new IllegalAccessError("Class \"CrispFunctionalUtils\" is not meant to be instantized!"); }
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    public static <B extends Block> @NotNull Function<BlockBehaviour.Properties, B> noArgCodec(@NotNull Supplier<B> construct)
    {
        requireNonNull(construct, "Param \"construct\" must not be null!");
        return UwU -> construct.get();
    }
    
    public static <T> void doIfNonNull(@Nullable T object, @NotNull Consumer<T> action)
    {
        requireNonNull(action, "Param \"action\" must not be null!");
        if(object == null)
        {
            LOGGER.warn("Current object appears to be null.");
            return;
        }
        
        action.accept(object);
    }
    
    public static void doIf(boolean condition, @NotNull Runnable action)
    {
        requireNonNull(action, "Param \"action\" must not be null!");
        
        if(condition)
            action.run();
    }
    
    public static <E extends Throwable> void throwIf(boolean condition, @NotNull String message, @NotNull Function<String, E> function) throws E
    {
        requireNonNull(message, "Param \"message\" must not be null!");
        
        if(!condition)
            return;
        
        throw function.apply(message);
    }
    
    @Contract("null, _ -> fail; !null, _ -> param1")
    public static <T, E extends Throwable> @NotNull T checkReturn(@Nullable T object, @NotNull Supplier<E> supplier) throws E
    {
        requireNonNull(supplier, "Param \"supplier\" must not be null!");
        
        if(object == null)
            throw supplier.get();
        
        return object;
    }
}
