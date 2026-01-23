package kurvcygnus.crispsweetberry.utils.misc;

import com.mojang.logging.LogUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;

import java.util.Objects;

/**
 * @since 1.0 Release
 */
public final class CrispVisualUtils
{
    private CrispVisualUtils() { throw new IllegalAccessError(); }
    
    private static final Logger LOGGER = LogUtils.getLogger();
    
    @Contract("null, _, _, _, _, _, _, _ -> fail; _, _, _, _, _, _, _, null -> fail")
    public static void addParticles(Level level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, ParticleOptions... particleOptions)
    {
        if(level == null)
            throw new NullPointerException("Level is null, please make sure this method is called at the proper time!");
        
        if(xSpeed < 0 || ySpeed < 0 || zSpeed < 0)
            LOGGER.warn("Using negative speed value is not recommended. Speeds: x: {}, y: {}, z: {}", xSpeed, ySpeed, zSpeed);
        
        for(int index = 0; index < particleOptions.length; index++)
        {
            ParticleOptions particle = particleOptions[index];
            Objects.requireNonNull(particle, "Particle can not be null! Null particle at index: " + index);
            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }
}
