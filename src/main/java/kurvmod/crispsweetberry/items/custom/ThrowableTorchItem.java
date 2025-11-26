package kurvmod.crispsweetberry.items.custom;

import kurvmod.crispsweetberry.entities.custom.throwntorch.ThrownTorchEntity;
import kurvmod.crispsweetberry.items.custom.abstractbase.AbstractThrowableTorchItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class ThrowableTorchItem extends AbstractThrowableTorchItem<ThrownTorchEntity>
{
    public ThrowableTorchItem(Properties properties) { super(properties); }
    
    @Override
    protected float getAccuracy() { return ALWAYS_ACCURATE; }
    
    @Override
    protected float getThrowVelocity() { return DEFAULT_TORCH_THROW_VELOCITY; }
    
    @Override
    protected int getThrowCooldown() { return DEFAULT_THROW_COOLDOWN; }
    
    @Override
    protected @NotNull SoundEvent getThrowSound() { return DEFAULT_THROW_SOUND; }
    
    @Override
    protected @NotNull ThrownTorchEntity createProjectile(LivingEntity shooter, Level level) { return new ThrownTorchEntity(shooter, level); }
    
    @Override
    protected @NotNull ThrownTorchEntity createProjectile(double x, double y, double z, Level level) { return new ThrownTorchEntity(x, y, z, level); }
}
