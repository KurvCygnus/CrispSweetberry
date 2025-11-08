package kurvmod.crispsweetberry.entities.custom;

import kurvmod.crispsweetberry.blocks.Blocks;
import kurvmod.crispsweetberry.entities.Entities;
import kurvmod.crispsweetberry.item.Items;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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

//TODO: 完成火把在不同的流体中的反应, 完成判定放置火把是否成功, 如果放置失败则播放投掷火把的粒子效果, 以及对应的Blockstate的代码.
public class ThrownTorch extends ThrowableItemProjectile
{
    FIRE_TIER tier = FIRE_TIER.NORMAL;
    SimpleParticleType longerParticle = ParticleTypes.SMALL_FLAME;
    
    public ThrownTorch(EntityType<? extends ThrownTorch> entityType, Level level) {super(entityType, level);}
    
    public ThrownTorch(Level level, LivingEntity shooter) {super(Entities.THROWN_TORCH.value(), shooter, level);}
    
    public ThrownTorch(Level level, double x, double y, double z) {super(Entities.THROWN_TORCH.value(), x, y, z, level);}
    
    @Override
    protected @NotNull Item getDefaultItem() {return Items.THROWABLE_TORCH.value();}
    
    
    @Override
    public void tick()
    {
        super.tick();
        
        if(this.level().isClientSide)
        {
            //Of course do these torches have interactions with different liquids.
            if(this.isInLava())
                checkAndSwitchTier(FIRE_TIER.WILD, ParticleTypes.LAVA, SoundEvents.LAVA_EXTINGUISH, ParticleTypes.FLAME);
            
            if(this.isInWater())
                checkAndSwitchTier(FIRE_TIER.GONE, ParticleTypes.SMOKE, SoundEvents.FIRE_EXTINGUISH, ParticleTypes.DRIPPING_WATER);
            else
                displayParticle(3, ParticleTypes.SMOKE);
            
            displayParticle(5, longerParticle);
        }
    }
    
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result)
    {
        super.onHitEntity(result);
        Entity entity = result.getEntity();
        int hitDamage = (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Zoglin) ? 0 : 1;
        if(hitDamage == 1 && entity.getRemainingFireTicks() <= 100 * tier.ordinal())
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 30 * tier.ordinal());//Bro got some fireXD
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), hitDamage);
    }
    
    @Override
    protected void onHitBlock(@NotNull BlockHitResult result)
    {
        super.onHitBlock(result);
        if(!this.level().isClientSide)
        {
            this.level().setBlockAndUpdate(getOnPos(), Blocks.TEMPORARY_TORCH.value().defaultBlockState());
            playSound(SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.5F);
        }
    }
    
    /**
     * Called when this EntityFireball hits a block or entity.
     */
    @Override
    protected void onHit(@NotNull HitResult result)
    {
        super.onHit(result);
        if(!this.level().isClientSide)
        {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }
    
    private void displayParticle(int frequency, ParticleOptions particle)
    {
        if(this.tickCount % frequency == 0)
        {
            double speed = 0.03D;
            
            this.level().addParticle(
                particle,
                this.getX(),
                this.getY(),
                this.getZ(),
                (this.random.nextDouble() * 2.0D - 1.0D) * speed,
                (this.random.nextDouble() * 2.0D - 1.0D) * speed,
                (this.random.nextDouble() * 2.0D - 1.0D) * speed
            );
        }
    }
    
    private void checkAndSwitchTier(FIRE_TIER goalTier, ParticleOptions particle, SoundEvent sound, SimpleParticleType newParticle)
    {
        if(tier == goalTier)
            return;
        tier = goalTier;
        playSound(sound, SoundSource.AMBIENT, 1);
        displayParticle(1, particle);
        longerParticle = newParticle;
    }
    
    private void playSound(SoundEvent sound, SoundSource soundSource, float volume)
        { this.level().playSound(null, getOnPos(), sound, soundSource, volume, 1.0F); }
    
    enum FIRE_TIER { GONE, NORMAL, WILD }
}
