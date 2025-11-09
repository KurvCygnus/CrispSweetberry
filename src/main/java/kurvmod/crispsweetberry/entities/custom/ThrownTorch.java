package kurvmod.crispsweetberry.entities.custom;

import kurvmod.crispsweetberry.blocks.CrispBlocks;
import kurvmod.crispsweetberry.entities.CrispEntities;
import kurvmod.crispsweetberry.item.CrispItems;
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

//TODO:
// 1.完成火把在不同的流体中的反应
// 2.完成判定放置火把是否成功
// 3.如果放置失败则播放投掷火把的粒子效果
// 4.以及对应的Blockstate的代码
/**
 * The entity version of the item throwable torch.
 */
public class ThrownTorch extends ThrowableItemProjectile
{
    /**
     * As the name implies, it is used for recording fire tier,
     * which will be used to calculate both the maximum length of the fire ticks and fire ticks that will be added to mobs.
     */
    FIRE_TIER tier = FIRE_TIER.NORMAL;
    
    SimpleParticleType longerParticle = ParticleTypes.SMALL_FLAME;
    
    /**
     * The construct method for entity registry.
     */
    public ThrownTorch(EntityType<? extends ThrownTorch> entityType, Level level) { super(entityType, level); }
    
    /**
     * The construct method for item usage.
     */
    public ThrownTorch(Level level, LivingEntity shooter) { super(CrispEntities.THROWN_TORCH.value(), shooter, level); }
    
    /**
     * What? You asked me what is this used for? IDK.
     */
    public ThrownTorch(Level level, double x, double y, double z) { super(CrispEntities.THROWN_TORCH.value(), x, y, z, level); }
    
    @Override
    protected @NotNull Item getDefaultItem() {return CrispItems.THROWABLE_TORCH.value();}
    
    @Override
    public void tick()
    {
        super.tick();
        
        if(this.level().isClientSide)
        {
            //TODO 1
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
        Entity entity = result.getEntity();
        super.onHitEntity(result);
        
        //Check whether the target resists fire.
        int hitDamage = (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Zoglin || tier == FIRE_TIER.GONE) ? 0 : 1;
        
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
            //TODO 2, 3
            this.level().setBlockAndUpdate(getOnPos(), CrispBlocks.TEMPORARY_TORCH.value().defaultBlockState());
            playSound(SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.5F);
        }
    }
    
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
            return;//Terminates these codes if the tier is as same as goalTier,
                   // you won't be glad to see tons of particles and hear a mess of sound, aren't cha?
        tier = goalTier;
        playSound(sound, SoundSource.AMBIENT, 1);
        displayParticle(1, particle);
        longerParticle = newParticle;
    }
    
    /**
     * The function to play sound with fewer args, the reason why it's here is simply because I'm lazy.
     * @param sound The sound that needs to be played.
     * @param soundSource The type of sound.
     * @param volume I believe you can understand this.
     */
    private void playSound(SoundEvent sound, SoundSource soundSource, float volume)
        { this.level().playSound(null, getOnPos(), sound, soundSource, volume, 1.0F); }
    
    /**
     * The enum for recording tier.
     */
    enum FIRE_TIER { GONE, NORMAL, WILD }
}
