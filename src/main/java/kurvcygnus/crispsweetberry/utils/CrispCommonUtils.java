package kurvcygnus.crispsweetberry.utils;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @since 1.0 Release
 */
public final class CrispCommonUtils
{
    private CrispCommonUtils() { throw new IllegalAccessError(); }
    
    @Contract("_ -> new")
    public static @NotNull ResourceLocation getModNamespacedLocation(@NotNull String assetLocation) 
    {
        Objects.requireNonNull(assetLocation, "assetLocation cannot be null!");
        return ResourceLocation.fromNamespaceAndPath(CrispSweetberry.MOD_ID, assetLocation);
    }
}
