package kurvcygnus.crispsweetberry.common.features.temporarytorches.entities;

import kurvcygnus.crispsweetberry.common.features.temporarytorches.client.renderers.ThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.abstracts.AbstractThrownTorchEntity;
import kurvcygnus.crispsweetberry.common.registries.CrispEntities;
import kurvcygnus.crispsweetberry.common.registries.CrispItems;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * The <b>entity version</b> of the item <b>throwable torch</b>.
 * @see ThrownTorchRenderer Renderer
 * @since CSB 1.0 release
 * @author Kurv Cygnus
 */
public final class ThrownTorchEntity extends AbstractThrownTorchEntity
{
    public ThrownTorchEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> entityType, @NotNull Level level) { super(entityType, level); }
    
    public ThrownTorchEntity(double x, double y, double z, @NotNull Level level) { super(CrispEntities.THROWN_TORCH.get(), x, y, z, level); }
    
    public ThrownTorchEntity(@NotNull LivingEntity shooter, @NotNull Level level) { super(CrispEntities.THROWN_TORCH.get(), shooter, level); }
    
    @Override
    protected @NotNull SimpleParticleType[] getLongerParticleStateList() { return DEFAULT_LONGER_PARTICLE_STATE_LIST; }
    
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