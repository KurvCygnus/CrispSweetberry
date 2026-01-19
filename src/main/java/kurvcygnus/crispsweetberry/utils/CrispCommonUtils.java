package kurvcygnus.crispsweetberry.utils;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class CrispCommonUtils
{
    private CrispCommonUtils() { throw new IllegalAccessError(); }
    
    @Contract("_ -> new")
    public static ResourceLocation getModNamespacedLocation(@NotNull String assetLocation) 
        { return ResourceLocation.fromNamespaceAndPath(CrispSweetberry.MOD_ID, assetLocation); }
}
