package kurvmod.crispsweetberry.utils;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;

/**
 * The class which holds some practical methods for mod developer(me UwU).
 */
public final class CrispUtils
{
    private CrispUtils() {}
    
    /**
     * An encapsulated method which <b>plays multiple particles at the same condition</b>.
     * @see net.minecraft.world.level Original method
     */
    public static void addParticles(Level level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, ParticleOptions... particleOptions)
    {
        for(ParticleOptions particle: particleOptions)
            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
    }
}
