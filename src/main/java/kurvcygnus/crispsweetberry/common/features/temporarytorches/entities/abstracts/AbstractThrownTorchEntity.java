package kurvcygnus.crispsweetberry.common.features.temporarytorches.entities.abstracts;

import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.blocks.abstracts.ITemporaryTorchBehaviors;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.client.renderers.abstracts.AbstractThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.temporarytorches.items.abstracts.AbstractThrowableTorchItem;
import kurvcygnus.crispsweetberry.common.registries.CrispBlocks;
import kurvcygnus.crispsweetberry.common.registries.CrispItems;
import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
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
import net.minecraft.util.StringRepresentable;
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

import static kurvcygnus.crispsweetberry.utils.definitions.ProjectileConstants.*;

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
    public static final int TIER_GONE = 0;//The tier for water contraction, named "GONE" as the fire is gone.
    public static final int TIER_NORM = 1;//The tier for standard case, the torch will be this tier as long as nothing special happens.
    public static final int TIER_WILD = 2;//The tier for lava contraction, named "WILD" since the fire will go wild as it torches lava.
    protected static final int PROPERTY_CORRECTION_INDEX = 1;
    protected static final int BRIGHTNESS_PER_STATE = 4;
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
    
    /**
     * The <b>enum property</b> that controls the <b>life cycle and brightness</b> of temporary torches.
     */
    public enum LightState implements StringRepresentable
    {
        DARK, DIM, BRIGHT, FULL_BRIGHT;//DARK coordinates burned out, but to put the state name more simple, the later one got deprecated.
        
        /**
         * The <b>formula</b> to <b>convert enum to actual brightness value</b>.<br><br>
         * <b>e.g.</b><br>
         * <b><u>{@link #FULL_BRIGHT}</u></b> ->
         * <b>3</b>(<u>{@link #FULL_BRIGHT FULL_BRIGHT}</u>{@code .ordinal()})
         * <b>× 4</b>(<u>{@link #BRIGHTNESS_PER_STATE}</u>) <b> = 12</b>
         */
        public int toBrightness() { return this.ordinal() * BRIGHTNESS_PER_STATE; }
        
        public @NotNull LightState getNextState()
        {
            //! Do boundary check at the same time.
            return this.ordinal() - PROPERTY_CORRECTION_INDEX > LightState.DARK.ordinal() ?
                LightState.values()[this.ordinal() - PROPERTY_CORRECTION_INDEX] : LightState.DARK;
        }
        
        /**
         * The <b>essential method</b> for <b>registering the state names correctly</b>.
         * @return The names of <b>corresponded states</b>.
         */
        @Override
        public @NotNull String getSerializedName() { return this.name().toLowerCase(); }
    }
    
    public static final EntityDataAccessor<Integer> FIRE_TIER_ID = SynchedEntityData.defineId(AbstractThrownTorchEntity.class, EntityDataSerializers.INT);
    
    protected final Map<Integer, SimpleParticleType> longerParticleStateList = processLongerParticleStateList(getLongerParticleStateList());
    
    protected final int LONGER_PARTICLE_FREQUENCY = getLongerParticleFrequency();
    protected final int SHORTER_PARTICLE_FREQUENCY = getShorterParticleFrequency();
    
    
    protected final boolean shouldCheckLiquids = getShouldCheckLiquidsFlag();
    protected final boolean noSmokeWhenBurnedOut = getNoSmokeWhenBurnedOutFlag();
    protected final boolean shouldCheckFireResistMob = getShouldCheckFireResistMobFlag();
    protected final boolean shouldLitMob = getShouldLitMobFlag();
    
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
        
        if(!this.level().isClientSide && shouldCheckLiquids)
        {
            if(this.isInLava())
                changeTier(TIER_WILD, SoundEvents.LAVA_EXTINGUISH);
            else if(this.isInWater())
                changeTier(TIER_GONE, SoundEvents.FIRE_EXTINGUISH);
        }
        else
        {
            displayParticle(LONGER_PARTICLE_FREQUENCY, longerParticleStateList.get(getTier()));
            
            if(!(getTier() == TIER_GONE && noSmokeWhenBurnedOut))
                displayParticle(SHORTER_PARTICLE_FREQUENCY, ParticleTypes.SMOKE);
        }
    }
    
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result)
    {
        final Entity entity = result.getEntity();
        super.onHitEntity(result);
        
        final boolean shouldExtendBurnTicks = entity.getRemainingFireTicks() <= HIT_STD_MAX_TICKS * getTier();
        
        final int hitResult = shouldCheckFireResistMob ?
            1 : (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Zoglin) ?
            HIT_RESULT_NO_DAMAGE : HIT_RESULT_DO_DAMAGE;//TODO: 做自定义Fire-Resistant Mob标签
        
        if(getTier() != TIER_GONE && hitResult == HIT_RESULT_DO_DAMAGE && shouldExtendBurnTicks && shouldLitMob)
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
        if(!this.level().isClientSide)
        {
            if(!tryHandleSpecialCase())
                return;
            
            final BlockPos placementPos = getPlacementPos(result);
            final BlockState stateToPlace = getPlacementState(result, placementPos);
            
            if(stateToPlace != null && tryPlaceTorch(stateToPlace, placementPos))
                playSound(SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, SoundConstants.LOUD_SOUND_VOLUME);
            else
                handlePlacementFailure();
        }
    }
    
    /**
     * @apiNote This method will always be called no matter what things did entity hit.
     */
    @Override
    protected void onHit(@NotNull HitResult result)
    {
        super.onHit(result);
        
        if(!this.level().isClientSide)
            this.discard();
    }
    //endregion
    
    //  region
    //* Placement Helpers
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
                
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, CrispItems.THROWABLE_TORCH.value().getDefaultInstance()),
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED);
            }
        }
        else
            super.handleEntityEvent(id);
    }
    
    /**
     * The method which is <b>a part of <u>{@link #onHitBlock onHitBlock()}</u></b>,<br>
     * <i>If any of the condition is false, also do failed events</i>.
     */
    private boolean tryHandleSpecialCase()
    {
        if(getTier() == TIER_WILD || this.isInLiquid())
        {
            playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, SoundConstants.NORMAL_SOUND_VOLUME);
            displayDestroyParticle();
            displayParticle(NO_FREQUENCY, ParticleTypes.SMOKE);
            
            return false;
        }
        
        return true;
    }
    
    /**
     * This method impacts <b>whether the torch will be standard one, or the wall one.</b>
     */
    private BlockPos getPlacementPos(@NotNull BlockHitResult result)
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
    
    private BlockState getPlacementState(@NotNull BlockHitResult result, BlockPos placementPos)
    {
        if(placementPos == null)
            return null;
        
        final Direction hitSide = result.getDirection();
        BlockState baseState = null;
        
        if(hitSide == Direction.UP)
            baseState = CrispBlocks.TEMPORARY_TORCH.value().defaultBlockState();
        else if(hitSide != Direction.DOWN)
            baseState = CrispBlocks.TEMPORARY_WALL_TORCH.value().defaultBlockState().setValue(TemporaryWallTorchBlock.FACING, hitSide);
        
        if(baseState != null && getTier() == TIER_GONE)
            return baseState.setValue(ITemporaryTorchBehaviors.LIGHT_PROPERTY, LightState.DARK);
        
        return baseState;
    }
    
    protected boolean tryPlaceTorch(@NotNull BlockState state, @NotNull BlockPos pos)
    {
        if(!(state.canSurvive(this.level(), pos) && canBeActuallyPlaced(pos)))
            return false;
        
        this.level().setBlockAndUpdate(pos, state);
        return true;
    }
    
    private void handlePlacementFailure()
    {
        playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, SoundConstants.LOUD_SOUND_VOLUME);
        
        displayDestroyParticle();
    }
    
    protected boolean canBeActuallyPlaced(@NotNull BlockPos pos)
    {
        final Block posBlock = this.level().getBlockState(pos).getBlock();
        final BlockState posBlockState = this.level().getBlockState(pos);
        
        if(posBlock instanceof AbstractTemporaryTorchBlock || posBlock instanceof AbstractTemporaryWallTorchBlock &&
            (posBlockState.getValue(ITemporaryTorchBehaviors.LIGHT_PROPERTY).ordinal() <= LightState.DIM.ordinal()))
            {//* If the state is too dark, of course the torch can be replaced.
                this.level().levelEvent(LEVEL_BLOCK_DESTROY_EVENT_ID, pos, Block.getId(posBlockState));
                return true;
            }
        
        //! If the block isn't a child of Temporary Torches, then check whether the block can be replaced by vanilla tags.
        //   (Of course air can be replaced)
        return posBlock == Blocks.AIR || posBlockState.is(BlockTags.REPLACEABLE);
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
    private void displayParticle(int frequency, ParticleOptions particle)
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
    
    private Map<Integer, SimpleParticleType> processLongerParticleStateList(SimpleParticleType... states)
    {
        final Map<Integer, SimpleParticleType> longerParticleStateList = new HashMap<>();
        
        for(int index = 0; index < LONGER_PARTICLE_STATES; index++)
            longerParticleStateList.put(index, states[index]);
        
        return longerParticleStateList;
    }
    
    public final int getTier() { return this.getEntityData().get(FIRE_TIER_ID); }
    
    private void changeTier(int goalTier, SoundEvent sound)
    {
        if(getTier() == goalTier)
            return;//! Directly terminates this method if the tier doesn't have to change.
        
        this.getEntityData().set(FIRE_TIER_ID, goalTier);
        playSound(sound, SoundSource.AMBIENT, SoundConstants.LOUD_SOUND_VOLUME);
    }
    
    /**
     * This method is a simplified version of the original one, which plays sound at normal pitch.
     */
    protected final void playSound(SoundEvent sound, SoundSource soundSource, float volume)
        { this.level().playSound(null, getOnPos(), sound, soundSource, volume, SoundConstants.NORMAL_SOUND_PITCH); }
    //endregion
    
    //  region
    //* Abstracts parameter getters
    /**
     * Getter method for <b>torch's main particles</b>, <b>if you don't want to customize this, just return <u>{@link #DEFAULT_LONGER_PARTICLE_STATE_LIST}</u></b>.
     */
    protected abstract @NotNull SimpleParticleType[] getLongerParticleStateList();
    
    protected abstract int getLongerParticleFrequency();//* Return DEFAULT_LONGER_PARTICLE_FREQUENCY if you don't need customization.
    protected abstract int getShorterParticleFrequency();//* Return DEFAULT_SHORTER_PARTICLE_FREQUENCY if you don't need customization.
    
    //* Return true if you don't need customizations for these elements.
    protected abstract boolean getShouldCheckLiquidsFlag();
    protected abstract boolean getNoSmokeWhenBurnedOutFlag();
    protected abstract boolean getShouldCheckFireResistMobFlag();
    protected abstract boolean getShouldLitMobFlag();
    //endregion
}
