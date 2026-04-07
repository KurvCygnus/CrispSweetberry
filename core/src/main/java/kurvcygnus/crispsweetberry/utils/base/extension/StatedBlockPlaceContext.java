//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils.base.extension;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * A extended <u>{@link BlockPlaceContext}</u>, which extends the ability of block placement with
 * the support of specifying the <u>{@link BlockState}</u> to place.
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class StatedBlockPlaceContext extends BlockPlaceContext
{
    private final BlockState placeState;
    
    public StatedBlockPlaceContext(@NotNull UseOnContext context, @NotNull BlockState placeState)
    {
        super(context);
        Objects.requireNonNull(placeState, "Param \"placeState\" must not be null!");
        this.placeState = placeState;
    }
    
    public @NotNull InteractionResult performPlace()
    {
        if(!this.placeState.getBlock().isEnabled(this.getLevel().enabledFeatures()) || !this.canPlace())
            return InteractionResult.FAIL;
        
        final Player player = this.getPlayer();
        
        final BlockPos pos = this.getClickedPos();
        final Level level = this.getLevel();
        final ItemStack itemStack = this.getItemInHand();
        
        if(
            !level.isUnobstructed(this.placeState, pos, player == null ? CollisionContext.empty() : CollisionContext.of(player)) ||
            !level.setBlock(pos, this.placeState, Block.UPDATE_ALL_IMMEDIATE)
        )
            return InteractionResult.FAIL;
        
        final BlockState currentState = level.getBlockState(pos);
        if(currentState.is(this.placeState.getBlock()))
        {
            currentState.getBlock().setPlacedBy(level, pos, currentState, player, itemStack);
            
            if(player instanceof ServerPlayer serverPlayer)
                CriteriaTriggers.PLACED_BLOCK.trigger(serverPlayer, pos, itemStack);
        }
        
        final SoundType soundType = currentState.getSoundType(level, pos, player);
        level.playSound(
            null,
            pos,
            soundType.getPlaceSound(),
            SoundSource.BLOCKS,
            (soundType.getVolume() + 1.0F) / 2.0F,
            soundType.getPitch() * 0.8F
        );
        
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, currentState));
        
        if(player == null || !player.getAbilities().instabuild)
            itemStack.shrink(1);
        
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    
    public boolean cancelPlacement() { return this.getLevel().setBlockAndUpdate(this.getClickedPos(), Blocks.VOID_AIR.defaultBlockState()); }
}
