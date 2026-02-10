package kurvcygnus.crispsweetberry.integrations;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import org.jetbrains.annotations.NotNull;

public final class LambDynamicLightsInitializer implements DynamicLightsInitializer
{
    @Override
    public void onInitializeDynamicLights(@NotNull DynamicLightsContext context)
    {
        
    }
    
    @Override @Deprecated(forRemoval = true)
    @SuppressWarnings({"UnstableApiUsage", "removal"})//! Has to be implemented. No choice.
    public void onInitializeDynamicLights(ItemLightSourceManager itemLightSourceManager) 
    {
        //! Intentionally empty. Required by BROKEN API design.
    }
}
