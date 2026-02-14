//==============================================================================
// Copyright (C) 2026 Kurv Cygnus                                              =
// This file is part of Crisp Sweetberry.                                      =
// Crisp Sweetberry is free software: you can redistribute it and/or modify    =
// it under the terms of the GNU Lesser General Public License as published by =
// the Free Software Foundation, either version 3 of the License.              =
//==============================================================================

package kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components;

import com.mojang.logging.LogUtils;
import kurvcygnus.crispsweetberry.common.features.kiln.blockstates.components.enums.VisualTrend;
import kurvcygnus.crispsweetberry.utils.log.MarkLogger;
import kurvcygnus.crispsweetberry.utils.misc.MiscConstants;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private double realProgress;
    private double visualProgress;
    private VisualTrend trend;
    private boolean isIgnited;
    
    private static final MarkLogger LOGGER = MarkLogger.markedLogger(LogUtils.getLogger(), "PROGRESS_MISMATCH");
    
    public KilnProgressModel()
    {
        this.realProgress = 0D;
        this.visualProgress = 0D;
        this.trend = VisualTrend.TIP;
        this.isIgnited = true;
    }
    
    public void synchronize(double realProgress, double visualProgress, @Nullable VisualTrend trend, boolean isIgnited)
    {
        this.realProgress = Math.clamp(realProgress, 0D, 1D);
        this.visualProgress = Math.clamp(visualProgress, 0D, 1D);
        this.trend = Objects.requireNonNullElse(trend, VisualTrend.TIP);
        this.isIgnited = isIgnited;
    }
    
    @CheckReturnValue
    public boolean upgradeProgress()
    {
        if(this.realProgress >= 1D)
        {
            if(this.visualProgress < 1D)
                LOGGER.warn("Kiln Model encountered a abnormal situation. Detail: visualProgress does not match realProgress when procession is " +
                        "already done. visualProgress: {} \n{}",
                    this.visualProgress,
                    MiscConstants.FEEDBACK_MESSAGE
                );
            
            this.realProgress = 0D;
            this.visualProgress = 0D;
            
            return true;
        }
        
        return false;
    }
    
    public double getRealProgress() { return this.realProgress; }
    
    public double getVisualProgress() { return this.visualProgress; }
    
    public int getProgressTrendIndex() { return this.trend.ordinal(); }
    
    public @NotNull VisualTrend getTrend() { return this.trend; }
    
    public int getIgnitionState() { return this.isIgnited ? TRUE : FALSE; }
    
    public void setVisualProgress(double visualProgress) { this.visualProgress = visualProgress; }
    
    public void setProgressTrend(VisualTrend trend) { this.trend = trend; }
    
    public void setIgnitionState(int state) { this.isIgnited = (state == TRUE); }
}