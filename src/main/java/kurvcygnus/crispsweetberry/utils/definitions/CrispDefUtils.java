package kurvcygnus.crispsweetberry.utils.definitions;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This util is used for generate definitions related stuff faster, like <u>{@link ResourceLocation}</u>.
 * @since 1.0 Release
 */
public final class CrispDefUtils
{
    private CrispDefUtils() { throw new IllegalAccessError(); }
    
    @Contract("_ -> new")
    public static @NotNull ResourceLocation getModNamespacedLocation(@NotNull String assetLocation) 
    {
        Objects.requireNonNull(assetLocation, "Param \"assetLocation\" cannot be null!");
        return ResourceLocation.fromNamespaceAndPath(CrispSweetberry.MOD_ID, assetLocation);
    }
}
