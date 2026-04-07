//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity;
import kurvcygnus.crispsweetberry.utils.constants.MetainfoConstants;
import kurvcygnus.crispsweetberry.utils.core.log.MarkLogger;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Objects;

import static kurvcygnus.crispsweetberry.common.features.kiln.KilnContainerData.FALSE;
import static kurvcygnus.crispsweetberry.common.features.kiln.KilnContainerData.TRUE;

/**
 * This class keeps the synchronization, and connections between 
 * {@link net.minecraft.world.level.block.entity.BaseContainerBlockEntity blockEntity} and {@link net.minecraft.world.inventory.AbstractContainerMenu menu}.
 * @see kurvcygnus.crispsweetberry.common.features.kiln.blockstates.KilnBlockEntity BlockEntity
 * @since 1.0 Release
 * @author Kurv Cygnus
 */
public final class KilnProgressModel
{
    private @Range(from = 0, to = 1) double realProgress;
    private @Range(from = 0, to = 1) double visualProgress;
    private KilnEnumCollections.VisualTrend trend;
    private boolean isIgnited;
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "PROGRESS_MISMATCH");
    
    public KilnProgressModel(@NotNull KilnBlockEntity blockEntity)
    {
        Objects.requireNonNull(blockEntity, "Param \"blockEntity\" must not be null!");
        
        this.realProgress = 0D;
        this.visualProgress = 0D;
        this.trend = KilnEnumCollections.VisualTrend.TIP;
        this.isIgnited = true;
    }
    
    public void synchronize(double realProgress, double visualProgress, @Nullable KilnEnumCollections.VisualTrend trend, boolean isIgnited)
        { this.synchronize(realProgress, visualProgress, trend, isIgnited, false); }
    
    public void synchronize(double realProgress, double visualProgress, @Nullable KilnEnumCollections.VisualTrend trend, boolean isIgnited, boolean isStateless)
    {
        this.realProgress = isStateless ? Math.max(0D, realProgress) : Math.clamp(realProgress, 0D, 1D);
        this.visualProgress = isStateless ? Math.max(0D, visualProgress) : Math.clamp(visualProgress, 0D, 1D);
        this.trend = Objects.requireNonNullElse(trend, KilnEnumCollections.VisualTrend.TIP);
        this.isIgnited = isIgnited;
    }
    
    @CheckReturnValue public boolean upgradeProgress(boolean isStateless)
    {
        if(this.realProgress >= 1D)
        {
            if(this.visualProgress < 1D)
                LOGGER.warn("""
                        Kiln Model encountered a abnormal situation. Detail: visualProgress does not match realProgress when procession is \
                        already done. visualProgress: {}{}
                        """,
                    this.visualProgress,
                    MetainfoConstants.FEEDBACK_MESSAGE
                );
            
            this.realProgress = isStateless ? this.realProgress % 1 : 0D;
            this.visualProgress = isStateless ? this.realProgress % 1 : 0D;
            
            return true;
        }
        
        return false;
    }
    
    public double getRealProgress() { return this.realProgress; }
    
    public double getVisualProgress() { return this.visualProgress; }
    
    public int getProgressTrendIndex() { return this.trend.ordinal(); }
    
    public @NotNull KilnEnumCollections.VisualTrend getTrend() { return this.trend; }
    
    public int getIgnitionState() { return this.isIgnited ? TRUE : FALSE; }
    
    public void setVisualProgress(@Range(from = 0, to = 1) double visualProgress) { this.visualProgress = visualProgress; }
    
    public void setProgressTrend(@NotNull KilnEnumCollections.VisualTrend trend) { this.trend = trend; }
    
    public void setIgnitionState(@Range(from = 0, to = 1) int state) { this.isIgnited = (state == TRUE); }
}