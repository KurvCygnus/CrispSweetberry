package kurvcygnus.crispsweetberry.utils.misc;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public final class CrispFunctionalUtils
{
    private CrispFunctionalUtils() { throw new IllegalAccessError("Class \"CrispFunctionalUtils\" is not meant to be instantized!"); }
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
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
    
    public static <E extends Throwable> void throwIf(boolean condition, @NotNull Supplier<E> supplier) throws E
    {
        requireNonNull(supplier, "Param \"supplier\" cannot be null!(Param 2, Bro, serious?)");
        
        if(!condition)
            return;
        
        throw supplier.get();
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
