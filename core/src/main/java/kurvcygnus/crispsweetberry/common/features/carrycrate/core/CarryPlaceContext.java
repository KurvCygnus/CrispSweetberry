//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core;

import kurvcygnus.crispsweetberry.common.features.carrycrate.mixins.IBlockSnapshotCreator;
import kurvcygnus.crispsweetberry.lib.base.extensions.INestedPrintable;
import kurvcygnus.crispsweetberry.lib.base.extensions.StatedBlockPlaceContext;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
import java.util.function.Function;

@ApiStatus.Internal
final class CarryPlaceContext extends StatedBlockPlaceContext
{
    private static final Map<String, Function<StatedBlockPlaceContext, Object>> FIELD_MAP = INestedPrintable.buildFieldMap(
        map ->
        {
            map.putAll(StatedBlockPlaceContext.FIELD_MAP);
            map.put("blockEntityData", c -> ((CarryPlaceContext) c).blockEntityData);
        }
    );
    
    private final @Nullable CompoundTag blockEntityData;
    
    public CarryPlaceContext(@NotNull UseOnContext context, BlockState placeState, @Nullable CompoundTag blockEntityData)
    {
        super(context, placeState);
        this.blockEntityData = blockEntityData;
    }
    
    @Override protected boolean setBlock(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState newState, int flags)
    {
        final boolean isClientSide = level.isClientSide;
        
        if(level.isOutsideBuildHeight(pos) || !isClientSide && level.isDebug())
            return false;
        
        final var levelchunk = level.getChunkAt(pos);
        
        final var immutablePos = pos.immutable();
        
        final @Nullable BlockSnapshot blockSnapshot;
        if(level.captureBlockSnapshots && !isClientSide)
        {
            blockSnapshot = IBlockSnapshotCreator._$csb_callInit(
                level.dimension(),
                level,
                immutablePos,
                newState,
                blockEntityData,
                flags
            );
            
            level.capturedBlockSnapshots.add(blockSnapshot);
        }
        else
            blockSnapshot = null;
        
        final var blockstate = levelchunk.setBlockState(immutablePos, newState, (flags & 64) != 0);
        if(blockstate == null)
        {
            if(blockSnapshot != null)
                level.capturedBlockSnapshots.remove(blockSnapshot);
            return false;
        }
        
        if(blockSnapshot == null)
            level.markAndNotifyBlock(immutablePos, levelchunk, blockstate, newState, flags, 512);
        
        return true;
    }
    
    @Override public @NotNull @Unmodifiable Map<String, Function<StatedBlockPlaceContext, @Nullable Object>> getFields() { return FIELD_MAP; }
}
