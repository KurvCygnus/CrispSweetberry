package kurvcygnus.crispsweetberry.common.registries;

import kurvcygnus.crispsweetberry.CrispSweetberry;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownTorchEntity;
import kurvcygnus.crispsweetberry.utils.registry.IRegistrant;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public enum CrispEntities implements IRegistrant
{
    INSTANCE;
    
    @Override
    public void register(@NotNull IEventBus bus) { CRISP_ENTITY_TYPE_REGISTER.register(bus); }
    
    @Override
    public boolean isFeature() { return false; }
    
    @Override
    public @NotNull String getJob() { return "Misc Entities"; }
    
    @Override
    public int getPriority() { return 2; }
    
    public static final DeferredRegister<EntityType<?>> CRISP_ENTITY_TYPE_REGISTER =
        DeferredRegister.create(Registries.ENTITY_TYPE, CrispSweetberry.NAMESPACE);
    
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownTorchEntity>> THROWN_TORCH = CRISP_ENTITY_TYPE_REGISTER.register("thrown_torch", () ->
        EntityType.Builder.<ThrownTorchEntity>of(ThrownTorchEntity::new, MobCategory.MISC).
            sized(0.25F, 0.25F).
            updateInterval(10).
            noSummon().
            build("thrown_torch")
    );
}