package kurvmod.crispsweetberry.entities.custom;

import kurvmod.crispsweetberry.blocks.CrispBlocks;
import kurvmod.crispsweetberry.blocks.custom.temporarytorch.TemporaryWallTorchBlock;
import kurvmod.crispsweetberry.entities.CrispEntities;
import kurvmod.crispsweetberry.items.CrispItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

//TODO: 如果放置失败则播放投掷火把的粒子效果, 飞行的动态光源

/**
 * The entity version of the item throwable torch.
 */
public class ThrownTorch extends ThrowableItemProjectile
{
    /**
     * ThrownTorch's all int constants here.
     */
    private static final int TIER_GONE = 0,
                             TIER_NORM = 1,
                             TIER_WILD = 2,
                             NO_FREQUENCY = 1,
                             LONGER_PARTICLE_FREQUENCY = 5,
                             SHORTER_PARTICLE_FREQUENCY = 3,
                             HIT_RESULT_DO_DAMAGE = 1,
                             HIT_RESULT_NO_DAMAGE = 0,
                             HIT_STD_EXTEND_FIRE_TICKS = 30,
                             HIT_STD_MAX_TICKS = 100;
    
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
    
    @Override
    public void tick()
    {
        super.tick();
        
        //Holy sh*t, I think I should spare some time to learn client and server stuff, seriously.
        //Serverside content
        if(!this.level().isClientSide)
        {
            if(this.isInLava())
                changeTier(TIER_WILD, SoundEvents.LAVA_EXTINGUISH);
            else if(this.isInWater())
                changeTier(TIER_GONE, SoundEvents.FIRE_EXTINGUISH);
        }
        else//Clientside content
        {
            boolean noSmoke = false;
            
            switch(this.getEntityData().get(DATA_TIER_ID))
            {
                case TIER_GONE:
                    longerParticle = ParticleTypes.DRIPPING_WATER;
                    noSmoke = true;
                    break;
                    
                case TIER_NORM:
                    longerParticle = ParticleTypes.SMALL_FLAME;
                    displayParticle(NO_FREQUENCY, ParticleTypes.SMOKE);
                    break;
                    
                case TIER_WILD:
                    longerParticle = ParticleTypes.FLAME;
            }
            displayParticle(LONGER_PARTICLE_FREQUENCY, longerParticle);
            
            if(!noSmoke)
                displayParticle(SHORTER_PARTICLE_FREQUENCY, ParticleTypes.SMOKE);
        }
    }
    
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result)
    {
        Entity entity = result.getEntity();
        super.onHitEntity(result);
        
        //Check whether the target resists fire.
        int hitResult = (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Zoglin) ?
            HIT_RESULT_NO_DAMAGE : HIT_RESULT_DO_DAMAGE;
        
        if(this.getEntityData().get(DATA_TIER_ID) != TIER_GONE && hitResult == HIT_RESULT_DO_DAMAGE &&
            entity.getRemainingFireTicks() <= HIT_STD_MAX_TICKS * this.getEntityData().get(DATA_TIER_ID))//Bro got some fire XD
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + HIT_STD_EXTEND_FIRE_TICKS * this.getEntityData().get(DATA_TIER_ID));
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), hitResult);
    }
    
    @Override
    protected void onHitBlock(@NotNull BlockHitResult result)
    {
        super.onHitBlock(result);
        if(!this.level().isClientSide)
        {
            BlockPos hitPos = result.getBlockPos();
            Direction hitSide = result.getDirection();
            
            if(this.getEntityData().get(DATA_TIER_ID) == TIER_WILD || this.isInLiquid())
            {
                playSound(SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F);
                this.level().broadcastEntityEvent(this, (byte) 3);
                displayParticle(NO_FREQUENCY, ParticleTypes.SMOKE);
                return;
            }
            
            BlockState stateToPlace = null;
            BlockPos placementPos = null;
            
            if(hitSide == Direction.UP)
            {
                placementPos = hitPos.above();
                stateToPlace = CrispBlocks.TEMPORARY_TORCH.value().defaultBlockState();
            }
            else if(hitSide != Direction.DOWN)
            {
                placementPos = hitPos.relative(hitSide);
                
                stateToPlace = CrispBlocks.TEMPORARY_WALL_TORCH.value().defaultBlockState()
                      .setValue(TemporaryWallTorchBlock.FACING, hitSide);
                System.out.println(stateToPlace);
            }
            
            if(stateToPlace != null && stateToPlace.canSurvive(this.level(), placementPos))
            {
                this.level().setBlockAndUpdate(placementPos, stateToPlace);
                playSound(SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.5F);
                return;
            }
            
            playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, 1.5F);
            this.level().broadcastEntityEvent(this, (byte) 3);
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
            final double PARTICLE_SPEED = (this.random.nextDouble() * 2.0D - 1.0D) * 0.03D;
            
            this.level().addParticle(
                particle,
                this.getX(),
                this.getY(),
                this.getZ(),
                PARTICLE_SPEED,
                PARTICLE_SPEED,
                PARTICLE_SPEED
            );
        }
    }
    
    
    private void changeTier(int goalTier, SoundEvent sound)
    {
        if(this.getEntityData().get(DATA_TIER_ID) == goalTier)
            return;
        
        this.getEntityData().set(DATA_TIER_ID, goalTier);
        playSound(sound, SoundSource.AMBIENT, 1.5F);
    }
    
    /**
     * The function to play sound with fewer args, the reason why it's here is simply because I'm lazy.
     *
     * @param sound       The sound that needs to be played.
     * @param soundSource The type of sound.
     */
    private void playSound(SoundEvent sound, SoundSource soundSource, float volume)
    {this.level().playSound(null, getOnPos(), sound, soundSource, volume, 1.0F);}
}
