//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.carriables;

import kurvcygnus.crispsweetberry.common.features.carrycrate.api.CarriableSimpleLogicCollection.ISimpleBlockEntityBreakLogic;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.CarriableSimpleLogicCollection.ISimpleBlockEntityPenaltyDropLogic;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.blockentity.AbstractBlockEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Optional;

/**
 * This holds all stuff that is related to <u>{@link JukeboxBlockEntity Jukebox}</u>'s compat.
 * @since 1.0 Release
 */
public final class JukeboxCompatCollection
{
    /**
     * The adapter of <u>{@link JukeboxBlockEntity Jukebox}</u>.
     * It is essentially more like a bridge, most logics are handled by {@link CrispMusicGroover}.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    public static final class JukeboxBlockEntityCarryAdapter extends AbstractBlockEntityCarryAdapter<JukeboxBlockEntity>
    implements ISimpleBlockEntityBreakLogic<JukeboxBlockEntity>, ISimpleBlockEntityPenaltyDropLogic<JukeboxBlockEntity>
    {
        public JukeboxBlockEntityCarryAdapter(@NotNull BlockEntity blockEntity) { super(blockEntity); }
        
        @Override protected void onCarriedSequence(@NotNull CarriedContext context, @NotNull JukeboxBlockEntity blockEntity)
        {
            final ItemStack disc = blockEntity.getTheItem();
            final JukeboxSong song = blockEntity.getSongPlayer().getSong();
            
            if(disc.isEmpty() || song == null)
                return;
            
            final int songID = context.level().registryAccess().registryOrThrow(Registries.JUKEBOX_SONG).getId(song);
            
            CrispMusicGroover.INST.addMusic(disc, songID, blockEntity.getSongPlayer().getTicksSinceSongStarted(), song.lengthInTicks(), context.carryID());
        }
        
        @Override public @Range(from = 0, to = Integer.MAX_VALUE) int getPenaltyRate(@NotNull JukeboxBlockEntity blockEntity) { return NO_PENALTY; }
        
        //? TODO: Feature: Bundle-like interaction will be supported in very soon, with changing disc on carrying allowed.
        @Override protected void loadCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, @NotNull JukeboxBlockEntity blockEntity)
            { blockEntity.loadCustomOnly(tag, registries); }
        
        @Override protected void saveCarryTag(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries, @NotNull JukeboxBlockEntity blockEntity)
        {
            final CompoundTag dataTag = blockEntity.saveCustomOnly(registries);
            tag.merge(dataTag);
        }
        
        @Override public void carryingTick(@NotNull CarriableExtensions.TickingContext context)
            { CrispMusicGroover.INST.playMusic(context.level(), context.entity().getOnPos(), context.uuid()); }
        
        @Override protected void onPlacedProcess(@NotNull ServerLevel level, long elapsedTime, @NotNull CarriedContext context, @NotNull JukeboxBlockEntity blockEntity)
        {
            //? TODO: See TODO above.
        }
        
        @Override public boolean causesOverweight() { return false; }
        
        @Override public @NotNull Class<?> getSupportedType() { return JukeboxBlockEntity.class; }
    }
    
    /**
     * Stores, plays music UwU.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    private enum CrispMusicGroover
    {
        INST;
        
        private final HashMap<String, MusicInfo> playList = new HashMap<>();
        
        private void addMusic(
            @NotNull ItemStack disc,
            int songID,
            long currentLength,
            int totalLength,
            @NotNull String carryID
        )
        {
            final MusicInfo info = new MusicInfo(disc, songID, currentLength, totalLength);
            playList.put(carryID, info);
        }
        
        private @NotNull Optional<ItemStack> getDisc(@NotNull String carryID)
        {
            final @Nullable MusicInfo info = playList.get(carryID);
            
            if(info == null)
                return Optional.empty();
            
            return Optional.of(info.disc());
        }
        
        private void playMusic(@NotNull Level level, @NotNull BlockPos pos, @NotNull String carryID)
        {
            final MusicInfo info = playList.get(carryID);
            
            //* This levelEvent is actually [[LevelRenderer#playJukeboxSong(Holder<JukeboxSong>, BlockPos)]].
            //* It is only implemented in client side, and doesn't exist in abstract class,
            //* So we can only call [[LevelAccessor#levelEvent(Player, int, BlockPos, int)]].
            //* On Serverside, this will send a packet to clientside,
            //* so there's no need to worry about sync stuff.
            level.levelEvent(null, 1010, pos, info.songID);
            
            if(info.isDone())
            {
                playList.remove(carryID);
                return;
            }
            
            playList.put(carryID, info.played());
        }
    }
    
    private record MusicInfo(@NotNull ItemStack disc, int songID, long currentLength, long totalLength)
    {
        @NotNull MusicInfo played() { return new MusicInfo(disc, songID, currentLength + 1, totalLength); }
        
        boolean isDone() { return currentLength + 1 >= totalLength; }
    }
}
