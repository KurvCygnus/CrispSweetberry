package kurvmod.crispsweetberry.entities.custom;

import kurvmod.crispsweetberry.blocks.CrispBlocks;
import kurvmod.crispsweetberry.entities.CrispEntities;
import kurvmod.crispsweetberry.item.CrispItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

//TODO:
// 1.如果放置失败则播放投掷火把的粒子效果
// 2.以及对应的Blockstate的代码

/**
 * The entity version of the item throwable torch.
 */
public class ThrownTorch extends ThrowableItemProjectile
{
    private static final int TIER_GONE = 0,
                             TIER_NORM = 1,
                             TIER_WILD = 2;
    
    /**
     * The accessor which storages the data of *tier*.
     */
    private static final EntityDataAccessor<Integer> DATA_TIER_ID = SynchedEntityData.defineId(ThrownTorch.class, EntityDataSerializers.INT);
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder)
    {
        super.defineSynchedData(builder);
        builder.define(DATA_TIER_ID, TIER_NORM);
    }
    
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
    protected @NotNull Item getDefaultItem() { return CrispItems.THROWABLE_TORCH.value(); }
    
    // ThrownTorch.java - 替换现有的 tick() 方法
    
    @Override
    public void tick()
    {
        super.tick();
        
        if(!this.level().isClientSide)
        {
            if(this.isInLava())
                changeTier(TIER_WILD, SoundEvents.LAVA_EXTINGUISH);
            else if(this.isInWater())
                changeTier(TIER_GONE, SoundEvents.FIRE_EXTINGUISH);
        }
        
        if(this.level().isClientSide)
        {
            int currentTier = this.getEntityData().get(DATA_TIER_ID);
            boolean noSmoke = false;
            
            if(currentTier == TIER_WILD)
                longerParticle = ParticleTypes.FLAME;
            else if (currentTier == TIER_GONE)
            {
                longerParticle = ParticleTypes.DRIPPING_WATER;
                noSmoke = true;
            }
            else
                longerParticle = ParticleTypes.SMALL_FLAME;
            displayParticle(5, longerParticle);
            
            if(!noSmoke)
                displayParticle(3, ParticleTypes.SMOKE);
        }
    }
    
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result)
    {
        Entity entity = result.getEntity();
        super.onHitEntity(result);
        
        //Check whether the target resists fire.
        int hitDamage = (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Zoglin) ? 0 : 1;
        
        if(this.getEntityData().get(DATA_TIER_ID) != TIER_GONE && hitDamage == 1 &&
            entity.getRemainingFireTicks() <= 100 * this.getEntityData().get(DATA_TIER_ID))
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + 30 * this.getEntityData().get(DATA_TIER_ID));//Bro got some fire XD
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), hitDamage);
    }
    
    @Override
    protected void onHitBlock(@NotNull BlockHitResult result)
    {
        super.onHitBlock(result);
        if(!this.level().isClientSide)
        {
            //TODO 2, 3
            BlockState targetState = CrispBlocks.TEMPORARY_TORCH.value().defaultBlockState();
            
            //Checks whether the position the torch lands fits its placement condition.
            //Condition 1: The torch can't be WILD tier, the torch can't hold the heat of lava, ya know.
            //Condition 2: The position can hold a torch block.
            //Condition 3: The position cannot be in liquid, it doesn't make any sense.
            if(this.getEntityData().get(DATA_TIER_ID) != TIER_WILD && targetState.canSurvive(level(), getOnPos()) && !this.isInLiquid())
            {
                this.level().setBlockAndUpdate(getOnPos(), targetState);
                playSound(SoundEvents.WOOD_PLACE, SoundSource.BLOCKS);
            }
            else
                playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS);
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
    
    
    private void changeTier(int goalTier, SoundEvent sound)
    {
        if(this.getEntityData().get(DATA_TIER_ID) == goalTier)
            return;
        
        this.getEntityData().set(DATA_TIER_ID, goalTier);
        playSound(sound, SoundSource.AMBIENT);
    }
    
    /**
     * The function to play sound with fewer args, the reason why it's here is simply because I'm lazy.
     *
     * @param sound       The sound that needs to be played.
     * @param soundSource The type of sound.
     */
    private void playSound(SoundEvent sound, SoundSource soundSource)
        { this.level().playSound(null, getOnPos(), sound, soundSource, 1.5F, 1.0F); }
    

}
