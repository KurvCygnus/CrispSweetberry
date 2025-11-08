package kurvmod.crispsweetberry.entities.custom;

import kurvmod.crispsweetberry.blocks.Blocks;
import kurvmod.crispsweetberry.entities.Entities;
import kurvmod.crispsweetberry.item.Items;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

//TODO: 修理粒子效果的问题, 搞定长按投掷物品不摆动的问题
public class ThrownTorch extends ThrowableItemProjectile
{
    public ThrownTorch(EntityType<? extends ThrownTorch> entityType, Level level) { super(entityType, level); }
    
    public ThrownTorch(Level level, LivingEntity shooter) { super(Entities.THROWN_TORCH.value(), shooter, level); }
    
    public ThrownTorch(Level level, double x, double y, double z) { super(Entities.THROWN_TORCH.value(), x, y, z, level); }
    
    @Override
    protected @NotNull Item getDefaultItem() { return Items.THROWABLE_TORCH.value(); }
    
    
    @Override
    public void tick()
    {
        super.tick();
        
        if (this.level().isClientSide) {
            if (this.tickCount % 5 == 0) {
                double speed = 0.03D;
                
                this.level().addParticle(
                    ParticleTypes.FLAME,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    (this.random.nextDouble() * 2.0D - 1.0D) * speed,
                    (this.random.nextDouble() * 2.0D - 1.0D) * speed,
                    (this.random.nextDouble() * 2.0D - 1.0D) * speed
                );
            }
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        boolean isFireMob = (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Zoglin);
        if(!isFireMob)
        {
            if(entity.getRemainingFireTicks() <= 100)
                entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 30);//火了
            entity.hurt(this.damageSources().thrown(this, this.getOwner()), 1);
        }
    }
    
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if(!this.level().isClientSide)
            this.level().setBlockAndUpdate(getOnPos(), Blocks.TEMPORARY_TORCH.value().defaultBlockState());
    }
    
    /**
     * Called when this EntityFireball hits a block or entity.
     */
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
        }
    }
}
