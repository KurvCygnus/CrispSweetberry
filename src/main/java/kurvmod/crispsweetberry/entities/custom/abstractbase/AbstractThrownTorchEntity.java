package kurvmod.crispsweetberry.entities.custom.abstractbase;

import kurvmod.crispsweetberry.blocks.CrispBlocks;
import kurvmod.crispsweetberry.blocks.custom.abstractbase.ITemporaryTorch;
import kurvmod.crispsweetberry.blocks.custom.temporarytorch.TemporaryWallTorchBlock;
import kurvmod.crispsweetberry.items.CrispItems;
import kurvmod.crispsweetberry.util.CrispEnums;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static kurvmod.crispsweetberry.util.CrispConstants.*;
import static kurvmod.crispsweetberry.util.CrispConstants.PROJECTILE_X_NO_SPEED;
import static kurvmod.crispsweetberry.util.CrispConstants.PROJECTILE_Y_NO_SPEED;
import static kurvmod.crispsweetberry.util.CrispConstants.PROJECTILE_Z_NO_SPEED;

//PROTOTYPE
//TODO
//DOC

/**
 * The abstract class for all thrown torch entities.
 */
public abstract class AbstractThrownTorchEntity extends ThrowableItemProjectile
{
    //Constants
    public static final int TIER_GONE = 0;//The tier for water contraction, named "GONE" as the fire is *gone*.
    public static final int TIER_NORM = 1;//The tier for standard case, the torch will be this tier as long as nothing special happens.
    public static final int TIER_WILD = 2;//The tier for lava contraction, named "WILD" since the fire will go wild as it torches lava.
    protected static final int LONGER_PARTICLE_STATES = 3;
    protected static final int DEFAULT_LONGER_PARTICLE_FREQUENCY = 5;
    protected static final int DEFAULT_SHORTER_PARTICLE_FREQUENCY = 3;
    protected static final int NO_FREQUENCY = 1;
    protected static final int HIT_RESULT_DO_DAMAGE = 1;
    protected static final int HIT_RESULT_NO_DAMAGE = 0;
    protected static final int HIT_STD_EXTEND_FIRE_TICKS = 30;
    protected static final int HIT_STD_MAX_TICKS = 100;
    protected static final int LEVEL_BLOCK_DESTROY_EVENT_ID = 2001;
    public static final int ENTITY_DESTROY_EVENT_ID = 3;
    
    protected static final double OFFSET_CALCULATE_CONSTANT = 0.5;
    
    protected static final SimpleParticleType[] DEFAULT_LONGER_PARTICLE_STATE_LIST = { ParticleTypes.DRIPPING_WATER, ParticleTypes.SMALL_FLAME, ParticleTypes.FLAME };
    
    //Data Synchronizer
    public static final EntityDataAccessor<Integer> FIRE_TIER_ID = SynchedEntityData.defineId(AbstractThrownTorchEntity.class, EntityDataSerializers.INT);
    
    //Variables
    protected final Map<Integer, SimpleParticleType> longerParticleStateList = processLongerParticleStateList(getLongerParticleStateList());
    
    protected final int LONGER_PARTICLE_FREQUENCY = getLongerParticleFrequency();
    protected final int SHORTER_PARTICLE_FREQUENCY = getShorterParticleFrequency();
    public int externalTickCount;
    
    protected final boolean shouldCheckLiquids = getShouldCheckLiquidsFlag();
    protected final boolean noSmokeWhenBurnedOut = getNoSmokeWhenBurnedOutFlag();
    protected final boolean shouldCheckFireResistMob = getShouldCheckFireResistMobFlag();
    protected final boolean shouldLitMob = getShouldLitMobFlag();
    
    //Data Definitions
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder)
    {
        super.defineSynchedData(builder);
        builder.define(FIRE_TIER_ID, TIER_NORM);
    }
    
    /**
     * Getter method for <b>torch's coordinated item</b>,<br><br>
     * <i><b>WARNING</b>:<br>
     * You should <b>return <u>{@code YOUR CUSTOM ITEM}</u></b> which <b>inherits <u>{@code AbstractThrowableTorchItem}</u></b>,
     * or else your custom stuff won't work properly!
     * @see net.minecraft.world.entity.projectile.ThrowableItemProjectile
     */
    protected abstract @NotNull Item getDefaultItem();
    
    /**
     * The construct method for <b>entity registry</b>.
     */
    public AbstractThrownTorchEntity(EntityType<? extends AbstractThrownTorchEntity> entityType, Level level) { super(entityType, level); }
    
    /**
     * The construct method for <b>item usage, or, player's usage</b>.<br><br>
     * <i><b>WARNING</b>:
     * You should <b>replace <u>{@code childrenClazzValue}</u> with <u>{@code YourEntityRegistryClass.YourCustomThrownTorch.value()}</u></b>,
     * then <b>remove {@code EntityType} in function's paras</b>,
     * or else your custom stuff won't work properly!
     * @see net.minecraft.world.entity.projectile.ThrowableItemProjectile
     */
    public AbstractThrownTorchEntity(EntityType<? extends AbstractThrownTorchEntity> childrenClazzValue, double x, double y, double z, Level level)
        { super(childrenClazzValue, x, y, z, level); }
    
    /**
     * The construct method for <b>dispenser's usage, or, player's usage</b>.<br><br>
     * <i><b>WARNING</b>:
     * You should <b>replace <u>{@code childrenClazzValue}</u> with <u>{@code YourEntityRegistryClass.YourCustomThrownTorch.value()}</u></b>,
     * then <b>remove {@code EntityType} in function's paras</b>,
     * or else your custom stuff won't work properly!
     * @see net.minecraft.world.entity.projectile.ThrowableItemProjectile
     */
    public AbstractThrownTorchEntity(EntityType<? extends AbstractThrownTorchEntity> childrenClazzValue, LivingEntity shooter, Level level)
        { super(childrenClazzValue, shooter, level); }
    
    /**
     * The <b>core method</b> of <b>ThrownTorchEntity</b>.<br>
     * The process:<br><pre>
     *  I. Execute the super method, and <b>synchronize the tick to renderer</b>.
     *  II. Check <b>whether this entity is in water or lava</b> and <b>change fire tier and glowing tag accordingly</b>, if the flag <b><u>{@code shouldCheckLiquids}</u> is true</b>
     *  III. <b>Display particle and play sound</b> based on <b>fire tier</b>, if the flag <b><u>{@code noSmokeWhenBurnedOut}</u> is false</b>
     *  IV. <b>Loop</b></pre>
     */
    @Override
    public void tick()
    {
        //Process I
        super.tick();
        this.externalTickCount = tickCount;
        
        //Process II
        //Serverside content, which processes data.
        if(!this.level().isClientSide && shouldCheckLiquids)
            checkLiquids();
        
        //Process III
        else//Clientside content, which mainly renderers stuff.
        {
            displayParticle(LONGER_PARTICLE_FREQUENCY, longerParticleStateList.get(getTier()));
            
            if(!(getTier() == TIER_GONE && noSmokeWhenBurnedOut))
                displayParticle(SHORTER_PARTICLE_FREQUENCY, ParticleTypes.SMOKE);
        }
    }
    
    /**
     * The variation method of the <b>hit result process</b>, whose target is an <b>entity</b>.<br>
     * The process:<br><pre>
     *  I. Get the target, and execute super method.
     *  II. Check <b>whether the target is a fire-resistance mob and the flag <u>{@code shouldCheckFireResistMob}</u> is true</b>, and decide <b>whether the torch deals damage</b>
     *  III. If the torch's fire tier isn't <b>{@code GONE}</b>, and flag <b><u>{@code shouldLitMob}</u> is true</b>
     *  <b>make target burn</b>, or <b>extend the burning time</b>
     *  (The maximum burning length is decided by torch's current fire tier)</pre>
     */
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result)
    {
        //Process I
        Entity entity = result.getEntity();
        super.onHitEntity(result);
        
        boolean shouldExtendBurnTicks = entity.getRemainingFireTicks() <= HIT_STD_MAX_TICKS * getTier();
        
        //Process II
        int hitResult = shouldCheckFireResistMob ?
            1 : (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Zoglin) ?
            HIT_RESULT_NO_DAMAGE : HIT_RESULT_DO_DAMAGE;//TODO: 做自定义Fire-Resistant Mob标签
        
        //Process III
        if(getTier() != TIER_GONE && hitResult == HIT_RESULT_DO_DAMAGE &&
            shouldExtendBurnTicks && shouldLitMob)
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + HIT_STD_EXTEND_FIRE_TICKS * getTier());//Bro got some fire XD
        
        //Yes, although the damage might be zero, we still need to do damage to make knockbacks, it's how vanilla works.
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), hitResult);
    }
    
    /**
     * The variation method of the <b>hit result process</b>, whose target is a <b>block</b>.
     * The process:<br><pre>
     *  I. Execute super method
     *  II. Check <b>whether the torch can be placed</b>
     *  (Only if it's not in liquid and its tier is not <b>{@code WILD}</b>,<br> then it can be placed
     *  III. Get which <b>torch block</b>, and their <b>state that will be placed</b>
     *  IV. Check <b>whether the hit position can place the torch</b>
     *  V. Process the <b>result</b></pre>
     */
    protected void onHitBlock(@NotNull BlockHitResult result)
    {
        //Process I
        super.onHitBlock(result);
        if(!this.level().isClientSide)
        {
            //Process II(Only on serverside)
            if(!tryHandleSpecialCase())
                return;
            
            //Process III
            BlockPos placementPos = getPlacementPos(result);
            BlockState stateToPlace = getPlacementState(result, placementPos);
            
            //Process IV, V
            if(stateToPlace != null && tryPlaceTorch(stateToPlace, placementPos))
                playSound(SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, LOUD_SOUND_VOLUME);
            else
                handlePlacementFailure();
        }
    }
    
    /**
     * The <b>universal method</b> that executes when you <b>hit things</b>.
     */
    @Override
    protected void onHit(@NotNull HitResult result)
    {
        super.onHit(result);
        if(!this.level().isClientSide)
            this.discard();
    }
    
    /**
     * The method overrides the super one in order to <b>make {@code displayDestroyParticle()} work</b>.
     */
    @Override
    public final void handleEntityEvent(byte id)
    {
        if(id == ENTITY_DESTROY_EVENT_ID)
        {
            if(this.level().isClientSide)
            {
                for(int loopTime = 0; loopTime < 5; ++loopTime)
                {
                    double offsetX = (this.random.nextDouble() - OFFSET_CALCULATE_CONSTANT) * OFFSET_CALCULATE_CONSTANT;
                    double offsetY = this.random.nextDouble() * OFFSET_CALCULATE_CONSTANT;
                    double offsetZ = (this.random.nextDouble() - OFFSET_CALCULATE_CONSTANT) * OFFSET_CALCULATE_CONSTANT;
                    
                    this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, CrispItems.THROWABLE_TORCH.value().getDefaultInstance()),
                        this.getX() + offsetX,
                        this.getY() + offsetY,
                        this.getZ() + offsetZ,
                        PROJECTILE_X_NO_SPEED, PROJECTILE_Y_NO_SPEED, PROJECTILE_Z_NO_SPEED);
                }
            }
        }
        else
            super.handleEntityEvent(id);
    }
    
    /**
     * The method which is <b>a part of onHitBlock</b>(Process <b>II</b>),<br>
     * checking whether torch meets <b>conditions</b> that are <b>impossible the place</b>:<br><pre>
     *  I. Whether the torch's <b>fire tier is {@code WILD}</b>
     *  II. Whether the torch is <b>in liquid</b></pre>
     *  (If any of the condition is false, also do the failed events)
     */
    private boolean tryHandleSpecialCase()
    {
        if(getTier() == TIER_WILD || this.isInLiquid())
        {
            playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, NORMAL_SOUND_VOLUME);
            displayDestroyParticle();
            displayParticle(NO_FREQUENCY, ParticleTypes.SMOKE);
            
            return false;
        }
        
        return true;
    }
    
    /**
     * This method impacts <b>whether the torch will be standard one, or the wall one.</b>
     */
    private BlockPos getPlacementPos(BlockHitResult result)
    {
        final BlockPos hitPos = result.getBlockPos();
        final Direction hitSide = result.getDirection();
        
        return switch(hitSide)
        {
            case Direction.UP -> hitPos.above();
            case Direction.DOWN -> null;
            default -> hitPos.relative(hitSide);
        };
    }
    
    /**
     * This method coordinates the <b>Process III in the method {@code onHitBlock()}</b>.
     */
    private BlockState getPlacementState(BlockHitResult result, BlockPos placementPos)
    {
        if(placementPos == null)
            return null;
        
        Direction hitSide = result.getDirection();
        BlockState baseState = null;
        
        if(hitSide == Direction.UP)
            baseState = CrispBlocks.TEMPORARY_TORCH.value().defaultBlockState();
        else if(hitSide != Direction.DOWN)
            baseState = CrispBlocks.TEMPORARY_WALL_TORCH.value().defaultBlockState().setValue(TemporaryWallTorchBlock.FACING, hitSide);
        
        if (baseState != null && getTier() == TIER_GONE)
            baseState = baseState.setValue(ITemporaryTorch.LIGHT_PROPERTY, CrispEnums.LIGHT_STATE.DARK);
        
        return baseState;
    }
    
    /**
     * This method coordinates the <b>Process IV and V in the method {@code onHitBlock()}</b>.
     */
    protected boolean tryPlaceTorch(BlockState state, BlockPos pos)
    {
        /*
          Conditions explanation:
            I. Check whether the position can support the torch
            II. Check whether the block can be replaced(Prevent block replacement that doesn't make sense)
          Only when both of these conditions are met, then the torch can be successfully placed.
        */
        if(state.canSurvive(this.level(), pos) && canBeActuallyPlaced(pos))
        {
            this.level().setBlockAndUpdate(pos, state);
            return true;
        }
        
        return false;
    }
    
    /**
     * This method coordinates the <b>Process V in the method {@code onHitBlock()}</b>.
     */
    private void handlePlacementFailure()
    {
        playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, LOUD_SOUND_VOLUME);
        displayDestroyParticle();
    }
    
    /**
     * The method which <b>checks whether the torch is in <u>liquids</u></b> and <b>execute sequences for {@code tick()}<b>.
     */
    private void checkLiquids()
    {
        if(this.isInLava())
            changeTier(TIER_WILD, SoundEvents.LAVA_EXTINGUISH);
        else if(this.isInWater())
            changeTier(TIER_GONE, SoundEvents.FIRE_EXTINGUISH);
    }
    
    /**
     * The method <b>checks whether the torch can actually be placed</b>.
     */
    protected boolean canBeActuallyPlaced(BlockPos pos)
    {
        Block posBlock = this.level().getBlockState(pos).getBlock();
        BlockState posBlockState = this.level().getBlockState(pos);
        
        if(posBlock == CrispBlocks.TEMPORARY_TORCH.value() || posBlock == CrispBlocks.TEMPORARY_WALL_TORCH.value())
            if(posBlockState.getValue(ITemporaryTorch.LIGHT_PROPERTY) == CrispEnums.LIGHT_STATE.DARK)
            {
                this.level().levelEvent(LEVEL_BLOCK_DESTROY_EVENT_ID, pos, Block.getId(posBlockState));
                return true;
            }
        return posBlock == Blocks.AIR || posBlockState.is(BlockTags.REPLACEABLE);
    }
    
    /**
     * For readability, this has been encapsulated into a single method.
     */
    private void displayDestroyParticle() { this.level().broadcastEntityEvent(this, (byte) ENTITY_DESTROY_EVENT_ID); }
    
    /**
     * The method that <b>simplifies {@code addParticle()} method</b>.
     * @param frequency The frequency of the particle. To simple, <b>every second the particle will display</b>
     *                  [<b>20(A single second to tick) ÷ frequency</b>] <b>times</b>.
     */
    private void displayParticle(int frequency, ParticleOptions particle)
    {
        if(this.tickCount % frequency != 0)
            return;
        
        final double PARTICLE_SPEED = (this.random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        this.level().addParticle(
            particle,
            this.getX(), this.getY(), this.getZ(),
            PARTICLE_SPEED, PARTICLE_SPEED, PARTICLE_SPEED
        );
    }
    
    /**
     * The method which processes array to Map.
     */
    private Map<Integer, SimpleParticleType> processLongerParticleStateList(SimpleParticleType[] states)
    {
        Map<Integer, SimpleParticleType> longerParticleStateList = new HashMap<>();
        
        for(int index = 0; index < LONGER_PARTICLE_STATES; index++)
            longerParticleStateList.put(index, states[index]);
        
        return longerParticleStateList;
    }
    
    /**
     * For readability, this has been encapsulated into a single method.
     */
    public final int getTier() { return this.getEntityData().get(FIRE_TIER_ID); }
    
    /**
     * The method that <b>changes torch's fire tier</b>.
     * @param goalTier The tier which is the switch goal.
     * @param sound The sound that would be played.
     */
    private void changeTier(int goalTier, SoundEvent sound)
    {
        if(getTier() == goalTier)
            return;//Directly terminates this method if the tier doesn't have to.
        
        this.getEntityData().set(FIRE_TIER_ID, goalTier);
        playSound(sound, SoundSource.AMBIENT, LOUD_SOUND_VOLUME);
    }
    
    /**
     * This method is a simplified version of the original one, which plays sound at normal pitch.
     */
    private void playSound(SoundEvent sound, SoundSource soundSource, float volume)
        { this.level().playSound(null, getOnPos(), sound, soundSource, volume, NORMAL_SOUND_PITCH); }
    
    //Abstracts
    /**
     * Getter method for <b>torch's main particles</b>, <b>if you don't want to customize this, just return <u>{@code DEFAULT_LONGER_PARTICLE_STATE_LIST}</u></b>.
     */
    protected abstract SimpleParticleType[] getLongerParticleStateList();
    
    protected abstract int getLongerParticleFrequency();//Return DEFAULT_LONGER_PARTICLE_FREQUENCY if you don't need customization.
    protected abstract int getShorterParticleFrequency();//Return DEFAULT_SHORTER_PARTICLE_FREQUENCY if you don't need customization.
    
    //Return true if you don't need customizations for these elements.
    protected abstract boolean getShouldCheckLiquidsFlag();
    protected abstract boolean getNoSmokeWhenBurnedOutFlag();
    protected abstract boolean getShouldCheckFireResistMobFlag();
    protected abstract boolean getShouldLitMobFlag();
}
