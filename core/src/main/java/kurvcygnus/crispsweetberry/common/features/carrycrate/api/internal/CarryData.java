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
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
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

import java.util.Objects;
import java.util.function.Function;

/**
 * A flexible, simple <u><a href="https://en.wikipedia.org/wiki/Tagged_union">Tagged Union Data Object</a></u> for boxed Carry Crate's unionData persistent, and
 * serialization.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see CarryType Simple Type Enum
 */
@ApiStatus.Internal
public final class CarryData
{
    //  region Fields & Constructors
    /**
     * @implNote Have to write <u>{@link CarryDataCodec implementation}</u> manually, because 
     * <u>{@link RecordCodecBuilder}</u>'s generic deduction is <b>STUPID and doesn't work at ALL</b>.
     */
    public static final Codec<CarryData> CODEC = CarryDataCodec.INST;
    
    /**
     * @implNote Have to write <u>{@link CarryDataStreamCodec implementation}</u> manually, because
     * <u>{@link StreamCodec#composite(StreamCodec, Function, Function)}</u>'s generic deduction is <b>STUPID and doesn't work at ALL</b>.
     */
    public static final StreamCodec<ByteBuf, CarryData> STREAM_CODEC = CarryDataStreamCodec.INST;
    
    private final @NotNull CarryType carryType;
    private final @NotNull CarryDataBaseHolder unionData;
    private final boolean causesOverweight;
    private final @Range(from = 0, to = Long.MAX_VALUE) long startTime;
    
    @SuppressWarnings("ConstantValue")//! Defensive check.
    private CarryData(
        @NotNull CarryType carryType,
        @NotNull CarryDataBaseHolder unionData,
        boolean causesOverweight,
        @Range(from = 0, to = Long.MAX_VALUE) long startTime
    )
    {
        Objects.requireNonNull(carryType, "Param \"carryType\" must not be null!");
        Objects.requireNonNull(unionData, "Param \"unionData\" must not be null!");
        FunctionalUtils.throwIf(startTime < 0, "Param \"startTime\" must be greater than 0!", IllegalArgumentException::new);
        
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
     * @apiNote <span style="color: red">Use it carefully, type mismatch will cause an immediate <u>{@link ClassCastException}</u></span>.
     */
    @SuppressWarnings("unchecked")//! Unsafe casting, but is used only by internals.
    public <T extends CarryDataBaseHolder> @NotNull T unionData()
    {
        return (T) switch(carryType)
        {
            case BLOCK_ENTITY -> (CarryBlockEntityDataHolder) this.unionData;
            case BLOCK        -> (CarryBlockDataHolder)       this.unionData;
            case ENTITY       -> (CarryEntityDataHolder)      this.unionData;
        };
    }
    
    public @NotNull CarryType carryType() { return carryType; }
    
    public boolean causesOverweight() { return causesOverweight; }
    
    public @Range(from = 0, to = Long.MAX_VALUE) long startTime() { return startTime; }
    
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
        return """
        CarryData
        {
            type: %s,
            causesOverweight: %s,
            startTime: %d,
            payload: %s
        }
        """.formatted(
            carryType,
            causesOverweight,
            startTime,
            unionData.toString().replace("\n", "\n    ")
        );
    }
    //endregion
    
    //  region Internal Data Holders
    public sealed abstract static class CarryDataBaseHolder permits CarryBlockDataHolder, CarryEntityDataHolder, CarryBlockEntityDataHolder
    {
        private final int penaltyRate;
        
        @SuppressWarnings("ConstantValue")//! Defensive check.
        protected CarryDataBaseHolder(@Range(from = 0, to = Integer.MAX_VALUE) int penaltyRate)
        {
            if(penaltyRate < 0)
                throw new IllegalArgumentException("Param \"penaltyRate\" must be a non-negative integer!");
            
            this.penaltyRate = penaltyRate;
        }
        
        public int getPenaltyRate() { return penaltyRate; }
        
        /**
         * This getter method is used by internals for abstracted adapter getting logics.<br>
         * <span style="color: red">DO NOT USE.</span>
         */
        @ApiStatus.Internal public abstract <T> @NotNull T getCreationData();
        
        public abstract @NotNull CarryType getBoundType();
        
        @Override public abstract @NotNull String toString();
    }
    
    public static final class CarryBlockDataHolder extends CarryDataBaseHolder
    {
        public static final MapCodec<CarryBlockDataHolder> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.INT.fieldOf(        "penalty_rate").forGetter(CarryDataBaseHolder::getPenaltyRate),
                BlockState.CODEC.fieldOf( "state").forGetter(CarryBlockDataHolder::getState),
                Codec.INT.fieldOf(        "carry_count").forGetter(CarryBlockDataHolder::getCarryCount),
                Codec.INT.fieldOf(        "max_carry_count").forGetter(CarryBlockDataHolder::getMaxCarryCount)
            ).apply(inst, CarryBlockDataHolder::new)
        );
        
        public static final StreamCodec<ByteBuf, CarryBlockDataHolder> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,                     CarryDataBaseHolder::getPenaltyRate,
            ByteBufCodecs.fromCodec(BlockState.CODEC), CarryBlockDataHolder::getState,
            ByteBufCodecs.VAR_INT,                     CarryBlockDataHolder::getCarryCount,
            ByteBufCodecs.VAR_INT,                     CarryBlockDataHolder::getMaxCarryCount,
            CarryBlockDataHolder::new
        );
        
        private final BlockState state;
        private final int carryCount;
        private final int maxCarryCount;
        
        @SuppressWarnings("ConstantValue")//! Defensive check.
        private CarryBlockDataHolder(
            @Range(from = 0, to = Integer.MAX_VALUE) int penaltyRate,
            @NotNull BlockState state,
            @Range(from = 1, to = Integer.MAX_VALUE) int carryCount,
            @Range(from = 1, to = Integer.MAX_VALUE) int maxCarryCount
        )
        {
            super(penaltyRate);
            
            Objects.requireNonNull(state, "Param \"state\" must not be null!");
            FunctionalUtils.throwIf(carryCount < 1, "Param \"carryCount\" must be a positive integer!", IllegalArgumentException::new);
            FunctionalUtils.throwIf(maxCarryCount < 1, "Param \"maxCarryCount\" must be a positive integer!", IllegalArgumentException::new);
            
            this.state         = state;
            this.carryCount    = carryCount;
            this.maxCarryCount = maxCarryCount;
        }
        
        public @NotNull BlockState getState() { return state; }
        
        public @Range(from = 1, to = Integer.MAX_VALUE) int getCarryCount() { return carryCount; }
        
        public @Range(from = 1, to = Integer.MAX_VALUE) int getMaxCarryCount() { return maxCarryCount; }
        
        @SuppressWarnings("unchecked")//! Unsafe casting, but is relatively safe as it is only used in internals.
        @Override public @NotNull <T> T getCreationData() { return (T) this.state.getBlock(); }
        
        @Override public @NotNull CarryType getBoundType() { return CarryType.BLOCK; }
        
        @Override public @NotNull String toString()
        {
            return """
                CarryBlockData
                {
                    state: %s,
                    count: %d / %d
                }
                """.formatted(
                state,
                carryCount,
                maxCarryCount
            );
        }
    }
    
    public static final class CarryEntityDataHolder extends CarryDataBaseHolder
    {
        public static final MapCodec<CarryEntityDataHolder> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.INT.fieldOf(                                   "penalty_rate").forGetter(CarryDataBaseHolder::getPenaltyRate),
                BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf( "type").forGetter(CarryEntityDataHolder::getType),
                CompoundTag.CODEC.fieldOf(                           "tag_data").forGetter(CarryEntityDataHolder::getTagData)
            ).apply(inst, CarryEntityDataHolder::new)
        );
        
        public static final StreamCodec<ByteBuf, CarryEntityDataHolder> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,                                                CarryDataBaseHolder::getPenaltyRate,
            ByteBufCodecs.fromCodec(BuiltInRegistries.ENTITY_TYPE.byNameCodec()), CarryEntityDataHolder::getType,
            ByteBufCodecs.COMPOUND_TAG,                                           CarryEntityDataHolder::getTagData,
            CarryEntityDataHolder::new
        );
        
        private final EntityType<?> type;
        private final CompoundTag tagData;
        
        private CarryEntityDataHolder(@Range(from = 0, to = Integer.MAX_VALUE) int penaltyRate, @NotNull EntityType<?> type, @NotNull CompoundTag tagData) 
        {
            super(penaltyRate);
            Objects.requireNonNull(type, "Param \"type\" must not be null!");
            Objects.requireNonNull(tagData, "Param \"tagData\" must not be null!");
            
            this.type    = type;
            this.tagData = tagData;
        }
        
        public @NotNull EntityType<?> getType() { return type; }
        
        public @NotNull CompoundTag getTagData() { return tagData; }
        
        @SuppressWarnings("unchecked")//! Unsafe casting, but is relatively markAsSafe as it is only used in internals.
        @Override public @NotNull <T> T getCreationData() { return (T) this.getType(); }
        
        @Override public @NotNull CarryType getBoundType() { return CarryType.ENTITY; }
        
        @Override public @NotNull String toString()
        {
            return """
                CarryEntityData
                {
                    entityType: %s,
                    tagData: %s
                }
                """.formatted(
                    type,
                    tagData
                );
        }
    }
    
    public static final class CarryBlockEntityDataHolder extends CarryDataBaseHolder
    {
        public static final MapCodec<CarryBlockEntityDataHolder> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                Codec.INT.fieldOf(                                         "penalty_rate").forGetter(CarryDataBaseHolder::getPenaltyRate),
                BlockState.CODEC.fieldOf(                                  "state").forGetter(CarryBlockEntityDataHolder::getState),
                BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf( "type").forGetter(CarryBlockEntityDataHolder::getType),
                CompoundTag.CODEC.fieldOf(                                 "tag_data").forGetter(CarryBlockEntityDataHolder::getTagData)
            ).apply(inst, CarryBlockEntityDataHolder::new)
        );
        
        public static final StreamCodec<ByteBuf, CarryBlockEntityDataHolder> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,                                                      CarryDataBaseHolder::getPenaltyRate,
            ByteBufCodecs.fromCodec(BlockState.CODEC),                                  CarryBlockEntityDataHolder::getState,
            ByteBufCodecs.fromCodec(BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec()), CarryBlockEntityDataHolder::getType,
            ByteBufCodecs.COMPOUND_TAG,                                                 CarryBlockEntityDataHolder::getTagData,
            CarryBlockEntityDataHolder::new
        );
        
        private final BlockState state;
        private final BlockEntityType<? extends BlockEntity> type;
        private final CompoundTag tagData;
        
        private CarryBlockEntityDataHolder(
            @Range(from = 0, to = Integer.MAX_VALUE) int penaltyRate,
            @NotNull BlockState state,
            @NotNull BlockEntityType<? extends BlockEntity> type,
            @NotNull CompoundTag tagData
        )
        {
            super(penaltyRate);
            
            Objects.requireNonNull(state, "Param \"state\" must not be null!");
            Objects.requireNonNull(type, "Param \"componentExecutionType\" must not be null!");
            Objects.requireNonNull(tagData, "Param \"tagData\" must not be null!");
            
            this.state   = state;
            this.type    = type;
            this.tagData = tagData;
        }
        
        public @NotNull BlockState getState() { return state; }
        
        public @NotNull BlockEntityType<? extends BlockEntity> getType() { return type; }
        
        public @NotNull CompoundTag getTagData() { return tagData; }
        
        @SuppressWarnings("unchecked")//! Unsafe casting, but is relatively markAsSafe as it is only used in internals.
        @Override public @NotNull <T> T getCreationData() { return (T) this.getType(); }
        
        @Override public @NotNull CarryType getBoundType() { return CarryType.BLOCK_ENTITY; }
        
        @Override public @NotNull String toString()
        {
            return """
                CarryBlockEntityData
                {
                    state: %s,
                    type: %s,
                    tagData: %s
                }
                """.formatted(
                    state,
                    type,
                    tagData
                );
        }
    }
    //endregion
    
    //  region Network Codec I/O Serialization
    @ApiStatus.Obsolete//! Not recommend to analyze this.
    private enum CarryDataCodec implements Codec<CarryData>
    {
        INST;
        
        @Override public <T> DataResult<Pair<CarryData, T>> decode(@NotNull DynamicOps<T> ops, @NotNull T input)
        {
            return ops.getMap(input).flatMap(
                map ->
                {
                    final T typeElement = map.get("carry_type");
                    if(typeElement == null)
                        return DataResult.error(() -> "Missing \"carry_type\" field");
                    
                    return CarryType.CODEC.decode(ops, typeElement).flatMap(
                        typePair ->
                        {
                            final CarryType type = typePair.getFirst();
                            
                            final T dataElement = map.get("data");
                            
                            if(dataElement == null)
                                return DataResult.error(() -> "Missing \"unionData\" field");
                            
                            final MapCodec<? extends CarryDataBaseHolder> subCodec = type.codec();
                            final DataResult<? extends CarryDataBaseHolder> dataResult = subCodec.codec().parse(ops, dataElement);
                            
                            final T timeElement = map.get("start_time");
                            final DataResult<Long> timeResult = timeElement == null ? 
                                DataResult.success(0L) : 
                                Codec.LONG.decode(ops, timeElement).map(Pair::getFirst);
                            
                            return dataResult.flatMap(
                                data -> timeResult.map(
                                    startTime -> Pair.of(new CarryData(type, data, true, startTime), ops.empty())
                                )
                            );
                        }
                    );
                }
            );
        }
        
        @SuppressWarnings("unchecked")//! Safe Casting.
        @Override public <T> @NotNull DataResult<T> encode(@NotNull CarryData input, @NotNull DynamicOps<T> ops, @NotNull T prefix)
        {
            final MapCodec<CarryDataBaseHolder> dataCodec = (MapCodec<CarryDataBaseHolder>) input.carryType().codec();
            
            return ops.mapBuilder().
                add("carry_type", CarryType.CODEC.encodeStart(ops, input.carryType())).
                add("data", dataCodec.encoder().encodeStart(ops, input.unionData())).
                add("start_time", Codec.LONG.encodeStart(ops, input.startTime())).
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
            final CarryType type = ByteBufCodecs.idMapper(i -> CarryType.values()[i], CarryType::ordinal).decode(buffer);
            
            final StreamCodec<ByteBuf, CarryDataBaseHolder> dataCodec = (StreamCodec<ByteBuf, CarryDataBaseHolder>) type.streamCodec();
            final CarryDataBaseHolder data = dataCodec.decode(buffer);
             
            final long startTime = buffer.readLong();
            
            return new CarryData(type, data, true, startTime);
        }
        
        @Override public void encode(@NotNull ByteBuf buffer, @NotNull CarryData value)
        {
            ByteBufCodecs.idMapper(i -> CarryType.values()[i], CarryType::ordinal).encode(buffer, value.carryType());
             
            final StreamCodec<ByteBuf, CarryDataBaseHolder> dataCodec = (StreamCodec<ByteBuf, CarryDataBaseHolder>) value.carryType().streamCodec();
            dataCodec.encode(buffer, value.unionData());
            
            buffer.writeLong(value.startTime());
        }
    }
    //endregion
}
