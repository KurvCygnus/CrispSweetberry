package kurvcygnus.crispsweetberry.common.features.ttorches.items;

import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.abstracts.AbstractThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.projectile.ITriProjectileFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public final class ThrowableTorchItem extends AbstractThrowableTorchItem<ThrownTorchEntity>
{
    public ThrowableTorchItem() { super(new Properties()); }
    
    @Override
    protected @NotNull BiFunction<LivingEntity, Level, ThrownTorchEntity> getPlayerUsedProjectile() { return ThrownTorchEntity::new; }
    
    @Override
    protected @NotNull ITriProjectileFunction<ThrownTorchEntity> getDispenserUsedProjectile() { return ThrownTorchEntity::new; }
}
