//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.utils;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * @since 1.0 Release
 */
public final class MinecraftConstants
{
    private MinecraftConstants() { throw new IllegalAccessError("Class \"MinecraftConstants\" is not meant to be instantized!"); }
    
    public static final class OfUniversalProperties
    {
        private OfUniversalProperties() { throw new IllegalAccessError("Class \"OfUniversalProperties\" is not meant to be instantized!"); }
        
        public static final Item.Properties NO_PROPERTY = new Item.Properties();
    }
    
    public static final class OfProjectileValues
    {
        private OfProjectileValues() { throw new IllegalAccessError("Class \"OfProjectileValues\" is not meant to be instantized!"); }
        
        public static final float PROJECTILE_SHOOT_Z_POS = 0F;
        public static final double X_NO_SPEED = .0;
        public static final double Y_NO_SPEED = .0;
        public static final double Z_NO_SPEED = .0;
    }
    
    public static final class OfSoundValues
    {
        private OfSoundValues() { throw new IllegalAccessError("Class \"OfSoundValues\" is not meant to be instantized!"); }
        
        public static final float NORMAL_SOUND_VOLUME = 1F;
        public static final float LOUD_SOUND_VOLUME  = 1.5F;
        public static final float QUIET_SOUND_VOLUME = .5F;
        public static final float NORMAL_SOUND_PITCH = 1F;
    }
    
    public static final class OfSerializationBasics
    {
        private OfSerializationBasics() { throw new IllegalAccessError("Class \"OfSerializationBasics\" is not meant to be instantized!"); }
        
        public static <B extends Block> @NotNull MapCodec<B> noArgCodec(@NotNull Supplier<B> construct)
        {
            requireNonNull(construct, "Param \"construct\" must not be null!");
            return BlockBehaviour.simpleCodec($ -> construct.get());
        }
        
        public static final Supplier<DataComponentType<Integer>> INT_TEMPLATE;
        public static final Supplier<DataComponentType<Float>>   FLOAT_TEMPLATE;
        public static final Supplier<DataComponentType<Long>>    LONG_TEMPLATE;
        public static final Supplier<DataComponentType<Byte>>    BYTE_TEMPLATE;
        public static final Supplier<DataComponentType<Boolean>> BOOLEAN_TEMPLATE;
        public static final Supplier<DataComponentType<Double>>  DOUBLE_TEMPLATE;
        public static final Supplier<DataComponentType<String>>  STRING_TEMPLATE;
        
        public static final Function<Supplier<Integer>, Supplier<AttachmentType<Integer>>> INT_DATA_SYNCER;
        public static final Function<Supplier<Float>,   Supplier<AttachmentType<Float>>>   FLOAT_DATA_SYNCER;
        public static final Function<Supplier<Long>,    Supplier<AttachmentType<Long>>>    LONG_DATA_SYNCER;
        public static final Function<Supplier<Byte>,    Supplier<AttachmentType<Byte>>>    BYTE_DATA_SYNCER;
        public static final Function<Supplier<Boolean>, Supplier<AttachmentType<Boolean>>> BOOLEAN_DATA_SYNCER;
        public static final Function<Supplier<Double>,  Supplier<AttachmentType<Double>>>  DOUBLE_DATA_SYNCER;
        public static final Function<Supplier<String>,  Supplier<AttachmentType<String>>>  STRING_DATA_SYNCER;
        
        public static <T> @NotNull Supplier<DataComponentType<T>> buildSerializeTemplate(
            @NotNull Codec<T> codec,
            @NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
        ) { return DataComponentType.<T>builder().persistent(codec).networkSynchronized(streamCodec)::build; }
        
        public static <T> @NotNull Function<Supplier<T>, Supplier<AttachmentType<T>>> buildSyncTemplate(
            @NotNull Codec<T> codec,
            @NotNull StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec
        ) { return t -> AttachmentType.builder(t).serialize(codec).sync(streamCodec)::build; }
        
        //* Making declarations and assignment separated results in better UX/read experience.
        static
        {
            INT_TEMPLATE     = buildSerializeTemplate(Codec.INT,    ByteBufCodecs.INT);
            FLOAT_TEMPLATE   = buildSerializeTemplate(Codec.FLOAT,  ByteBufCodecs.FLOAT);
            LONG_TEMPLATE    = buildSerializeTemplate(Codec.LONG,   ByteBufCodecs.VAR_LONG);
            BYTE_TEMPLATE    = buildSerializeTemplate(Codec.BYTE,   ByteBufCodecs.BYTE);
            BOOLEAN_TEMPLATE = buildSerializeTemplate(Codec.BOOL,   ByteBufCodecs.BOOL);
            DOUBLE_TEMPLATE  = buildSerializeTemplate(Codec.DOUBLE, ByteBufCodecs.DOUBLE);
            STRING_TEMPLATE  = buildSerializeTemplate(Codec.STRING, ByteBufCodecs.STRING_UTF8);
            
            INT_DATA_SYNCER     = buildSyncTemplate(Codec.INT,    ByteBufCodecs.INT);
            FLOAT_DATA_SYNCER   = buildSyncTemplate(Codec.FLOAT,  ByteBufCodecs.FLOAT);
            LONG_DATA_SYNCER    = buildSyncTemplate(Codec.LONG,   ByteBufCodecs.VAR_LONG);
            BYTE_DATA_SYNCER    = buildSyncTemplate(Codec.BYTE,   ByteBufCodecs.BYTE);
            BOOLEAN_DATA_SYNCER = buildSyncTemplate(Codec.BOOL,   ByteBufCodecs.BOOL);
            DOUBLE_DATA_SYNCER  = buildSyncTemplate(Codec.DOUBLE, ByteBufCodecs.DOUBLE);
            STRING_DATA_SYNCER  = buildSyncTemplate(Codec.STRING, ByteBufCodecs.STRING_UTF8);
        }
    }
    
    public static final class OfUI
    {
        private OfUI() { throw new IllegalAccessError("Class \"OfUI\" is not meant to be instantized!"); }
        
        public static final int NO_OFFSET   = 0;
        public static final int WHITE_COLOR = 0xFFFFFF;
        public static final int GOLD_COLOR  = 0xFFFF55;
        public static final int GRAY_COLOR  = 0x8B8B8B;
    }
    
    public static final class OfMetainfo
    {
        private OfMetainfo() { throw new IllegalAccessError("Class \"OfMetainfo\" is not meant to be instantized!"); }
        
        public static final String FEEDBACK_MESSAGE =
            "If you found this log, please feedback to us at https://github.com/KurvCygnus/CrispSweetberry/issues, with detailed debug log.";
    }
    
    public static final class OfResourceLocationLiterals
    {
        private OfResourceLocationLiterals() { throw new IllegalAccessError("Class \"OfResourceLocationLiterals\" is not meant to be instantized!"); }
        
        public static final String UI = "ui";
        public static final String CARRY_CRATE = "carry_crate";
    }
    
    public static final class OfVanillaSlotIndexes
    {
        private OfVanillaSlotIndexes() { throw new IllegalAccessError("Class \"OfVanillaSlotIndexes\" is not meant to be instantized!"); }
        
        public static final int NO_X_SLOT_OFFSET = 0;
        
        /**
         * <b>{@code GRID}</b> start indexes are used for <u>{@link net.minecraft.world.inventory.AbstractContainerMenu Menu}</u> layout initialization,
         * they are mostly universal, which are different from other constants in this class.
         * @see UIUtils#addGridSlots Recommended Usage
         */
        public static final int INVENTORY_SLOTS_GRID_START_INDEX = 9;
        public static final int INVENTORY_SLOTS_START_X_POS      = 8;
        public static final int INVENTORY_SLOTS_START_Y_POS      = 84;
        public static final int INVENTORY_SLOTS_TOTAL_ROWS       = 3;
        public static final int INVENTORY_SLOTS_TOTAL_COLS       = 9;
        
        /**
         * <b>{@code GRID}</b> start indexes are used for <u>{@link net.minecraft.world.inventory.AbstractContainerMenu Menu}</u> layout initialization,
         * they are mostly universal, which are different from other constants in this class.
         * @see UIUtils#addGridSlots Recommended Usage
         */
        public static final int HOTBAR_SLOTS_GRID_START_INDEX = 0;
        public static final int HOTBAR_SLOTS_START_X_POS      = 8;
        public static final int HOTBAR_SLOTS_START_Y_POS      = 142;
        public static final int HOTBAR_SLOTS_TOTAL_ROWS       = 1;
        public static final int HOTBAR_SLOTS_TOTAL_COLS       = 9;
        
        /**
         * This constant exists because <b>the source code of the method {@code quickMoveStack()} originally and oddly uses "closedOpen" style to check indexes</b>,
         * thus we <b>need this to make constant's name fits the actual context meaning</b>.<br><b>
         * <i>It is also used for <b>correct {@code endIdx}</b> in method
         * <b>{@link net.minecraft.world.inventory.AbstractContainerMenu#quickMoveStack(Player, int) moveItemStackTo()}</b></i>.
         */
        public static final int CORRECTION_INDEX          = 1;
        public static final int ERROR                     = -1;
        public static final int INPUT_SLOT                = 0;
        public static final int FUEL_SLOT                 = 1;
        public static final int OUTPUT_SLOT               = 2;
        public static final int BACKPACK_SLOT_START_INDEX = 3;
        public static final int BACKPACK_SLOT_END_INDEX   = 29;
        public static final int HOTBAR_SLOT_START_INDEX   = 30;
        public static final int HOTBAR_SLOT_END_INDEX     = 38;
        public static final int INPUT_RANGE               = 0;
        public static final int OUTPUT_RANGE              = 1;
        public static final int BACKPACK_RANGE            = 2;
        public static final int HOTBAR_RANGE              = 3;
        public static final int SLOT_GAP                  = 18;
    }
}