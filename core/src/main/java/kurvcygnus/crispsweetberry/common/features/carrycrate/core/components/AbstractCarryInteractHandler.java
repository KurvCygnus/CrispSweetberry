//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.carrycrate.core.components;

import kurvcygnus.crispsweetberry.common.features.carrycrate.CarryCrateRegistries;
import kurvcygnus.crispsweetberry.common.features.carrycrate.core.data.CarryData;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public abstract sealed class AbstractCarryInteractHandler permits CarryBlockEntityInteractHandler, CarryBlockInteractHandler, CarryEntityInteractHandler
{
    protected static final String MISUSE_FAIL_MSG = 
        "Assertion failed: \"data\" must not be null. This only means the internal logic has flawed, or get misused. %s".formatted(MiscConstants.FEEDBACK_MESSAGE);
    
    protected final ServerLevel level;
    
    /**
     * @implNote 
     * <u>{@link kurvcygnus.crispsweetberry.common.features.carrycrate.core.CarryEngine#interactOnBlock(UseOnContext) CarryEngine#interactOnBlock(UseOnContext)}</u> 
     * has granted the null safety of player.
     */
    protected final @NotNull ServerPlayer player;
    protected final ItemStack carryCrate;
    private final @Nullable BlockPos targetPos;
    private final @Nullable BlockState targetState;
    private final @Nullable LivingEntity targetEntity;
    protected final @Nullable String optionalCarryID;
    protected final boolean hasData;
    
    public AbstractCarryInteractHandler(
        @NotNull ServerLevel level,
        @NotNull ServerPlayer player,
        @NotNull ItemStack carryCrate,
        @Nullable BlockPos targetPos,
        @Nullable BlockState targetState,
        @Nullable LivingEntity targetEntity,
        @Nullable String optionalCarryID
    )
    {
        requireNonNull(level, "Param \"level\" must not be null!");
        requireNonNull(player, "Param \"player\" must not be null!");
        requireNonNull(carryCrate, "Param \"carryCrate\" must not be null!");
        
        this.level = level;
        this.player = player;
        this.carryCrate = carryCrate;
        this.targetPos = targetPos;
        this.targetState = targetState;
        this.targetEntity = targetEntity;
        this.optionalCarryID = optionalCarryID;
        this.hasData = this.carryCrate.has(CarryCrateRegistries.CARRY_CRATE_DATA.get());
    }
    
    public final @NotNull HandleResult handle()
    {
        if(optionalCarryID == null && !hasData)
            return boxIn();
        
        return unbox();
    }
    
    protected abstract @NotNull HandleResult boxIn();
    
    protected abstract @NotNull HandleResult unbox();
    
    protected abstract @NotNull ResourceLocation getCarryResourceLocation();
    
    protected final @NotNull String generateCarryID()
    {
        final UUID uuid = UUID.randomUUID();
        
        return "%s§%s".formatted(getCarryResourceLocation(), uuid.toString().replace("-", ""));
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
        //! 2. data is null.
        //! So we can do this assertion.
        assert optionalCarryID != null;
        
        return HandleResult.removeWithUUID(optionalCarryID);
    }
    
    protected abstract @NotNull MarkLogger getLogger();
    
    protected final @NotNull BlockPos getTargetPos()
    {
        requireNonNull(
            this.targetPos,
            "Assertion failed: \"targetPos\" must not be null. This only means the code implementation has flawed, or get misused."
        );
        
        return targetPos;
    }
    
    protected final @NotNull BlockState getTargetState()
    {
        requireNonNull(
            this.targetState,
            "Assertion failed: \"targetState\" must not be null. This only means the code implementation has flawed, or get misused."
        );
        
        return targetState;
    }
    
    protected final @NotNull LivingEntity getTargetEntity()
    {
        requireNonNull(
            this.targetEntity,
            "Assertion failed: \"targetEntity\" must not be null. This only means the code implementation has flawed, or get misused."
        );
        return targetEntity;
    }
    
    public record HandleResult(
        @NotNull Optional<CarryData> data,
        int flags,
        @NotNull InteractionResult result,
        @NotNull Optional<String> carryID
    )
    {
        private static final int LISTENER_ADD = 1;// 1 << 0
        private static final int LISTENER_REMOVE = 2;// 2 << 0
        
        private static final int COMPONENT_INSERT = 1 << 2;// 4
        private static final int COMPONENT_REMOVE = 2 << 2;// 8
        
        private static final int TARGET_TAKE = 1 << 4;// 16
        private static final int TARGET_RELEASE = 2 << 4;// 32
        
        public static @NotNull HandleResult boxIn(
            @NotNull CarryData carryData,
            @NotNull InteractionResult result,
            @Nullable String carryID,
            boolean addAsExtra
        )
        {
            requireNonNull(carryData, "Param \"carryData\" must not be null!");
            requireNonNull(result, "Param \"result\" must not be null!");
            CrispFunctionalUtils.throwIf(
                carryID == null && !addAsExtra,
                () -> new IllegalArgumentException("Param \"carryID\" must not be null when data won't be added as extra!")
            );
            
            final int flags = COMPONENT_INSERT | TARGET_TAKE | (addAsExtra ? 0 : LISTENER_ADD);
            
            return new HandleResult(
                Optional.of(carryData),
                flags,
                result,
                Optional.ofNullable(carryID)
            );
        }
        
        public static @NotNull HandleResult failed() { return new HandleResult(Optional.empty(), 0, InteractionResult.FAIL, Optional.empty()); }
        
        public static @NotNull HandleResult removeWithUUID(@NotNull String carryID)
        {
            return new HandleResult(
                Optional.empty(),
                LISTENER_REMOVE,
                InteractionResult.PASS,
                Optional.of(carryID)
            );
        }
        
        public static @NotNull HandleResult unbox(@NotNull CarryData data, @Nullable String carryID)
        {
            final int flags = LISTENER_REMOVE | COMPONENT_REMOVE | TARGET_RELEASE;
            
            return new HandleResult(
                Optional.of(data),
                flags,
                InteractionResult.SUCCESS,
                Optional.ofNullable(carryID)
            );
        }
        
        public boolean shouldAddListener() { return (flags & 0x3) == LISTENER_ADD; }
        public boolean shouldRemoveListener() { return (flags & 0x3) == LISTENER_REMOVE; }
        public boolean shouldInsertComponent() { return (flags >> 0x3 & 0x3) == 1; }
        public boolean shouldRemoveComponent() { return (flags >> 0x3 & 0x3) == 2; }
        public boolean shouldTakeTarget() { return (flags >> 0x4 & 0x3) == 1; }
        public boolean shouldReleaseTarget() { return (flags >> 0x4 & 0x3) == 2; }
    }
}
