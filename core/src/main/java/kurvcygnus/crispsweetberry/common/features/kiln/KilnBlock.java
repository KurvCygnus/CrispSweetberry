//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln;

import com.mojang.serialization.MapCodec;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnMenu;
import kurvcygnus.crispsweetberry.utils.definitions.CrispDefUtils;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import kurvcygnus.crispsweetberry.utils.misc.CrispVisualUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static kurvcygnus.crispsweetberry.utils.definitions.SoundConstants.*;
import static kurvcygnus.crispsweetberry.utils.projectile.ProjectileConstants.*;

/**
 * The <b>physically interactable, seen</b> part of the Kiln Block.<br>
 * It mainly holds the <b>attachTag</b>, <b>basic properties</b> and some <b>logical config</b> of an interactable block.
 *
 * @author Kurv Cygnus
 * @implNote Kiln currently won't support dye variants, since it'll make player's inventory exploded.<br>
 * <b><i>We'll introduce this vanilla feature in future updates, with a fantastic QoL...</i></b>
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity Functional Part
 * @see KilnContainerData Data Sync Part
 * @see KilnMenu Menu Part
 * @see KilnRecipeCacheEvent Recipe Initialization
 * @since 1.0 Release
 */
public final class KilnBlock extends BaseEntityBlock
{
    //region Constants & Fields
    //*:=== Constants
    private static final double SOUND_HORIZONTICAL_OFFSET = 0.5D;
    
    public static final String LIT_PROPERTY = "lit";
    
    //*:=== Property Fields
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    //? public static final EnumProperty<DyeColor> COLOR = EnumProperty.apply("color", DyeColor.class);
    //endregion
    
    //region Constructors & Basic Definitions
    public KilnBlock()
    {
        super(BlockBehaviour.Properties.of().
            destroyTime(3.5F).
            requiresCorrectToolForDrops().
            explosionResistance(1.5F).
            sound(SoundType.STONE).
            lightLevel(bs -> bs.getValue(LIT) ? 13 : 0)
        );
        
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, true));
        //? this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, null));
    }
    
    @Override protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, LIT); }
    
    /**
     * @implNote <b>Directly inherits <u>{@link BaseEntityBlock}</u> won't render the appearance of block,
     * unless you specify its <u>{@link RenderShape}</u> like this.</b>
     */
    @Override public @NotNull RenderShape getRenderShape(@NotNull BlockState state) { return RenderShape.MODEL; }
    
    @Override protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(CrispFunctionalUtils.noArgCodec(KilnBlock::new)); }
    //endregion
    
    //region World Logic & Life Cycles
    @Override public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
    {
        if(oldState.is(state.getBlock()))
            return;
        
        if(!state.getValue(LIT))
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, QUIET_SOUND_VOLUME, 2.6F);
        
        super.onPlace(state, level, pos, oldState, isMoving);
    }
    
    @Override public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        final BlockState state = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        
        final ItemStack itemstack = context.getItemInHand();
        
        boolean isLit = true;
        final CustomData customData = itemstack.get(DataComponents.CUSTOM_DATA);
        if(customData != null && customData.contains(LIT_PROPERTY))
            isLit = customData.copyTag().getBoolean(LIT_PROPERTY);
        
        if(context.getLevel().getFluidState(context.getClickedPos()).is(FluidTags.WATER))
            isLit = false;
        
        return state.setValue(LIT, isLit);
    }
    
    @Override public @NotNull BlockState updateShape(
        @NotNull BlockState state,
        @NotNull Direction direction,
        @NotNull BlockState neighborState,
        @NotNull LevelAccessor level,
        @NotNull BlockPos currentPos,
        @NotNull BlockPos neighborPos
    )
    {
        if(state.getValue(LIT) && level.getFluidState(currentPos).is(FluidTags.WATER))
        {
            level.playSound(null, currentPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, QUIET_SOUND_VOLUME, 2.6F);
            return state.setValue(LIT, false);
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
    
    @Override public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving)
    {
        if(state.is(newState.getBlock()))
            return;
        
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        
        if(blockEntity instanceof KilnBlockEntity kiln)
        {
            Containers.dropContents(level, pos, kiln);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        
        super.onRemove(state, level, pos, newState, isMoving);
    }
    
    @Override public @NotNull ItemStack getCloneItemStack(
        @NotNull BlockState state,
        @NotNull HitResult target,
        @NotNull LevelReader level,
        @NotNull BlockPos pos,
        @NotNull Player player
    )
    {
        final ItemStack stack = new ItemStack(this);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(CrispDefUtils.createTag(t -> t.putBoolean(LIT_PROPERTY, state.getValue(LIT)))));
        return stack;
    }
    
    @Override public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder params)
    {
        final List<ItemStack> drops = super.getDrops(state, params);
        
        for(final ItemStack stack: drops)
            if(stack.is(this.asItem()))
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(CrispDefUtils.createTag(t -> t.putBoolean(LIT_PROPERTY, state.getValue(LIT)))));
        
        return drops;
    }
    //endregion
    
    //region Block Entity Linking
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T>
    getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> serverBlockEntityType)
    {
        if(level.isClientSide)//! Tick is handled by server, client shouldn't touch this.
            return null;
        else
            return createTickerHelper(serverBlockEntityType, KilnRegistries.KILN_BLOCK_ENTITY.get(), KilnBlockEntity::serverTick);
    }
    
    @Override @Contract("_, _ -> new")
    public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) { return new KilnBlockEntity(pos, state); }
    //endregion
    
    //region Interact Basics
    @Override protected @NotNull InteractionResult useWithoutItem
    (@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult)
    {
        if(level.isClientSide)
            return InteractionResult.SUCCESS;
        else
        {
            final BlockEntity blockEntity = level.getBlockEntity(pos);
            if(blockEntity instanceof KilnBlockEntity kiln)
            {
                player.openMenu(kiln, pos);
                player.awardStat(KilnRegistries.INTERACT_WITH_KILN.value());
            }
            
            return InteractionResult.CONSUME;
        }
    }
    
    @Override protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level,
        @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult)
            {
                if(state.getValue(LIT))
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                
                final Item itemInHand = stack.getItem();
                
                if(canLitStuff(stack, itemInHand))
                {
                    final boolean isDamageable = stack.isDamageableItem();
                    final float DAMAGEABLE_ITEM_PITCH = level.getRandom().nextFloat() * 0.4F + 0.8F;
                    
                    level.playSound(null, pos, isDamageable ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE,
                        SoundSource.BLOCKS, NORMAL_SOUND_VOLUME, isDamageable ? DAMAGEABLE_ITEM_PITCH : NORMAL_SOUND_PITCH
                    );
                    
                    if(!level.isClientSide)
                    {
                        level.setBlockAndUpdate(pos, state.setValue(LIT, true));
                        if(isDamageable)
                            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                        else
                            stack.consume(1, player);
                    }
                    
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
                
                return ItemInteractionResult.FAIL;
            }
    //endregion
    
    //region Visual Display & Helpers
    //? TODO: Particle Pos Adjust
    @Override public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random)
    {
        final double X_POS = (double) pos.getX() + SOUND_HORIZONTICAL_OFFSET;
        final double Y_POS = pos.getY();
        final double Z_POS = (double) pos.getZ() + SOUND_HORIZONTICAL_OFFSET;
        
        if(random.nextDouble() < 0.1)
            level.playLocalSound(
                X_POS, Y_POS, Z_POS,
                SoundEvents.FURNACE_FIRE_CRACKLE,
                SoundSource.BLOCKS,
                NORMAL_SOUND_VOLUME,
                NORMAL_SOUND_PITCH,
                false
            );
        
        final Direction direction = state.getValue(FACING);
        final Direction.Axis directionAxis = direction.getAxis();
        
        final double PARTICLE_BASE_RANDOM_OFFSET = random.nextDouble() * 0.6 - 0.3;
        final double PARTICLE_X_OFFSET = directionAxis == Direction.Axis.X ? (double) direction.getStepX() * 0.52 : PARTICLE_BASE_RANDOM_OFFSET;
        final double PARTICLE_Y_OFFSET = random.nextDouble() * 6.0 / 16.0;
        final double PARTICLE_Z_OFFSET = directionAxis == Direction.Axis.Z ? (double) direction.getStepZ() * 0.52 : PARTICLE_BASE_RANDOM_OFFSET;
        
        CrispVisualUtils.addParticles(level,
            X_POS + PARTICLE_X_OFFSET, Y_POS + PARTICLE_Y_OFFSET, Z_POS + PARTICLE_Z_OFFSET, X_NO_SPEED, Y_NO_SPEED, Z_NO_SPEED,
            ParticleTypes.SMOKE, ParticleTypes.FLAME
        );
    }
    
    private static boolean canLitStuff(@NotNull ItemStack stack, Item itemInHand)
    {
        return stack.is(ItemTags.CREEPER_IGNITERS) ||
            stack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT) ||
            itemInHand instanceof FlintAndSteelItem ||
            itemInHand instanceof FireChargeItem;
    }
    //endregion
}
