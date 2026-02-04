package kurvcygnus.crispsweetberry.common.features.ttorches;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum TTorchRegistries implements IRegistrant
{
    INSTANCE;
    
    @Override
    public void register(@NotNull IEventBus bus) 
    {
        //REGISTRIES.forEach(register ->  register.register(bus));
    }
    
    @Override
    public boolean isFeature() { return true; }
    
    @Override
    public @NotNull String getJob() { return "Temporary Torches"; }
    
    @Override
    public int getPriority() { return 6; }
    
    private static final DeferredRegister<Item> THROWABLE_TORCH_REGISTER = DeferredRegister.createItems(CrispSweetberry.ID);
    private static final DeferredRegister<Block> TEMPORARY_TORCH_REGISTER = DeferredRegister.createBlocks(CrispSweetberry.ID); 
    
    public static final List<DeferredRegister<?>> REGISTRIES = List.of(
        THROWABLE_TORCH_REGISTER,
        TEMPORARY_TORCH_REGISTER
    );
}
