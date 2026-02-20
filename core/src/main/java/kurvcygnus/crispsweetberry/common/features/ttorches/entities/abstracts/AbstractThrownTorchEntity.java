//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.ttorches.entities.abstracts;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchConstants;
import kurvcygnus.crispsweetberry.common.features.ttorches.TTorchRegistries;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.FakeLightBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractGenericTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.abstracts.AbstractTemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.blocks.basic.TemporaryWallTorchBlock;
import kurvcygnus.crispsweetberry.common.features.ttorches.client.renderers.abstracts.AbstractThrownTorchRenderer;
import kurvcygnus.crispsweetberry.common.features.ttorches.items.abstracts.AbstractThrowableTorchItem;
import kurvcygnus.crispsweetberry.utils.definitions.SoundConstants;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
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
import org.intellij.lang.annotations.MagicConstant;
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
    /**
     * The tier for water contraction, named "GONE" as the fire is gone.
     */
    public static final byte TIER_GONE = 0;
    
    /**
     * The tier for standard case, the torch will be this tier as long as nothing special happens.
     */
    public static final byte TIER_NORM = 1;
    
    /**
     * The tier for lava contraction, named "WILD" since the fire will go wild as it torches lava.
     */
    public static final byte TIER_WILD = 2;
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
    
    /**
     * The list used for tick participle display.<br>
     * Each particle's index corresponds the index constants of tiers above.
     */
    private static final ParticleOptions[] DEFAULT_LONGER_PARTICLE_STATE_LIST = { ParticleTypes.DRIPPING_WATER, ParticleTypes.SMALL_FLAME, ParticleTypes.FLAME };
    private static final ParticleOptions DEFAULT_SHORTER_PARTICLE = ParticleTypes.SMOKE;
    
    //! EntityDataAccessor doesn't support Enum, so we use byte instead.
    public static final EntityDataAccessor<Byte> FIRE_TIER_ID = SynchedEntityData.defineId(AbstractThrownTorchEntity.class, EntityDataSerializers.BYTE);
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "THROWN_TORCH");
    
    protected final Map<Byte, ParticleOptions> longerParticleStateList = processLongerParticleStateList(getLongerParticleStateList());
    
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
        double x, double y, double z, @NotNull Level level) { super(childrenClazzValue, x, y, z, level); }
    
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
        @NotNull LivingEntity shooter, @NotNull Level level) { super(childrenClazzValue, shooter, level); }
    //endregion
    
    //  region
    //* Lifecycles & Logics
    @Override
    public void tick()
    {
        super.tick();
        
        final Level level = this.level();
        try(MarkLogger.MarkerHandle handle = LOGGER.pushMarker("TICK"))
        {
            if(!level.isClientSide)
            {
                if(shouldCheckLiquids())
                {
                    try(MarkLogger.MarkerHandle ignored = LOGGER.pushMarker("LIQUID_CHECK"))
                    {
                        if(this.isInLava())
                        {
                            LOGGER.debug("In lava. Tier switched to WILD.");
                            changeTier(TIER_WILD, SoundEvents.LAVA_EXTINGUISH);
                        }
                        else if(this.isInWater())
                        {
                            LOGGER.debug("In water. Tier switched to GONE.");
                            changeTier(TIER_GONE, SoundEvents.FIRE_EXTINGUISH);
                        }
                    }
                }
                
                if(!this.isInLiquid())
                {
                    handle.changeMarker("LIGHT_FUNC");
                    
                    if(this.onGround() || this.horizontalCollision || this.verticalCollision)
                        return;
                    
                    final @Nullable BlockPos pos = pickPos(level, this.getOnPos());
                    
                    if(pos == null)
                        return;
                    
                    final BlockState light = TTorchRegistries.FAKE_LIGHT_BLOCK.value().defaultBlockState().
                        setValue(LIGHT_PROPERTY, getTier() == TIER_GONE ? TTorchConstants.LightState.DARK : TTorchConstants.LightState.FULL_BRIGHT);
                    
                    LOGGER.debug("Current isn't in any liquid, emulate lighting.");
                    
                    level.setBlock(pos, light, Block.UPDATE_CLIENTS);
                }
            }
            else
            {
                final byte longerParticleIndex = shouldCheckLiquids() ? getTier() : 0;
                
                displayParticle(getLongerParticleFrequency(), longerParticleStateList.get(longerParticleIndex));
                
                if(!(getTier() == TIER_GONE && shouldShowNoSmokeWhenBurnedOut()))
                    displayParticle(getShorterParticleFrequency(), getShorterParticle());
            }
        }
    }
    
    private @Nullable BlockPos pickPos(@NotNull Level level, @NotNull BlockPos pos)
    {
        final BlockPos[] candidates = { pos, pos.above(), pos.below() };
        
        for(final BlockPos candidate: candidates)
        {
            final BlockState state = level.getBlockState(candidate);
            
            if(state.isAir())
                return candidate;
        }
        
        return null;
    }
    
    @Override
    protected void onHitEntity(@NotNull EntityHitResult result)
    {
        final Entity entity = result.getEntity();
        super.onHitEntity(result);
        
        final boolean shouldExtendBurnTicks = entity.getRemainingFireTicks() <= HIT_STD_MAX_TICKS * getTier();
        
        final int hitDamage = shouldCheckFireResistMob() ? 1 :
            (entity instanceof Blaze || entity instanceof MagmaCube || entity instanceof Zoglin) ?
            HIT_RESULT_NO_DAMAGE : HIT_RESULT_DO_DAMAGE;
        
        if(getTier() != TIER_GONE && hitDamage == HIT_RESULT_DO_DAMAGE && shouldExtendBurnTicks && shouldLitMob())
            entity.setRemainingFireTicks(entity.getRemainingFireTicks() + HIT_STD_EXTEND_FIRE_TICKS * getTier());
        
        this.onHitEntitySequence(entity);
        
        //! Yes, although the damage might be zero, we still need to do damage to make knockbacks, it's how vanilla works.
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), hitDamage);
    }
    
    @Override
    protected void onHitBlock(@NotNull BlockHitResult result)
    {
        super.onHitBlock(result);
        
        if(this.level().isClientSide)
            return;
        
        try(MarkLogger.MarkerHandle handle = LOGGER.pushMarker("HIT_BLOCK"))
        {
            if(getTier() == TIER_WILD || this.isInLiquid())
            {
                handle.changeMarker("HIT_FAILURE");
                playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, SoundConstants.NORMAL_SOUND_VOLUME);
                displayDestroyParticle();
                displayParticle(NO_FREQUENCY, DEFAULT_SHORTER_PARTICLE);
                LOGGER.debug("{}", getTier() == TIER_WILD ?
                    "Torch is too hot to be placed, shattered instead." :
                    "Torch is in liquid and can't be placed. Shattered."
                );
                
                return;
            }
            
            final BlockPos hitPos = result.getBlockPos();
            final Direction hitSide = result.getDirection();
            final @Nullable BlockPos placementPos;
            final @Nullable BlockState stateToPlace;
            
            try(MarkLogger.MarkerHandle ignored = LOGGER.pushMarker("DIRECTION_PICK"))
            {
                switch(hitSide)
                {
                    case Direction.UP ->
                    {
                        placementPos = hitPos.above();
                        stateToPlace = getFloorTorchBlock().defaultBlockState();
                    }
                    case Direction.DOWN ->
                    {
                        placementPos = null;
                        stateToPlace = null;
                    }
                    default ->
                    {
                        placementPos = hitPos.relative(hitSide);
                        stateToPlace = getWallTorchBlock().defaultBlockState().setValue(TemporaryWallTorchBlock.FACING, hitSide);
                    }
                }
                
                LOGGER.when(placementPos != null).
                    debug(() -> "Legal Direction & State Confirmed. Direction: {}, State: {}", () -> new Object[] { placementPos, stateToPlace });
                
                LOGGER.when(placementPos == null).
                    debug(() -> "Illegal Direction. Both Direction and State are set to null.");
            }
            
            if(stateToPlace != null && getTier() == TIER_GONE)
            {
                LOGGER.debug("Torch is dark, the final placed block is also set to dark.");
                stateToPlace.setValue(LIGHT_PROPERTY, TTorchConstants.LightState.DARK);
            }
            
            if(stateToPlace != null && tryPlaceTorch(stateToPlace, placementPos))
                playSound(SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, SoundConstants.LOUD_SOUND_VOLUME);
            else
            {
                playSound(SoundEvents.SCAFFOLDING_BREAK, SoundSource.BLOCKS, SoundConstants.LOUD_SOUND_VOLUME);
                displayDestroyParticle();
            }
        }
    }
    
    protected boolean tryPlaceTorch(@NotNull BlockState state, @NotNull BlockPos pos)
    {
        try(MarkLogger.MarkerHandle ignored = LOGGER.pushMarker("PLACE_TORCH"))
        {
            if(!(state.canSurvive(this.level(), pos) && canBeActuallyPlaced(pos)))
            {
                LOGGER.debug(
                    "Can't place torch. Placement check fails at {}.",
                    !state.canSurvive(this.level(), pos) ? 
                        "the survive check of block itself" :
                        "method #canBeActuallyPlaced()"
                );
                return false;
            }
            
            LOGGER.debug("Placement succeed.");
            this.level().setBlockAndUpdate(pos, state);
            return true;
        }
    }
    
    protected boolean canBeActuallyPlaced(@NotNull BlockPos pos)
    {
        final Block posBlock = this.level().getBlockState(pos).getBlock();
        final BlockState posBlockState = this.level().getBlockState(pos);
        
        if(posBlock instanceof AbstractGenericTorchBlock<?> && (posBlockState.getValue(LIGHT_PROPERTY).ordinal() <= TTorchConstants.LightState.DIM.ordinal()))
        {
            this.level().levelEvent(LEVEL_BLOCK_DESTROY_EVENT_ID, pos, Block.getId(posBlockState));
            return true;
        }
        
        //! If the block isn't a child of Temporary Torches, then check whether the block can be replaced by vanilla tags.
        return posBlock == Blocks.AIR || posBlock instanceof FakeLightBlock || posBlockState.is(BlockTags.REPLACEABLE);
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
     * The method which key the super method in order to <b>make <u>{@link #displayDestroyParticle()}</u> work</b>.
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
        
        final double particleSpeed = (this.random.nextDouble() * 2.0D - 1.0D) * 0.03D;
        
        this.level().addParticle(
            particle,
            this.getX(),
            this.getY(),
            this.getZ(),
            particleSpeed,
            particleSpeed,
            particleSpeed
        );
    }
    
    private @NotNull Map<Byte, ParticleOptions> processLongerParticleStateList(ParticleOptions @NotNull ... states)
    {
        if(states.length != 1 && states.length != 3)
            throw new IllegalArgumentException("Invalid length of states: %d, it should be 1, or 3.".formatted(states.length));
        
        final boolean duplicate = !shouldCheckLiquids();
        
        final Map<Byte, ParticleOptions> longerParticleStateList = new HashMap<>(3);
        
        for(int index = 0; index < LONGER_PARTICLE_STATES; index++)
            longerParticleStateList.put((byte) index, states[duplicate ? 0 : index]);
        
        return longerParticleStateList;
    }
    
    public final byte getTier() { return this.getEntityData().get(FIRE_TIER_ID); }
    
    private void changeTier(@MagicConstant(intValues = {TIER_GONE, TIER_NORM, TIER_WILD}) byte goalTier, @NotNull SoundEvent sound)
    {
        //noinspection MagicConstant
        if(goalTier == getTier())//! MagicConstant is only used for giving warnings to external usage.
            return;
        
        this.getEntityData().set(FIRE_TIER_ID, goalTier);
        playSound(sound, SoundSource.AMBIENT, SoundConstants.LOUD_SOUND_VOLUME);
    }
    
    protected final void playSound(@NotNull SoundEvent sound, @NotNull SoundSource soundSource, float volume)
        { this.level().playSound(null, getOnPos(), sound, soundSource, volume, SoundConstants.NORMAL_SOUND_PITCH); }
    //endregion
    
    //  region
    //* Misc & Parameter getters
    protected void onHitEntitySequence(@NotNull Entity entity) {}
    
    /**
     * Get the list of Longer Particles.
     * @apiNote Length of this array should be neither 1, or 3, 1 is only used in the case of <u>{@link #shouldCheckLiquids()}</u> always returns {@code false}.
     */
    protected @NotNull ParticleOptions @NotNull [] getLongerParticleStateList() { return DEFAULT_LONGER_PARTICLE_STATE_LIST; }
    protected @NotNull ParticleOptions getShorterParticle() { return DEFAULT_SHORTER_PARTICLE; }
    protected abstract @NotNull AbstractTemporaryTorchBlock<?> getFloorTorchBlock();
    protected abstract @NotNull AbstractTemporaryWallTorchBlock<?> getWallTorchBlock();
    
    protected int getLongerParticleFrequency() { return DEFAULT_LONGER_PARTICLE_FREQUENCY; }
    protected int getShorterParticleFrequency() { return DEFAULT_SHORTER_PARTICLE_FREQUENCY; }
    
    protected boolean shouldCheckLiquids() { return true; }
    protected boolean shouldShowNoSmokeWhenBurnedOut() { return true; }
    protected boolean shouldCheckFireResistMob() { return true; }
    protected boolean shouldLitMob() { return true; }
    //endregion
}
