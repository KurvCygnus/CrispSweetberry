package kurvcygnus.crispsweetberry.utils.projectile;

import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;

@FunctionalInterface
public interface ITriProjectileFunction<E extends ThrowableItemProjectile>
{
    E apply(double x, double y, double z, Level level);
}
