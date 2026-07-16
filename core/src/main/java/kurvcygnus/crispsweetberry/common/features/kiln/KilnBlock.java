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
import kurvcygnus.crispsweetberry.utils.DefinitionUtils;
import kurvcygnus.crispsweetberry.utils.MinecraftConstants;
import kurvcygnus.crispsweetberry.utils.VisualUtils;
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
    private static final double SOUND_HORIZONTICAL_OFFSET = .5D;
    
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
    
    @Override protected @NotNull MapCodec<? extends BaseEntityBlock> codec() { return MinecraftConstants.OfSerializationBasics.noArgCodec(KilnBlock::new); }
    //endregion
    
    //region World Logic & Life Cycles
    @Override public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean isMoving)
    {
        if(oldState.is(state.getBlock()))
            return;
        
        if(!state.getValue(LIT))
            level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, MinecraftConstants.OfSoundValues.QUIET_SOUND_VOLUME, 2.6F);
        
        super.onPlace(state, level, pos, oldState, isMoving);
    }
    
    @Override public @NotNull BlockState getStateForPlacement(@NotNull BlockPlaceContext context)
    {
        final var state = this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        
        final var itemstack = context.getItemInHand();
        
        boolean isLit = true;
        final var customData = itemstack.get(DataComponents.CUSTOM_DATA);
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
            level.playSound(null, currentPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, MinecraftConstants.OfSoundValues.QUIET_SOUND_VOLUME, 2.6F);
            return state.setValue(LIT, false);
        }
        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }
    
    @Override public void onRemove(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving)
    {
        if(state.is(newState.getBlock()))
            return;
        
        final var blockEntity = level.getBlockEntity(pos);
        
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
        final var stack = new ItemStack(this);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(DefinitionUtils.createTag(t -> t.putBoolean(LIT_PROPERTY, state.getValue(LIT)))));
        return stack;
    }
    
    @Override public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder params)
    {
        final var drops = super.getDrops(state, params);
        
        for(final var stack: drops)
            if(stack.is(this.asItem()))
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(DefinitionUtils.createTag(t -> t.putBoolean(LIT_PROPERTY, state.getValue(LIT)))));
        
        return drops;
    }
    //endregion
    
    //region Block Entity Linking
    @Override public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
        @NotNull Level level,
        @NotNull BlockState state,
        @NotNull BlockEntityType<T> serverBlockEntityType
    )
    {
        return !level.isClientSide ?
            createTickerHelper(serverBlockEntityType, KilnRegistries.KILN_BLOCK_ENTITY.get(), KilnBlockEntity::serverTick) :
            null;//! Tick is handled by server, client shouldn't touch this.
    }
    
    @Override @Contract("_, _ -> new") public @NotNull BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) { return new KilnBlockEntity(pos, state); }
    //endregion
    
    //region Interact Basics
    @Override protected @NotNull InteractionResult useWithoutItem(
        @NotNull BlockState state,
        @NotNull Level level,
        @NotNull BlockPos pos,
        @NotNull Player player,
        @NotNull BlockHitResult hitResult
    )
    {
        if(level.isClientSide)
            return InteractionResult.SUCCESS;
        
        final var blockEntity = level.getBlockEntity(pos);
        if(blockEntity instanceof KilnBlockEntity kiln)
        {
            player.openMenu(kiln, pos);
            player.awardStat(KilnRegistries.INTERACT_WITH_KILN.value());
        }
        
        return InteractionResult.CONSUME;
    }
    
    @Override protected @NotNull ItemInteractionResult useItemOn(
        @NotNull ItemStack stack,
        @NotNull BlockState state,
        @NotNull Level level,
        @NotNull BlockPos pos,
        @NotNull Player player,
        @NotNull InteractionHand hand,
        @NotNull BlockHitResult hitResult
    )
    {
        if(state.getValue(LIT))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        
        final var itemInHand = stack.getItem();
        
        if(canLitStuff(stack, itemInHand))
        {
            final boolean isDamageable = stack.isDamageableItem();
            final float DAMAGEABLE_ITEM_PITCH = level.getRandom().nextFloat() * .4F + .8F;
            
            level.playSound(null, pos, isDamageable ? SoundEvents.FLINTANDSTEEL_USE : SoundEvents.FIRECHARGE_USE,
                            SoundSource.BLOCKS, MinecraftConstants.OfSoundValues.NORMAL_SOUND_VOLUME, isDamageable ? DAMAGEABLE_ITEM_PITCH : MinecraftConstants.OfSoundValues.NORMAL_SOUND_PITCH
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
        final double xPos = (double) pos.getX() + SOUND_HORIZONTICAL_OFFSET;
        final double yPos = pos.getY();
        final double zPos = (double) pos.getZ() + SOUND_HORIZONTICAL_OFFSET;
        
        if(random.nextDouble() < .1)
            level.playLocalSound(
                xPos,
                yPos,
                zPos,
                SoundEvents.FURNACE_FIRE_CRACKLE,
                SoundSource.BLOCKS,
                MinecraftConstants.OfSoundValues.NORMAL_SOUND_VOLUME,
                MinecraftConstants.OfSoundValues.NORMAL_SOUND_PITCH,
                false
            );
        
        final var direction = state.getValue(FACING);
        final var directionAxis = direction.getAxis();
        
        final double particleBaseRandomOffset = random.nextDouble() * .6 - .3;
        final double particleXOffset = directionAxis == Direction.Axis.X ? (double) direction.getStepX() * .52 : particleBaseRandomOffset;
        final double particleYOffset = random.nextDouble() * 6. / 16.;
        final double particleZOffset = directionAxis == Direction.Axis.Z ? (double) direction.getStepZ() * .52 : particleBaseRandomOffset;
        
        VisualUtils.addParticles(
            level,
            xPos + particleXOffset,
            yPos + particleYOffset,
            zPos + particleZOffset,
            MinecraftConstants.OfProjectileValues.X_NO_SPEED,
            MinecraftConstants.OfProjectileValues.Y_NO_SPEED,
            MinecraftConstants.OfProjectileValues.Z_NO_SPEED,
            ParticleTypes.SMOKE,
            ParticleTypes.FLAME
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
