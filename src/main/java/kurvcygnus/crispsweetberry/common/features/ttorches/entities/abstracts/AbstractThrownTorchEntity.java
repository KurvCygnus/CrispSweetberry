package kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts;

import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.abstracts.AbstractThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.abstracts.AbstractThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
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
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants.LIGHT_PROPERTY;
import static kurvcygnus.crispsweetberry.utils.projectile.ProjectileConstants.*;

/**
 * The basic of all thrown torch entities.
 * @see AbstractThrownTorchRenderer Renderer
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public abstract class AbstractThrownTorchEntity extends ThrowableItemProjectile
{
    //  region
    //* Constants, Fields & Data Basics
    public static final int TIER_GONE = 0;//* The tier for water contraction, named "GONE" as the fire is gone.
    public static final int TIER_NORM = 1;//* The tier for standard case, the torch will be this tier as long as nothing special happens.
    public static final int TIER_WILD = 2;//* The tier for lava contraction, named "WILD" since the fire will go wild as it torches lava.
    public static final int HIT_STD_EXTEND_FIRE_TICKS = 30;
    public static final int HIT_STD_MAX_TICKS = 100;
    private static final int LONGER_PARTICLE_STATES = 3;
    private static final int DEFAULT_LONGER_PARTICLE_FREQUENCY = 5;
    private static final int DEFAULT_SHORTER_PARTICLE_FREQUENCY = 3;
    private static final int NO_FREQUENCY = 1;
    private static final int HIT_RESULT_DO_DAMAGE = 1;
    private static final int HIT_RESULT_NO_DAMAGE = 0;
    private static final int LEVEL_BLOCK_DESTROY_EVENT_ID = 2001;
    private static final int ENTITY_DESTROY_EVENT_ID = 3;
    
    private static final double OFFSET_CALCULATE_CONSTANT = 0.5;
    
    private static final ParticleOptions[] DEFAULT_LONGER_PARTICLE_STATE_LIST = { ParticleTypes.DRIPPING_WATER, ParticleTypes.SMALL_FLAME, ParticleTypes.FLAME };
    
    public static final EntityDataAccessor<Integer> FIRE_TIER_ID = SynchedEntityData.defineId(AbstractThrownTorchEntity.class, EntityDataSerializers.INT);
    
    protected final Map<Integer, ParticleOptions> longerParticleStateList = processLongerParticleStateList(getLongerParticleStateList());
    
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder)
    {
        super.defineSynchedData(builder);
        builder.define(FIRE_TIER_ID, TIER_NORM);
    }
    
    /**
     * Getter method for <b>torch's coordinated item</b>,<br><br>
     * @implSpec <pre>{@code
     *      protected @NotNull Item getDefaultItem() { return MyItems.Foo.value(); }
     * }</pre>
     * @apiNote You should <b>return {@code YOUR CUSTOM ITEM}</u> which <b>inherits <u>{@link AbstractThrowableTorchItem AbstractThrowableTorchItem}</u></b>,
     * or else your custom stuff won't work properly!
     * @see ThrowableItemProjectile#getDefaultItem() Source
     */
    protected abstract @NotNull Item getDefaultItem();
    //endregion
    
    //  region
    //* Constructors
    /**
     * The construct method for <b>entity registry</b>.
     */
    public AbstractThrownTorchEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> entityType, @NotNull Level level)
        { super(entityType, level); }
    
    /**
     * The construct method for <b>item usage, or, player's usage</b>.<br><br>
     * @implSpec <pre>{@code
     *      public FoolyingTorchEntity(double x, double y, double z, Level level)
     *          { super(MyEntities.FoolyingTorch.value(), ...); }
     * }</pre>
     * @apiNote You should <b>replace {@code childrenClazzValue} with {@code YourEntityRegistryClass.YourCustomThrownTorch.value()}</b>,
     * then <b>remove {@code EntityType} in function's paras</b>,
     * or else your custom stuff won't work properly!
     * @see net.minecraft.world.entity.projectile.ThrowableItemProjectile
     */
    public AbstractThrownTorchEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> childrenClazzValue, 
        double x, double y, double z, @NotNull Level level)
            { super(childrenClazzValue, x, y, z, level); }
    
    /**
     * The construct method for <b>dispenser's usage.<br><br>
     * @implSpec <pre>{@code
     *      public FoolyingTorchEntity(double x, double y, double z, Level level)
     *          { super(MyEntities.FoolyingTorch.value(), ...); }
     * }</pre>
     * @apiNote You should <b>replace {@code childrenClazzValue} with {@code YourEntityRegistryClass.YourCustomThrownTorch.value()}</b>,
     * then <b>remove {@code EntityType} in function's paras</b>,
     * or else your custom stuff won't work properly!
     * @see net.minecraft.world.entity.projectile.ThrowableItemProjectile
     */
    public AbstractThrownTorchEntity(@NotNull EntityType<? extends AbstractThrownTorchEntity> childrenClazzValue,
        @NotNull LivingEntity shooter, @NotNull Level level)
            { super(childrenClazzValue, shooter, level); }
    //endregion
    
    //  region
    //* Lifecycles & Logics
    @Override
    public void tick()
    {
        super.tick();
        
        final Level level = this.level();
        
        if(!level.isClientSide)
        {
            if(shouldCheckLiquids())
            {
                if(this.isInLava())
                    changeTier(TIER_WILD, SoundEvents.LAVA_EXTINGUISH);
                else if(this.isInWater())
                    changeTier(TIER_GONE, SoundEvents.FIRE_EXTINGUISH);
            }
            
            if(!this.isInLiquid())
            {
                final BlockState blockState = level.getBlockState(this.getOnPos());
                
                if(!blockState.is(Blocks.AIR) || blockState.is(TTorchRegistries.FAKE_LIGHT_BLOCK))
                    return;
                
                final BlockState light = TTorchRegistries.FAKE_LIGHT_BLOCK.value().defaultBlockState().
                    setValue(LIGHT_PROPERTY, getTier() == TIER_GONE ? TTorchConstants.LightState.DARK : TTorchConstants.LightState.FULL_BRIGHT);
                
                level.setBlockAndUpdate(this.getOnPos(), light);
            }
        }
        else
        {
            displayParticle(getLongerParticleFrequency(), longerParticleStateList.get(getTier()));
            
            if(!(getTier() == TIER_GONE && shouldShowNoSmokeWhenBurnedOut()))
                displayParticle(getShorterParticleFrequency(), ParticleTypes.SMOKE);
        }
    }
    
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result)
    {
        final Entity entity = result.getEntity();
        super.onHitEntity(result);
        
        final boolean shouldExtendBurnTicks = entity.getRemainingFireTicks() <= HIT_STD_MAX_TICKS * getTier();
        
        final int hitResult = shouldCheckFireResistMob() ? 1 :
            (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Zoglin) ?
            HIT_RESULT_NO_DAMAGE : HIT_RESULT_DO_DAMAGE;
        
        if(getTier() != TIER_GONE && hitResult == HIT_RESULT_DO_DAMAGE && shouldExtendBurnTicks && shouldLitMob())
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + HIT_STD_EXTEND_FIRE_TICKS * getTier());//OH↑ MY↓ GOD→, I'm fired!!!(＃°Д°)
        
        //! Yes, although the damage might be zero, we still need to do damage to make knockbacks, it's how vanilla works.
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), hitResult);
    }
    
    /**
     * The variation method of the <b>hit result process</b>, whose target is a <b>block</b>.
     */
    protected void onHitBlock(@NotNull BlockHitResult result)
    {
        super.onHitBlock(result);
        if(this.level().isClientSide)
            return;
        
        if(getTier() == TIER_WILD || this.isInLiquid())
        {
            playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, SoundConstants.NORMAL_SOUND_VOLUME);
            displayDestroyParticle();
            displayParticle(NO_FREQUENCY, ParticleTypes.SMOKE);
            
            return;
        }
        
        final BlockPos hitPos = result.getBlockPos();
        final Direction hitSide = result.getDirection();
        final @Nullable BlockPos placementPos;
        
        switch(hitSide)
        {
            case Direction.UP -> placementPos = hitPos.above();
            case Direction.DOWN -> placementPos = null;
            default -> placementPos = hitPos.relative(hitSide);
        }
        
        @Nullable BlockState stateToPlace = null;
        
        if(hitSide == Direction.UP)
            stateToPlace = TTorchRegistries.TEMPORARY_TORCH.value().defaultBlockState();
        else if(hitSide != Direction.DOWN)
            stateToPlace = TTorchRegistries.TEMPORARY_WALL_TORCH.value().defaultBlockState().setValue(TemporaryWallTorchBlock.FACING, hitSide);
        
        if(stateToPlace != null && getTier() == TIER_GONE)
            stateToPlace.setValue(LIGHT_PROPERTY, TTorchConstants.LightState.DARK);
        
        if(stateToPlace != null && tryPlaceTorch(stateToPlace, placementPos))
            playSound(SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, SoundConstants.LOUD_SOUND_VOLUME);
        else
        {
            playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, SoundConstants.LOUD_SOUND_VOLUME);
            displayDestroyParticle();
        }
    }
    
    protected boolean tryPlaceTorch(@NotNull BlockState state, @NotNull BlockPos pos)
    {
        if(!(state.canSurvive(this.level(), pos) && canBeActuallyPlaced(pos)))
            return false;
        
        this.level().setBlockAndUpdate(pos, state);
        return true;
    }
    
    protected boolean canBeActuallyPlaced(@NotNull BlockPos pos)
    {
        final Block posBlock = this.level().getBlockState(pos).getBlock();
        final BlockState posBlockState = this.level().getBlockState(pos);
        
        if(posBlock instanceof AbstractTemporaryTorchBlock || posBlock instanceof AbstractTemporaryWallTorchBlock &&
            (posBlockState.getValue(LIGHT_PROPERTY).ordinal() <= TTorchConstants.LightState.DIM.ordinal()))
        {//* If the state is too dark, of course the torch can be replaced.
            this.level().levelEvent(LEVEL_BLOCK_DESTROY_EVENT_ID, pos, Block.getId(posBlockState));
            return true;
        }
        
        //! If the block isn't a child of Temporary Torches, then check whether the block can be replaced by vanilla tags.
        return posBlock == Blocks.AIR || posBlockState.is(BlockTags.REPLACEABLE);
    }
    
    /**
     * @apiNote This method will always be called no matter what things did entity hit.
     */
    @Override
    protected final void onHit(@NotNull HitResult result)
    {
        super.onHit(result);
        
        if(!this.level().isClientSide)
            this.discard();
    }
    
    /**
     * The method which overrides the super method in order to <b>make <u>{@link #displayDestroyParticle()}</u> work</b>.
     */
    @Override
    public final void handleEntityEvent(byte id)
    {
        if(id == ENTITY_DESTROY_EVENT_ID && this.level().isClientSide)
        {
            for(int attempt = 0; attempt < 5; attempt++)
            {
                final double offsetX = (this.random.nextDouble() - OFFSET_CALCULATE_CONSTANT) * OFFSET_CALCULATE_CONSTANT;
                final double offsetY = this.random.nextDouble() * OFFSET_CALCULATE_CONSTANT;
                final double offsetZ = (this.random.nextDouble() - OFFSET_CALCULATE_CONSTANT) * OFFSET_CALCULATE_CONSTANT;
                
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, getDefaultItem().getDefaultInstance()),
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED
                );
            }
        }
        else
            super.handleEntityEvent(id);
    }
    //endregion
    
    //  region
    //* Utils
    private void displayDestroyParticle() { this.level().broadcastEntityEvent(this, (byte) ENTITY_DESTROY_EVENT_ID); }
    
    /**
     * The method that <b>simplifies <u>{@link net.minecraft.world.level.Level#addParticle(ParticleOptions, double, double, double, double, double, double) addParticle()}</u> method</b>.
     * @param frequency The frequency of the particle. To simple, <b>every second the particle will display</b>
     *                  [<b>20(A single second to tick) ÷ frequency</b>] <b>times</b>.
     */
    private void displayParticle(int frequency, @NotNull ParticleOptions particle)
    {
        //! Checks whether the current tick matches the frequency.
        if(this.tickCount % frequency != 0)
            return;
        
        final double PARTICLE_SPEED = (this.random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        this.level().addParticle(
            particle,
            this.getX(), this.getY(), this.getZ(),
            PARTICLE_SPEED, PARTICLE_SPEED, PARTICLE_SPEED
        );
    }
    
    private @NotNull Map<Integer, ParticleOptions> processLongerParticleStateList(ParticleOptions @NotNull ... states)
    {
        final Map<Integer, ParticleOptions> longerParticleStateList = new HashMap<>();
        
        for(int index = 0; index < LONGER_PARTICLE_STATES; index++)
            longerParticleStateList.put(index, states[index]);
        
        return longerParticleStateList;
    }
    
    public final int getTier() { return this.getEntityData().get(FIRE_TIER_ID); }
    
    private void changeTier(int goalTier, @NotNull SoundEvent sound)
    {
        if(getTier() == goalTier)
            return;
        
        this.getEntityData().set(FIRE_TIER_ID, goalTier);
        playSound(sound, SoundSource.AMBIENT, SoundConstants.LOUD_SOUND_VOLUME);
    }
    
    protected final void playSound(SoundEvent sound, SoundSource soundSource, float volume)
        { this.level().playSound(null, getOnPos(), sound, soundSource, volume, SoundConstants.NORMAL_SOUND_PITCH); }
    //endregion
    
    //  region
    //* Parameter getters
    protected @NotNull ParticleOptions[] getLongerParticleStateList() { return DEFAULT_LONGER_PARTICLE_STATE_LIST; }
    
    protected int getLongerParticleFrequency() { return DEFAULT_LONGER_PARTICLE_FREQUENCY; }
    protected int getShorterParticleFrequency() { return DEFAULT_SHORTER_PARTICLE_FREQUENCY; }
    
    protected boolean shouldCheckLiquids() { return true; }
    protected boolean shouldShowNoSmokeWhenBurnedOut() { return true; }
    protected boolean shouldCheckFireResistMob() { return true; }
    protected boolean shouldLitMob() { return true; }
    //endregion
}
