package kurvcygnus.crispsweetberry.common.features.ttorches.entities;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.ThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts.AbstractThrownTorchEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * The <b>entity version</b> of the item <b>throwable torch</b>.
 * @see ThrownTorchRenderer Renderer
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class ThrownTorchEntity extends AbstractThrownTorchEntity
{
    public ThrownTorchEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> entityType, @NotNull Level level) { super(entityType, level); }
    
    public ThrownTorchEntity(double x, double y, double z, @NotNull Level level) { super(TTorchRegistries.THROWN_TORCH.get(), x, y, z, level); }
    
    public ThrownTorchEntity(@NotNull LivingEntity shooter, @NotNull Level level) { super(TTorchRegistries.THROWN_TORCH.get(), shooter, level); }
    
    @Override
    protected @NotNull Item getDefaultItem() { return TTorchRegistries.THROWABLE_TORCH.value(); }
}