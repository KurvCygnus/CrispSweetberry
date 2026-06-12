//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.slf4j.helpers.MessageFormatter;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A flexible, simple <u><a href="https://en.wikipedia.org/wiki/Tagged_union">Tagged Union Data Object</a></u> for boxed Carry Crate's data persistent, and
 * serialization.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see CarryType Simple Type Enum
 */
@ApiStatus.Internal
public final class CarryData
{
    //  region Fields & Constructors
    public static final Supplier<DataComponentType<CarryData>> SERIALIZATION_DEF =
        DataComponentType.<CarryData>builder().persistent(CarryDataCodec.INST).networkSynchronized(CarryDataStreamCodec.INST)::build;
    
    public final @NotNull CarryType carryType;
    private final @NotNull CarryDataBaseHolder unionData;
    public final boolean causesOverweight;
    public final @Range(from = 0, to = Long.MAX_VALUE) long startTime;
    
    private CarryData(
        @NotNull CarryType carryType,
        @NotNull CarryDataBaseHolder unionData,
        boolean causesOverweight,
        @Range(from = 0, to = Long.MAX_VALUE) long startTime
    )
    {
        Objects.requireNonNull(carryType, "Param \"carryType\" must not be null!");
        Objects.requireNonNull(unionData, "Param \"unionData\" must not be null!");
        if(startTime < 0)
            throw new IllegalArgumentException("Param \"startTime\" must be greater than 0!");
        
        this.carryType = carryType;
        this.unionData = unionData;
        this.causesOverweight = causesOverweight;
        this.startTime = startTime;
    }
    
    public static @NotNull CarryData createBlock(
        @NotNull BlockState state,
        int penaltyRate,
        int carryCount,
        int maxCarryCount,
        boolean causesOverweight,
        long startTime
    )
    {
        return new CarryData(
            CarryType.BLOCK,
            new CarryBlockDataHolder(
                penaltyRate,
                state,
                carryCount,
                maxCarryCount
            ),
            causesOverweight,
            startTime
        );
    }
    
    public static @NotNull CarryData createBlockEntity(
        @NotNull BlockState state,
        @NotNull CompoundTag tagData,
        @NotNull BlockEntityType<? extends BlockEntity> type,
        int penaltyRate,
        boolean causesOverweight,
        long startTime
    )
    {
        return new CarryData(
            CarryType.BLOCK_ENTITY,
            new CarryBlockEntityDataHolder(
                penaltyRate,
                state,
                type,
                tagData
            ),
            causesOverweight,
            startTime
        );
    }
    
    public static @NotNull CarryData createEntity(
        int penaltyRate,
        @NotNull EntityType<?> type,
        @NotNull CompoundTag tagData,
        boolean causesOverweight,
        long startTime
    )
    {
        return new CarryData(
            CarryType.ENTITY,
            new CarryEntityDataHolder(penaltyRate, type, tagData),
            causesOverweight, 
            startTime
        );
    }
    //endregion
    
    //  region Data Getters & Essential methods
    /**
     * Gets the exact dataHolder of a specific type.
     * @apiNote <span style="color: f84b4b">Use it carefully, type mismatch will cause an immediate <u>{@link ClassCastException}</u></span>.
     */
    @ApiStatus.Internal @SuppressWarnings("unchecked")//! Unsafe casting, but is used only by internals.
    public <T extends CarryDataBaseHolder> @NotNull T unionData()
    {
        return (T) switch(carryType)
        {
            case BLOCK_ENTITY -> (CarryBlockEntityDataHolder) this.unionData;
            case BLOCK        -> (CarryBlockDataHolder)       this.unionData;
            case ENTITY       -> (CarryEntityDataHolder)      this.unionData;
        };
    }
    
    @Override public boolean equals(@Nullable Object obj)
    {
        return obj instanceof CarryData that &&
            Objects.equals(this.carryType, that.carryType) &&
            Objects.equals(this.unionData, that.unionData) &&
            this.startTime == that.startTime;
    }
    
    @Override public int hashCode() { return Objects.hash(carryType, unionData, startTime); }
    
    @Override public @NotNull String toString()
    {
        return MessageFormatter.format(
            """
            CarryData
            {
                type: {},
                causesOverweight: {},
                startTime: {},
                payload:
            {}
            }
            """,//! Notes that `unionData`'s print is called with [[String#indent]]. So we shouldn't align placeholder to "payload".
            new Object[] { carryType, causesOverweight, startTime, unionData.toString().indent(4) }
        ).getMessage();
    }
    //endregion
    
    //  region Internal Data Holders
    public sealed abstract static class CarryDataBaseHolder permits CarryBlockDataHolder, CarryEntityDataHolder, CarryBlockEntityDataHolder
    {
        public final int penaltyRate;
        
        protected CarryDataBaseHolder(@Range(from = 0, to = Integer.MAX_VALUE) int penaltyRate)
        {
            if(penaltyRate < 0)
                throw new IllegalArgumentException("Param \"penaltyRate\" must be a non-negative integer!");
            
            this.penaltyRate = penaltyRate;
        }
        
        protected int getPenaltyRate() { return penaltyRate; }
        
        /**
         * This getter method is used by internals for abstracted adapter getting logics.<br>
         * <span style="color: f84b4b">DO NOT USE.</span>
         */
        @ApiStatus.Internal public abstract @NotNull Object getCreationData();
        
        @Override public abstract @NotNull String toString();
    }
    
    public static final class CarryBlockDataHolder extends CarryDataBaseHolder
    {
        private static final Function<CarryBlockDataHolder, BlockState> BLOCK_STATE_GETTER = holder -> holder.state;
        private static final Function<CarryBlockDataHolder, Integer> COUNT_GETTER = holder -> holder.carryCount;
        private static final Function<CarryBlockDataHolder, Integer> MAX_COUNT_GETTER = holder -> holder.maxCarryCount;
        
        static final MapCodec<CarryBlockDataHolder> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                Codec.INT.fieldOf(        "penalty_rate").forGetter(CarryDataBaseHolder::getPenaltyRate),
                BlockState.CODEC.fieldOf( "state").forGetter(BLOCK_STATE_GETTER),
                Codec.INT.fieldOf(        "carry_count").forGetter(COUNT_GETTER),
                Codec.INT.fieldOf(        "max_carry_count").forGetter(MAX_COUNT_GETTER)
            ).apply(inst, CarryBlockDataHolder::new)
        );
        
        static final StreamCodec<ByteBuf, CarryBlockDataHolder> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,                     CarryDataBaseHolder::getPenaltyRate,
            ByteBufCodecs.fromCodec(BlockState.CODEC), BLOCK_STATE_GETTER,
            ByteBufCodecs.VAR_INT,                     COUNT_GETTER,
            ByteBufCodecs.VAR_INT,                     MAX_COUNT_GETTER,
            CarryBlockDataHolder::new
        );
        
        public final BlockState state;
        public final int carryCount;
        public final int maxCarryCount;
        
        private CarryBlockDataHolder(
            @Range(from = 0, to = Integer.MAX_VALUE) int penaltyRate,
            @NotNull BlockState state,
            @Range(from = 1, to = Integer.MAX_VALUE) int carryCount,
            @Range(from = 1, to = Integer.MAX_VALUE) int maxCarryCount
        )
        {
            super(penaltyRate);
            
            Objects.requireNonNull(state, "Param \"state\" must not be null!");
            if(carryCount < 1)
                throw new IllegalArgumentException("Param \"carryCount\" must be a positive integer!");
            if(maxCarryCount < 1)
                throw new IllegalArgumentException("Param \"maxCarryCount\" must be a positive integer!");
            
            this.state         = state;
            this.carryCount    = carryCount;
            this.maxCarryCount = maxCarryCount;
        }
        
        @Override public @NotNull Object getCreationData() { return this.state.getBlock(); }
        
        @Override public @NotNull String toString()
        {
            return MessageFormatter.format(
                """
                CarryBlockData
                {
                    state: {},
                    count: {} / {}
                }
                """,
                new Object[] { state, carryCount, maxCarryCount }
            ).getMessage().trim();
        }
    }
    
    public static final class CarryEntityDataHolder extends CarryDataBaseHolder
    {
        private static final Function<CarryEntityDataHolder, EntityType<?>> TYPE_GETTER = holder -> holder.type;
        private static final Function<CarryEntityDataHolder, CompoundTag> TAG_GETTER = holder -> holder.tagData;
        
        static final MapCodec<CarryEntityDataHolder> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                Codec.INT.fieldOf(                                   "penalty_rate").forGetter(CarryDataBaseHolder::getPenaltyRate),
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf( "type").forGetter(TYPE_GETTER),
                CompoundTag.CODEC.fieldOf(                           "tag_data").forGetter(TAG_GETTER)
            ).apply(inst, CarryEntityDataHolder::new)
        );
        
        static final StreamCodec<ByteBuf, CarryEntityDataHolder> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CarryDataBaseHolder::getPenaltyRate,
            ByteBufCodecs.fromCodec(BuiltInRegistries.ENTITY_TYPE.byNameCodec()), TYPE_GETTER,
            ByteBufCodecs.COMPOUND_TAG, TAG_GETTER,
            CarryEntityDataHolder::new
        );
        
        public final EntityType<?> type;
        public final CompoundTag tagData;
        
        private CarryEntityDataHolder(@Range(from = 0, to = Integer.MAX_VALUE) int penaltyRate, @NotNull EntityType<?> type, @NotNull CompoundTag tagData)
        {
            super(penaltyRate);
            Objects.requireNonNull(type, "Param \"type\" must not be null!");
            Objects.requireNonNull(tagData, "Param \"tagData\" must not be null!");
            
            this.type = type;
            this.tagData = tagData;
        }
        
        @Override public @NotNull Object getCreationData() { return type; }
        
        @Override public @NotNull String toString()
        {
            return MessageFormatter.format(
                """
                CarryEntityData
                {
                    entityType: {},
                    tagData: {}
                }
                """,
                type,
                tagData
            ).getMessage().trim();
        }
    }
    
    public static final class CarryBlockEntityDataHolder extends CarryDataBaseHolder
    {
        private static final Function<CarryBlockEntityDataHolder, BlockState> STATE_GETTER = holder -> holder.state;
        private static final Function<CarryBlockEntityDataHolder, BlockEntityType<?>> TYPE_GETTER = holder -> holder.type;
        private static final Function<CarryBlockEntityDataHolder, CompoundTag> TAG_GETTER = holder -> holder.tagData;
        
        static final MapCodec<CarryBlockEntityDataHolder> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                Codec.INT.fieldOf(                                         "penalty_rate").forGetter(CarryDataBaseHolder::getPenaltyRate),
                BlockState.CODEC.fieldOf(                                  "state").forGetter(STATE_GETTER),
                BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf( "type").forGetter(TYPE_GETTER),
                CompoundTag.CODEC.fieldOf(                                 "tag_data").forGetter(TAG_GETTER)
            ).apply(inst, CarryBlockEntityDataHolder::new)
        );
        
        static final StreamCodec<ByteBuf, CarryBlockEntityDataHolder> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,                                                      CarryDataBaseHolder::getPenaltyRate,
            ByteBufCodecs.fromCodec(BlockState.CODEC),                                  STATE_GETTER,
            ByteBufCodecs.fromCodec(BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec()), TYPE_GETTER,
            ByteBufCodecs.COMPOUND_TAG,                                                 TAG_GETTER,
            CarryBlockEntityDataHolder::new
        );
        
        public final BlockState state;
        public final BlockEntityType<? extends BlockEntity> type;
        public final CompoundTag tagData;
        
        private CarryBlockEntityDataHolder(
            @Range(from = 0, to = Integer.MAX_VALUE) int penaltyRate,
            @NotNull BlockState state,
            @NotNull BlockEntityType<? extends BlockEntity> type,
            @NotNull CompoundTag tagData
        )
        {
            super(penaltyRate);
            
            Objects.requireNonNull(state, "Param \"state\" must not be null!");
            Objects.requireNonNull(type, "Param \"type\" must not be null!");
            Objects.requireNonNull(tagData, "Param \"tagData\" must not be null!");
            
            this.state = state;
            this.type = type;
            this.tagData = tagData;
        }
        
        @Override public @NotNull Object getCreationData() { return type; }
        
        @Override public @NotNull String toString()
        {
            return MessageFormatter.format(
                """
                CarryBlockEntityData
                {
                    state: {},
                    type: {},
                    tagData: {}
                }
                """,
                new Object[] { state, type, tagData }
            ).getMessage().trim();
        }
    }
    //endregion
    
    //  region Network Codec I/O Serialization
    @ApiStatus.Obsolete//! Not recommend to analyze this.
    private enum CarryDataCodec implements Codec<CarryData>
    {
        INST;
        
        private static final String CARRY_TYPE = "carry_type";
        private static final String DATA = "data";
        private static final String START_TIME = "start_time";
        private static final String CAUSES = "causes_overweight";
        
        private static final long TIME_FALLBACK_VALUE = 0L;
        private static final boolean CAUSE_FLAG_FALLBACK_VALUE = true;
        
        @Override public <T> DataResult<Pair<CarryData, T>> decode(@NotNull DynamicOps<T> ops, @NotNull T input)
        {
            return ops.getMap(input).flatMap(
                map ->
                {
                    final @Nullable T typeElement = map.get(CARRY_TYPE);
                    if(typeElement == null)
                        return DataResult.error(() -> "Missing \"%s\" field".formatted(CARRY_TYPE));
                    
                    return CarryType.CODEC.decode(ops, typeElement).flatMap(
                        typePair ->
                        {
                            final var type = typePair.getFirst();
                            
                            final T dataElement = map.get(DATA);
                            
                            if(dataElement == null)
                                return DataResult.error(() -> "Missing \"%s\" field".formatted(DATA));
                            
                            final MapCodec<? extends CarryDataBaseHolder> subCodec = type.codec;
                            final DataResult<? extends CarryDataBaseHolder> dataResult = subCodec.codec().parse(ops, dataElement);
                            
                            final @Nullable T timeElement = map.get(START_TIME);
                            final var timeResult = timeElement != null ?
                                Codec.LONG.decode(ops, timeElement).map(Pair::getFirst) :
                                DataResult.success(TIME_FALLBACK_VALUE);
                            
                            final @Nullable T causesElement = map.get(CAUSES);
                            final var causesResult = causesElement != null ?
                                Codec.BOOL.decode(ops, causesElement).map(Pair::getFirst) :
                                DataResult.success(CAUSE_FLAG_FALLBACK_VALUE);
                            
                            return dataResult.flatMap(
                                data ->
                                {
                                    //? Yes, [[DataResult]] doesn't even have a fucking method like `getOrDefault`, this is an annoying monad implementation.
                                    final long time = timeResult.isSuccess() ? timeResult.getOrThrow() : TIME_FALLBACK_VALUE;
                                    final boolean causes = causesResult.isSuccess() ? causesResult.getOrThrow() : CAUSE_FLAG_FALLBACK_VALUE;
                                    
                                    return DataResult.success(Pair.of(new CarryData(type, data, causes, time), ops.empty()));
                                }
                            );
                        }
                    );
                }
            );
        }
        
        @SuppressWarnings("unchecked")//! Safe Casting.
        @Override public <T> @NotNull DataResult<T> encode(@NotNull CarryData input, @NotNull DynamicOps<T> ops, @NotNull T prefix)
        {
            final var dataCodec = (MapCodec<CarryDataBaseHolder>) input.carryType.codec;
            
            return ops.mapBuilder().
                add(CARRY_TYPE, CarryType.CODEC.encodeStart(ops,     input.carryType)).
                add(DATA,       dataCodec.encoder().encodeStart(ops, input.unionData)).
                add(CAUSES,     Codec.BOOL.encodeStart(ops,          input.causesOverweight)).
                add(START_TIME, Codec.LONG.encodeStart(ops,          input.startTime)).
                build(prefix);
        }
    }
    
    @SuppressWarnings("unchecked")//! All safe castings.
    @ApiStatus.Obsolete//! Not recommend to analyze this.
    private enum CarryDataStreamCodec implements StreamCodec<ByteBuf, CarryData>
    {
        INST;
        
        @Override public @NotNull CarryData decode(@NotNull ByteBuf buffer)
        {
            final var type = ByteBufCodecs.idMapper(i -> CarryType.values()[i], CarryType::ordinal).decode(buffer);
            
            final var dataCodec = (StreamCodec<ByteBuf, CarryDataBaseHolder>) type.streamCodec;
            final var data = dataCodec.decode(buffer);
            
            final long startTime = buffer.readLong();
            final boolean causesOverweight = buffer.readBoolean();
            
            return new CarryData(type, data, causesOverweight, startTime);
        }
        
        @Override public void encode(@NotNull ByteBuf buffer, @NotNull CarryData value)
        {
            ByteBufCodecs.idMapper(i -> CarryType.values()[i], CarryType::ordinal).encode(buffer, value.carryType);
            
            final var dataCodec = (StreamCodec<ByteBuf, CarryDataBaseHolder>) value.carryType.streamCodec;
            dataCodec.encode(buffer, value.unionData);
            
            buffer.writeLong(value.startTime);
            buffer.writeBoolean(value.causesOverweight);
        }
    }
    //endregion
}
