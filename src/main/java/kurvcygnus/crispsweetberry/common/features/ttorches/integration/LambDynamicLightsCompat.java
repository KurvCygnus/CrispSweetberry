package kurvcygnus.crispsweetberry.common.features.ttorches.integration;

import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public record LambDynamicLightsCompat(boolean invert) implements EntityLuminance
{
    @Override
    public @NotNull Type type()
    {
        return null;
    }
    
    @Override
    public @Range(from = 0L, to = 15L) int getLuminance(@NotNull ItemLightSourceManager itemLightSourceManager, @NotNull Entity entity)
    {
        return 0;
    }
}
