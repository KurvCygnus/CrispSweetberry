package kurvcygnus.crispsweetberry.common.features.carrycrate;

import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.NotNull;

public enum CarryCrateRegistries implements IRegistrant
{
    INSTANCE;
    
    @Override
    public void register(@NotNull IEventBus bus)
    {
        
    }
    
    @Override
    public boolean isFeature() { return true; }
    
    @Override
    public @NotNull String getJob() { return "Carry Crate"; }
    
    @Override
    public int getPriority() { return -1; }
}