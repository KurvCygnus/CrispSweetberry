package kurvcygnus.crispsweetberry.utils;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;

/**
 * @since CSB Release 1.0
 */
public final class CrispVisualUtils
{
    private CrispVisualUtils() { throw new IllegalAccessError(); }
    
    @Contract("null, _, _, _, _, _, _, _ -> fail; _, _, _, _, _, _, _, null -> fail")
    public static void addParticles(Level level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, ParticleOptions... particleOptions)
    {
        if(level == null)
            throw new NullPointerException("Level is null, please make sure this method is called at the proper time!");
        
        for(ParticleOptions particle: particleOptions)
            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
    }
}
