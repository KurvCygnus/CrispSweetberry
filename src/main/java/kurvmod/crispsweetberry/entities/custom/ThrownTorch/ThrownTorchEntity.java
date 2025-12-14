package kurvmod.crispsweetberry.entities.custom.throwntorch;

import kurvmod.crispsweetberry.entities.CrispEntities;
import kurvmod.crispsweetberry.entities.custom.abstractbase.AbstractThrownTorchEntity;
import kurvmod.crispsweetberry.items.CrispItems;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * The <b>entity version</b> of the item <b>throwable torch</b>.
 * @since CSB 1.0 release
 * @author Kurv
 */
public final class ThrownTorchEntity extends AbstractThrownTorchEntity
{
    
    public ThrownTorchEntity(EntityType<? extends AbstractThrownTorchEntity> entityType, Level level) { super(entityType, level); }
    
    public ThrownTorchEntity(double x, double y, double z, Level level) { super(CrispEntities.THROWN_TORCH.get(), x, y, z, level); }
    
    public ThrownTorchEntity(LivingEntity shooter, Level level) { super(CrispEntities.THROWN_TORCH.get(), shooter, level); }
    
    @Override
    protected SimpleParticleType[] getLongerParticleStateList() { return DEFAULT_LONGER_PARTICLE_STATE_LIST; }
    
    @Override
    protected @NotNull Item getDefaultItem() { return CrispItems.THROWABLE_TORCH.value(); }
    
    @Override
    protected int getLongerParticleFrequency() { return DEFAULT_LONGER_PARTICLE_FREQUENCY; }
    
    @Override
    protected int getShorterParticleFrequency() { return DEFAULT_SHORTER_PARTICLE_FREQUENCY; }
    
    @Override
    protected boolean getShouldCheckLiquidsFlag() { return true; }
    
    @Override
    protected boolean getNoSmokeWhenBurnedOutFlag() { return true; }
    
    @Override
    protected boolean getShouldCheckFireResistMobFlag() { return true; }
    
    @Override
    protected boolean getShouldLitMobFlag() { return true; }
}