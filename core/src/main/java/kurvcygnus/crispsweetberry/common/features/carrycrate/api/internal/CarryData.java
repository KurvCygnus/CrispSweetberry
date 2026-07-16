//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.entity.AbstractEntityCarryAdapter;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.extensions.CarriableExtensions;
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
    //region Fields & Constructors
    public static final Supplier<DataComponentType<CarryData>> SERIALIZATION_DEF =
        DataComponentType.<CarryData>builder().persistent(CarryDataCodec.INST).networkSynchronized(CarryDataStreamCodec.INST)::build;
    
    public final @NotNull CarryType carryType;
    private final @NotNull CarryData.OfUniqueDataBase uniqueData;
    public final boolean causesOverweight;
    public final @Range(from = 0, to = Long.MAX_VALUE) long startTime;
    public final @Range(from = CarriableExtensions.ICarriableLifecycle.NO_PENALTY, to = Integer.MAX_VALUE) int penaltyRate;
    
    private CarryData(
        @NotNull CarryType carryType,
        @NotNull OfUniqueDataBase uniqueData,
        boolean causesOverweight,
        @Range(from = 0, to = Long.MAX_VALUE) long startTime,
        @Range(from = CarriableExtensions.ICarriableLifecycle.NO_PENALTY, to = Integer.MAX_VALUE) int penaltyRate
    )
    {
        Objects.requireNonNull(carryType, "Param \"carryType\" must not be null!");
        Objects.requireNonNull(uniqueData, "Param \"uniqueData\" must not be null!");
        if(startTime < 0)
            throw new IllegalArgumentException("Param \"startTime\" must be an unsigned number!");
        if(penaltyRate < 0)
            throw new IllegalArgumentException("Param \"penaltyRate\" must be an unsigned number!");
        
        this.carryType = carryType;
        this.uniqueData = uniqueData;
        this.causesOverweight = causesOverweight;
        this.startTime = startTime;
        this.penaltyRate = penaltyRate;
    }
    
    public static @NotNull CarryData createBlock(
        @NotNull BlockState state,
        @Range(from = CarriableExtensions.ICarriableLifecycle.NO_PENALTY, to = Integer.MAX_VALUE) int penaltyRate,
        int carryCount,
        int maxCarryCount,
        boolean causesOverweight,
        @Range(from = 0, to = Long.MAX_VALUE) long startTime
    )
    {
        return new CarryData(
            CarryType.BLOCK,
            new OfBlockUniqueData(
                state,
                carryCount,
                maxCarryCount
            ),
            causesOverweight,
            startTime,
            penaltyRate
        );
    }
    
    public static @NotNull CarryData createBlockEntity(
        @NotNull BlockState state,
        @NotNull CompoundTag tagData,
        @NotNull BlockEntityType<? extends BlockEntity> type,
        @Range(from = CarriableExtensions.ICarriableLifecycle.NO_PENALTY, to = Integer.MAX_VALUE) int penaltyRate,
        boolean causesOverweight,
        @Range(from = 0, to = Long.MAX_VALUE) long startTime
    )
    {
        return new CarryData(
            CarryType.BLOCK_ENTITY,
            new OfBlockEntityUniqueData(
                state,
                type,
                tagData
            ),
            causesOverweight,
            startTime,
            penaltyRate
        );
    }
    
    public static @NotNull CarryData createEntity(
        @NotNull AbstractEntityCarryAdapter<?> adapter,
        @NotNull EntityType<?> type,
        @NotNull CompoundTag tagData,
        @Range(from = 0, to = Long.MAX_VALUE) long startTime
    )
    {
        Objects.requireNonNull(adapter, "Param \"adapter\" must not be null!");
        return new CarryData(
            CarryType.ENTITY,
            new OfEntityUniqueData(type, tagData),
            adapter.causesOverweight(),
            startTime,
            adapter.getPenaltyRate()
        );
    }
    //endregion
    
    //region Data Getters & Essential methods
    /**
     * Gets the exact unique data of a specific type.
     * @apiNote <span style="color: f84b4b">Use it carefully, type mismatch will cause an immediate <u>{@link ClassCastException}</u></span>.
     */
    @ApiStatus.Internal @SuppressWarnings("unchecked")//! Unsafe casting, but is used only by internals.
    public <T extends OfUniqueDataBase> @NotNull T matchUnique()
    {
        return (T) switch(carryType)
        {
            case BLOCK_ENTITY -> (OfBlockEntityUniqueData) this.uniqueData;
            case BLOCK        -> (OfBlockUniqueData)       this.uniqueData;
            case ENTITY       -> (OfEntityUniqueData)      this.uniqueData;
        };
    }
    
    @Override public boolean equals(@Nullable Object obj)
    {
        return obj instanceof CarryData that &&
            Objects.equals(this.carryType, that.carryType) &&
            Objects.equals(this.uniqueData, that.uniqueData) &&
            this.startTime == that.startTime &&
            this.penaltyRate == that.penaltyRate;
    }
    
    @Override public int hashCode() { return Objects.hash(carryType, uniqueData, startTime, penaltyRate); }
    
    @Override public @NotNull String toString()
    {
        return MessageFormatter.arrayFormat(
            """
            CarryData
            {
                type: {},
                causesOverweight: {},
                startTime: {},
                penaltyRate: {}
                payload:
            {}
            }
            """,//! Notes that `matchUnique`'s print is called with [[String#indent]]. So we shouldn't align placeholder to "payload".
            new Object[] { carryType, causesOverweight, startTime, penaltyRate, uniqueData.toString().indent(4) }
        ).getMessage();
    }
    //endregion
    
    //region Internal Unique Data
    public sealed abstract static class OfUniqueDataBase permits OfBlockUniqueData, OfEntityUniqueData, OfBlockEntityUniqueData
    {
        protected OfUniqueDataBase() {}
        
        /**
         * This getter method is used by internals for abstracted adapter getting logics.<br>
         * <span style="color: f84b4b">DO NOT USE.</span>
         */
        @ApiStatus.Internal public abstract @NotNull Object getCreationData();
        
        @Override public abstract @NotNull String toString();
    }
    
    public static final class OfBlockUniqueData extends OfUniqueDataBase
    {
        private static final Function<OfBlockUniqueData, BlockState> BLOCK_STATE_GETTER = holder -> holder.state;
        private static final Function<OfBlockUniqueData, Integer> COUNT_GETTER = holder -> holder.carryCount;
        private static final Function<OfBlockUniqueData, Integer> MAX_COUNT_GETTER = holder -> holder.maxCarryCount;
        
        static final MapCodec<OfBlockUniqueData> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                BlockState.CODEC.fieldOf( "state").forGetter(BLOCK_STATE_GETTER),
                Codec.INT.fieldOf(        "carry_count").forGetter(COUNT_GETTER),
                Codec.INT.fieldOf(        "max_carry_count").forGetter(MAX_COUNT_GETTER)
            ).apply(inst, OfBlockUniqueData::new)
        );
        
        static final StreamCodec<ByteBuf, OfBlockUniqueData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BlockState.CODEC), BLOCK_STATE_GETTER,
            ByteBufCodecs.VAR_INT,                     COUNT_GETTER,
            ByteBufCodecs.VAR_INT,                     MAX_COUNT_GETTER,
            OfBlockUniqueData::new
        );
        
        public final BlockState state;
        public final int carryCount;
        public final int maxCarryCount;
        
        private OfBlockUniqueData(
            @NotNull BlockState state,
            @Range(from = 1, to = Integer.MAX_VALUE) int carryCount,
            @Range(from = 1, to = Integer.MAX_VALUE) int maxCarryCount
        )
        {
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
            return MessageFormatter.arrayFormat(
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
    
    public static final class OfEntityUniqueData extends OfUniqueDataBase
    {
        private static final Function<OfEntityUniqueData, EntityType<?>> TYPE_GETTER = holder -> holder.type;
        private static final Function<OfEntityUniqueData, CompoundTag> TAG_GETTER = holder -> holder.tagData;
        
        static final MapCodec<OfEntityUniqueData> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf( "type").forGetter(TYPE_GETTER),
                CompoundTag.CODEC.fieldOf(                           "tag_data").forGetter(TAG_GETTER)
            ).apply(inst, OfEntityUniqueData::new)
        );
        
        static final StreamCodec<ByteBuf, OfEntityUniqueData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BuiltInRegistries.ENTITY_TYPE.byNameCodec()), TYPE_GETTER,
            ByteBufCodecs.COMPOUND_TAG, TAG_GETTER,
            OfEntityUniqueData::new
        );
        
        public final EntityType<?> type;
        public final CompoundTag tagData;
        
        private OfEntityUniqueData(@NotNull EntityType<?> type, @NotNull CompoundTag tagData)
        {
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
    
    public static final class OfBlockEntityUniqueData extends OfUniqueDataBase
    {
        private static final Function<OfBlockEntityUniqueData, BlockState> STATE_GETTER = holder -> holder.state;
        private static final Function<OfBlockEntityUniqueData, BlockEntityType<?>> TYPE_GETTER = holder -> holder.type;
        private static final Function<OfBlockEntityUniqueData, CompoundTag> TAG_GETTER = holder -> holder.tagData;
        
        static final MapCodec<OfBlockEntityUniqueData> CODEC = RecordCodecBuilder.mapCodec(
            inst -> inst.group(
                BlockState.CODEC.fieldOf(                                  "state").forGetter(STATE_GETTER),
                BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf( "type").forGetter(TYPE_GETTER),
                CompoundTag.CODEC.fieldOf(                                 "tag_data").forGetter(TAG_GETTER)
            ).apply(inst, OfBlockEntityUniqueData::new)
        );
        
        static final StreamCodec<ByteBuf, OfBlockEntityUniqueData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BlockState.CODEC),                                  STATE_GETTER,
            ByteBufCodecs.fromCodec(BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec()), TYPE_GETTER,
            ByteBufCodecs.COMPOUND_TAG,                                                 TAG_GETTER,
            OfBlockEntityUniqueData::new
        );
        
        public final BlockState state;
        public final BlockEntityType<? extends BlockEntity> type;
        public final CompoundTag tagData;
        
        private OfBlockEntityUniqueData(
            @NotNull BlockState state,
            @NotNull BlockEntityType<? extends BlockEntity> type,
            @NotNull CompoundTag tagData
        )
        {
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
            return MessageFormatter.arrayFormat(
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
    
    //region Network Codec I/O Serialization
    private enum CarryDataCodec implements Codec<CarryData>
    {
        INST;
        
        private static final String CARRY_TYPE = "carry_type";
        private static final String DATA = "data";
        private static final String START_TIME = "start_time";
        private static final String CAUSES = "causes_overweight";
        private static final String PENALTY_RATE = "penalty_rate";
        
        private static final long TIME_FALLBACK_VALUE = 0L;
        private static final int PENALTY_RATE_FALLBACK_VALUE = 0;
        private static final boolean CAUSE_FLAG_FALLBACK_VALUE = true;
        
        //* |==================================================================================================|
        //* | Notes that, generic `TTargetType` stands for the file type that the data will be transformed to, |
        //* | or unwrapped from, it could be NBT from Minecraft, TOML, YAML, JSON, etc.                        |
        //* |==================================================================================================|
        
        @Override public <TTargetType> DataResult<Pair<CarryData, TTargetType>> decode(@NotNull DynamicOps<TTargetType> ops, @NotNull TTargetType input)
        {
            return ops.getMap(input).flatMap(
                map ->
                {
                    final @Nullable TTargetType typeElement = map.get(CARRY_TYPE);
                    if(typeElement == null)
                        return DataResult.error(() -> "Missing \"%s\" field".formatted(CARRY_TYPE));
                    
                    return CarryType.CODEC.decode(ops, typeElement).flatMap(
                        typePair ->
                        {
                            final var type = typePair.getFirst();
                            
                            final TTargetType dataElement = map.get(DATA);
                            
                            if(dataElement == null)
                                return DataResult.error(() -> "Missing \"%s\" field".formatted(DATA));
                            
                            final var subCodec = type.codec;
                            final var dataResult = subCodec.codec().parse(ops, dataElement);
                            
                            final var timeResult = unwrapData(map, ops, START_TIME, Codec.LONG, TIME_FALLBACK_VALUE);
                            final var causesResult = unwrapData(map, ops, CAUSES, Codec.BOOL, CAUSE_FLAG_FALLBACK_VALUE);
                            final var penaltyRateResult = unwrapData(map, ops, PENALTY_RATE, Codec.INT, PENALTY_RATE_FALLBACK_VALUE);
                            
                            return dataResult.flatMap(
                                data ->
                                {
                                    //? Yes, [[DataResult]] doesn't even have a fucking method like `getOrDefault`, this is an annoying monad implementation.
                                    final long time = unwrapResult(timeResult, TIME_FALLBACK_VALUE);
                                    final boolean causes = unwrapResult(causesResult, CAUSE_FLAG_FALLBACK_VALUE);
                                    final int penaltyRate = unwrapResult(penaltyRateResult, PENALTY_RATE_FALLBACK_VALUE);
                                    
                                    return DataResult.success(Pair.of(new CarryData(type, data, causes, time, penaltyRate), ops.empty()));
                                }
                            );
                        }
                    );
                }
            );
        }
        
        private static <TargetType, T> @NotNull DataResult<T> unwrapData(
            @NotNull MapLike<TargetType> map,
            @NotNull DynamicOps<TargetType> ops,
            @NotNull String id,
            @NotNull Codec<T> codec,
            @NotNull T fallback
        )
        {
            final @Nullable TargetType element = map.get(id);
            return element != null ?
                codec.decode(ops, element).map(Pair::getFirst) :
                DataResult.success(fallback);
        }
        
        private static <T> @NotNull T unwrapResult(@NotNull DataResult<T> result, @NotNull T fallback) { return result.isSuccess() ? result.getOrThrow() : fallback; }
        
        @SuppressWarnings("unchecked")//! Safe Casting.
        @Override public <TargetType> @NotNull DataResult<TargetType> encode(@NotNull CarryData input, @NotNull DynamicOps<TargetType> ops, @NotNull TargetType prefix)
        {
            final var dataCodec = (MapCodec<OfUniqueDataBase>) input.carryType.codec;
            
            return ops.mapBuilder().
                add(CARRY_TYPE,   CarryType.CODEC.encodeStart(ops,     input.carryType)).
                add(DATA,         dataCodec.encoder().encodeStart(ops, input.uniqueData)).
                add(CAUSES,       Codec.BOOL.encodeStart(ops,          input.causesOverweight)).
                add(START_TIME,   Codec.LONG.encodeStart(ops,          input.startTime)).
                add(PENALTY_RATE, Codec.INT.encodeStart(ops,           input.penaltyRate)).
                build(prefix);
        }
    }
    
    @SuppressWarnings("unchecked")//! All safe castings.
    private enum CarryDataStreamCodec implements StreamCodec<ByteBuf, CarryData>
    {
        INST;
        
        @Override public @NotNull CarryData decode(@NotNull ByteBuf buffer)
        {
            final var type = ByteBufCodecs.idMapper(i -> CarryType.values()[i], CarryType::ordinal).decode(buffer);
            
            final var dataCodec = (StreamCodec<ByteBuf, OfUniqueDataBase>) type.streamCodec;
            final var data = dataCodec.decode(buffer);
            
            final long startTime = buffer.readLong();
            final boolean causesOverweight = buffer.readBoolean();
            final int penaltyRate = buffer.readInt();
            
            return new CarryData(type, data, causesOverweight, startTime, penaltyRate);
        }
        
        @Override public void encode(@NotNull ByteBuf buffer, @NotNull CarryData value)
        {
            ByteBufCodecs.idMapper(i -> CarryType.values()[i], CarryType::ordinal).encode(buffer, value.carryType);
            
            final var dataCodec = (StreamCodec<ByteBuf, OfUniqueDataBase>) value.carryType.streamCodec;
            dataCodec.encode(buffer, value.uniqueData);
            
            buffer.writeLong(value.startTime);
            buffer.writeBoolean(value.causesOverweight);
            buffer.writeInt(value.penaltyRate);
        }
    }
    //endregion
}
