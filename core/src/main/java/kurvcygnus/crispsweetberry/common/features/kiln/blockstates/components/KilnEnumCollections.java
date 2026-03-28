//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import kurvcygnus.crispsweetberry.common.features.kiln.KilnContainerData;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import static kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnScreen.*;

public final class KilnEnumCollections
{
    /**
     * An internal enum used bu <u>{@link KilnBlockEntity}</u>.<br>
     * It is used to determine the final <u>{@link ProcessionState ProcessionState}</u> of <u>{@link KilnBlockEntity}</u>.
     *
     * @author Kurv Cygnus
     * @see KilnBlockEntity BlockEntity
     * @since 1.0 Release
     */
    @ApiStatus.Internal public enum InputState
    {
        ALL_EMPTY,
        HAS_TIP(LogicalResult.BLAST_TIP),
        VALID(LogicalResult.CONTINUE);
        
        private final LogicalResult correspondedResult;
        
        InputState(@NotNull LogicalResult correspondedResult) { this.correspondedResult = correspondedResult; }
        
        InputState() { this.correspondedResult = LogicalResult.SKIP; }
        
        public @NotNull LogicalResult getCorrespondedResult() { return correspondedResult; }
    }
    
    /**
     * An internal enum for <u>{@link KilnProgressCalculator KilnProgressCalculator}</u>.<br>
     * It is used for <u>{@link KilnBlockEntity BlockEntity}</u>'s result procession.
     * @since 1.0 Release
     * @author Kurv Cygnus
     * @see KilnProgressCalculator#calculateRates Usage
     * @see KilnBlockEntity#serverTick Result variants
     */
    @ApiStatus.Internal public enum LogicalResult
    {
        CONTINUE,
        BALANCING,
        SKIP,
        INVALID,
        BLAST_TIP
    }
    
    /**
     * An internal enum for <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnScreen Screen}</u>'s visual display effects.<br>
     * It is deduced by <u>{@link KilnProgressCalculator#calculateRates Calculator}</u>,
     * then synchronized to <u>{@link kurvcygnus.crispsweetberry.common.features.kiln.client.ui.KilnScreen Screen}</u> by
     * <u>{@link KilnContainerData ContainerData}</u>.
     * @since 1.0 Release
     * @author Kurv Cygnus
     */
    @ApiStatus.Internal public enum VisualTrend
    {
        NORMAL,
        BALANCE(BALANCE_DECREASE_ARROW_TEXTURE),
        BURST(BALANCE_INCREASE_ARROW_TEXTURE),
        TIP(TIP_ARROW_TEXTURE),
        NONE;
        
        private final ResourceLocation boundArrowSprite;
        
        VisualTrend(@NotNull ResourceLocation boundArrowSprite) { this.boundArrowSprite = boundArrowSprite; }
        
        VisualTrend() { this.boundArrowSprite = PROGRESS_ARROW_TEXTURE; }
        
        public @NotNull ResourceLocation getBoundArrowSprite() { return boundArrowSprite; }
    }
    
    @ApiStatus.Internal public enum ProcessionState { WORKING, COOLDOWN }
}
