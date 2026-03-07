//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.carriables;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarriableExtensions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class JukeboxBlockEntityCarryAdapter extends AbstractBlockEntityCarryAdapter<JukeboxBlockEntity>
{
    private long playingLength;
    private @Nullable JukeboxSong song;
    
    public JukeboxBlockEntityCarryAdapter(@NotNull JukeboxBlockEntity blockEntity) { super(blockEntity); }
    
    @Override protected void onCarriedSequence(@NotNull CarriedContext context, @NotNull JukeboxBlockEntity blockEntity)
    {
        this.playingLength = blockEntity.getSongPlayer().getTicksSinceSongStarted();
        this.song = blockEntity.getSongPlayer().getSong();
    }
    
    @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate(@NotNull JukeboxBlockEntity blockEntity) { return NO_PENALTY; }
    
    @Override protected void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, @NotNull JukeboxBlockEntity blockEntity) 
    {
        //? TODO: Cannot rely on BE's serialization.
    }
    
    @Override protected void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, @NotNull JukeboxBlockEntity blockEntity)
    {
        //? TODO: Cannot rely on BE's serialization.
    }
    
    @Override public void carryingTick(@NotNull CarriableExtensions.ICarryTickable.TickingContext context)
    {
        if(song == null) 
            return;
        
        if(this.playingLength >= song.lengthInTicks())
        {
            this.song = null;
            this.playingLength = 0L;
            return;
        }
        
        final Level level = context.level();
        final int songId = level.registryAccess().registryOrThrow(Registries.JUKEBOX_SONG).getId(song);
        
        //* This levelEvent is actually LevelRenderer#playJukeboxSong(Holder<JukeboxSong>, BlockPos).
        //* It is only implemented in client side, and doesn't exist in abstract class,
        //* So we can only call Level#levelEvent(Player, int, BlockPos, int).
        level.levelEvent(null, 1010, context.entity().getOnPos(), songId);
        this.playingLength++;
    }
    
    @Override public boolean causesOverweight() { return false; }
}
