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
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryBlockPlaceContext;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryID;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryInteractContextCollection;
import kurvcygnus.crispsweetberry.utils.data.Pair;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.CrispFunctionalUtils;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

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
        "Assertion failed: \"unionData\" must not be null. This only means the internal logic has flawed, or get misused. %s".formatted(MiscConstants.FEEDBACK_MESSAGE);
    
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
    private final @Nullable Function<BlockState, CarryBlockPlaceContext> contextGenerator;
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
        @Nullable Function<BlockState, CarryBlockPlaceContext> contextGenerator,
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
        this.contextGenerator = contextGenerator;
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
            MiscConstants.FEEDBACK_MESSAGE
        );
        
        //! From this#handle(UseOnContext), #unbox(CarryBlockPlaceContext) only have two cases:
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
    protected final @NotNull Function<BlockState, CarryBlockPlaceContext> getContextGenerator()
    {
        requireNonNull(
            contextGenerator,
            "Assertion failed: \"placeAction\" must not be null. This only means the code implementation has flawed, or get misused."
        );
        
        return contextGenerator;
    }
    
    /**
     * The result object, holding the procession result, and the sequence of following operations.
     * @since 1.0 Release
     * @param data The serialized unionData of the carry object.
     * @param flags The operations that will be processed by <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine Engine}</u>.<br>
     *              <i>Based on <u><a href="https://en.wikipedia.org/wiki/Mask_(computing)">Bitmask</a></u>.</i>
     * @param result Used by <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine Engine}</u>, passing as the
     *               <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.self.CarryCrateItem Carry Crate}</u>'s interact result.
     * @param carryID The collection of this carry object's <u>{@link ResourceLocation}</u> and UUID.
     * @author Kurv Cygnus
     */
    public record HandleResult(
        @NotNull Optional<CarryData> data,
        @Range(from = 0, to = Integer.MAX_VALUE) int flags,
        @NotNull InteractionResult result,
        @NotNull Optional<CarryID> carryID,
        @NotNull Optional<BlockEntityType<?>> blockEntityType
    )
    {
        private static final int LISTENER_ADD    = 1;// 1 << 0
        private static final int LISTENER_REMOVE = 2;// 2 << 0
        
        private static final int COMPONENT_INSERT = 1 << 2;// 4
        private static final int COMPONENT_REMOVE = 2 << 2;// 8
        
        private static final int TARGET_TAKE    = 1 << 4;// 16
        private static final int TARGET_RELEASE = 2 << 4;// 32
        
        public static @NotNull HandleResult boxIn(
            @NotNull CarryData carryData,
            @NotNull CarryID carryID
        ) { return boxIn(carryData, carryID, false); }
        
        /**
         * Represents the expected procession result of <u>{@link #boxIn()}</u> method.
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
            CrispFunctionalUtils.throwIf(
                carryID == null && !addAsExtra,
                "Param \"uuid\" must not be null when unionData won't be added as extra!",
                IllegalArgumentException::new
            );
            
            final int flags = COMPONENT_INSERT | TARGET_TAKE | (addAsExtra ? 0 : LISTENER_ADD);
            
            return new HandleResult(
                Optional.of(carryData),
                flags,
                InteractionResult.SUCCESS,
                Optional.ofNullable(carryID),
                Optional.empty()
            );
        }
        
        /**
         * Represents the expected situation of failed attempt.
         */
        public static @NotNull HandleResult failed()
        {
            return new HandleResult(
                Optional.empty(),
                0,
                InteractionResult.FAIL,
                Optional.empty(),
                Optional.empty()
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
                Optional.empty(),
                LISTENER_REMOVE,
                InteractionResult.PASS,
                Optional.of(carryID),
                Optional.empty()
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
                Optional.of(data),
                flags,
                InteractionResult.SUCCESS,
                Optional.ofNullable(carryID),
                Optional.ofNullable(blockEntityType)
            );
        }
        
        @Deprecated public boolean shouldAddListener() { return (flags & 0x3) == 1; }
        @Deprecated public boolean shouldRemoveListener() { return (flags & 0x3) == 2; }
        @Deprecated public boolean shouldInsertComponent() { return (flags >> 0x2 & 0x3) == 1; }
        @Deprecated public boolean shouldRemoveComponent() { return (flags >> 0x2 & 0x3) == 2; }
        @Deprecated public boolean shouldTakeTarget() { return (flags >> 0x4 & 0x3) == 1; }
        @Deprecated public boolean shouldReleaseTarget() { return (flags >> 0x4 & 0x3) == 2; }
        
        public @NotNull Pair<OperationType, TriState> getListenerState()
            { return new Pair<>(OperationType.LISTENER, getTriFlag((flags & 0x3) == 1, (flags & 0x3) == 2)); }
        
        public @NotNull Pair<OperationType, TriState> getComponentState()
            { return new Pair<>(OperationType.COMPONENT, getTriFlag((flags >> 0x2 & 0x3) == 1, (flags >> 0x2 & 0x3) == 2)); }
        
        public @NotNull Pair<OperationType, TriState> getTargetState()
            { return new Pair<>(OperationType.TARGET, getTriFlag((flags >> 0x4 & 0x3) == 1, (flags >> 0x4 & 0x3) == 2)); }
        
        private @NotNull TriState getTriFlag(boolean trueCondition, boolean falseCondition)
        {
            return trueCondition ?
                TriState.TRUE : falseCondition ?
                TriState.FALSE : TriState.DEFAULT;
        }
        
        public enum OperationType
        {
            LISTENER,
            COMPONENT,
            TARGET
        }
    }
}
