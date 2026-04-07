//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core.components;

import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.api.internal.CarryData;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContextCollection;
import kurvcygnus.crispsweetberry.common.features.carrycrate.self.CarryCrateItem;
import kurvcygnus.crispsweetberry.utils.FunctionalUtils;
import kurvcygnus.crispsweetberry.utils.base.extension.StatedBlockPlaceContext;
import kurvcygnus.crispsweetberry.utils.base.lang.Pair;
import kurvcygnus.crispsweetberry.utils.base.trait.IBitmaskedEnum;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.TriState;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static kurvcygnus.crispsweetberry.common.features.carrycrate.core.components.AbstractCarryInteractHandler.OperationType.*;

/**
 * The basic of carry crate's I/O<i>(in/out)</i> handler.<br>
 * It handles unionData process, serialization, and decide the following
 * logic decisions, which will be passed to <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine Engine}</u> by
 * <u>{@link HandleResult HandleResult}</u>.
 * @since 1.0 Release
 * @author Kurv Cygnus
 * @see CarryBlockEntityInteractHandler BlockEntity Handler
 * @see CarryEntityInteractHandler Entity Handler
 * @see CarryBlockInteractHandler Block Handler
 */
public abstract sealed class AbstractCarryInteractHandler permits CarryBlockEntityInteractHandler, CarryBlockInteractHandler, CarryEntityInteractHandler
{
    protected static final String MISUSE_FAIL_MSG = 
        "Assertion failed: \"unionData\" must not be null. This only means the internal logic has flawed, or get misused. %s".formatted(MetainfoConstants.FEEDBACK_MESSAGE);
    
    protected final ServerLevel level;
    
    /**
     * @implNote 
     * <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine#interact(CarryInteractContextCollection.ICarryInteractContext) CarryEngine#interact(ICarryInteractContext)}</u>
     * has granted the null safety of player.
     */
    protected final @NotNull ServerPlayer player;
    protected final ItemStack carryCrate;
    private final @Nullable BlockPos targetPos;
    private final @Nullable BlockState targetState;
    private final @Nullable LivingEntity targetEntity;
    private final @Nullable BlockEntity targetBlockEntity;
    private final @Nullable Function<BlockState, StatedBlockPlaceContext> contextGenerator;
    protected final @Nullable CarryID optionalCarryID;
    protected final boolean hasData;
    
    public AbstractCarryInteractHandler(
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull ItemStack carryCrate,
        @Nullable BlockPos targetPos,
        @Nullable BlockState targetState,
        @Nullable LivingEntity targetEntity,
        @Nullable BlockEntity targetBlockEntity,
        @Nullable Function<BlockState, StatedBlockPlaceContext> contextGenerator,
        @Nullable CarryID optionalCarryID
    )
    {
        requireNonNull(level, "Param \"level\" must not be null!");
        requireNonNull(player, "Param \"player\" must not be null!");
        requireNonNull(carryCrate, "Param \"carryCrate\" must not be null!");
        
        this.level             = level;
        this.player            = player;
        this.carryCrate        = carryCrate;
        this.targetPos         = targetPos;
        this.targetState       = targetState;
        this.targetEntity      = targetEntity;
        this.targetBlockEntity = targetBlockEntity;
        this.contextGenerator  = contextGenerator;
        this.optionalCarryID   = optionalCarryID;
        this.hasData           = this.carryCrate.has(CarryCrateRegistries.CARRY_CRATE_DATA.get());
    }
    
    public final @NotNull HandleResult handle()
    {
        if(optionalCarryID == null && !hasData)
        {
            getLogger().debug("Action was confirmed as \"boxIn\".");
            return boxIn();
        }
        
        getLogger().debug("Action was confirmed as \"unbox\".");
        return unbox();
    }
    
    protected abstract @NotNull HandleResult boxIn();
    
    protected abstract @NotNull HandleResult unbox();
    
    /**
     * Gets the <u>{@link ResourceLocation}</u> of the carry object, which is used for {@code CarryEngine}'s listener recovery.
     */
    protected abstract @NotNull ResourceLocation getCarryResourceLocation();
    
    protected final @NotNull CarryID generateCarryID()
    {
        final UUID uuid = UUID.randomUUID();
        final CarryID carryID = new CarryID(getCarryResourceLocation().toString(), uuid.toString().replace("-", ""));
        
        getLogger().debug("Generated CarryID \"{}\" for indexing.", carryID);
        
        return carryID;
    }
    
    protected final @NotNull HandleResult handleEx()
    {
        getLogger().error(
            "ItemStack \"{}\" has CarryID \"{}\", but has no carryData, returning \"PASS\" as interact result. This is a serious persistent issue. {}",
            carryCrate.toString(),
            optionalCarryID,
            MetainfoConstants.FEEDBACK_MESSAGE
        );
        
        //! From [[AbstractCarryInteractHandler#handle(UseOnContext)]],
        //! [[AbstractCarryInteractHandler#unbox(StatedBlockPlaceContext)]] only have two cases:
        //! 1. optionalUUID is null.
        //! 2. unionData is null.
        //! So we can do this assertion.
        assert optionalCarryID != null;
        
        return HandleResult.removeWithUUID(optionalCarryID);
    }
    
    protected abstract @NotNull MarkLogger getLogger();
    
    /**
     * @apiNote <span style="color: red">Throws <u>{@link NullPointerException NPE}</u> the caller is not <u>{@link CarryBlockEntityInteractHandler}</u>.</span>
     */
    protected final @NotNull BlockPos getTargetPos()
    {
        requireNonNull(
            this.targetPos,
            "Assertion failed: \"targetPos\" must not be null. This only means the code implementation has flawed, or get misused."
        );
        
        return targetPos;
    }
    
    /**
     * @apiNote <span style="color: red">Throws <u>{@link NullPointerException NPE}</u> the caller is <u>{@link CarryEntityInteractHandler}</u>.</span>
     */
    protected final @NotNull BlockState getTargetState()
    {
        requireNonNull(
            this.targetState,
            "Assertion failed: \"targetState\" must not be null. This only means the code implementation has flawed, or get misused."
        );
        
        return targetState;
    }
    
    /**
     * @apiNote <span style="color: red">Throws <u>{@link NullPointerException NPE}</u> the caller is not <u>{@link CarryEntityInteractHandler}</u>.</span>
     */
    protected final @NotNull LivingEntity getTargetEntity()
    {
        requireNonNull(
            this.targetEntity,
            "Assertion failed: \"targetEntity\" must not be null. This only means the code implementation has flawed, or get misused."
        );
        return targetEntity;
    }
    
    /**
     * @apiNote <span style="color: red">Throws <u>{@link NullPointerException NPE}</u> the caller is not <u>{@link CarryBlockEntityInteractHandler}</u>.</span>
     */
    protected final @NotNull BlockEntity getTargetBlockEntity()
    {
        requireNonNull(
            this.targetBlockEntity,
            "Assertion failed: \"targetBlockEntity\" must not be null. This only means the code implementation has flawed, or get misused."
        );
        return targetBlockEntity;
    }
    
    /**
     * @apiNote <span style="color: red">Throws <u>{@link NullPointerException NPE}</u> the caller is <u>{@link CarryEntityInteractHandler}</u>.</span>
     */
    protected final @NotNull Function<BlockState, StatedBlockPlaceContext> getContextGenerator()
    {
        requireNonNull(
            contextGenerator,
            "Assertion failed: \"placeAction\" must not be null. This only means the code implementation has flawed, or get misused."
        );
        
        return contextGenerator;
    }
    
    /**
     * The result object, holding the procession result, and the sequence of following operations.
     * @author Kurv Cygnus
     * @since 1.0 Release
     */
    public static final class HandleResult
    {
        private static final int LISTENER_ADD    = LISTENER.shiftTrue();//  1
        private static final int LISTENER_REMOVE = LISTENER.shiftFalse();// 2
        
        private static final int COMPONENT_INSERT = COMPONENT.shiftTrue();//  4
        private static final int COMPONENT_REMOVE = COMPONENT.shiftFalse();// 8
        
        private static final int TARGET_TAKE    = TARGET.shiftTrue();//  16
        private static final int TARGET_RELEASE = TARGET.shiftFalse();// 32

        
        /**
         * Represents the expected situation of failed attempt.
         */
        public static final HandleResult FAILED = new HandleResult(
            null,
            0,
            InteractionResult.FAIL,
            null,
            null
        );
        
        /**
         * The serialized data of the carry object.
         */
        private final @Nullable CarryData data;
        
        /**
         * The operations that will be processed by <u>{@link CarryEngine Engine}</u>.<br>
         * <i>Based on <u><a href="https://en.wikipedia.org/wiki/Mask_(computing)">Bitmask</a></u>.</i>
         * @see OperationType
         */
        @MagicConstant(flagsFromClass = HandleResult.class) private final @Range(from = 0, to = Integer.MAX_VALUE) int flags;
        
        /**
         * Used by <u>{@link CarryEngine Engine}</u>, passing as the
         * <u>{@link CarryCrateItem Carry Crate}</u>'s interact result.
         */
        private final @NotNull InteractionResult result;
        
        /**
         * The collection of this carry object's <u>{@link ResourceLocation}</u> and UUID.
         */
        private final @Nullable CarryID carryID;
        private final @Nullable BlockEntityType<?> blockEntityType;
        
        private HandleResult(
            @Nullable CarryData data,
            @Range(from = 0, to = Integer.MAX_VALUE) @MagicConstant(flagsFromClass = HandleResult.class) int flags,
            @NotNull InteractionResult result,
            @Nullable CarryID carryID,
            @Nullable BlockEntityType<?> blockEntityType
        )
        {
            requireNonNull(result, "Param \"result\" must not be null!");
            
            this.data            = data;
            this.flags           = flags;
            this.result          = result;
            this.carryID         = carryID;
            this.blockEntityType = blockEntityType;
        }
        
        public static @NotNull HandleResult boxIn(
            @NotNull CarryData carryData,
            @NotNull CarryID carryID
        ) { return boxIn(carryData, carryID, false); }
        
        /**
         * Represents the expected procession result of <u>{@link #boxIn()}</u> method.
         *
         * @param addAsExtra Decides whether the following procession is increase the value of {@code carryCount}.
         *                   <span style="color: red">Only meant to be used by <u>{@link CarryBlockInteractHandler}</u>.</span>
         */
        public static @NotNull HandleResult boxIn(
            @NotNull CarryData carryData,
            @Nullable CarryID carryID,
            boolean addAsExtra
        )
        {
            requireNonNull(carryData, "Param \"carryData\" must not be null!");
            FunctionalUtils.throwIf(
                carryID == null && !addAsExtra,
                "Param \"uuid\" must not be null when unionData won't be added as extra!",
                IllegalArgumentException::new
            );
            
            final int flags = COMPONENT_INSERT | TARGET_TAKE | (addAsExtra ? 0 : LISTENER_ADD);
            
            return new HandleResult(
                carryData,
                flags,
                InteractionResult.SUCCESS,
                carryID,
                null
            );
        }
        
        /**
         * Represents the unexpected situation, which a Carry Crate has a <u>{@link CarryID}</u>, and no <u>{@link CarryData}</u>,
         * we need to remove such a abnormal Carry Crate's <u>{@link CarryID}</u>.
         */
        public static @NotNull HandleResult removeWithUUID(@NotNull CarryID carryID)
        {
            requireNonNull(carryID, "Param \"carryID\" must not be null!");
            
            return new HandleResult(
                null,
                LISTENER_REMOVE,
                InteractionResult.PASS,
                carryID,
                null
            );
        }
        
        public static @NotNull HandleResult unbox(@NotNull CarryData data, @Nullable CarryID carryID)
            { return unbox(data, carryID, null, false); }
        
        public static @NotNull HandleResult unbox(
            @NotNull CarryData data,
            @Nullable CarryID carryID,
            boolean hasRemaining
        ) { return unbox(data, carryID, null, hasRemaining); }
        
        /**
         * Represents the expected procession result of <u>{@link #unbox()}</u> method.
         */
        public static @NotNull HandleResult unbox(
            @NotNull CarryData data,
            @Nullable CarryID carryID,
            @Nullable BlockEntityType<?> blockEntityType,
            boolean hasRemaining
        )
        {
            requireNonNull(data, "Param \"unionData\" must not be null!");
            
            final int flags = hasRemaining ?
                COMPONENT_INSERT | TARGET_RELEASE :
                LISTENER_REMOVE | COMPONENT_REMOVE | TARGET_RELEASE;
            
            return new HandleResult(
                data,
                flags,
                InteractionResult.SUCCESS,
                carryID,
                blockEntityType
            );
        }
        
        public @NotNull Pair<OperationType, TriState> getListenerState() { return new Pair<>(LISTENER, LISTENER.compute(flags)); }
        public @NotNull Pair<OperationType, TriState> getComponentState() { return new Pair<>(COMPONENT, COMPONENT.compute(flags)); }
        public @NotNull Pair<OperationType, TriState> getTargetState() { return new Pair<>(TARGET, TARGET.compute(flags)); }
        
        public @NotNull Optional<CarryData> data() { return Optional.ofNullable(data); }
        
        public @NotNull InteractionResult result() { return result; }
        
        public @NotNull Optional<CarryID> carryID() { return Optional.ofNullable(carryID); }
        
        public @NotNull Optional<BlockEntityType<?>> blockEntityType() { return Optional.ofNullable(blockEntityType); }
        
        @Override public boolean equals(@Nullable Object obj)
        {
            return obj instanceof HandleResult that &&
                Objects.equals(this.data, that.data) &&
                this.flags == that.flags &&
                Objects.equals(this.result, that.result) &&
                Objects.equals(this.carryID, that.carryID) &&
                Objects.equals(this.blockEntityType, that.blockEntityType);
        }
        
        @Override public int hashCode() { return Objects.hash(data, flags, result, carryID, blockEntityType); }
        
        /**
         * @apiNote <b>It is recommend to use this in debug only. It brings more performance penalty comparing to most <u>{@link Object#toString() toStrings}</u>.</b>
         */
        @Override public @NotNull String toString()
        {
            return """
                   
                   HandleResult
                   {
                       data: %s
                       flags: %s
                       result: %s
                       carryID: %s
                       blockEntityType: %s
                   }
                   """.
                formatted(
                    this.data,
                    """
                        
                        {
                            Listener: %s
                            Component: %s
                            Target: %s
                        }
                        """.
                        formatted(
                            this.getListenerState().toString(),
                            this.getComponentState().toString(),
                            this.getTargetState().toString()
                        ),
                    this.result,
                    this.carryID,
                    this.blockEntityType
                );
        }
    }
    
    public enum OperationType implements IBitmaskedEnum<OperationType> { LISTENER, COMPONENT, TARGET }
}
