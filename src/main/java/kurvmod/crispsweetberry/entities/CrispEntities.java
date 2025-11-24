package kurvmod.crispsweetberry.entities;

import kurvmod.crispsweetberry.CrispSweetberry;
import kurvmod.crispsweetberry.entities.custom.throwntorch.ThrownTorchEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CrispEntities
{
    private CrispEntities() {}
    
    public static final DeferredRegister<EntityType<?>> CRISP_ENTITY_TYPE_REGISTER =
        DeferredRegister.create(Registries.ENTITY_TYPE, CrispSweetberry.MOD_ID);
    
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownTorchEntity>> THROWN_TORCH = CRISP_ENTITY_TYPE_REGISTER.register("thrown_torch", () ->
        EntityType.Builder.<ThrownTorchEntity>of(ThrownTorchEntity::new, MobCategory.MISC)
            .sized(0.25F, 0.25F)
            .updateInterval(10)
            .noSummon()
            .build("thrown_torch")
    );
}