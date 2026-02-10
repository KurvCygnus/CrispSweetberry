package kurvcygnus.crispsweetberry.common.features.ttorches.items;

import kurvcygnus.crispsweetberry.common.features.ttorches.entities.ThrownRedstoneTorchEntity;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.abstracts.AbstractThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.projectile.ITriProjectileFunction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;

public final class ThrowableRedstoneTorchItem extends AbstractThrowableTorchItem<ThrownRedstoneTorchEntity>
{
    public ThrowableRedstoneTorchItem() { super(new Properties()); }
    
    @Override
    protected @NotNull BiFunction<LivingEntity, Level, ThrownRedstoneTorchEntity> getPlayerUsedProjectile() { return ThrownRedstoneTorchEntity::new; }
    
    @Override
    protected @NotNull ITriProjectileFunction<ThrownRedstoneTorchEntity> getDispenserUsedProjectile() { return ThrownRedstoneTorchEntity::new; }
}
